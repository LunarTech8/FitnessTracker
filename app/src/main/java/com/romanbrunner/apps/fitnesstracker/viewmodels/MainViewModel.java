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
import com.romanbrunner.apps.fitnesstracker.database.WorkoutInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutUnitEntity;
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
    private final MediatorLiveData<List<WorkoutInfoEntity>> observableWorkoutInfo;
    private final MediatorLiveData<List<ExerciseInfoEntity>> observableExerciseInfo;
    private final MediatorLiveData<WorkoutUnitEntity> observableWorkoutUnit;
    private final MediatorLiveData<List<ExerciseSetEntity>> observableExerciseSets;

    public MainViewModel(Application application)
    {
        super(application);
        repository = ((BasicApp)application).getRepository();
        observableWorkoutInfo = new MediatorLiveData<>();
        observableExerciseInfo = new MediatorLiveData<>();
        observableWorkoutUnit = new MediatorLiveData<>();
        observableExerciseSets = new MediatorLiveData<>();
        // Set null by default until we get data from the database:
        observableWorkoutInfo.setValue(null);
        observableExerciseInfo.setValue(null);
        observableWorkoutUnit.setValue(null);
        observableExerciseSets.setValue(null);

        // Observe the changes from the database and forward them:
        observableWorkoutInfo.addSource(repository.getWorkoutInfo(), observableWorkoutInfo::postValue);
        observableExerciseInfo.addSource(repository.getExerciseInfo(), observableExerciseInfo::postValue);
        observableWorkoutUnit.addSource(repository.getCurrentWorkoutUnit(), observableWorkoutUnit::postValue);
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

    private void printWorkoutUnitsData(@NonNull String headerMessage, @Nullable String nullMessage, List<WorkoutUnitEntity> workoutUnits)
    {
        if (workoutUnits != null)
        {
            java.lang.System.out.println(headerMessage);
            for (WorkoutUnitEntity workout : workoutUnits)
            {
                java.lang.System.out.print("Id: " + workout.getId() + ", ");
                java.lang.System.out.print("WorkoutInfoName: " + workout.getWorkoutInfoName() + ", ");
                java.lang.System.out.print("WorkoutInfoVersion: " + workout.getWorkoutInfoVersion() + ", ");
                java.lang.System.out.print("Date: " + SimpleDateFormat.getDateTimeInstance().format(workout.getDate()) + ", ");
            }
        }
        else if (nullMessage != null)
        {
            java.lang.System.out.println(nullMessage);
        }
    }

    private void printExerciseSetsData(@NonNull String headerMessage, @Nullable String nullMessage, List<ExerciseSetEntity> exerciseSets)
    {
        if (exerciseSets != null)
        {
            java.lang.System.out.println(headerMessage);
            for (ExerciseSetEntity exercise : exerciseSets)
            {

                java.lang.System.out.print("Id: " + exercise.getId() + ", ");
                java.lang.System.out.print("WorkoutId: " + exercise.getWorkoutUnitId() + ", ");
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

    public LiveData<List<WorkoutInfoEntity>> getWorkoutInfo()
    {
        return observableWorkoutInfo;
    }

    public LiveData<List<ExerciseInfoEntity>> getExerciseInfo()
    {
        return observableExerciseInfo;
    }

    public void setExerciseInfo(final List<ExerciseInfoEntity> exerciseInfoList)
    {
        repository.setExerciseInfo(exerciseInfoList);
    }

    public LiveData<WorkoutUnitEntity> getCurrentWorkoutUnit()
    {
        return observableWorkoutUnit;
    }

    public LiveData<List<WorkoutUnitEntity>> getAllWorkoutUnits()
    {
        return repository.getAllWorkoutUnits();
    }

    public LiveData<WorkoutUnitEntity> getLastWorkoutUnit()
    {
        return repository.getLastWorkoutUnit();
    }

    public LiveData<List<ExerciseSetEntity>> getCurrentExerciseSets()
    {
        return observableExerciseSets;
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

    public void printDebugLog(@NonNull LifecycleOwner owner)  // TODO: refactor
    {
        java.lang.System.out.println("--- DEBUG LOG ---");
        printWorkoutUnitsData("Observed workout unit:", "No workout unit observed", Collections.singletonList(repository.getCurrentWorkoutUnit().getValue()));
        printExerciseSetsData("Observed exercise sets:", "No exercise sets observed", repository.getCurrentExerciseSets().getValue());
        java.lang.System.out.println("---");
        if (MainActivity.DEBUG_LOG_MODE == 0)  // All workout units and all exercise sets
        {
            repository.getAllWorkoutUnits().observe(owner, (@Nullable List<WorkoutUnitEntity> workoutUnits) -> printWorkoutUnitsData("All workout units:", null, workoutUnits));
            repository.getAllExerciseSets().observe(owner, (@Nullable List<ExerciseSetEntity> exerciseSets) -> printExerciseSetsData("All exercise sets (normal and debug):", null, exerciseSets));
        }
        else if (MainActivity.DEBUG_LOG_MODE == 1)  // Last workout unit and last exercise sets
        {
            repository.getLastWorkoutUnit().observe(owner, (@Nullable WorkoutUnitEntity workoutUnit) ->
            {
                if (workoutUnit != null)
                {
                    printWorkoutUnitsData("Last workout unit:", null, Collections.singletonList(workoutUnit));
                    repository.getExerciseSetsByWorkoutUnit(workoutUnit).observe(owner, (@Nullable List<ExerciseSetEntity> exerciseSets) -> printExerciseSetsData("Last exercise sets:", null, exerciseSets));
                }
            });
        }
        else if (MainActivity.DEBUG_LOG_MODE == 2)  // All workout units
        {
            repository.getAllWorkoutUnits().observe(owner, (@Nullable List<WorkoutUnitEntity> workoutUnits) -> printWorkoutUnitsData("All workout units:", null, workoutUnits));
        }
    }

    public void removeDebugWorkoutUnits()
    {
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            List<WorkoutUnitEntity> workoutUnits = repository.getAllWorkoutUnits().getValue();
            if (workoutUnits != null && workoutUnits.size() > 1)
            {
                workoutUnits.remove(0);
                repository.deleteWorkoutUnits(workoutUnits);
            }
        }
    }
}