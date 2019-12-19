package com.romanbrunner.apps.fitnesstracker.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.romanbrunner.apps.fitnesstracker.AppExecutors;

import java.util.ArrayList;
import java.util.List;


@Database(entities = {ExerciseEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase
{
    // --------------------
    // Data code
    // --------------------

    private final static String DATABASE_NAME = "fitness-tracker-database";

    private static List<ExerciseEntity> initializeData()
    {
        List<ExerciseEntity> exercises = new ArrayList<>();
        // "Default" exercise plan:
        exercises.add(new ExerciseEntity("Cross-Walker", "-", 8, 0.F, "Laufwiderstand: 10; Repeats in Minuten"));
        exercises.add(new ExerciseEntity("Negativ-Crunch", "-", 20, 0.F));
        exercises.add(new ExerciseEntity("Klimmzug breit zur Brust", "-", 8, 0.F));
        exercises.add(new ExerciseEntity("Klimmzug breit zur Brust", "-", 6, 0.F));
        exercises.add(new ExerciseEntity("Klimmzug breit zur Brust", "-", 4, 0.F));
        exercises.add(new ExerciseEntity("Beinstrecker", "-", 19, 35.F, "Fuß: 3; Beine: 11; Sitz: 1,5"));
        exercises.add(new ExerciseEntity("Beinbeuger", "-", 15, 40.F, "Fuß: 6; Beine: 12"));
        exercises.add(new ExerciseEntity("Butterfly", "-", 16, 35.F));
        exercises.add(new ExerciseEntity("Wadenheben an der Beinpresse", "-", 16, 105.F, "Rücken: 2; Sitz: 5"));
        exercises.add(new ExerciseEntity("Duale Schrägband-Drückmaschine", "-", 18, 30.F, "Sitz: 1"));
        exercises.add(new ExerciseEntity("Bizepsmaschine", "-", 16, 35.F));
        exercises.add(new ExerciseEntity("Pushdown am Kabelzug", "-", 16, 20.F));
        exercises.add(new ExerciseEntity("Rückenstrecker", "-", 21, 0.F, "Beine: 4"));
        exercises.add(new ExerciseEntity("Beinheben liegend", "-", 22, 0.F));
        return exercises;
    }


    // --------------------
    // Functional code
    // --------------------

    private static AppDatabase instance;

    public abstract ExerciseDao exerciseDao();

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
                    // Add a delay to simulate a long-running operation:
                    try
                    {
                        Thread.sleep(5000);  // TEST: only for test, remove later
                    }
                    catch (InterruptedException ignored) {}

                    // Initialise database:
                    AppDatabase database = AppDatabase.getInstance(appContext, executors);
                    database.runInTransaction(() -> database.exerciseDao().insert(initializeData()));
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