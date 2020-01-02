package com.romanbrunner.apps.fitnesstracker.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.BasicApp;
import com.romanbrunner.apps.fitnesstracker.DataRepository;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutEntity;

import java.util.List;


public class MainViewModel extends AndroidViewModel
{
    // --------------------
    // Functional code
    // --------------------

    private final DataRepository repository;
    private final MediatorLiveData<WorkoutEntity> observableWorkout;
    private final MediatorLiveData<List<ExerciseEntity>> observableExercises;

    public MainViewModel(Application application)
    {
        super(application);
        repository = ((BasicApp)application).getRepository();
        observableWorkout = new MediatorLiveData<>();
        observableExercises = new MediatorLiveData<>();
        // Set null by default until we get data from the database:
        observableWorkout.setValue(null);
        observableExercises.setValue(null);

        // Observe the changes from the database and forward them:
        observableWorkout.addSource(repository.getWorkout(), observableWorkout::setValue);
        observableExercises.addSource(repository.getExercises(), observableExercises::setValue);
    }

    public LiveData<WorkoutEntity> getWorkout()
    {
        return observableWorkout;
    }

    public LiveData<List<ExerciseEntity>> getExercises()
    {
        return observableExercises;
    }

    public void setExercise(final ExerciseEntity exercise)
    {
        repository.setExercise(exercise);
    }

    public void finishExercises()
    {
        repository.finishExercises();
    }
}