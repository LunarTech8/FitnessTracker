package com.romanbrunner.apps.fitnesstracker;

import androidx.annotation.NonNull;
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
import java.util.Objects;
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
    private final Executor executor;

    public interface CallbackAction<T>
    {
        void execute(@Nullable T object);
    }

    public interface CallbackCondition<T>
    {
        boolean check(@Nullable T object);
    }

    private DataRepository(final AppDatabase database)
    {
        this.database = database;
        observableWorkoutUnit = new MutableLiveData<>();
        executor = Executors.newSingleThreadExecutor();

        // Load newest workout unit and post its value into its observable as soon as the database is ready:
        executeOnceForLiveData(getNewestWorkoutUnit(), workoutUnit -> database.getDatabaseCreated().getValue() != null, workoutUnit ->
        {
            if (workoutUnit == null) throw new AssertionError("object cannot be null");
            workoutUnit.setDate(new Date());  // Set to current date
            observableWorkoutUnit.postValue(workoutUnit);
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

    public static <T> void executeOnceForLiveData(LiveData<T> liveData, CallbackCondition<T> condition, CallbackAction<T> action)
    {
        liveData.observeForever(new Observer<T>()
        {
            @Override
            /* Is called once directly after observer is added if liveData is not empty. */
            public void onChanged(@Nullable T object)
            {
                if (condition.check(object))
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

    public LiveData<WorkoutInfoEntity> getWorkoutInfo(String name, int version)
    {
        return database.workoutInfoDao().loadByNameAndVersion(name, version);
    }

    public LiveData<WorkoutInfoEntity> getNewestWorkoutInfo(String name)
    {
        return database.workoutInfoDao().loadNewestByName(name);
    }

    public LiveData<List<WorkoutInfoEntity>> getAllWorkoutInfo()
    {
        return database.workoutInfoDao().loadAll();
    }

    public LiveData<List<ExerciseInfoEntity>> getExerciseInfo(Set<String> names)
    {
        return database.exerciseInfoDao().loadByNames(names);
    }

    public LiveData<WorkoutUnitEntity> getCurrentWorkoutUnit()
    {
        return observableWorkoutUnit;
    }

    private LiveData<WorkoutUnitEntity> getNewestWorkoutUnit()
    {
        if (MainActivity.DEBUG_MODE_ACTIVE)
        {
            return database.workoutUnitDao().loadNewestDebug();
        }
        else
        {
            return database.workoutUnitDao().loadNewestNormal();
        }
    }

    public LiveData<WorkoutUnitEntity> getLastWorkoutUnit()
    {
        if (MainActivity.DEBUG_MODE_ACTIVE)
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
        if (MainActivity.DEBUG_MODE_ACTIVE)
        {
            return database.workoutUnitDao().loadAllDebug();
        }
        else
        {
            return database.workoutUnitDao().loadAllNormal();
        }
    }

    public LiveData<List<ExerciseSetEntity>> getExerciseSets(WorkoutUnitEntity workoutUnit)
    {
        return database.exerciseSetDao().loadByWorkoutUnitId(workoutUnit.getId());
    }

    public LiveData<List<ExerciseSetEntity>> getAllExerciseSets()
    {
        if (MainActivity.DEBUG_MODE_ACTIVE)
        {
            return database.exerciseSetDao().loadAllDebug();
        }
        else
        {
            return database.exerciseSetDao().loadAllNormal();
        }
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

    public void setCurrentWorkout(WorkoutUnitEntity workoutUnitEntity)
    {
        observableWorkoutUnit.setValue(workoutUnitEntity);
    }

    public void deleteNewerWorkoutInfoVersions(String name, int version)
    {
        executor.execute(() -> database.workoutInfoDao().deleteNewerVersions(name, version));
    }

    public void deleteWorkoutUnits(List<WorkoutUnitEntity> workoutUnits)
    {
        executor.execute(() -> database.workoutUnitDao().delete(workoutUnits));
    }

    public void finishWorkout(@NonNull WorkoutUnitEntity oldWorkoutUnit, @NonNull List<ExerciseSetEntity> oldExerciseSets)
    {
        // Update current entries:
        executor.execute(() ->
        {
            database.workoutUnitDao().update(oldWorkoutUnit);
            database.exerciseSetDao().update(oldExerciseSets);
        });
        // Clone new entries:
        WorkoutUnitEntity newWorkoutUnit = new WorkoutUnitEntity(oldWorkoutUnit);
        List<ExerciseSetEntity> newExercises = new ArrayList<>(oldExerciseSets.size());
        final int newWorkoutId = newWorkoutUnit.getId();
        for (ExerciseSetEntity exercise : oldExerciseSets)
        {
            newExercises.add(new ExerciseSetEntity(exercise, newWorkoutId));
        }
        // Insert new entries:
        executor.execute(() ->
        {
            database.workoutUnitDao().insert(newWorkoutUnit);
            database.exerciseSetDao().insert(newExercises);
        });
        // Adjust current workout unit:
        observableWorkoutUnit.setValue(newWorkoutUnit);
    }

    public WorkoutUnitEntity changeWorkout(@NonNull WorkoutInfoEntity newWorkoutInfo)
    {
        final int newWorkoutId = Objects.requireNonNull(observableWorkoutUnit.getValue()).getId() + 1;  // TODO: maybe don't use getValue()
        // Create new entries:
        WorkoutUnitEntity newWorkoutUnit = new WorkoutUnitEntity(newWorkoutId, newWorkoutInfo.getName(), newWorkoutInfo.getVersion());
        List<ExerciseSetEntity> newExercises = new ArrayList<>();
        for (String exerciseInfoName : WorkoutInfoEntity.exerciseInfoNames2Array(newWorkoutInfo.getExerciseInfoNames()))
        {
            AppDatabase.createDefaultExercise(newExercises, newWorkoutId, exerciseInfoName);
        }
        // FIXME: finish implementation
        // Insert new entries:
        executor.execute(() ->
        {
            database.workoutUnitDao().insert(newWorkoutUnit);
            database.exerciseSetDao().insert(newExercises);
        });
        // Adjust current workout unit:
        observableWorkoutUnit.setValue(newWorkoutUnit);
        return newWorkoutUnit;
    }
}