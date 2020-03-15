package com.romanbrunner.apps.fitnesstracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.romanbrunner.apps.fitnesstracker.database.AppDatabase;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoDao;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutInfoDao;
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

    private void storeWorkoutInfo(final List<WorkoutInfoEntity> workoutInfoList)
    {
        // Insert or update given entries:
        executor.execute(() ->
        {
            WorkoutInfoDao workoutInfoDao = database.workoutInfoDao();
            for (WorkoutInfoEntity workoutInfo: workoutInfoList)
            {
                if (workoutInfoDao.insertIgnore(workoutInfo) == -1L)
                {
                    workoutInfoDao.update(workoutInfo);
                }
            }

        });
    }

    private void storeExerciseInfo(final List<ExerciseInfoEntity> exerciseInfoList)
    {
        // Insert or update given entries:
        executor.execute(() ->
        {
            ExerciseInfoDao exerciseInfoDao = database.exerciseInfoDao();
            for (ExerciseInfoEntity exerciseInfo: exerciseInfoList)
            {
                if (exerciseInfoDao.insertIgnore(exerciseInfo) == -1L)
                {
                    exerciseInfoDao.update(exerciseInfo);
                }
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

    public void setCurrentWorkout(WorkoutUnitEntity workoutUnitEntity, List<ExerciseSetEntity> exerciseSetEntities)
    {
        observableWorkoutUnit.setValue(workoutUnitEntity);
        observableExerciseSets.setValue(exerciseSetEntities);
    }

    public void saveCurrentData()
    {
        // Store workout info:
        List<WorkoutInfoEntity> workoutInfoList = observableWorkoutInfo.getValue();
        if (workoutInfoList != null)
        {
            storeWorkoutInfo(workoutInfoList);
        }
        else
        {
            java.lang.System.out.println("ERROR: Could not retrieve value from observableWorkoutInfo");
        }
        // Store exercise info:
        List<ExerciseInfoEntity> exerciseInfoList = observableExerciseInfo.getValue();
        if (exerciseInfoList != null)
        {
            storeExerciseInfo(exerciseInfoList);
        }
        else
        {
            java.lang.System.out.println("ERROR: Could not retrieve value from observableExerciseInfo");
        }
        // Update current workout unit:
        WorkoutUnitEntity currentWorkoutUnit = observableWorkoutUnit.getValue();
        if (currentWorkoutUnit != null)
        {
            executor.execute(() -> database.workoutUnitDao().update(currentWorkoutUnit));
        }
        else
        {
            java.lang.System.out.println("ERROR: Could not retrieve value from observableWorkoutUnit");
        }
        // Update current exercise sets:
        List<ExerciseSetEntity> currentExerciseSets = observableExerciseSets.getValue();
        if (currentExerciseSets != null)
        {
            executor.execute(() -> database.exerciseSetDao().update(currentExerciseSets));
        }
        else
        {
            java.lang.System.out.println("ERROR: Could not retrieve value from observableExerciseSets");
        }
    }

    public void finishExercises()
    {
        // Store info entries:
        List<WorkoutInfoEntity> workoutInfoList = observableWorkoutInfo.getValue();
        if (workoutInfoList != null) 
        {
            storeWorkoutInfo(workoutInfoList);
        }
        else
        {
            java.lang.System.out.println("ERROR: Could not retrieve value from observableWorkoutInfo");
        }
        List<ExerciseInfoEntity> exerciseInfoList = observableExerciseInfo.getValue();
        if (exerciseInfoList != null)
        {
            storeExerciseInfo(exerciseInfoList);
        }
        else
        {
            java.lang.System.out.println("ERROR: Could not retrieve value from observableExerciseInfo");
        }

        // Store and create new unit and set entries:
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
            else
            {
                java.lang.System.out.println("ERROR: Could not retrieve value from observableExerciseSets");
            }
        }
        else
        {
            java.lang.System.out.println("ERROR: Could not retrieve value from observableWorkoutUnit");
        }
    }
}