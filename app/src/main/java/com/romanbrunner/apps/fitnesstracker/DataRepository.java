package com.romanbrunner.apps.fitnesstracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.database.AppDatabase;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutUnitEntity;
import com.romanbrunner.apps.fitnesstracker.ui.MainActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private final MediatorLiveData<List<WorkoutInfoEntity>> observableWorkoutInfo;
    private final MediatorLiveData<List<ExerciseInfoEntity>> observableExerciseInfo;
    private final MediatorLiveData<WorkoutUnitEntity> observableWorkoutUnit;
    private final MediatorLiveData<List<ExerciseSetEntity>> observableExerciseSets;
    private final Executor executor;

    private DataRepository(final AppDatabase database)
    {
        this.database = database;
        observableWorkoutInfo = new MediatorLiveData<>();
        observableExerciseInfo = new MediatorLiveData<>();
        observableWorkoutUnit = new MediatorLiveData<>();
        observableExerciseSets = new MediatorLiveData<>();
        executor = Executors.newSingleThreadExecutor();

        // Load all workout info and exercise info as mediators as soon as the database is ready:
        observableWorkoutInfo.addSource(database.workoutInfoDao().loadAll(), workoutInfo ->
        {
            if (database.getDatabaseCreated().getValue() != null)
            {
                observableWorkoutInfo.postValue(workoutInfo);
            }
        });
        observableExerciseInfo.addSource(database.exerciseInfoDao().loadAll(), exerciseInfo ->
        {
            if (database.getDatabaseCreated().getValue() != null)
            {
                observableExerciseInfo.postValue(exerciseInfo);
            }
        });

        // Load newest workout unit with associated exercise sets as mediators as soon as the database is ready:
        LiveData<WorkoutUnitEntity> source = database.workoutUnitDao().loadNewest();
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            source = database.workoutUnitDao().loadNewestDebug();
        }
        observableWorkoutUnit.addSource(source, workoutUnit ->
        {
            if (database.getDatabaseCreated().getValue() != null)
            {
                workoutUnit.setDate(new Date());  // Set to current date
                observableWorkoutUnit.postValue(workoutUnit);
                observableExerciseSets.addSource(database.exerciseSetDao().loadByWorkoutId(workoutUnit.getId()), observableExerciseSets::postValue);
            }
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

    public LiveData<WorkoutInfoEntity> getCurrentWorkoutInfo()
    {
        return observableWorkoutInfo;
    }

    public LiveData<List<WorkoutInfoEntity>> getAllWorkoutInfo()
    {
        return database.workoutInfoDao().loadAll();
    }

    public LiveData<WorkoutInfoEntity> getLastWorkoutInfo()
    {
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            return database.workoutInfoDao().loadLastDebug();
        }
        else
        {
            return database.workoutInfoDao().loadLast();
        }
    }

    public LiveData<List<ExerciseInfoEntity>> getCurrentExerciseInfo()
    {
        return observableExerciseInfo;
    }

    public LiveData<List<ExerciseSetEntity>> getCurrentExerciseSets()
    {
        return observableExerciseSets;
    }

    public LiveData<List<ExerciseSetEntity>> getAllExerciseSets()
    {
        return database.exerciseSetDao().loadAll();
    }

    public LiveData<List<ExerciseSetEntity>> getExerciseSetsByWorkout(WorkoutInfoEntity workout)
    {
        return database.exerciseSetDao().loadByWorkoutId(workout.getId());
    }

    public void setExerciseSet(final ExerciseSetEntity exercise)
    {
        executor.execute(() -> database.exerciseSetDao().insertOrReplace(exercise));
    }

    public void setExerciseInfo(final List<ExerciseInfoEntity> exerciseInfoList)
    {
        List<ExerciseInfoEntity> observedExerciseInfoList = observableExerciseInfo.getValue();
        for (ExerciseInfoEntity exerciseInfo : exerciseInfoList)
        {
            if (observedExerciseInfoList != null && observedExerciseInfoList.contains(exerciseInfo))
            {
                executor.execute(() -> database.exerciseInfoDao().update(exerciseInfoList));
            }
            else
            {
                executor.execute(() -> database.exerciseInfoDao().insert(exerciseInfoList));
            }
        }
    }

    public void deleteWorkouts(List<WorkoutInfoEntity> workouts)
    {
        database.workoutInfoDao().delete(workouts);
    }

    public void saveCurrentData()
    {
        // Update exercise info:
        List<ExerciseInfoEntity> exerciseInfoList = observableExerciseInfo.getValue();
        if (exerciseInfoList != null)
        {
            executor.execute(() -> database.exerciseInfoDao().update(exerciseInfoList));
        }
        // Update current workout:
        WorkoutInfoEntity currentWorkout = observableWorkoutInfo.getValue();
        if (currentWorkout != null)
        {
            executor.execute(() -> database.workoutInfoDao().update(currentWorkout));
        }
        // Update current exercises:
        List<ExerciseSetEntity> currentExercises = observableExerciseSets.getValue();
        if (currentExercises != null)
        {
            executor.execute(() -> database.exerciseSetDao().update(currentExercises));
        }
    }

    public void finishExercises()
    {
        List<ExerciseInfoEntity> exerciseInfoList = observableExerciseInfo.getValue();
        if (exerciseInfoList != null)
        {
            // Update current entries:
            executor.execute(() -> database.exerciseInfoDao().update(exerciseInfoList));
        }

        WorkoutInfoEntity oldWorkout = observableWorkoutInfo.getValue();
        if (oldWorkout != null)
        {
            // Update current entry:
            executor.execute(() -> database.workoutInfoDao().update(oldWorkout));
            // Clone and insert new entry:
            WorkoutInfoEntity newWorkout = new WorkoutInfoEntity(oldWorkout);
            final int newWorkoutId = newWorkout.getId();
            executor.execute(() -> database.workoutInfoDao().insert(newWorkout));
            // Adjust current entry:
            observableWorkoutInfo.setValue(newWorkout);

            List<ExerciseSetEntity> oldExercises = observableExerciseSets.getValue();
            if (oldExercises != null)
            {
                // Update current entries:
                executor.execute(() -> database.exerciseSetDao().update(oldExercises));
                // Clone and insert new entries:
                List<ExerciseSetEntity> newExercises = new ArrayList<>(oldExercises.size());
                for (ExerciseSetEntity exercise : oldExercises)
                {
                    newExercises.add(new ExerciseSetEntity(exercise, newWorkoutId));
                }
                executor.execute(() -> database.exerciseSetDao().insert(newExercises));
                // Adjust current entries:
                observableExerciseSets.setValue(newExercises);
            }
        }
    }
}