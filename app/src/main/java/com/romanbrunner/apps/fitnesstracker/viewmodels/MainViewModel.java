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
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
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
    private final MediatorLiveData<List<ExerciseInfoEntity>> observableExerciseInfo;
    private final MediatorLiveData<List<ExerciseSetEntity>> observableExerciseSets;

    public MainViewModel(Application application)
    {
        super(application);
        repository = ((BasicApp)application).getRepository();
        observableWorkout = new MediatorLiveData<>();
        observableExerciseInfo = new MediatorLiveData<>();
        observableExerciseSets = new MediatorLiveData<>();
        // Set null by default until we get data from the database:
        observableWorkout.setValue(null);
        observableExerciseInfo.setValue(null);
        observableExerciseSets.setValue(null);

        // Observe the changes from the database and forward them:
        observableWorkout.addSource(repository.getCurrentWorkout(), observableWorkout::postValue);
        observableExerciseInfo.addSource(repository.getCurrentExerciseInfo(), observableExerciseInfo::postValue);
        observableExerciseSets.addSource(repository.getCurrentExerciseSets(), observableExerciseSets::postValue);
    }

    private void printExerciseInfoData(@NonNull String headerMessage, @Nullable String nullMessage, List<ExerciseInfoEntity> exerciseInfoList)  // TODO: use in printDebugLog
    {
        if (exerciseInfoList != null)
        {
            java.lang.System.out.println(headerMessage);
            for (ExerciseInfoEntity exercise : exerciseInfoList)
            {

                java.lang.System.out.print("Name: " + exercise.getName() + ", ");
                java.lang.System.out.print("Token: " + exercise.getToken() + ", ");
                java.lang.System.out.print("Remarks: " + exercise.getRemarks() + ", ");
            }
        }
        else if (nullMessage != null)
        {
            java.lang.System.out.println(nullMessage);
        }
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

    private void printExerciseData(@NonNull String headerMessage, @Nullable String nullMessage, List<ExerciseSetEntity> exercises)
    {
        if (exercises != null)
        {
            java.lang.System.out.println(headerMessage);
            for (ExerciseSetEntity exercise : exercises)
            {

                java.lang.System.out.print("Id: " + exercise.getId() + ", ");
                java.lang.System.out.print("WorkoutId: " + exercise.getWorkoutId() + ", ");
                java.lang.System.out.print("ExerciseInfoName: " + exercise.getExerciseInfoName() + ", ");
                java.lang.System.out.print("Repeats: " + exercise.getRepeats() + ", ");
                java.lang.System.out.print("Weight: " + exercise.getWeight() + ", ");
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

    public LiveData<List<ExerciseInfoEntity>> getCurrentExerciseInfo()
    {
        return observableExerciseInfo;
    }

    public LiveData<List<ExerciseSetEntity>> getCurrentExerciseSets()
    {
        return observableExerciseSets;
    }

    public void setExerciseInfo(final List<ExerciseInfoEntity> exerciseInfoList)
    {
        repository.setExerciseInfo(exerciseInfoList);
    }

    public void setExerciseSet(final ExerciseSetEntity exerciseSet)  // UNUSED:
    {
        repository.setExerciseSet(exerciseSet);
    }

    public void saveCurrentData()  // UNUSED:
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
        printExerciseData("Observed exercises:", "No exercises observed", repository.getCurrentExerciseSets().getValue());
        java.lang.System.out.println("---");
        if (MainActivity.DEBUG_LOG_MODE == 0)  // All workouts and all exercises
        {
            repository.getAllWorkouts().observe(owner, (@Nullable List<WorkoutEntity> workouts) -> printWorkoutData("All workouts:", null, workouts));
            repository.getAllExerciseSets().observe(owner, (@Nullable List<ExerciseSetEntity> exercises) -> printExerciseData("All exercises (normal and debug):", null, exercises));
        }
        else if (MainActivity.DEBUG_LOG_MODE == 1)  // Last workout and last exercises
        {
            repository.getLastWorkout().observe(owner, (@Nullable WorkoutEntity workout) ->
            {
                if (workout != null)
                {
                    printWorkoutData("Last workout:", null, Collections.singletonList(workout));
                    repository.getExerciseSetsByWorkout(workout).observe(owner, (@Nullable List<ExerciseSetEntity> exercises) -> printExerciseData("Last exercises:", null, exercises));
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