package com.romanbrunner.apps.fitnesstracker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


@Database(entities = {Exercise.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase
{
    // --------------------
    // Data code
    // --------------------

    private final static String DATABASE_NAME = "fitness-tracker-database";

    private static List<Exercise> initializeData()
    {
        List<Exercise> exercises = new ArrayList<>();
        // "Default" exercise plan:
        exercises.add(new Exercise("Cross-Walker", "-", 8, 0.F, "Laufwiderstand: 10; Repeats in Minuten"));
        exercises.add(new Exercise("Negativ-Crunch", "-", 20, 0.F));
        exercises.add(new Exercise("Klimmzug breit zur Brust", "-", 8, 0.F));
        exercises.add(new Exercise("Klimmzug breit zur Brust", "-", 6, 0.F));
        exercises.add(new Exercise("Klimmzug breit zur Brust", "-", 4, 0.F));
        exercises.add(new Exercise("Beinstrecker", "-", 19, 35.F, "Fuß: 3; Beine: 11; Sitz: 1,5"));
        exercises.add(new Exercise("Beinbeuger", "-", 15, 40.F, "Fuß: 6; Beine: 12"));
        exercises.add(new Exercise("Butterfly", "-", 16, 35.F));
        exercises.add(new Exercise("Wadenheben an der Beinpresse", "-", 16, 105.F, "Rücken: 2; Sitz: 5"));
        exercises.add(new Exercise("Duale Schrägband-Drückmaschine", "-", 18, 30.F, "Sitz: 1"));
        exercises.add(new Exercise("Bizepsmaschine", "-", 16, 35.F));
        exercises.add(new Exercise("Pushdown am Kabelzug", "-", 16, 20.F));
        exercises.add(new Exercise("Rückenstrecker", "-", 21, 0.F, "Beine: 4"));
        exercises.add(new Exercise("Beinheben liegend", "-", 22, 0.F));
        return exercises;
    }


    // --------------------
    // Functional code
    // --------------------

    private static AppDatabase instance;

    public abstract ExerciseDao exerciseDao();

    public static AppDatabase getInstance(final Context context, final AppExecutors executors)  // TODO: this has to be called at least once somewhere
    {
        if (instance == null)
        {
            synchronized (AppDatabase.class)
            {
                if (instance == null)
                {
                    instance = buildDatabase(context.getApplicationContext(), executors);
                }
            }
        }
        return instance;
    }

    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static AppDatabase buildDatabase(final Context appContext, final AppExecutors executors)
    {
        Callback callback = new Callback()
        {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db)
            {
                super.onCreate(db);
                executors.diskIO().execute(() ->
                {
                    // Add a delay to simulate a long-running operation:
                    try { Thread.sleep(4000); }  // TODO: probably only for test, remove
                    catch (InterruptedException ignored) {}
                    // Generate the data for pre-population
                    AppDatabase database = AppDatabase.getInstance(appContext, executors);

                    // Load or initialise database:
                    List<Exercise> exercises = database.exerciseDao().getAll();
                    if (exercises.isEmpty())
                    {
                        final List<Exercise> initExercises = initializeData();
                        database.runInTransaction(() -> database.exerciseDao().insert(initExercises));
                        exercises = initExercises;
                    }
                    MainActivity.adapter.setExercises(exercises);
                });
            }
        };
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME).addCallback(callback).build();
    }
}