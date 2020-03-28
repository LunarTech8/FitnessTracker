package com.romanbrunner.apps.fitnesstracker.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.romanbrunner.apps.fitnesstracker.BasicApp;
import com.romanbrunner.apps.fitnesstracker.DataRepository;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
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

    public interface CallbackSimple<T>
    {
        void execute(@Nullable T object);
    }

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

    private <T> void executeOnceOnChanged(@NonNull LifecycleOwner owner, LiveData<T> liveData, CallbackSimple<T> callback)
    {
        liveData.observe(owner, new Observer<T>()
        {
            @Override
            public void onChanged(@Nullable T object)
            {
                callback.execute(object);
                liveData.removeObserver(this);
            }
        });
    }

    public LiveData<List<WorkoutInfoEntity>> getWorkoutInfo()
    {
        return observableWorkoutInfo;
    }

    public LiveData<List<ExerciseInfoEntity>> getExerciseInfo()
    {
        return observableExerciseInfo;
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

    public void finishExercises()
    {
        repository.finishExercises();
    }

    public void printDebugLog(@NonNull LifecycleOwner owner)
    {
        java.lang.System.out.println("--- DEBUG LOG ---");
        if (MainActivity.DEBUG_LOG_MODE == 0)  // Observed workout units and exercise sets
        {
            printWorkoutUnitsData("Observed workout unit:", "No workout unit observed", Collections.singletonList(repository.getCurrentWorkoutUnit().getValue()));
            printExerciseSetsData("Observed exercise sets:", "No exercise sets observed", repository.getCurrentExerciseSets().getValue());
        }
        else if (MainActivity.DEBUG_LOG_MODE == 1)  // Stored workout units and exercise sets
        {
            executeOnceOnChanged(owner, repository.getAllWorkoutUnits(), workoutUnits -> printWorkoutUnitsData("Stored workout units:", null, workoutUnits));
            executeOnceOnChanged(owner, repository.getAllExerciseSets(), exerciseSets -> printExerciseSetsData("Stored exercise sets (normal and debug):", null, exerciseSets));
        }
        else if (MainActivity.DEBUG_LOG_MODE == 2)  // Last stored workout unit and exercise sets
        {
            executeOnceOnChanged(owner, repository.getLastWorkoutUnit(), workoutUnit ->
            {
                if (workoutUnit != null)
                {
                    printWorkoutUnitsData("Last stored workout unit:", null, Collections.singletonList(workoutUnit));
                    executeOnceOnChanged(owner, repository.getExerciseSetsByWorkoutUnit(workoutUnit), exerciseSets -> printExerciseSetsData("Last stored exercise sets:", null, exerciseSets));
                }
            });
        }
        else if (MainActivity.DEBUG_LOG_MODE == 3)  // Stored workout units
        {
            executeOnceOnChanged(owner, repository.getAllWorkoutUnits(), workoutUnits -> printWorkoutUnitsData("Stored workout units:", null, workoutUnits));
        }
        else if (MainActivity.DEBUG_LOG_MODE == 4)  // Observed workout info and exercise info
        {
            printWorkoutInfoData("Observed workout info:", "No workout info observed", repository.getWorkoutInfo().getValue());
            printExerciseInfoData("Observed exercise info:", "No exercise info observed", repository.getExerciseInfo().getValue());
        }
    }

    public void removeDebugWorkoutUnits(@NonNull LifecycleOwner owner)
    {
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            executeOnceOnChanged(owner, repository.getAllWorkoutUnits(), workoutUnits ->
            {
                if (workoutUnits != null && workoutUnits.size() > 1)
                {
                    WorkoutUnitEntity firstWorkoutUnit = workoutUnits.remove(0);
                    executeOnceOnChanged(owner, repository.getExerciseSetsByWorkoutUnit(firstWorkoutUnit), exerciseSets -> repository.setCurrentWorkout(firstWorkoutUnit, exerciseSets));
                    repository.deleteWorkoutUnits(workoutUnits);
                }
            });
        }
    }
}