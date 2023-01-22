package com.romanbrunner.apps.fitnesstracker;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.romanbrunner.apps.fitnesstracker.database.AppDatabase;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoDao;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutUnitEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/* Repository handling the work with exercises. */
public class DataRepository
{
    // --------------------
    // Functional code
    // --------------------

    private static DataRepository instance;

    private final AppDatabase database;
    private final MutableLiveData<WorkoutUnitEntity> observableWorkoutUnit;
    private final Executor executor;

    public interface CallbackAction<T>
    {
        void execute(@Nullable T object);
    }

    public interface CallbackCondition<T>
    {
        boolean check(@Nullable T object);
    }

    private DataRepository(final AppDatabase database)
    {
        this.database = database;
        observableWorkoutUnit = new MutableLiveData<>();
        executor = Executors.newSingleThreadExecutor();

        // Load newest workout unit and post its value into its observable as soon as the database is ready:
        executeOnceForLiveData(getNewestWorkoutUnit(), workoutUnit -> database.getDatabaseCreated().getValue() != null, workoutUnit ->
        {
            if (workoutUnit == null) throw new AssertionError("object cannot be null");
            workoutUnit.setDate(new Date());  // Set to current date
            observableWorkoutUnit.postValue(workoutUnit);
        });
    }

    private void replaceCurrentWorkoutUnit(final WorkoutUnitEntity currentWorkoutUnit, final WorkoutUnitEntity newWorkoutUnit, final List<ExerciseSetEntity> newExercises)
    {
        // Safeguard against empty new entries:
        if (newWorkoutUnit == null || newExercises == null || newExercises.isEmpty())
        {
            Log.e("replaceCurrentWorkoutUnit", "Replacement aborted because one of the given new entries is null/empty");
            return;
        }

        // Delete current entries and insert new entries:
        executor.execute(() ->
        {
            database.workoutUnitDao().delete(currentWorkoutUnit);  // Associated exercise sets are automatically deleted
            database.workoutUnitDao().insert(newWorkoutUnit);
            database.exerciseSetDao().insert(newExercises);
        });
        // Adjust current workout unit:
        observableWorkoutUnit.setValue(newWorkoutUnit);
    }

    static DataRepository getInstance(final AppDatabase database)
    {
        if (instance == null)
        {
            synchronized (DataRepository.class)
            {
                if (instance == null)
                {
                    instance = new DataRepository(database);
                }
            }
        }
        return instance;
    }

    public static <T> void executeOnceForLiveData(LiveData<T> liveData, CallbackCondition<T> condition, CallbackAction<T> action)
    {
        liveData.observeForever(new Observer<T>()
        {
            @Override
            /* Is called once directly after observer is added if liveData is not empty. */
            public void onChanged(@Nullable T object)
            {
                if (condition.check(object))
                {
                    action.execute(object);
                    liveData.removeObserver(this);
                }
            }
        });
    }
    public static <T> void executeOnceForLiveData(LiveData<T> liveData, CallbackAction<T> action)
    {
        liveData.observeForever(new Observer<T>()
        {
            @Override
            /* Is called once directly after observer is added if liveData is not empty. */
            public void onChanged(@Nullable T object)
            {
                action.execute(object);
                liveData.removeObserver(this);
            }
        });
    }

    public LiveData<List<ExerciseInfoEntity>> getExerciseInfo(Set<String> names)
    {
        return database.exerciseInfoDao().loadByNames(names);
    }

    public LiveData<WorkoutUnitEntity> getCurrentWorkoutUnit()
    {
        return observableWorkoutUnit;
    }

    public LiveData<WorkoutUnitEntity> getNewestWorkoutUnit()
    {
        return database.workoutUnitDao().loadNewest();
    }
    public LiveData<WorkoutUnitEntity> getNewestWorkoutUnit(String studio)
    {
        return database.workoutUnitDao().loadNewestByStudio(studio);
    }
    public LiveData<WorkoutUnitEntity> getNewestWorkoutUnit(String studio, String name)
    {
        return database.workoutUnitDao().loadNewestByStudioAndName(studio, name);
    }

    public LiveData<WorkoutUnitEntity> getLastWorkoutUnit()
    {
        return database.workoutUnitDao().loadLast();
    }

    public LiveData<List<WorkoutUnitEntity>> getAllWorkoutUnits()
    {
        return database.workoutUnitDao().loadAll();
    }
    public LiveData<List<WorkoutUnitEntity>> getAllWorkoutUnits(String studio)
    {
        return database.workoutUnitDao().loadAllByStudio(studio);
    }
    public LiveData<List<WorkoutUnitEntity>> getAllWorkoutUnits(String studio, String name)
    {
        return database.workoutUnitDao().loadAllByStudioAndName(studio, name);
    }

    public LiveData<List<ExerciseSetEntity>> getExerciseSets(WorkoutUnitEntity workoutUnit)
    {
        return database.exerciseSetDao().loadByWorkoutUnitDate(workoutUnit.getDate());
    }

    public LiveData<List<ExerciseSetEntity>> getAllExerciseSets()
    {
        return database.exerciseSetDao().loadAll();
    }

    public void storeExerciseInfo(final List<ExerciseInfoEntity> exerciseInfoList)
    {
        // Insert or update given entries:
        executor.execute(() ->
        {
            ExerciseInfoDao exerciseInfoDao = database.exerciseInfoDao();
            for (ExerciseInfoEntity exerciseInfo: exerciseInfoList)
            {
                if (exerciseInfoDao.insertIgnore(exerciseInfo) == -1L)
                {
                    exerciseInfoDao.update(exerciseInfo);
                }
            }

        });
    }

    public void setCurrentWorkout(WorkoutUnitEntity workoutUnitEntity)
    {
        observableWorkoutUnit.setValue(workoutUnitEntity);
    }

    public void deleteWorkoutUnits(List<WorkoutUnitEntity> workoutUnits)
    {
        executor.execute(() -> database.workoutUnitDao().delete(workoutUnits));
    }

    public void finishWorkout(@NonNull WorkoutUnitEntity oldWorkoutUnit, @NonNull List<ExerciseSetEntity> oldExerciseSets)
    {
        // Update current entries:
        executor.execute(() ->
        {
            database.workoutUnitDao().update(oldWorkoutUnit);
            database.exerciseSetDao().update(oldExerciseSets);
        });
        // Clone new entries:
        WorkoutUnitEntity newWorkoutUnit = new WorkoutUnitEntity(oldWorkoutUnit);
        List<ExerciseSetEntity> newExercises = new ArrayList<>(oldExerciseSets.size());
        final Date newWorkoutDate = newWorkoutUnit.getDate();
        for (ExerciseSetEntity exercise : oldExerciseSets)
        {
            newExercises.add(new ExerciseSetEntity(exercise, newWorkoutDate));
        }
        // Insert new entries:
        executor.execute(() ->
        {
            database.workoutUnitDao().insert(newWorkoutUnit);
            database.exerciseSetDao().insert(newExercises);
        });
        // Adjust current workout unit:
        observableWorkoutUnit.setValue(newWorkoutUnit);
    }

    public LiveData<WorkoutUnitEntity> changeWorkout(@NonNull WorkoutUnitEntity baseWorkoutUnit)
    {
        Log.d("changeWorkout", "base workout studio: " + baseWorkoutUnit.getStudio());  // DEBUG:
        Log.d("changeWorkout", "base workout name: " + baseWorkoutUnit.getName());  // DEBUG:
        DataRepository.executeOnceForLiveData(observableWorkoutUnit, currentWorkoutUnit ->
        {
            if (currentWorkoutUnit == null) throw new AssertionError("object cannot be null");
            final Date workoutDate = currentWorkoutUnit.getDate();
            final WorkoutUnitEntity newWorkoutUnit = new WorkoutUnitEntity(workoutDate, baseWorkoutUnit);
            // Create new entries by cloning last entries:
            final List<ExerciseSetEntity> newExercises = new ArrayList<>();
            DataRepository.executeOnceForLiveData(getExerciseSets(baseWorkoutUnit), oldExerciseSets ->
            {
                if (oldExerciseSets == null) throw new AssertionError("object cannot be null");
                for (ExerciseSetEntity exercise : oldExerciseSets)
                {
                    newExercises.add(new ExerciseSetEntity(exercise, workoutDate));
                }
                replaceCurrentWorkoutUnit(currentWorkoutUnit, newWorkoutUnit, newExercises);
            });
        });
        return observableWorkoutUnit;
    }
}