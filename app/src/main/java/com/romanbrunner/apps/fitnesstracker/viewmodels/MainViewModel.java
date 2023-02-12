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

    public MainViewModel(Application application)
    {
        super(application);
        repository = ((BasicApp)application).getRepository();
        observableWorkoutUnit = new MediatorLiveData<>();

        // Set null by default until we get data from the database:
        observableWorkoutUnit.setValue(null);

        // Observe the changes from the database and forward them:
        observableWorkoutUnit.addSource(repository.getCurrentWorkoutUnit(), observableWorkoutUnit::postValue);
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
                Log.i("printWorkoutUnitsData", "WorkoutUnit -> Date: " + SimpleDateFormat.getDateTimeInstance().format(workout.getDate()) + ", ");
                Log.i("printWorkoutUnitsData", "Studio: " + workout.getStudio() + ", ");
                Log.i("printWorkoutUnitsData", "Name: " + workout.getName() + ", ");
                Log.i("printWorkoutUnitsData", "Description: " + workout.getDescription() + ", ");
                Log.i("printWorkoutUnitsData", "ExerciseInfoNames: " + workout.getExerciseNames() + "\n");
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
                Log.i("printExerciseSetsData", "WorkoutDate: " + SimpleDateFormat.getDateTimeInstance().format(exercise.getWorkoutUnitDate()) + ", ");
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

    public LiveData<List<ExerciseInfoEntity>> getExerciseInfo(List<ExerciseSetEntity> exerciseSetList)
    {
        final Set<String> exerciseInfoNames = new HashSet<>();
        for (ExerciseSetEntity exerciseSet: exerciseSetList)
        {
            exerciseInfoNames.add(exerciseSet.getExerciseInfoName());
        }
        return repository.getExerciseInfo(exerciseInfoNames);
    }

    public LiveData<WorkoutUnitEntity> getCurrentWorkoutUnit()
    {
        return observableWorkoutUnit;
    }

    public LiveData<WorkoutUnitEntity> getNewestWorkoutUnit()
    {
        return repository.getNewestWorkoutUnit();
    }
    public LiveData<WorkoutUnitEntity> getNewestWorkoutUnit(String studio)
    {
        return repository.getNewestWorkoutUnit(studio);
    }
    public LiveData<WorkoutUnitEntity> getNewestWorkoutUnit(String studio, String name)
    {
        return repository.getNewestWorkoutUnit(studio, name);
    }

    public LiveData<WorkoutUnitEntity> getLastWorkoutUnit()
    {
        return repository.getLastWorkoutUnit();
    }

    public LiveData<List<WorkoutUnitEntity>> getAllWorkoutUnits()
    {
        return repository.getAllWorkoutUnits();
    }
    public LiveData<List<WorkoutUnitEntity>> getAllWorkoutUnits(String studio)
    {
        return repository.getAllWorkoutUnits(studio);
    }
    public LiveData<List<WorkoutUnitEntity>> getAllWorkoutUnits(String studio, String name)
    {
        return repository.getAllWorkoutUnits(studio, name);
    }

    public LiveData<List<ExerciseSetEntity>> getExerciseSets(WorkoutUnitEntity workoutUnit)
    {
        return repository.getExerciseSets(workoutUnit);
    }

    public void storeExerciseInfo(final List<ExerciseInfoEntity> exerciseInfo)
    {
        repository.storeExerciseInfo(exerciseInfo);
    }

    public void finishWorkout(@NonNull WorkoutUnitEntity oldWorkoutUnit, @NonNull List<ExerciseSetEntity> oldExerciseSets)
    {
        repository.finishWorkout(oldWorkoutUnit, oldExerciseSets);
    }

    public LiveData<WorkoutUnitEntity> changeWorkout(@NonNull WorkoutUnitEntity baseWorkoutUnit)
    {
        return repository.changeWorkout(baseWorkoutUnit);
    }

    public void updateExerciseInfo(final List<ExerciseInfoEntity> exerciseInfo, final List<ExerciseSetEntity> orderedExerciseSets)
    {
        for (ExerciseInfoEntity exerciseInfoEntity : exerciseInfo)
        {
            exerciseInfoEntity.setDefaultValues(ExerciseInfoEntity.exerciseSets2defaultValues(exerciseInfoEntity.getName(), orderedExerciseSets));
        }
    }

    public void printDebugLog()
    {
        Log.i("printDebugLog", "--- DEBUG LOG (" + MainActivity.debugLogMode + ") ---");
        if (MainActivity.debugLogMode == 0)  // Observed workout units and exercise sets
        {
            DataRepository.executeOnceForLiveData(observableWorkoutUnit, workoutUnit ->
            {
                if (workoutUnit == null) throw new AssertionError("object cannot be null");
                printWorkoutUnitsData("Observed workout unit:", "No workout unit observed", Collections.singletonList(workoutUnit));
                DataRepository.executeOnceForLiveData(repository.getExerciseSets(workoutUnit), exerciseSets -> printExerciseSetsData("Observed exercise sets:", "No exercise sets observed", exerciseSets));
            });
        }
        else if (MainActivity.debugLogMode == 1)  // Stored workout units and exercise sets
        {
            DataRepository.executeOnceForLiveData(repository.getAllWorkoutUnits(), workoutUnits -> printWorkoutUnitsData("Stored workout units:", null, workoutUnits));
            DataRepository.executeOnceForLiveData(repository.getAllExerciseSets(), exerciseSets -> printExerciseSetsData("Stored exercise sets:", null, exerciseSets));
        }
        else if (MainActivity.debugLogMode == 2)  // Last stored workout unit and exercise sets
        {
            DataRepository.executeOnceForLiveData(repository.getLastWorkoutUnit(), workoutUnit ->
            {
                if (workoutUnit == null) throw new AssertionError("object cannot be null");
                printWorkoutUnitsData("Last stored workout unit:", null, Collections.singletonList(workoutUnit));
                DataRepository.executeOnceForLiveData(repository.getExerciseSets(workoutUnit), exerciseSets -> printExerciseSetsData("Last stored exercise sets:", null, exerciseSets));
            });
        }
        else if (MainActivity.debugLogMode == 3)  // Observed and stored workout units and exercise sets
        {
            DataRepository.executeOnceForLiveData(observableWorkoutUnit, workoutUnit ->
            {
                if (workoutUnit == null) throw new AssertionError("object cannot be null");
                printWorkoutUnitsData("Observed workout unit:", "No workout unit observed", Collections.singletonList(workoutUnit));
                DataRepository.executeOnceForLiveData(repository.getExerciseSets(workoutUnit), exerciseSets -> printExerciseSetsData("Observed exercise sets:", "No exercise sets observed", exerciseSets));
            });
            DataRepository.executeOnceForLiveData(repository.getAllWorkoutUnits(), workoutUnits -> printWorkoutUnitsData("Stored workout units:", null, workoutUnits));
            DataRepository.executeOnceForLiveData(repository.getAllExerciseSets(), exerciseSets -> printExerciseSetsData("Stored exercise sets:", null, exerciseSets));
        }
        else if (MainActivity.debugLogMode == 4)  // Current exercise info
        {
            DataRepository.executeOnceForLiveData(observableWorkoutUnit, workoutUnit ->
            {
                if (workoutUnit == null) throw new AssertionError("object cannot be null");
                DataRepository.executeOnceForLiveData(repository.getExerciseSets(workoutUnit), exerciseSetList ->
                {
                    if (exerciseSetList == null) throw new AssertionError("object cannot be null");
                    Set<String> exerciseInfoNames = new HashSet<>();
                    for (ExerciseSetEntity exerciseSet: exerciseSetList)
                    {
                        exerciseInfoNames.add(exerciseSet.getExerciseInfoName());
                    }
                    DataRepository.executeOnceForLiveData(repository.getExerciseInfo(exerciseInfoNames), exerciseInfo -> printExerciseInfoData("Current exercise info:", "No current exercise info", exerciseInfo));
                });
            });
        }
        else if (MainActivity.debugLogMode == 5)  // Observed workout unit
        {
            DataRepository.executeOnceForLiveData(observableWorkoutUnit, workoutUnit -> printWorkoutUnitsData("Observed workout unit:", "No workout unit observed", Collections.singletonList(workoutUnit)));
        }
        else if (MainActivity.debugLogMode == 6)  // Stored workout units
        {
            DataRepository.executeOnceForLiveData(repository.getAllWorkoutUnits(), workoutUnits -> printWorkoutUnitsData("Stored workout units:", null, workoutUnits));
        }
        else if (MainActivity.debugLogMode == 7)  // Observed and stored workout units
        {
            DataRepository.executeOnceForLiveData(observableWorkoutUnit, workoutUnit -> printWorkoutUnitsData("Observed workout unit:", "No workout unit observed", Collections.singletonList(workoutUnit)));
            DataRepository.executeOnceForLiveData(repository.getAllWorkoutUnits(), workoutUnits -> printWorkoutUnitsData("Stored workout units:", null, workoutUnits));
        }
    }

    public void removeAllWorkoutUnits()
    {
        DataRepository.executeOnceForLiveData(repository.getAllWorkoutUnits(), workoutUnits ->
        {
            if (workoutUnits != null && workoutUnits.size() > 1)
            {
                repository.setCurrentWorkout(workoutUnits.remove(0));
                repository.deleteWorkoutUnits(workoutUnits);
            }
        });
    }
}