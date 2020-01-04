package com.romanbrunner.apps.fitnesstracker.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.BasicApp;
import com.romanbrunner.apps.fitnesstracker.DataRepository;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutEntity;
import com.romanbrunner.apps.fitnesstracker.model.Workout;

import java.text.SimpleDateFormat;
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
        observableWorkout.addSource(repository.getCurrentWorkout(), observableWorkout::setValue);
        observableExercises.addSource(repository.getCurrentExercises(), observableExercises::setValue);
    }

    public LiveData<WorkoutEntity> getCurrentWorkout()
    {
        return observableWorkout;
    }

    public LiveData<List<ExerciseEntity>> getCurrentExercises()
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

    public void printDebugLog(@NonNull LifecycleOwner owner)
    {
        java.lang.System.out.println("--- DEBUG LOG ---");

        java.lang.System.out.println("Observed workout:");
        WorkoutEntity obsWorkout = repository.getCurrentWorkout().getValue();
        if (obsWorkout != null)
        {
            java.lang.System.out.print("Id: " + obsWorkout.getId() + ", ");
            java.lang.System.out.print("Name: " + obsWorkout.getName() + ", ");
            java.lang.System.out.print("Date: " + SimpleDateFormat.getDateTimeInstance().format(obsWorkout.getDate()) + ", ");
            java.lang.System.out.print("Description: " + obsWorkout.getDescription() + "\n");
        }
        else
        {
            java.lang.System.out.println("No workout observed");
        }

        java.lang.System.out.println("Observed exercises:");
        List<ExerciseEntity> obsExercises = repository.getCurrentExercises().getValue();
        if (obsExercises != null)
        {
            for (ExerciseEntity exercise : obsExercises)
            {
                java.lang.System.out.print("Id: " + exercise.getId() + ", ");
                java.lang.System.out.print("WorkoutId: " + exercise.getWorkoutId() + ", ");
                java.lang.System.out.print("Name: " + exercise.getName() + ", ");
                java.lang.System.out.print("Token: " + exercise.getToken() + ", ");
                java.lang.System.out.print("Repeats: " + exercise.getRepeats() + ", ");
                java.lang.System.out.print("Weight: " + exercise.getWeight() + ", ");
                java.lang.System.out.print("Remarks: " + exercise.getRemarks() + ", ");
                java.lang.System.out.print("Done: " + exercise.isDone() + "\n");
            }
        }
        else
        {
            java.lang.System.out.println("No exercises observed");
        }

        java.lang.System.out.println("---");

        repository.getAllWorkouts().observe(owner, (@Nullable List<WorkoutEntity> workouts) ->
        {
            if (workouts != null)
            {
                java.lang.System.out.println("All workouts:");
                for (WorkoutEntity workout : workouts)
                {
                    java.lang.System.out.print("Id: " + workout.getId() + ", ");
                    java.lang.System.out.print("Name: " + workout.getName() + ", ");
                    java.lang.System.out.print("Date: " + SimpleDateFormat.getDateTimeInstance().format(workout.getDate()) + ", ");
                    java.lang.System.out.print("Description: " + workout.getDescription() + "\n");
                }
            }
        });

        repository.getAllExercises().observe(owner, (@Nullable List<ExerciseEntity> exercises) ->
        {
            if (exercises != null)
            {
                java.lang.System.out.println("All exercises:");
                for (ExerciseEntity exercise : exercises)
                {
                    java.lang.System.out.print("Id: " + exercise.getId() + ", ");
                    java.lang.System.out.print("WorkoutId: " + exercise.getWorkoutId() + ", ");
                    java.lang.System.out.print("Name: " + exercise.getName() + ", ");
                    java.lang.System.out.print("Token: " + exercise.getToken() + ", ");
                    java.lang.System.out.print("Repeats: " + exercise.getRepeats() + ", ");
                    java.lang.System.out.print("Weight: " + exercise.getWeight() + ", ");
                    java.lang.System.out.print("Remarks: " + exercise.getRemarks() + ", ");
                    java.lang.System.out.print("Done: " + exercise.isDone() + "\n");
                }
            }
        });
    }
}