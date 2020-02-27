package com.romanbrunner.apps.fitnesstracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.database.AppDatabase;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutEntity;
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
    private final MediatorLiveData<WorkoutEntity> observableWorkout;
    private final MediatorLiveData<List<ExerciseInfoEntity>> observableExerciseInfo;
    private final MediatorLiveData<List<ExerciseSetEntity>> observableExerciseSets;
    private final Executor executor;

    private DataRepository(final AppDatabase database)
    {
        this.database = database;
        observableWorkout = new MediatorLiveData<>();
        observableExerciseInfo = new MediatorLiveData<>();
        observableExerciseSets = new MediatorLiveData<>();
        executor = Executors.newSingleThreadExecutor();

        // Load newest workout with associated exercises as mediators as soon as the database is ready:
        LiveData<WorkoutEntity> source = database.workoutDao().loadNewest();
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            source = database.workoutDao().loadNewestDebug();
        }
        observableWorkout.addSource(source, workout ->
        {
            if (database.getDatabaseCreated().getValue() != null)
            {
                workout.setDate(new Date());  // Set to current date
                observableWorkout.postValue(workout);
                observableExerciseInfo.addSource(database.exerciseInfoDao().loadByWorkoutId(workout.getId()), observableExerciseInfo::postValue);
                observableExerciseSets.addSource(database.exerciseSetDao().loadByWorkoutId(workout.getId()), observableExerciseSets::postValue);
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

    public LiveData<WorkoutEntity> getCurrentWorkout()
    {
        return observableWorkout;
    }

    public LiveData<List<WorkoutEntity>> getAllWorkouts()
    {
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            return database.workoutDao().loadAllDebug();
        }
        else
        {
            return database.workoutDao().loadAll();
        }
    }

    public LiveData<WorkoutEntity> getLastWorkout()
    {
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            return database.workoutDao().loadLastDebug();
        }
        else
        {
            return database.workoutDao().loadLast();
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

    public LiveData<List<ExerciseSetEntity>> getExerciseSetsByWorkout(WorkoutEntity workout)
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

    public void deleteWorkouts(List<WorkoutEntity> workouts)
    {
        database.workoutDao().delete(workouts);
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
        WorkoutEntity currentWorkout = observableWorkout.getValue();
        if (currentWorkout != null)
        {
            executor.execute(() -> database.workoutDao().update(currentWorkout));
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

        WorkoutEntity oldWorkout = observableWorkout.getValue();
        if (oldWorkout != null)
        {
            // Update current entry:
            executor.execute(() -> database.workoutDao().update(oldWorkout));
            // Clone and insert new entry:
            WorkoutEntity newWorkout = new WorkoutEntity(oldWorkout);
            final int newWorkoutId = newWorkout.getId();
            executor.execute(() -> database.workoutDao().insert(newWorkout));
            // Adjust current entry:
            observableWorkout.setValue(newWorkout);

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