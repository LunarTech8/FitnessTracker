package com.romanbrunner.apps.fitnesstracker.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.BasicApp;
import com.romanbrunner.apps.fitnesstracker.DataRepository;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseEntity;

import java.util.List;


public class ExercisesViewModel extends AndroidViewModel
{
    // --------------------
    // Functional code
    // --------------------

    private final DataRepository repository;
    private final MediatorLiveData<List<ExerciseEntity>> observableExercises;

    public ExercisesViewModel(Application application)
    {
        super(application);
        observableExercises = new MediatorLiveData<>();
        observableExercises.setValue(null);  // Set null by default until we get data from the database

        // Observe the changes of the exercises from the database and forward them:
        repository = ((BasicApp)application).getRepository();
        observableExercises.addSource(repository.getExercises(), observableExercises::setValue);
    }

    public LiveData<List<ExerciseEntity>> getExercises()
    {
        return observableExercises;
    }

    public void setExercise(final ExerciseEntity exercise)
    {
        repository.setExercise(exercise);
    }

    public void saveExercises()
    {
        repository.saveExercises();
    }
}