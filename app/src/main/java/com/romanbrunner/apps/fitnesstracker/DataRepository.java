package com.romanbrunner.apps.fitnesstracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.database.AppDatabase;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseEntity;

import java.util.List;


/* Repository handling the work with exercises. */
public class DataRepository
{
    // --------------------
    // Functional code
    // --------------------

    private static DataRepository instance;

    private final AppDatabase database;
    private MediatorLiveData<List<ExerciseEntity>> observableExercises;

    private DataRepository(final AppDatabase database)
    {

        this.database = database;
        observableExercises = new MediatorLiveData<>();

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
        if (database.exerciseDao().loadById(exercise.getId()) == null)  // TODO: maybe there is a contains function in exerciseDao that could be used instead
        {
            database.exerciseDao().insert(exercise);
        }
        else
        {
            database.exerciseDao().update(exercise);
        }
    }

    public void saveExercises()
    {
        database.exerciseDao().update(observableExercises.getValue());
    }
}