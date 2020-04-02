package com.romanbrunner.apps.fitnesstracker.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.BasicApp;
import com.romanbrunner.apps.fitnesstracker.DataRepository;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutUnitEntity;
import com.romanbrunner.apps.fitnesstracker.ui.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainViewModel extends AndroidViewModel
{
    // --------------------
    // Functional code
    // --------------------

    private final DataRepository repository;
    private final MediatorLiveData<WorkoutUnitEntity> observableWorkoutUnit;
    private final MediatorLiveData<List<ExerciseSetEntity>> observableExerciseSets;

    public MainViewModel(Application application)
    {
        super(application);
        repository = ((BasicApp)application).getRepository();
        observableWorkoutUnit = new MediatorLiveData<>();
        observableExerciseSets = new MediatorLiveData<>();
        // Set null by default until we get data from the database:
        observableWorkoutUnit.setValue(null);
        observableExerciseSets.setValue(null);

        // Observe the changes from the database and forward them:
        observableWorkoutUnit.addSource(repository.getCurrentWorkoutUnit(), observableWorkoutUnit::postValue);
        observableExerciseSets.addSource(repository.getCurrentExerciseSets(), observableExerciseSets::postValue);
    }

    private void printWorkoutInfoData(@NonNull String headerMessage, @Nullable String nullMessage, List<WorkoutInfoEntity> workoutInfoList)
    {
        if (workoutInfoList != null)
        {
            java.lang.System.out.println(headerMessage);
            for (WorkoutInfoEntity workout : workoutInfoList)
            {

                java.lang.System.out.print("WorkoutInfo -> Name: " + workout.getName() + ", ");
                java.lang.System.out.print("Version: " + workout.getVersion() + ", ");
                java.lang.System.out.print("Description: " + workout.getDescription() + ", ");
                java.lang.System.out.print("ExerciseInfoNames: " + workout.getExerciseInfoNames() + "\n");
            }
        }
        else if (nullMessage != null)
        {
            java.lang.System.out.println(nullMessage);
        }
    }

    private void printExerciseInfoData(@NonNull String headerMessage, @Nullable String nullMessage, List<ExerciseInfoEntity> exerciseInfoList)
    {
        if (exerciseInfoList != null)
        {
            java.lang.System.out.println(headerMessage);
            for (ExerciseInfoEntity exercise : exerciseInfoList)
            {

                java.lang.System.out.print("ExerciseInfo -> Name: " + exercise.getName() + ", ");
                java.lang.System.out.print("Token: " + exercise.getToken() + ", ");
                java.lang.System.out.print("Remarks: " + exercise.getRemarks() + "\n");
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
                java.lang.System.out.print("WorkoutUnit -> Id: " + workout.getId() + ", ");
                java.lang.System.out.print("WorkoutInfoName: " + workout.getWorkoutInfoName() + ", ");
                java.lang.System.out.print("WorkoutInfoVersion: " + workout.getWorkoutInfoVersion() + ", ");
                java.lang.System.out.print("Date: " + SimpleDateFormat.getDateTimeInstance().format(workout.getDate()) + "\n");
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

                java.lang.System.out.print("ExerciseSet -> Id: " + exercise.getId() + ", ");
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

    public LiveData<WorkoutInfoEntity> getWorkoutInfo(String name, int version)
    {
        return repository.getWorkoutInfo(name, version);
    }

    public LiveData<List<ExerciseInfoEntity>> getExerciseInfo(Set<String> names)
    {
        return repository.getExerciseInfo(names);
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

    public void saveCurrentData()  // UNUSED:
    {
        repository.saveCurrentData();
    }

    public void finishWorkout()
    {
        repository.finishWorkout();
    }

    public void printDebugLog()
    {
        java.lang.System.out.println("--- DEBUG LOG ---");
        if (MainActivity.DEBUG_LOG_MODE == 0)  // Observed workout units and exercise sets
        {
            DataRepository.executeOnceForLiveData(observableWorkoutUnit, workoutUnit -> printWorkoutUnitsData("Observed workout unit:", "No workout unit observed", Collections.singletonList(workoutUnit)));
            DataRepository.executeOnceForLiveData(observableExerciseSets, exerciseSets -> printExerciseSetsData("Observed exercise sets:", "No exercise sets observed", exerciseSets));
        }
        else if (MainActivity.DEBUG_LOG_MODE == 1)  // Stored workout units and exercise sets
        {
            DataRepository.executeOnceForLiveData(repository.getAllWorkoutUnits(), workoutUnits -> printWorkoutUnitsData("Stored workout units:", null, workoutUnits));
            DataRepository.executeOnceForLiveData(repository.getAllExerciseSets(), exerciseSets -> printExerciseSetsData("Stored exercise sets:", null, exerciseSets));
        }
        else if (MainActivity.DEBUG_LOG_MODE == 2)  // Last stored workout unit and exercise sets
        {
            DataRepository.executeOnceForLiveData(repository.getLastWorkoutUnit(), workoutUnit ->
            {
                if (workoutUnit != null)
                {
                    printWorkoutUnitsData("Last stored workout unit:", null, Collections.singletonList(workoutUnit));
                    DataRepository.executeOnceForLiveData(repository.getExerciseSetsByWorkoutUnit(workoutUnit), exerciseSets -> printExerciseSetsData("Last stored exercise sets:", null, exerciseSets));
                }
                else
                {
                    java.lang.System.out.println("ERROR: Could not retrieve value from getLastWorkoutUnit");
                }
            });
        }
        else if (MainActivity.DEBUG_LOG_MODE == 3)  // Stored workout units
        {
            DataRepository.executeOnceForLiveData(repository.getAllWorkoutUnits(), workoutUnits -> printWorkoutUnitsData("Stored workout units:", null, workoutUnits));
        }
        else if (MainActivity.DEBUG_LOG_MODE == 4)  // Current workout info and exercise info
        {
            DataRepository.executeOnceForLiveData(observableWorkoutUnit, workoutUnitEntity ->
            {
                if (workoutUnitEntity != null)
                {
                    DataRepository.executeOnceForLiveData(repository.getWorkoutInfo(workoutUnitEntity.getWorkoutInfoName(), workoutUnitEntity.getWorkoutInfoVersion()), workoutInfo -> printWorkoutInfoData("Current workout info:", "No current workout info", Collections.singletonList(workoutInfo)));
                }
                else
                {
                    java.lang.System.out.println("ERROR: Could not retrieve value from observableWorkoutUnit");
                }
            });
            DataRepository.executeOnceForLiveData(observableExerciseSets, exerciseSetList ->
            {
                if (exerciseSetList != null)
                {
                    Set<String> exerciseInfoNames = new HashSet<>();
                    for (ExerciseSetEntity exerciseSet: exerciseSetList)
                    {
                        exerciseInfoNames.add(exerciseSet.getExerciseInfoName());
                    }
                    DataRepository.executeOnceForLiveData(repository.getExerciseInfo(exerciseInfoNames), exerciseInfo -> printExerciseInfoData("Current exercise info:", "No current exercise info", exerciseInfo));
                }
                else
                {
                    java.lang.System.out.println("ERROR: Could not retrieve value from observableExerciseSets");
                }
            });
        }
        else if (MainActivity.DEBUG_LOG_MODE == 5)  // Observed and stored workout units and exercise sets
        {
            DataRepository.executeOnceForLiveData(observableWorkoutUnit, workoutUnit -> printWorkoutUnitsData("Observed workout unit:", "No workout unit observed", Collections.singletonList(workoutUnit)));
            DataRepository.executeOnceForLiveData(observableExerciseSets, exerciseSets -> printExerciseSetsData("Observed exercise sets:", "No exercise sets observed", exerciseSets));
            DataRepository.executeOnceForLiveData(repository.getAllWorkoutUnits(), workoutUnits -> printWorkoutUnitsData("Stored workout units:", null, workoutUnits));
            DataRepository.executeOnceForLiveData(repository.getAllExerciseSets(), exerciseSets -> printExerciseSetsData("Stored exercise sets:", null, exerciseSets));
        }
    }

    public void removeDebugWorkoutUnits()
    {
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            DataRepository.executeOnceForLiveData(repository.getAllWorkoutUnits(), workoutUnits ->
            {
                if (workoutUnits != null && workoutUnits.size() > 1)
                {
                    WorkoutUnitEntity firstWorkoutUnit = workoutUnits.remove(0);
                    DataRepository.executeOnceForLiveData(repository.getExerciseSetsByWorkoutUnit(firstWorkoutUnit), exerciseSets -> repository.setCurrentWorkout(firstWorkoutUnit, exerciseSets));
                    repository.deleteWorkoutUnits(workoutUnits);
                }
            });
        }
    }
}