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
                observableWorkoutUnit.postValue(newWorkoutUnit);
            });
        });
    }

    // TODO: maybe instead of replacing the entries can be updated via database.<...>Dao().update
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
            Log.d("finishWorkout", "oldWorkoutUnit.getExerciseNames() = " + oldWorkoutUnit.getExerciseNames());  // DEBUG:
            Log.d("finishWorkout", "oldWorkoutUnit.getDate() = " + oldWorkoutUnit.getDate().toString());  // DEBUG:
            Log.d("finishWorkout", "oldExerciseSets = " + oldExerciseSets.stream().map(element -> element.getExerciseInfoName() + " " + element.getWorkoutUnitDate().toString()).collect(Collectors.joining(", ")));  // DEBUG:
            database.workoutUnitDao().update(oldWorkoutUnit);
            database.exerciseSetDao().update(oldExerciseSets);
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
            database.workoutUnitDao().insert(newWorkoutUnit);
            database.exerciseSetDao().insert(newExercises);
        });
        // Adjust current workout unit:
        observableWorkoutUnit.setValue(newWorkoutUnit);
    }

    public void changeWorkout(@NonNull WorkoutUnitEntity baseWorkoutUnit)
    {
//        Log.d("changeWorkout", "base workout studio = " + baseWorkoutUnit.getStudio());  // DEBUG:
//        Log.d("changeWorkout", "base workout name = " + baseWorkoutUnit.getName());  // DEBUG:
        DataRepository.executeOnceForLiveData(observableWorkoutUnit, currentWorkoutUnit ->
        {
            assert currentWorkoutUnit != null : "object cannot be null";
            final Date workoutDate = currentWorkoutUnit.getDate();
            final WorkoutUnitEntity newWorkoutUnit = new WorkoutUnitEntity(baseWorkoutUnit, workoutDate);
            // Create new entries by cloning last entries:
            final List<ExerciseSetEntity> newExercises = new ArrayList<>();
            DataRepository.executeOnceForLiveData(getExerciseSets(baseWorkoutUnit), oldExerciseSets ->
            {
                assert oldExerciseSets != null : "object cannot be null";
                Log.d("changeWorkout", "baseWorkoutUnit.getExerciseNames() = " + baseWorkoutUnit.getExerciseNames());  // DEBUG:
                Log.d("changeWorkout", "baseWorkoutUnit.getDate() = " + baseWorkoutUnit.getDate().toString());  // DEBUG:
                Log.d("changeWorkout", "oldExerciseSets = " + oldExerciseSets.stream().map(element -> element.getExerciseInfoName() + " " + element.getWorkoutUnitDate().toString()).collect(Collectors.joining(", ")));  // DEBUG:
                // FIXME: problem when creating second entry with different name and then changing to first entry
                // correct after creating second entry (finish)
                // baseWorkoutUnit and oldExerciseSets is correct on first change with two entries
                // subscribeUi has correct workoutUnit BUT wrong exerciseSetList and exerciseInfoList (but with correct date)
                // -> wrong loaded or overwritten during workout change?
                // (problem with same date for exercises? -> then more should be displayed as everything with the date is loaded)
                // (BUT debug log 7 shows correct entries -> why difference between subscribeUi and printDebugLog? -> printDebugLog for log 7 doesn't load exercises but shows workout string -> use log 0)
                // (-> maybe exerciseSet isn't stored away properly after edit (without accomplished workout unit))
                // (printDebugLog log 0 verifies that: after the edit the date is current one but the exercises are still the old ones (with old values))
                // (updateEditMode shows the correct displayed inside adapter.getExerciseSets() but they don't seem to be stored away / updated anywhere)
                // (TODO: check where/when exerciseSets are stored (compare to storage of workoutUnit), should be updated when exiting edit mode)
                // (only updated for exercises and workouts when finishing workout and when replaceCurrentWorkoutUnit is called here (but only the new ones))
                // (-> DESIGN MISUNDERSTANDING: only when workout is finished workoutUnit and exerciseSets are stored for good, else they get removed on change)
                // -> Problem still there on second switch if modified workout was finished, diversion now in workoutUnit and exerciseSets
                // exerciseSets seems to never be stored correctly (at least if only sets were modified)
                // TODO: check if database.exerciseSetDao().update(oldExerciseSets) does what it's supposed to do in finishWorkout
                for (ExerciseSetEntity exercise : oldExerciseSets)
                {
                    newExercises.add(new ExerciseSetEntity(exercise, workoutDate));
                }
                replaceCurrentWorkoutUnit(currentWorkoutUnit, newWorkoutUnit, newExercises);
                // FIXME: with adding these debugs the problem is only there at second workout change! -> then already inconsistency between oldExerciseSets and baseWorkoutUnit -> maybe because date then doesn't change
                // -> not reproducible during same simulation! -> it seems then to happen every second time!
                // FIXME: but always if you change more it will finally stay at the first entry exercises
                Log.d("changeWorkout", "newWorkoutUnit.getExerciseNames() = " + newWorkoutUnit.getExerciseNames());  // DEBUG:
                Log.d("changeWorkout", "newWorkoutUnit.getDate() = " + newWorkoutUnit.getDate().toString());  // DEBUG:
                Log.d("changeWorkout", "newExercises = " + newExercises.stream().map(element -> element.getExerciseInfoName() + " " + element.getWorkoutUnitDate().toString()).collect(Collectors.joining(", ")));  // DEBUG:
            });
        });
    }
}