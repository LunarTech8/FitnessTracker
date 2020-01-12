package com.romanbrunner.apps.fitnesstracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.database.AppDatabase;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutEntity;

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
    private final MediatorLiveData<List<ExerciseEntity>> observableExercises;
    private final Executor executor;

    private DataRepository(final AppDatabase database)
    {
        this.database = database;
        observableWorkout = new MediatorLiveData<>();
        observableExercises = new MediatorLiveData<>();
        executor = Executors.newSingleThreadExecutor();

        // Load newest workout with associated exercises as mediators as soon as the database is ready:
        observableWorkout.addSource(database.workoutDao().loadNewest(), workout ->
        {
            if (database.getDatabaseCreated().getValue() != null)
            {
                workout.setDate(new Date());  // Set to current date
                observableWorkout.postValue(workout);
                observableWorkout.addSource(database.exerciseDao().loadByWorkoutId(workout.getId()), observableExercises::postValue);
            }
        });
    }

    public static DataRepository getInstance(final AppDatabase database)
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
        return database.workoutDao().loadAll();
    }

    public LiveData<WorkoutEntity> getSecondNewestWorkout()
    {
        return database.workoutDao().loadSecondNewest();
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

    public void setExercise(final ExerciseEntity exercise)
    {
        executor.execute(() -> database.exerciseDao().insertOrReplace(exercise));
    }

    public void saveCurrentData()
    {
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