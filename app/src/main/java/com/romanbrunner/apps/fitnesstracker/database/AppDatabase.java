package com.romanbrunner.apps.fitnesstracker.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.romanbrunner.apps.fitnesstracker.AppExecutors;
import com.romanbrunner.apps.fitnesstracker.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Database(entities = {WorkoutUnitEntity.class, WorkoutInfoEntity.class, ExerciseSetEntity.class, ExerciseInfoEntity.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase
{
    // --------------------
    // Data code
    // --------------------

    private final static String DATABASE_NAME = "fitness-tracker-database";

    private static WorkoutInfoEntity initializeWorkoutInfo()
    {
        return new WorkoutInfoEntity("HIT full-body", "High intensity training full-body");  // "Default" workout plan
    }

    private static List<ExerciseInfoEntity> initializeExerciseInfo(final WorkoutInfoEntity workoutInfo)
    {
        List<ExerciseInfoEntity> exerciseInfoList = new ArrayList<>(12);
        final String workoutInfoName = workoutInfo.getName();
        if (Objects.equals(workoutInfoName, "HIT full-body"))
        {
            exerciseInfoList.add(new ExerciseInfoEntity("Cross-Walker", workoutInfoName, "-", "Laufwiderstand: 10; Repeats in Minuten"));
            exerciseInfoList.add(new ExerciseInfoEntity("Negativ-Crunch", workoutInfoName, "-"));
            exerciseInfoList.add(new ExerciseInfoEntity("Klimmzug breit zur Brust", workoutInfoName, "-"));
            exerciseInfoList.add(new ExerciseInfoEntity("Beinstrecker", workoutInfoName, "E04", "Fuß: 3; Beine: 11; Sitz: 1,5"));
            exerciseInfoList.add(new ExerciseInfoEntity("Beinbeuger", workoutInfoName, "E05", "Fuß: 6; Beine: 12"));
            exerciseInfoList.add(new ExerciseInfoEntity("Butterfly", workoutInfoName, "A02"));
            exerciseInfoList.add(new ExerciseInfoEntity("Wadenheben an der Beinpresse", workoutInfoName, "E01", "Rücken: 2; Sitz: 5"));
            exerciseInfoList.add(new ExerciseInfoEntity("Duale Schrägband-Drückmaschine", workoutInfoName, "C02", "Sitz: 1"));
            exerciseInfoList.add(new ExerciseInfoEntity("Bizepsmaschine", workoutInfoName, "D01"));
            exerciseInfoList.add(new ExerciseInfoEntity("Pushdown am Kabelzug", workoutInfoName, "B06"));
            exerciseInfoList.add(new ExerciseInfoEntity("Rückenstrecker", workoutInfoName, "B03", "Beine: 4"));
            exerciseInfoList.add(new ExerciseInfoEntity("Crunch Bauchbank", workoutInfoName, "F01", "Beine: 3"));
        }
        return exerciseInfoList;
    }

    private static List<ExerciseSetEntity> initializeExerciseSets(final WorkoutUnitEntity workoutUnit)
    {
        List<ExerciseSetEntity> exerciseSetList = new ArrayList<>(14);
        final int workoutUnitId = workoutUnit.getId();
        if (Objects.equals(workoutUnit.getWorkoutInfoName(), "HIT full-body"))
        {
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Cross-Walker", 8, 0.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Negativ-Crunch", 20, 0.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Klimmzug breit zur Brust", 8, 0.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Klimmzug breit zur Brust", 6, 0.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Klimmzug breit zur Brust", 4, 0.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Beinstrecker", 15, 40.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Beinbeuger", 16, 40.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Butterfly", 17, 35.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Wadenheben an der Beinpresse", 18, 110.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Duale Schrägband-Drückmaschine", 17, 30.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Bizepsmaschine", 17, 35.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Pushdown am Kabelzug", 17, 20.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Rückenstrecker", 21, 0.F));
            exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Crunch Bauchbank", 19, 0.F));
        }
        return exerciseSetList;
    }


    // --------------------
    // Functional code
    // --------------------

    private static AppDatabase instance;

    public abstract WorkoutInfoDao workoutInfoDao();
    public abstract WorkoutUnitDao workoutUnitDao();
    public abstract ExerciseInfoDao exerciseInfoDao();
    public abstract ExerciseSetDao exerciseSetDao();

    private final MutableLiveData<Boolean> isDatabaseCreated = new MutableLiveData<>();

    public static AppDatabase getInstance(final Context context, final AppExecutors executors)
    {
        if (instance == null)
        {
            synchronized (AppDatabase.class)
            {
                if (instance == null)
                {
                    instance = buildDatabase(context.getApplicationContext(), executors);
                    instance.updateDatabaseCreated(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /* Build the database. {@link Builder#build()} only sets up the database configuration and creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time. */
    private static AppDatabase buildDatabase(final Context appContext, final AppExecutors executors)
    {
        Callback callback = new Callback()
        {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db)
            {
                super.onCreate(db);
                executors.getDiskIO().execute(() ->
                {
                    // Initialise database:
                    AppDatabase database = AppDatabase.getInstance(appContext, executors);
                    database.runInTransaction(() ->
                    {
                        final WorkoutInfoEntity workoutInfo = initializeWorkoutInfo();
                        database.workoutInfoDao().insert(workoutInfo);
                        final WorkoutUnitEntity workoutUnit = new WorkoutUnitEntity(0, workoutInfo.getName());
                        database.workoutUnitDao().insert(workoutUnit);
                        database.exerciseInfoDao().insert(initializeExerciseInfo(workoutInfo));
                        database.exerciseSetDao().insert(initializeExerciseSets(workoutUnit));

                        // Debugging entities:
                        final WorkoutUnitEntity workoutUnitDebug = new WorkoutUnitEntity(MainActivity.DEBUG_WORKOUT_MIN_ID, workoutInfo.getName());
                        database.workoutUnitDao().insert(workoutUnitDebug);
                        database.exerciseSetDao().insert(initializeExerciseSets(workoutUnitDebug));

                    });
                    // Notify that the database was created and is ready to be used:
                    database.setDatabaseCreated();
                });
            }
        };
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME).addCallback(callback).build();
    }

    /* Check whether the database already exists and expose it via {@link #getDatabaseCreated()}. */
    private void updateDatabaseCreated(final Context context)
    {
        if (context.getDatabasePath(DATABASE_NAME).exists())
        {
            setDatabaseCreated();
        }
    }

    public LiveData<Boolean> getDatabaseCreated()
    {
        return isDatabaseCreated;
    }

    private void setDatabaseCreated()
    {
        isDatabaseCreated.postValue(true);
    }
}