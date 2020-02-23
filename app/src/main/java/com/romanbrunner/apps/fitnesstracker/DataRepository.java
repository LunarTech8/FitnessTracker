package com.romanbrunner.apps.fitnesstracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.database.AppDatabase;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseEntity;
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
    private final MediatorLiveData<List<ExerciseInfoEntity>> observableExerciseInfo;
    private final MediatorLiveData<WorkoutEntity> observableWorkout;
    private final MediatorLiveData<List<ExerciseEntity>> observableExercises;
    private final Executor executor;

    private DataRepository(final AppDatabase database)
    {
        this.database = database;
        observableExerciseInfo = new MediatorLiveData<>();
        observableWorkout = new MediatorLiveData<>();
        observableExercises = new MediatorLiveData<>();
        executor = Executors.newSingleThreadExecutor();

        observableExerciseInfo.addSource(database.exerciseInfoDao().loadAll(), observableExerciseInfo::postValue);
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
                observableWorkout.addSource(database.exerciseDao().loadByWorkoutId(workout.getId()), observableExercises::postValue);
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

    public LiveData<List<ExerciseInfoEntity>> getAllExerciseInfo()
    {
        return observableExerciseInfo;
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

    public LiveData<List<ExerciseEntity>> getCurrentExercises()
    {
        return observableExercises;
    }

    public LiveData<List<ExerciseEntity>> getAllExercises()
    {
        return database.exerciseDao().loadAll();
    }

    public LiveData<List<ExerciseEntity>> getExercisesByWorkout(WorkoutEntity workout)
    {
        return database.exerciseDao().loadByWorkoutId(workout.getId());
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

    public void setExercise(final ExerciseEntity exercise)
    {
        executor.execute(() -> database.exerciseDao().insertOrReplace(exercise));
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
        List<ExerciseEntity> currentExercises = observableExercises.getValue();
        if (currentExercises != null)
        {
            executor.execute(() -> database.exerciseDao().update(currentExercises));
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

            List<ExerciseEntity> oldExercises = observableExercises.getValue();
            if (oldExercises != null)
            {
                // Update current entries:
                executor.execute(() -> database.exerciseDao().update(oldExercises));
                // Clone and insert new entries:
                List<ExerciseEntity> newExercises = new ArrayList<>(oldExercises.size());
                for (ExerciseEntity exercise : oldExercises)
                {
                    newExercises.add(new ExerciseEntity(exercise, newWorkoutId));
                }
                executor.execute(() -> database.exerciseDao().insert(newExercises));
                // Adjust current entries:
                observableExercises.setValue(newExercises);
            }
        }
    }
}