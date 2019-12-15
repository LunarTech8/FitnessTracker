package com.romanbrunner.apps.fitnesstracker.ViewModels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.BasicApp;
import com.romanbrunner.apps.fitnesstracker.DataRepository;
import com.romanbrunner.apps.fitnesstracker.Database.ExerciseEntity;

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
        observableExercises.addSource(repository.getExercisesAll(), observableExercises::setValue);
    }

    public LiveData<List<ExerciseEntity>> getAllExercises()
    {
        return observableExercises;
    }
}