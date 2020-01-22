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
import com.romanbrunner.apps.fitnesstracker.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Collections;
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
        observableWorkout.addSource(repository.getCurrentWorkout(), observableWorkout::postValue);
        observableExercises.addSource(repository.getCurrentExercises(), observableExercises::postValue);
    }

    private void printWorkoutData(@NonNull String headerMessage, @Nullable String nullMessage, List<WorkoutEntity> workouts)
    {
        if (workouts != null)
        {
            java.lang.System.out.println(headerMessage);
            for (WorkoutEntity workout : workouts)
            {
                java.lang.System.out.print("Id: " + workout.getId() + ", ");
                java.lang.System.out.print("Name: " + workout.getName() + ", ");
                java.lang.System.out.print("Date: " + SimpleDateFormat.getDateTimeInstance().format(workout.getDate()) + ", ");
                java.lang.System.out.print("Description: " + workout.getDescription() + "\n");
            }
        }
        else if (nullMessage != null)
        {
            java.lang.System.out.println(nullMessage);
        }
    }

    private void printExerciseData(@NonNull String headerMessage, @Nullable String nullMessage, List<ExerciseEntity> exercises)
    {
        if (exercises != null)
        {
            java.lang.System.out.println(headerMessage);
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
        else if (nullMessage != null)
        {
            java.lang.System.out.println(nullMessage);
        }
    }

    public LiveData<WorkoutEntity> getCurrentWorkout()
    {
        return observableWorkout;
    }

    public LiveData<WorkoutEntity> getLastWorkout()
    {
        return repository.getLastWorkout();
    }

    public LiveData<List<WorkoutEntity>> getAllWorkouts()
    {
        return repository.getAllWorkouts();
    }

    public LiveData<List<ExerciseEntity>> getCurrentExercises()
    {
        return observableExercises;
    }

    public void setExercise(final ExerciseEntity exercise)  // UNUSED:
    {
        repository.setExercise(exercise);
    }

    public void saveCurrentData()
    {
        repository.saveCurrentData();
    }

    public void finishExercises()
    {
        repository.finishExercises();
    }

    public void printDebugLog(@NonNull LifecycleOwner owner)
    {
        java.lang.System.out.println("--- DEBUG LOG ---");
        printWorkoutData("Observed workout:", "No workout observed", Collections.singletonList(repository.getCurrentWorkout().getValue()));
        printExerciseData("Observed exercises:", "No exercises observed", repository.getCurrentExercises().getValue());
        java.lang.System.out.println("---");
        if (MainActivity.DEBUG_LOG_MODE == 0)  // All workouts and all exercises
        {
            repository.getAllWorkouts().observe(owner, (@Nullable List<WorkoutEntity> workouts) -> printWorkoutData("All workouts:", null, workouts));
            repository.getAllExercises().observe(owner, (@Nullable List<ExerciseEntity> exercises) -> printExerciseData("All exercises (normal and debug):", null, exercises));
        }
        else if (MainActivity.DEBUG_LOG_MODE == 1)  // Last workout and last exercises
        {
            repository.getLastWorkout().observe(owner, (@Nullable WorkoutEntity workout) ->
            {
                if (workout != null)
                {
                    printWorkoutData("Last workout:", null, Collections.singletonList(workout));
                    repository.getExercisesByWorkout(workout).observe(owner, (@Nullable List<ExerciseEntity> exercises) -> printExerciseData("Last exercises:", null, exercises));
                }
            });
        }
        else if (MainActivity.DEBUG_LOG_MODE == 2)  // All workouts
        {
            repository.getAllWorkouts().observe(owner, (@Nullable List<WorkoutEntity> workouts) -> printWorkoutData("All workouts:", null, workouts));
        }
    }

    public void removeDebugWorkouts(@NonNull LifecycleOwner owner)
    {
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            repository.getAllWorkouts().observe(owner, (@Nullable List<WorkoutEntity> workouts) ->
            {
                if (workouts != null && workouts.size() > 1)
                {
                    workouts.remove(0);
                    repository.deleteWorkouts(workouts);
                }
            });
        }
    }
}