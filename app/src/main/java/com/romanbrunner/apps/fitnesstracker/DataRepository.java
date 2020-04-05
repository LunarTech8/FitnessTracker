package com.romanbrunner.apps.fitnesstracker;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.romanbrunner.apps.fitnesstracker.database.AppDatabase;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoDao;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutInfoDao;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutUnitEntity;
import com.romanbrunner.apps.fitnesstracker.ui.MainActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
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
    private final MutableLiveData<WorkoutUnitEntity> observableWorkoutUnit;
    private final MutableLiveData<List<ExerciseSetEntity>> observableExerciseSets;
    private final Executor executor;

    public interface CallbackAction<T>
    {
        void execute(@Nullable T object);
    }

    public interface CallbackCondition
    {
        boolean check();
    }

    private DataRepository(final AppDatabase database)
    {
        this.database = database;
        observableWorkoutUnit = new MutableLiveData<>();
        observableExerciseSets = new MutableLiveData<>();
        executor = Executors.newSingleThreadExecutor();

        // Load newest workout unit with associated exercise sets and post their values into observables as soon as the database is ready:
        executeOnceForLiveData(getNewestWorkoutUnit(), () -> database.getDatabaseCreated().getValue() != null, workoutUnit ->
        {
            assert workoutUnit != null;
            workoutUnit.setDate(new Date());  // Set to current date
            observableWorkoutUnit.postValue(workoutUnit);
            executeOnceForLiveData(database.exerciseSetDao().loadByWorkoutUnitId(workoutUnit.getId()), observableExerciseSets::postValue);
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

    private static <T> void executeOnceForLiveData(LiveData<T> liveData, CallbackCondition condition, CallbackAction<T> action)
    {
        liveData.observeForever(new Observer<T>()
        {
            @Override
            /* Is called once directly after observer is added if liveData is not empty. */
            public void onChanged(@Nullable T object)
            {
                if (condition.check())
                {
                    action.execute(object);
                    liveData.removeObserver(this);
                }
            }
        });
    }
    public static <T> void executeOnceForLiveData(LiveData<T> liveData, CallbackAction<T> action)
    {
        liveData.observeForever(new Observer<T>()
        {
            @Override
            /* Is called once directly after observer is added if liveData is not empty. */
            public void onChanged(@Nullable T object)
            {
                action.execute(object);
                liveData.removeObserver(this);
            }
        });
    }

    private LiveData<WorkoutUnitEntity> getNewestWorkoutUnit()
    {
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            return database.workoutUnitDao().loadNewestDebug();
        }
        else
        {
            return database.workoutUnitDao().loadNewestNormal();
        }
    }

    public LiveData<WorkoutInfoEntity> getWorkoutInfo(String name, int version)
    {
        return database.workoutInfoDao().loadByNameAndVersion(name, version);
    }

    public LiveData<List<ExerciseInfoEntity>> getExerciseInfo(Set<String> names)
    {
        return database.exerciseInfoDao().loadByNames(names);
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
            return database.workoutUnitDao().loadLastNormal();
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
            return database.workoutUnitDao().loadAllNormal();
        }
    }

    public LiveData<List<ExerciseSetEntity>> getCurrentExerciseSets()
    {
        return observableExerciseSets;
    }

    public LiveData<List<ExerciseSetEntity>> getAllExerciseSets()
    {
        if (MainActivity.TEST_MODE_ACTIVE)
        {
            return database.exerciseSetDao().loadAllDebug();
        }
        else
        {
            return database.exerciseSetDao().loadAllNormal();
        }
    }

    public LiveData<List<ExerciseSetEntity>> getExerciseSetsByWorkoutUnit(WorkoutUnitEntity workoutUnit)
    {
        return database.exerciseSetDao().loadByWorkoutUnitId(workoutUnit.getId());
    }

    public void storeWorkoutInfo(final List<WorkoutInfoEntity> workoutInfoList)
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

    public void storeExerciseInfo(final List<ExerciseInfoEntity> exerciseInfoList)
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

    public void setCurrentWorkout(WorkoutUnitEntity workoutUnitEntity, List<ExerciseSetEntity> exerciseSetEntities)
    {
        observableWorkoutUnit.setValue(workoutUnitEntity);
        observableExerciseSets.setValue(exerciseSetEntities);
    }

    public void deleteWorkoutUnits(List<WorkoutUnitEntity> workoutUnits)
    {
        executor.execute(() -> database.workoutUnitDao().delete(workoutUnits));
    }

    public void finishWorkout()
    {
        // Store and create new workout unit entries:
        WorkoutUnitEntity oldWorkoutUnit = observableWorkoutUnit.getValue();
        assert oldWorkoutUnit != null;
        // Update current entry:
        executor.execute(() -> database.workoutUnitDao().update(oldWorkoutUnit));
        // Clone and insert new entry:
        WorkoutUnitEntity newWorkoutUnit = new WorkoutUnitEntity(oldWorkoutUnit);
        final int newWorkoutId = newWorkoutUnit.getId();
        executor.execute(() -> database.workoutUnitDao().insert(newWorkoutUnit));
        // Adjust current entry:
        observableWorkoutUnit.setValue(newWorkoutUnit);

        // Store and create new exercise set entries:
        List<ExerciseSetEntity> oldExerciseSets = observableExerciseSets.getValue();
        assert oldExerciseSets != null;
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