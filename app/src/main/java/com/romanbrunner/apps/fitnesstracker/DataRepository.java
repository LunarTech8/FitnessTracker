package com.romanbrunner.apps.fitnesstracker;

import android.util.Log;
import android.util.Pair;

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
import com.romanbrunner.apps.fitnesstracker.ui.ExerciseSetAdapter;
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

    private void replaceCurrentWorkoutUnit(final WorkoutUnitEntity currentWorkoutUnit, final WorkoutUnitEntity newWorkoutUnit, final List<ExerciseSetEntity> newExercises)
    {
        // Safeguard against empty new entries:
        if (newWorkoutUnit == null || newExercises == null || newExercises.isEmpty())
        {
            Log.e("replaceCurrentWorkoutUnit", "Replacement aborted because one of the given new entries is null/empty");
            return;
        }

        // Delete current entries and insert new entries:
        executor.execute(() ->
        {
            database.workoutUnitDao().delete(currentWorkoutUnit);  // Associated exercise sets are automatically deleted
            database.workoutUnitDao().insert(newWorkoutUnit);
            database.exerciseSetDao().insert(newExercises);
        });
        // Adjust current workout unit:
        observableWorkoutUnit.setValue(newWorkoutUnit);
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

    public LiveData<WorkoutInfoEntity> getWorkoutInfo(String studio, String name, int version)
    {
        return database.workoutInfoDao().loadByStudioAndNameAndVersion(studio, name, version);
    }

    public LiveData<WorkoutInfoEntity> getNewestWorkoutInfo(String studio, String name)
    {
        return database.workoutInfoDao().loadNewestByStudioAndName(studio, name);
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
    private LiveData<WorkoutUnitEntity> getNewestWorkoutUnit(String workoutInfoStudio, String workoutInfoName, int workoutInfoVersion)
    {
        if (MainActivity.DEBUG_MODE_ACTIVE)
        {
            return database.workoutUnitDao().loadNewestByWorkoutInfoDebug(workoutInfoStudio, workoutInfoName, workoutInfoVersion);
        }
        else
        {
            return database.workoutUnitDao().loadNewestByWorkoutInfoNormal(workoutInfoStudio, workoutInfoName, workoutInfoVersion);
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

    public void deleteNewerWorkoutInfoVersions(String studio, String name, int version)
    {
        executor.execute(() -> database.workoutInfoDao().deleteNewerVersions(studio, name, version));
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
        WorkoutUnitEntity newWorkoutUnit = new WorkoutUnitEntity(oldWorkoutUnit, oldWorkoutUnit.getId() + 1);  // Increment Id by one
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

    public LiveData<WorkoutUnitEntity> changeWorkout(@NonNull WorkoutInfoEntity newWorkoutInfo)
    {
        Log.d("changeWorkout", "workout info studio: " + newWorkoutInfo.getStudio());  // DEBUG:
        Log.d("changeWorkout", "workout info name: " + newWorkoutInfo.getName());  // DEBUG:
        Log.d("changeWorkout", "workout info version: " + newWorkoutInfo.getVersion());  // DEBUG:
        DataRepository.executeOnceForLiveData(observableWorkoutUnit, currentWorkoutUnit ->
        {
            if (currentWorkoutUnit == null) throw new AssertionError("object cannot be null");
            final int workoutId = currentWorkoutUnit.getId();
            DataRepository.executeOnceForLiveData(getNewestWorkoutUnit(newWorkoutInfo.getStudio(), newWorkoutInfo.getName(), newWorkoutInfo.getVersion()), oldWorkoutUnit ->
            {
                final WorkoutUnitEntity newWorkoutUnit;
                final List<ExerciseSetEntity> newExercises = new ArrayList<>();
                if (oldWorkoutUnit == null)
                {
                    Log.d("changeWorkout", "default entries created");  // DEBUG:
                    // Create new entries by using default values:
                    newWorkoutUnit = new WorkoutUnitEntity(workoutId, newWorkoutInfo.getStudio(), newWorkoutInfo.getName(), newWorkoutInfo.getVersion());
                    DataRepository.executeOnceForLiveData(getExerciseInfo(WorkoutInfoEntity.exerciseNames2NameSet(newWorkoutInfo.getExerciseNames())), exerciseInfoList ->
                    {
                        if (exerciseInfoList == null) throw new AssertionError("object cannot be null");
                        for (ExerciseInfoEntity exerciseInfo : exerciseInfoList)
                        {
                            String exerciseInfoName = exerciseInfo.getName();
                            String defaultValues = exerciseInfo.getDefaultValues();
                            if (defaultValues.isEmpty())
                            {
                                Log.w("changeWorkout", "No default values for " + exerciseInfoName + " in exerciseInfo, using min values instead");
                                newExercises.add(new ExerciseSetEntity(workoutId, exerciseInfoName, ExerciseSetAdapter.WEIGHTED_EXERCISE_REPEATS_MIN, 0F));
                            }
                            else
                            {
                                for (Pair<Integer, Float> dataList: ExerciseInfoEntity.defaultValues2DataList(defaultValues))
                                {
                                    newExercises.add(new ExerciseSetEntity(workoutId, exerciseInfoName, dataList.first, dataList.second));
                                }
                            }
                        }
                        replaceCurrentWorkoutUnit(currentWorkoutUnit, newWorkoutUnit, newExercises);
                    });
                }
                else
                {
                    // Create new entries by cloning last entries:
                    newWorkoutUnit = new WorkoutUnitEntity(oldWorkoutUnit, workoutId);
                    DataRepository.executeOnceForLiveData(getExerciseSets(oldWorkoutUnit), oldExerciseSets ->
                    {
                        if (oldExerciseSets == null) throw new AssertionError("object cannot be null");
                        for (ExerciseSetEntity exercise : oldExerciseSets)
                        {
                            newExercises.add(new ExerciseSetEntity(exercise, workoutId));
                        }
                        replaceCurrentWorkoutUnit(currentWorkoutUnit, newWorkoutUnit, newExercises);
                    });
                }
            });
        });
        return observableWorkoutUnit;
    }
}