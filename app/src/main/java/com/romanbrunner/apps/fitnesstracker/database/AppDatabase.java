package com.romanbrunner.apps.fitnesstracker.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.romanbrunner.apps.fitnesstracker.AppExecutors;
import com.romanbrunner.apps.fitnesstracker.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;


@Database(entities = {WorkoutUnitEntity.class, WorkoutInfoEntity.class, ExerciseSetEntity.class, ExerciseInfoEntity.class}, version = 2)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase
{
    // --------------------
    // Data code
    // --------------------

    private final static String DATABASE_NAME = "fitness-tracker-database";

    private static List<WorkoutInfoEntity> initializeWorkoutInfo()
    {
        List<WorkoutInfoEntity> workoutInfoList = new ArrayList<>(2);
        String exerciseInfoNames = "";
        exerciseInfoNames += "Cross-Walker" + ";";
        exerciseInfoNames += "Negativ-Crunch" + ";";
        exerciseInfoNames += "Klimmzug breit zur Brust" + ";";
        exerciseInfoNames += "Beinstrecker" + ";";
        exerciseInfoNames += "Beinbeuger" + ";";
        exerciseInfoNames += "Butterfly" + ";";
        exerciseInfoNames += "Wadenheben an der Beinpresse" + ";";
        exerciseInfoNames += "Duale Schrägband-Drückmaschine" + ";";
        exerciseInfoNames += "Bizepsmaschine" + ";";
        exerciseInfoNames += "Pushdown am Kabelzug" + ";";
        exerciseInfoNames += "Rückenstrecker" + ";";
        exerciseInfoNames += "Crunch Bauchbank" + ";";
        workoutInfoList.add(new WorkoutInfoEntity("HIT full-body (McFit)", 1, "High intensity training full-body at McFit", exerciseInfoNames));
        exerciseInfoNames = "";
        exerciseInfoNames += "Cross-Walker" + ";";
        exerciseInfoNames += "Klimmzug breit zur Brust" + ";";
        exerciseInfoNames += "Beinstrecker" + ";";
        exerciseInfoNames += "Beinbeuger" + ";";
        exerciseInfoNames += "Butterfly" + ";";
        exerciseInfoNames += "Wadenheben an der Beinpresse" + ";";
        exerciseInfoNames += "Duale Schrägband-Drückmaschine" + ";";
        exerciseInfoNames += "Bizepsmaschine" + ";";
        exerciseInfoNames += "Pushdown am Kabelzug" + ";";
        exerciseInfoNames += "Rückenstrecker" + ";";
        exerciseInfoNames += "Crunch Bauchbank" + ";";
        workoutInfoList.add(new WorkoutInfoEntity("HIT full-body (Body+Souls)", 1, "High intensity training full-body at Body+Souls", exerciseInfoNames));
        return workoutInfoList;
    }

    private static List<ExerciseInfoEntity> initializeExerciseInfo()
    {
        List<ExerciseInfoEntity> exerciseInfoList = new ArrayList<>(12);
        exerciseInfoList.add(new ExerciseInfoEntity("Cross-Walker", "", "Laufwiderstand: 10; Repeats in Minuten"));
        exerciseInfoList.add(new ExerciseInfoEntity("Negativ-Crunch", ""));
        exerciseInfoList.add(new ExerciseInfoEntity("Klimmzug breit zur Brust", ""));
        exerciseInfoList.add(new ExerciseInfoEntity("Beinstrecker", "E04", "Fuß: 3; Beine: 11; Sitz: 1,5"));
        exerciseInfoList.add(new ExerciseInfoEntity("Beinbeuger", "E05", "Fuß: 6; Beine: 12"));
        exerciseInfoList.add(new ExerciseInfoEntity("Butterfly", "A02"));
        exerciseInfoList.add(new ExerciseInfoEntity("Wadenheben an der Beinpresse", "E01", "Rücken: 2; Sitz: 5"));
        exerciseInfoList.add(new ExerciseInfoEntity("Duale Schrägband-Drückmaschine", "C02", "Sitz: 1"));
        exerciseInfoList.add(new ExerciseInfoEntity("Bizepsmaschine", "D01"));
        exerciseInfoList.add(new ExerciseInfoEntity("Pushdown am Kabelzug", "B06"));
        exerciseInfoList.add(new ExerciseInfoEntity("Rückenstrecker", "B03", "Beine: 4"));
        exerciseInfoList.add(new ExerciseInfoEntity("Crunch Bauchbank", "F01", "Beine: 3"));
        return exerciseInfoList;
    }

    private static void createDefaultWorkoutUnit(final AppDatabase database, int workoutUnitId)
    {
        database.workoutUnitDao().insert(new WorkoutUnitEntity(workoutUnitId, "HIT full-body (McFit)", 1));
        List<ExerciseSetEntity> exerciseSetList = new ArrayList<>(14);
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
        database.exerciseSetDao().insert(exerciseSetList);
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            database.execSQL("CREATE TABLE IF NOT EXISTS `workoutUnits` (`id` INTEGER, `workoutInfoName` TEXT, `workoutInfoVersion` INTEGER, `date` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`workoutInfoName`, `workoutInfoVersion`) REFERENCES `workoutInfo`(`name`, `version`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("INSERT INTO `workoutUnits` (`id`, `workoutInfoName`, `workoutInfoVersion`, `date`) SELECT `id`, `name`, 1, `date` FROM `workouts`");

            database.execSQL("CREATE TABLE `workoutInfo` (`name` TEXT NOT NULL, `version` INTEGER, `description` TEXT, `exerciseInfoNames` TEXT, PRIMARY KEY(`name`, `version`))");
            instance.workoutInfoDao().insert(initializeWorkoutInfo());

            database.execSQL("CREATE TABLE `exerciseSets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `workoutUnitId` INTEGER, `exerciseInfoName` TEXT, `repeats` INTEGER, `weight` REAL, `done` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`workoutUnitId`) REFERENCES `workoutUnits`(`id`) ON DELETE CASCADE, FOREIGN KEY(`exerciseInfoName`) REFERENCES `exerciseInfo`(`name`) ON DELETE RESTRICT)");
            database.execSQL("INSERT INTO `exerciseSets` (`id`, `workoutUnitId`, `exerciseInfoName`, `repeats`, `weight`, `done`) SELECT `id`, `workoutId`, `name`, `repeats`, `weight`, `done` FROM `exercises`");

            database.execSQL("CREATE TABLE `exerciseInfo` (`name` TEXT NOT NULL, `token` TEXT, `remarks` TEXT, PRIMARY KEY(`name`))");
            instance.exerciseInfoDao().insert(initializeExerciseInfo());
        }
    };


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
                    // Initialize database:
                    AppDatabase database = AppDatabase.getInstance(appContext, executors);
                    database.runInTransaction(() ->
                    {
                        // Initialize info data:
                        database.workoutInfoDao().insert(initializeWorkoutInfo());
                        database.exerciseInfoDao().insert(initializeExerciseInfo());

                        // Create first workout unit:
                        createDefaultWorkoutUnit(database, 0);

                        // Create first debugging workout unit:
                        createDefaultWorkoutUnit(database, MainActivity.DEBUG_WORKOUT_MIN_ID);
                    });
                    // Notify that the database was created and is ready to be used:
                    database.setDatabaseCreated();
                });
            }
        };
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME).addCallback(callback).addMigrations(MIGRATION_1_2).build();
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