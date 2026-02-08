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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


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
        executeOnceForLiveData(getNewestWorkoutUnit(), oldWorkoutUnit -> database.getDatabaseCreated().getValue() != null, oldWorkoutUnit ->
        {
            assert oldWorkoutUnit != null : "object cannot be null";
            // Create new entries by cloning last entries:
            final List<ExerciseSetEntity> newExercises = new ArrayList<>();
            DataRepository.executeOnceForLiveData(getExerciseSets(oldWorkoutUnit), oldExerciseSets ->
            {
                assert oldExerciseSets != null : "object cannot be null";
                final Date newWorkoutDate = new Date();  // Set to current date
                final WorkoutUnitEntity newWorkoutUnit = new WorkoutUnitEntity(oldWorkoutUnit, newWorkoutDate);
                for (ExerciseSetEntity exercise : oldExerciseSets)
                {
                    newExercises.add(new ExerciseSetEntity(exercise, newWorkoutDate));
                }
                replaceCurrentWorkoutUnit(oldWorkoutUnit, newWorkoutUnit, newExercises);
            });
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
            /* Delete and insert is used instead of update to make sure that all associated old exercises are removed and new exercises are added correctly. */
            database.runInTransaction(() -> {
                database.workoutUnitDao().delete(currentWorkoutUnit);
                database.workoutUnitDao().insert(newWorkoutUnit);
                database.exerciseSetDao().insert(newExercises);
            });
            /* Update the observable after database operations complete to prevent race conditions where UI tries to load exercise sets before they're properly stored. */
            observableWorkoutUnit.postValue(newWorkoutUnit);
        });
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

    public LiveData<List<ExerciseInfoEntity>> getAllExerciseInfo()
    {
        return database.exerciseInfoDao().loadAll();
    }

    public LiveData<List<ExerciseSetEntity>> getNewestExerciseSets(String exerciseInfoName)
    {
        return database.exerciseSetDao().loadNewestByExerciseInfoName(exerciseInfoName);
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

    public void removeExerciseCompletely(String exerciseInfoName)
    {
        executor.execute(() -> database.runInTransaction(() ->
        {
            // Delete all exercise sets referencing this exercise:
            database.exerciseSetDao().deleteByExerciseInfoName(exerciseInfoName);
            // Update all workout units to remove this exercise from their exerciseNames:
            List<WorkoutUnitEntity> allWorkoutUnits = database.workoutUnitDao().loadAllSync();
            for (WorkoutUnitEntity workoutUnit : allWorkoutUnits)
            {
                String names = workoutUnit.getExerciseNames();
                if (names != null && WorkoutUnitEntity.exerciseNames2NameSet(names).contains(exerciseInfoName))
                {
                    String updatedNames = WorkoutUnitEntity.removeExerciseFromNames(names, exerciseInfoName);
                    if (updatedNames.isEmpty())
                    {
                        database.workoutUnitDao().delete(workoutUnit);
                    }
                    else
                    {
                        workoutUnit.setExerciseNames(updatedNames);
                        database.workoutUnitDao().update(workoutUnit);
                    }
                }
            }
            // Delete the exercise info entry:
            database.exerciseInfoDao().deleteByName(exerciseInfoName);
        }));
    }

    public void setCurrentWorkout(WorkoutUnitEntity workoutUnit)
    {
        observableWorkoutUnit.setValue(workoutUnit);
    }

    public void storeWorkout(@NonNull WorkoutUnitEntity workoutUnit, @NonNull List<ExerciseSetEntity> exerciseSets)
    {
        executor.execute(() ->
        {
            /* Delete and insert is used instead of update to make sure that all associated old exercises are removed and new exercises are added correctly. */
            database.runInTransaction(() -> {
                database.workoutUnitDao().delete(workoutUnit);
                database.workoutUnitDao().insert(workoutUnit);
                database.exerciseSetDao().insert(exerciseSets);
            });
        });
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
            /* Delete and insert is used instead of update to make sure that all associated old exercises are removed and new exercises are added correctly. */
            database.runInTransaction(() -> {
                database.workoutUnitDao().delete(oldWorkoutUnit);
                database.workoutUnitDao().insert(oldWorkoutUnit);
                database.exerciseSetDao().insert(oldExerciseSets);
            });
        });
        // Clone new entries:
        final WorkoutUnitEntity newWorkoutUnit = new WorkoutUnitEntity(oldWorkoutUnit);
        final List<ExerciseSetEntity> newExercises = new ArrayList<>(oldExerciseSets.size());
        final Date newWorkoutDate = newWorkoutUnit.getDate();
        for (ExerciseSetEntity exercise : oldExerciseSets)
        {
            newExercises.add(new ExerciseSetEntity(exercise, newWorkoutDate));
        }
        // Insert new entries:
        executor.execute(() ->
        {
            database.runInTransaction(() -> {
                database.workoutUnitDao().insert(newWorkoutUnit);
                database.exerciseSetDao().insert(newExercises);
            });
            observableWorkoutUnit.postValue(newWorkoutUnit);
        });
    }

    public void changeWorkout(@NonNull WorkoutUnitEntity baseWorkoutUnit)
    {
        DataRepository.executeOnceForLiveData(observableWorkoutUnit, currentWorkoutUnit ->
        {
            assert currentWorkoutUnit != null : "object cannot be null";
            final Date workoutDate = currentWorkoutUnit.getDate();
            final WorkoutUnitEntity newWorkoutUnit = new WorkoutUnitEntity(baseWorkoutUnit, workoutDate);

            /* Ensure we're getting exercise sets from the newest workout unit. */
            /* The baseWorkoutUnit should already be the newest, but we explicitly get its exercise sets using its original date to ensure we get the most recent saved state. */
            DataRepository.executeOnceForLiveData(getExerciseSets(baseWorkoutUnit), oldExerciseSets ->
            {
                assert oldExerciseSets != null : "object cannot be null";
                // Create new exercises for the current session by cloning from the base workout:
                final List<ExerciseSetEntity> newExercises = new ArrayList<>();
                for (ExerciseSetEntity exercise : oldExerciseSets)
                {
                    newExercises.add(new ExerciseSetEntity(exercise, workoutDate));
                }
                // Replace the current workout data with the new workout configuration
                replaceCurrentWorkoutUnit(currentWorkoutUnit, newWorkoutUnit, newExercises);
            });
        });
    }
}