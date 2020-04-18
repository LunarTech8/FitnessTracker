package com.romanbrunner.apps.fitnesstracker.viewmodels;

import android.app.Application;
import android.util.Log;

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
            Log.i("printWorkoutInfoData", headerMessage);
            for (WorkoutInfoEntity workout : workoutInfoList)
            {

                Log.i("printWorkoutInfoData", "WorkoutInfo -> Name: " + workout.getName() + ", ");
                Log.i("printWorkoutInfoData", "Version: " + workout.getVersion() + ", ");
                Log.i("printWorkoutInfoData", "Description: " + workout.getDescription() + ", ");
                Log.i("printWorkoutInfoData", "ExerciseInfoNames: " + workout.getExerciseInfoNames() + "\n");
            }
        }
        else if (nullMessage != null)
        {
            Log.i("printWorkoutInfoData", nullMessage);
        }
    }

    private void printExerciseInfoData(@NonNull String headerMessage, @Nullable String nullMessage, List<ExerciseInfoEntity> exerciseInfoList)
    {
        if (exerciseInfoList != null)
        {
            Log.i("printExerciseInfoData", headerMessage);
            for (ExerciseInfoEntity exercise : exerciseInfoList)
            {

                Log.i("printExerciseInfoData", "ExerciseInfo -> Name: " + exercise.getName() + ", ");
                Log.i("printExerciseInfoData", "Token: " + exercise.getToken() + ", ");
                Log.i("printExerciseInfoData", "Remarks: " + exercise.getRemarks() + "\n");
            }
        }
        else if (nullMessage != null)
        {
            Log.i("printExerciseInfoData", nullMessage);
        }
    }

    private void printWorkoutUnitsData(@NonNull String headerMessage, @Nullable String nullMessage, List<WorkoutUnitEntity> workoutUnits)
    {
        if (workoutUnits != null)
        {
            Log.i("printWorkoutUnitsData", headerMessage);
            for (WorkoutUnitEntity workout : workoutUnits)
            {
                Log.i("printWorkoutUnitsData", "WorkoutUnit -> Id: " + workout.getId() + ", ");
                Log.i("printWorkoutUnitsData", "WorkoutInfoName: " + workout.getWorkoutInfoName() + ", ");
                Log.i("printWorkoutUnitsData", "WorkoutInfoVersion: " + workout.getWorkoutInfoVersion() + ", ");
                Log.i("printWorkoutUnitsData", "Date: " + SimpleDateFormat.getDateTimeInstance().format(workout.getDate()) + "\n");
            }
        }
        else if (nullMessage != null)
        {
            Log.i("printWorkoutUnitsData", nullMessage);
        }
    }

    private void printExerciseSetsData(@NonNull String headerMessage, @Nullable String nullMessage, List<ExerciseSetEntity> exerciseSets)
    {
        if (exerciseSets != null)
        {
            Log.i("printExerciseSetsData", headerMessage);
            for (ExerciseSetEntity exercise : exerciseSets)
            {

                Log.i("printExerciseSetsData", "ExerciseSet -> Id: " + exercise.getId() + ", ");
                Log.i("printExerciseSetsData", "WorkoutId: " + exercise.getWorkoutUnitId() + ", ");
                Log.i("printExerciseSetsData", "ExerciseInfoName: " + exercise.getExerciseInfoName() + ", ");
                Log.i("printExerciseSetsData", "Repeats: " + exercise.getRepeats() + ", ");
                Log.i("printExerciseSetsData", "Weight: " + exercise.getWeight() + ", ");
                Log.i("printExerciseSetsData", "Done: " + exercise.isDone() + "\n");
            }
        }
        else if (nullMessage != null)
        {
            Log.i("printExerciseSetsData", nullMessage);
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

    public void storeWorkoutInfo(final List<WorkoutInfoEntity> workoutInfoList)
    {
        repository.storeWorkoutInfo(workoutInfoList);
    }

    public void storeExerciseInfo(final List<ExerciseInfoEntity> exerciseInfo)
    {
        repository.storeExerciseInfo(exerciseInfo);
    }

    public void finishWorkout()
    {
        repository.finishWorkout();
    }

    public void printDebugLog()
    {
        Log.i("printDebugLog", "--- DEBUG LOG ---");
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
                assert workoutUnit != null;
                printWorkoutUnitsData("Last stored workout unit:", null, Collections.singletonList(workoutUnit));
                DataRepository.executeOnceForLiveData(repository.getExerciseSetsByWorkoutUnit(workoutUnit), exerciseSets -> printExerciseSetsData("Last stored exercise sets:", null, exerciseSets));
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
                assert workoutUnitEntity != null;
                DataRepository.executeOnceForLiveData(repository.getWorkoutInfo(workoutUnitEntity.getWorkoutInfoName(), workoutUnitEntity.getWorkoutInfoVersion()), workoutInfo -> printWorkoutInfoData("Current workout info:", "No current workout info", Collections.singletonList(workoutInfo)));
            });
            DataRepository.executeOnceForLiveData(observableExerciseSets, exerciseSetList ->
            {
                assert exerciseSetList != null;
                Set<String> exerciseInfoNames = new HashSet<>();
                for (ExerciseSetEntity exerciseSet: exerciseSetList)
                {
                    exerciseInfoNames.add(exerciseSet.getExerciseInfoName());
                }
                DataRepository.executeOnceForLiveData(repository.getExerciseInfo(exerciseInfoNames), exerciseInfo -> printExerciseInfoData("Current exercise info:", "No current exercise info", exerciseInfo));
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