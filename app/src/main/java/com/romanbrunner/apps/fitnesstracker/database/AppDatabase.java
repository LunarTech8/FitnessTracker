package com.romanbrunner.apps.fitnesstracker.database;

import android.content.Context;
import android.util.Log;

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
    private static final Migration MIGRATION_1_2 = new Migration(1, 2)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            database.execSQL("CREATE TABLE `workoutInfo` (`name` TEXT NOT NULL, `version` INTEGER NOT NULL, `description` TEXT, `exerciseInfoNames` TEXT, PRIMARY KEY(`name`, `version`))");
            insertDefaultWorkoutInfo(database);

            database.execSQL("CREATE TABLE `exerciseInfo` (`name` TEXT NOT NULL, `token` TEXT, `remarks` TEXT, PRIMARY KEY(`name`))");
            insertDefaultExerciseInfo(database);

            database.execSQL("CREATE TABLE `workoutUnits` (`id` INTEGER NOT NULL, `workoutInfoName` TEXT, `workoutInfoVersion` INTEGER NOT NULL, `date` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`workoutInfoName`, `workoutInfoVersion`) REFERENCES `workoutInfo`(`name`, `version`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX `index_workoutUnits_workoutInfoName_workoutInfoVersion` ON `workoutUnits` (`workoutInfoName`, `workoutInfoVersion`)");
            database.execSQL("INSERT INTO `workoutUnits` (`id`, `workoutInfoName`, `workoutInfoVersion`, `date`) SELECT `id`, `name`, 1, `date` FROM `workouts`");
            database.execSQL("UPDATE `workoutUnits` SET `workoutInfoName`='HIT full-body (McFit)' WHERE `workoutInfoName`='HIT full-body'");

            database.execSQL("CREATE TABLE `exerciseSets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `workoutUnitId` INTEGER NOT NULL, `exerciseInfoName` TEXT, `repeats` INTEGER NOT NULL, `weight` REAL NOT NULL, `done` INTEGER NOT NULL, FOREIGN KEY(`workoutUnitId`) REFERENCES `workoutUnits`(`id`) ON DELETE CASCADE, FOREIGN KEY(`exerciseInfoName`) REFERENCES `exerciseInfo`(`name`) ON DELETE RESTRICT)");
            database.execSQL("CREATE INDEX `index_exerciseSets_workoutUnitId` ON `exerciseSets` (`workoutUnitId`)");
            database.execSQL("CREATE INDEX `index_exerciseSets_exerciseInfoName` ON `exerciseSets` (`exerciseInfoName`)");
            database.execSQL("INSERT INTO `exerciseSets` (`id`, `workoutUnitId`, `exerciseInfoName`, `repeats`, `weight`, `done`) SELECT `id`, `workoutId`, `name`, `repeats`, `weight`, `done` FROM `exercises`");

            database.execSQL("DROP TABLE `workouts`");
            database.execSQL("DROP TABLE `exercises`");
        }
    };

    private static void insertDefaultWorkoutInfo(@NonNull SupportSQLiteDatabase database)
    {
        final String delimiter = WorkoutInfoEntity.EXERCISE_INFO_NAMES_DELIMITER;
        String exerciseInfoNames = "";
        exerciseInfoNames += "Cross-Walker" + delimiter;
        exerciseInfoNames += "Negativ-Crunch" + delimiter;
        exerciseInfoNames += "Klimmzug breit zur Brust" + delimiter;
        exerciseInfoNames += "Beinstrecker" + delimiter;
        exerciseInfoNames += "Beinbeuger" + delimiter;
        exerciseInfoNames += "Butterfly" + delimiter;
        exerciseInfoNames += "Wadenheben an der Beinpresse" + delimiter;
        exerciseInfoNames += "Duale Schrägband-Drückmaschine" + delimiter;
        exerciseInfoNames += "Bizepsmaschine" + delimiter;
        exerciseInfoNames += "Pushdown am Kabelzug" + delimiter;
        exerciseInfoNames += "Rückenstrecker" + delimiter;
        exerciseInfoNames += "Crunch Bauchbank" + delimiter;
        database.execSQL("INSERT INTO `workoutInfo` (`name`, `version`, `description`, `exerciseInfoNames`) VALUES('HIT full-body (McFit)', 1, 'High intensity training full-body at McFit', '" + exerciseInfoNames + "')");
        exerciseInfoNames = "";
        exerciseInfoNames += "Cross-Walker" + delimiter;
        exerciseInfoNames += "Klimmzug breit zur Brust" + delimiter;
        exerciseInfoNames += "Beinstrecker" + delimiter;
        exerciseInfoNames += "Beinbeuger" + delimiter;
        exerciseInfoNames += "Butterfly" + delimiter;
        exerciseInfoNames += "Wadenheben an der Beinpresse" + delimiter;
        exerciseInfoNames += "Duale Schrägband-Drückmaschine" + delimiter;
        exerciseInfoNames += "Bizepsmaschine" + delimiter;
        exerciseInfoNames += "Pushdown am Kabelzug" + delimiter;
        exerciseInfoNames += "Rückenstrecker" + delimiter;
        exerciseInfoNames += "Crunch Bauchbank" + delimiter;
        database.execSQL("INSERT INTO `workoutInfo` (`name`, `version`, `description`, `exerciseInfoNames`) VALUES('HIT full-body (Body+Souls)', 1, 'High intensity training full-body at Body+Souls', '" + exerciseInfoNames + "')");
    }

    private static void insertDefaultExerciseInfo(@NonNull SupportSQLiteDatabase database)
    {
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`) VALUES('Cross-Walker', '', 'Laufwiderstand: 10; Repeats in Minuten')");
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`) VALUES('Negativ-Crunch', '', '')");
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`) VALUES('Klimmzug breit zur Brust', '', '')");
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`) VALUES('Beinstrecker', 'E04', 'Fuß: 3; Beine: 11; Sitz: 1,5')");
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`) VALUES('Beinbeuger', 'E05', 'Fuß: 6; Beine: 12')");
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`) VALUES('Butterfly', 'A02', '')");
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`) VALUES('Wadenheben an der Beinpresse', 'E01', 'Rücken: 2; Sitz: 5')");
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`) VALUES('Duale Schrägband-Drückmaschine', 'C02', 'Sitz: 1')");
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`) VALUES('Bizepsmaschine', 'D01', '')");
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`) VALUES('Pushdown am Kabelzug', 'B06', '')");
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`) VALUES('Rückenstrecker', 'B03', 'Beine: 4')");
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`) VALUES('Crunch Bauchbank', 'F01', 'Beine: 3')");
    }

    public static void createDefaultExercise(final List<ExerciseSetEntity> exerciseSetList, int workoutUnitId, String exerciseInfoName)
    {
        switch (exerciseInfoName)
        {
            case "Cross-Walker":
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 8, 0.F));
                break;
            case "Negativ-Crunch":
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 20, 0.F));
                break;
            case "Klimmzug breit zur Brust":
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 8, 0.F));
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 6, 0.F));
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 4, 0.F));
                break;
            case "Beinstrecker":
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 15, 40.F));
                break;
            case "Beinbeuger":
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 16, 40.F));
                break;
            case "Butterfly":
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 17, 35.F));
                break;
            case "Wadenheben an der Beinpresse":
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 19, 110.F));
                break;
            case "Duale Schrägband-Drückmaschine":
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 17, 30.F));
                break;
            case "Bizepsmaschine":
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 17, 35.F));
                break;
            case "Pushdown am Kabelzug":
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 17, 20.F));
                break;
            case "Rückenstrecker":
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 21, 0.F));
                break;
            case "Crunch Bauchbank":
                exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 19, 0.F));
                break;
            default:
                Log.e("createDefaultExerciseSets", "Unrecognized exerciseInfoName (" + exerciseInfoName + ")");
        }
    }

    private static void createDefaultWorkout(final AppDatabase database, int workoutUnitId)
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
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Wadenheben an der Beinpresse", 19, 110.F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Duale Schrägband-Drückmaschine", 17, 30.F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Bizepsmaschine", 17, 35.F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Pushdown am Kabelzug", 17, 20.F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Rückenstrecker", 21, 0.F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "Crunch Bauchbank", 19, 0.F));
        // TODO: maybe createDefaultExerciseSets can be used here
        database.exerciseSetDao().insert(exerciseSetList);
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

    /* Build the database. {@link Builder#build()} only sets up the database configuration and creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time. */
    private static AppDatabase buildDatabase(final Context context, final AppExecutors executors)
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
                    AppDatabase database = AppDatabase.getInstance(context, executors);
                    database.runInTransaction(() ->
                    {
                        // Insert default info data:
                        insertDefaultWorkoutInfo(db);
                        insertDefaultExerciseInfo(db);

                        // Create first workout unit:
                        createDefaultWorkout(database, 0);

                        // Create first debugging workout unit:
                        createDefaultWorkout(database, MainActivity.DEBUG_WORKOUT_MIN_ID);
                    });
                    // Notify that the database was created and is ready to be used:
                    database.setDatabaseCreated();
                });
            }
        };
        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).addCallback(callback).addMigrations(MIGRATION_1_2).build();
    }

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

    /* Check whether the database already exists and expose it via {@link #getDatabaseCreated()}. */
    private void updateDatabaseCreated(final Context context)
    {
        if (context.getDatabasePath(DATABASE_NAME).exists())
        {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated()
    {
        isDatabaseCreated.postValue(true);
    }

    public LiveData<Boolean> getDatabaseCreated()
    {
        return isDatabaseCreated;
    }

    public void deleteDatabase(final Context context)  // TODO: maybe not needed
    {
        context.deleteDatabase(DATABASE_NAME);
        isDatabaseCreated.postValue(false);
    }
}