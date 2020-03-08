package com.romanbrunner.apps.fitnesstracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.database.AppDatabase;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutUnitEntity;
import com.romanbrunner.apps.fitnesstracker.ui.MainActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/* Repository handling the work with exercises. */
public class DataRepository
{
    // --------------------
    // Functional code
    // --------------------

    private static DataRepository instance;

    private final AppDatabase database;
    private final MediatorLiveData<List<WorkoutInfoEntity>> observableWorkoutInfo;  // TODO: maybe find another way to store this data
    private final MediatorLiveData<List<ExerciseInfoEntity>> observableExerciseInfo;  // TODO: maybe find another way to store this data
    private final MediatorLiveData<WorkoutUnitEntity> observableWorkoutUnit;
    private final MediatorLiveData<List<ExerciseSetEntity>> observableExerciseSets;
    private final Executor executor;

    private DataRepository(final AppDatabase database)
    {
        this.database = database;
        observableWorkoutInfo = new MediatorLiveData<>();
        observableExerciseInfo = new MediatorLiveData<>();
        observableWorkoutUnit = new MediatorLiveData<>();
        observableExerciseSets = new MediatorLiveData<>();
        executor = Executors.newSingleThreadExecutor();

        // Load all info entries as mediators as soon as the database is ready:
        observableWorkoutInfo.addSource(database.workoutInfoDao().loadAll(), workoutInfo ->
        {
            if (database.getDatabaseCreated().getValue() != null)
            {
                observableWorkoutInfo.postValue(workoutInfo);
            }
        });
        observableExerciseInfo.addSource(database.exerciseInfoDao().loadAll(), exerciseInfo ->
        {
            if (database.getDatabaseCreated().getValue() != null)
            {
                observableExerciseInfo.postValue(exerciseInfo);
            }
        });

        // Load newest workout unit with associated exercise sets as mediators as soon as the database is ready:
        LiveData<WorkoutUnitEntity> source = database.workoutUnitDao().loadNewest();
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            source = database.workoutUnitDao().loadNewestDebug();
        }
        observableWorkoutUnit.addSource(source, workoutUnit ->
        {
            if (database.getDatabaseCreated().getValue() != null)
            {
                workoutUnit.setDate(new Date());  // Set to current date
                observableWorkoutUnit.postValue(workoutUnit);
                observableExerciseSets.addSource(database.exerciseSetDao().loadByWorkoutUnitId(workoutUnit.getId()), observableExerciseSets::postValue);
            }
        });
    }

    static DataRepository getInstance(final AppDatabase database)
    {
        if (instance == null)
        {
            synchronized (DataRepository.class)
            {
                if (instance == null)
                {
                    instance = new DataRepository(database);
                }
            }
        }
        return instance;
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
        List<ExerciseInfoEntity> observedExerciseInfoList = observableExerciseInfo.getValue();
        for (ExerciseInfoEntity exerciseInfo : exerciseInfoList)
        {
            if (observedExerciseInfoList != null && observedExerciseInfoList.contains(exerciseInfo))
            {
                executor.execute(() -> database.exerciseInfoDao().update(exerciseInfoList));
            }
            else
            {
                executor.execute(() -> database.exerciseInfoDao().insert(exerciseInfoList));
            }
        }
    }

    public LiveData<WorkoutUnitEntity> getCurrentWorkoutUnit()
    {
        return observableWorkoutUnit;
    }

    public LiveData<WorkoutUnitEntity> getLastWorkoutUnit()
    {
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            return database.workoutUnitDao().loadLastDebug();
        }
        else
        {
            return database.workoutUnitDao().loadLast();
        }
    }

    public LiveData<List<WorkoutUnitEntity>> getAllWorkoutUnits()
    {
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            return database.workoutUnitDao().loadAllDebug();
        }
        else
        {
            return database.workoutUnitDao().loadAll();
        }
    }

    public void deleteWorkoutUnits(List<WorkoutUnitEntity> workoutUnits)
    {
        database.workoutUnitDao().delete(workoutUnits);
    }

    public LiveData<List<ExerciseSetEntity>> getCurrentExerciseSets()
    {
        return observableExerciseSets;
    }

    public LiveData<List<ExerciseSetEntity>> getAllExerciseSets()
    {
        return database.exerciseSetDao().loadAll();
    }

    public LiveData<List<ExerciseSetEntity>> getExerciseSetsByWorkoutUnit(WorkoutUnitEntity workoutUnit)
    {
        return database.exerciseSetDao().loadByWorkoutUnitId(workoutUnit.getId());
    }

    public void setExerciseSet(final ExerciseSetEntity exercise)
    {
        executor.execute(() -> database.exerciseSetDao().insertOrReplace(exercise));
    }

    public void saveCurrentData()
    {
        // Update workout info:
        List<WorkoutInfoEntity> workoutInfoList = observableWorkoutInfo.getValue();
        if (workoutInfoList != null)
        {
            executor.execute(() -> database.workoutInfoDao().update(workoutInfoList));
        }
        // Update exercise info:
        List<ExerciseInfoEntity> exerciseInfoList = observableExerciseInfo.getValue();
        if (exerciseInfoList != null)
        {
            executor.execute(() -> database.exerciseInfoDao().update(exerciseInfoList));
        }
        // Update current workout units:
        WorkoutUnitEntity currentWorkoutUnit = observableWorkoutUnit.getValue();
        if (currentWorkoutUnit != null)
        {
            executor.execute(() -> database.workoutUnitDao().update(currentWorkoutUnit));
        }
        // Update current exercise sets:
        List<ExerciseSetEntity> currentExerciseSets = observableExerciseSets.getValue();
        if (currentExerciseSets != null)
        {
            executor.execute(() -> database.exerciseSetDao().update(currentExerciseSets));
        }
    }

    public void finishExercises()
    {
        // Update current info entries:
        List<WorkoutInfoEntity> workoutInfoList = observableWorkoutInfo.getValue();
        if (workoutInfoList != null) 
        {
            // Update current entries:
            executor.execute(() -> database.workoutInfoDao().update(workoutInfoList));
        }
        List<ExerciseInfoEntity> exerciseInfoList = observableExerciseInfo.getValue();
        if (exerciseInfoList != null)
        {
            // Update current entries:
            executor.execute(() -> database.exerciseInfoDao().update(exerciseInfoList));
        }

        // Update and create new unit and sets:
        WorkoutUnitEntity oldWorkoutUnit = observableWorkoutUnit.getValue();
        if (oldWorkoutUnit != null)
        {
            // Update current entry:
            executor.execute(() -> database.workoutUnitDao().update(oldWorkoutUnit));
            // Clone and insert new entry:
            WorkoutUnitEntity newWorkoutUnit = new WorkoutUnitEntity(oldWorkoutUnit);
            final int newWorkoutId = newWorkoutUnit.getId();
            executor.execute(() -> database.workoutUnitDao().insert(newWorkoutUnit));
            // Adjust current entry:
            observableWorkoutUnit.setValue(newWorkoutUnit);

            List<ExerciseSetEntity> oldExerciseSets = observableExerciseSets.getValue();
            if (oldExerciseSets != null)
            {
                // Update current entries:
                executor.execute(() -> database.exerciseSetDao().update(oldExerciseSets));
                // Clone and insert new entries:
                List<ExerciseSetEntity> newExercises = new ArrayList<>(oldExerciseSets.size());
                for (ExerciseSetEntity exercise : oldExerciseSets)
                {
                    newExercises.add(new ExerciseSetEntity(exercise, newWorkoutId));
                }
                executor.execute(() -> database.exerciseSetDao().insert(newExercises));
                // Adjust current entries:
                observableExerciseSets.setValue(newExercises);
            }
        }
    }
}