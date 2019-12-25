package com.romanbrunner.apps.fitnesstracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.database.AppDatabase;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseEntity;
import com.romanbrunner.apps.fitnesstracker.model.Exercise;

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
    private final MediatorLiveData<List<ExerciseEntity>> observableExercises;
    private final Executor executor;

    private DataRepository(final AppDatabase database)
    {
        this.database = database;
        observableExercises = new MediatorLiveData<>();
        executor = Executors.newSingleThreadExecutor();

        // Load and and add all exercises to the mediator list as soon as the database is ready:
        observableExercises.addSource(database.exerciseDao().loadAll(), exerciseEntities ->
        {
            if (database.getDatabaseCreated().getValue() != null)
            {
                observableExercises.postValue(exerciseEntities);
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

    public LiveData<List<ExerciseEntity>> getExercises()
    {
        return observableExercises;
    }

    public LiveData<ExerciseEntity> getExercise(final int exerciseId)
    {
        return database.exerciseDao().loadById(exerciseId);
    }

    public void setExercise(final ExerciseEntity exercise)
    {
        executor.execute(() -> database.exerciseDao().insertOrReplace(exercise));
    }

    public void finishExercises()
    {
        List<ExerciseEntity> exercises = observableExercises.getValue();
        if (exercises != null)
        {
            for (ExerciseEntity exercise : exercises)
            {
                exercise.setDone(false);
            }
            executor.execute(() -> database.exerciseDao().update(exercises));
        }
    }
}