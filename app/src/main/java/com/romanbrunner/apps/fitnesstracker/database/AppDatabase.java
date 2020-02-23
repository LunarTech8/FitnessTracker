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


@Database(entities = {WorkoutEntity.class, ExerciseEntity.class, ExerciseInfoEntity.class}, version = 1)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase
{
    // --------------------
    // Data code
    // --------------------

    private final static String DATABASE_NAME = "fitness-tracker-database";

    private static WorkoutEntity initializeWorkout()
    {
        return new WorkoutEntity(0, "HIT full-body", "High intensity training full-body");  // "Default" workout plan
    }

    private static List<ExerciseInfoEntity> initializeExerciseInfo()
    {
        List<ExerciseInfoEntity> exerciseInfo = new ArrayList<>(12);

        exerciseInfo.add(new ExerciseInfoEntity("Cross-Walker", "-", "Laufwiderstand: 10; Repeats in Minuten"));
        exerciseInfo.add(new ExerciseInfoEntity("Negativ-Crunch", "-"));
        exerciseInfo.add(new ExerciseInfoEntity("Klimmzug breit zur Brust", "-"));
        exerciseInfo.add(new ExerciseInfoEntity("Beinstrecker", "E04", "Fuß: 3; Beine: 11; Sitz: 1,5"));
        exerciseInfo.add(new ExerciseInfoEntity("Beinbeuger", "E05", "Fuß: 6; Beine: 12"));
        exerciseInfo.add(new ExerciseInfoEntity("Butterfly", "A02"));
        exerciseInfo.add(new ExerciseInfoEntity("Wadenheben an der Beinpresse", "E01", "Rücken: 2; Sitz: 5"));
        exerciseInfo.add(new ExerciseInfoEntity("Duale Schrägband-Drückmaschine", "C02", "Sitz: 1"));
        exerciseInfo.add(new ExerciseInfoEntity("Bizepsmaschine", "D01"));
        exerciseInfo.add(new ExerciseInfoEntity("Pushdown am Kabelzug", "B06"));
        exerciseInfo.add(new ExerciseInfoEntity("Rückenstrecker", "B03", "Beine: 4"));
        exerciseInfo.add(new ExerciseInfoEntity("Crunch Bauchbank", "F01", "Beine: 3"));

        return exerciseInfo;
    }

    private static List<ExerciseEntity> initializeExercises(final WorkoutEntity workout)
    {
        List<ExerciseEntity> exercises = new ArrayList<>(14);
        final int workoutId = workout.getId();
        if (Objects.equals(workout.getName(), "HIT full-body"))
        {
            exercises.add(new ExerciseEntity(workoutId, "Cross-Walker", 8, 0.F));
            exercises.add(new ExerciseEntity(workoutId, "Negativ-Crunch", 20, 0.F));
            exercises.add(new ExerciseEntity(workoutId, "Klimmzug breit zur Brust", 8, 0.F));
            exercises.add(new ExerciseEntity(workoutId, "Klimmzug breit zur Brust", 6, 0.F));
            exercises.add(new ExerciseEntity(workoutId, "Klimmzug breit zur Brust", 4, 0.F));
            exercises.add(new ExerciseEntity(workoutId, "Beinstrecker", 15, 40.F));
            exercises.add(new ExerciseEntity(workoutId, "Beinbeuger", 16, 40.F));
            exercises.add(new ExerciseEntity(workoutId, "Butterfly", 17, 35.F));
            exercises.add(new ExerciseEntity(workoutId, "Wadenheben an der Beinpresse", 18, 110.F));
            exercises.add(new ExerciseEntity(workoutId, "Duale Schrägband-Drückmaschine", 17, 30.F));
            exercises.add(new ExerciseEntity(workoutId, "Bizepsmaschine", 17, 35.F));
            exercises.add(new ExerciseEntity(workoutId, "Pushdown am Kabelzug", 17, 20.F));
            exercises.add(new ExerciseEntity(workoutId, "Rückenstrecker", 21, 0.F));
            exercises.add(new ExerciseEntity(workoutId, "Crunch Bauchbank", 19, 0.F));
        }
        return exercises;
    }


    // --------------------
    // Functional code
    // --------------------

    private static AppDatabase instance;

    public abstract ExerciseInfoDao exerciseInfoDao();
    public abstract ExerciseDao exerciseDao();
    public abstract WorkoutDao workoutDao();

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
                        final WorkoutEntity workout = initializeWorkout();
                        database.workoutDao().insert(workout);
                        database.exerciseInfoDao().insert(initializeExerciseInfo());
                        database.exerciseDao().insert(initializeExercises(workout));

                        // Debugging workout:
                        final WorkoutEntity workoutDebug = initializeWorkout();
                        workoutDebug.setId(MainActivity.DEBUG_WORKOUT_MIN_ID);
                        database.workoutDao().insert(workoutDebug);
                        database.exerciseDao().insert(initializeExercises(workoutDebug));

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