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


@Database(entities = {WorkoutUnitEntity.class, WorkoutInfoEntity.class, ExerciseSetEntity.class, ExerciseInfoEntity.class}, version = 3)
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
            database.execSQL("UPDATE `workoutUnits` SET `workoutInfoName` = 'HIT full-body (McFit)' WHERE `workoutInfoName` = 'HIT full-body'");

            database.execSQL("CREATE TABLE `exerciseSets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `workoutUnitId` INTEGER NOT NULL, `exerciseInfoName` TEXT, `repeats` INTEGER NOT NULL, `weight` REAL NOT NULL, `done` INTEGER NOT NULL, FOREIGN KEY(`workoutUnitId`) REFERENCES `workoutUnits`(`id`) ON DELETE CASCADE, FOREIGN KEY(`exerciseInfoName`) REFERENCES `exerciseInfo`(`name`) ON DELETE RESTRICT)");
            database.execSQL("CREATE INDEX `index_exerciseSets_workoutUnitId` ON `exerciseSets` (`workoutUnitId`)");
            database.execSQL("CREATE INDEX `index_exerciseSets_exerciseInfoName` ON `exerciseSets` (`exerciseInfoName`)");
            database.execSQL("INSERT INTO `exerciseSets` (`id`, `workoutUnitId`, `exerciseInfoName`, `repeats`, `weight`, `done`) SELECT `id`, `workoutId`, `name`, `repeats`, `weight`, `done` FROM `exercises`");

            database.execSQL("DROP TABLE `workouts`");
            database.execSQL("DROP TABLE `exercises`");
        }
    };
    private static final Migration MIGRATION_2_3 = new Migration(2, 3)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            database.execSQL("ALTER TABLE `workoutInfo` RENAME TO `workoutInfo_old`");
            database.execSQL("CREATE TABLE `workoutInfo` (`name` TEXT NOT NULL, `version` INTEGER NOT NULL, `description` TEXT, `exerciseNames` TEXT, PRIMARY KEY(`name`, `version`))");
            database.execSQL("INSERT INTO `workoutInfo` (`name`, `version`, `description`, `exerciseNames`) SELECT `name`, `version`, `description`, `exerciseInfoNames` FROM `workoutInfo_old`");
            database.execSQL("UPDATE `workoutInfo` SET `exerciseNames` = REPLACE(`exerciseNames`, '" + WorkoutInfoEntity.EXERCISE_NAMES_DELIMITER + "', '" + WorkoutInfoEntity.EXERCISE_NAMES_COUNT_SEPARATOR + "1" + WorkoutInfoEntity.EXERCISE_NAMES_DELIMITER + "')");
            database.execSQL("DROP TABLE `workoutInfo_old`");
        }
    };

    private static void insertDefaultWorkoutInfo(@NonNull SupportSQLiteDatabase database)
    {
        final String delimiter = WorkoutInfoEntity.EXERCISE_NAMES_DELIMITER;
        final String separator = WorkoutInfoEntity.EXERCISE_NAMES_COUNT_SEPARATOR;
        String exerciseNames = "";
        exerciseNames += "Cross-Walker" + separator + "1" + delimiter;
        exerciseNames += "Negativ-Crunch" + separator + "1" + delimiter;
        exerciseNames += "Klimmzug breit zur Brust" + separator + "3" + delimiter;
        exerciseNames += "Beinstrecker" + separator + "1" + delimiter;
        exerciseNames += "Beinbeuger" + separator + "1" + delimiter;
        exerciseNames += "Butterfly" + separator + "1" + delimiter;
        exerciseNames += "Wadenheben an der Beinpresse" + separator + "1" + delimiter;
        exerciseNames += "Duale Schrägband-Drückmaschine" + separator + "1" + delimiter;
        exerciseNames += "Bizepsmaschine" + separator + "1" + delimiter;
        exerciseNames += "Pushdown am Kabelzug" + separator + "1" + delimiter;
        exerciseNames += "Rückenstrecker" + separator + "1" + delimiter;
        exerciseNames += "Crunch Bauchbank" + separator + "1" + delimiter;
        database.execSQL("INSERT INTO `workoutInfo` (`name`, `version`, `description`, `exerciseNames`) VALUES('HIT full-body (McFit)', 1, 'High intensity training full-body at McFit', '" + exerciseNames + "')");
        exerciseNames = "";
        exerciseNames += "Cross-Walker" + separator + "1" + delimiter;
        exerciseNames += "Klimmzug breit zur Brust" + separator + "3" + delimiter;
        exerciseNames += "Beinstrecker" + separator + "1" + delimiter;
        exerciseNames += "Beinbeuger" + separator + "1" + delimiter;
        exerciseNames += "Butterfly" + separator + "1" + delimiter;
        exerciseNames += "Wadenheben an der Beinpresse" + separator + "1" + delimiter;
        exerciseNames += "Duale Schrägband-Drückmaschine" + separator + "1" + delimiter;
        exerciseNames += "Bizepsmaschine" + separator + "1" + delimiter;
        exerciseNames += "Pushdown am Kabelzug" + separator + "1" + delimiter;
        exerciseNames += "Rückenstrecker" + separator + "1" + delimiter;
        exerciseNames += "Crunch Bauchbank" + separator + "1" + delimiter;
        database.execSQL("INSERT INTO `workoutInfo` (`name`, `version`, `description`, `exerciseNames`) VALUES('HIT full-body (Body+Souls)', 1, 'High intensity training full-body at Body+Souls', '" + exerciseNames + "')");
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

    private static void insertInitWorkoutUnit(final AppDatabase database, final int workoutUnitId)
    {
        database.workoutUnitDao().insert(new WorkoutUnitEntity(workoutUnitId, "HIT full-body (McFit)", 1));
        final List<ExerciseSetEntity> exerciseSetList = new ArrayList<>(14);
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Cross-Walker"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Negativ-Crunch"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Klimmzug breit zur Brust"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Klimmzug breit zur Brust"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Klimmzug breit zur Brust"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Beinstrecker"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Beinbeuger"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Butterfly"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Wadenheben an der Beinpresse"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Duale Schrägband-Drückmaschine"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Bizepsmaschine"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Pushdown am Kabelzug"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Rückenstrecker"));
        exerciseSetList.add(createDefaultExerciseSet(workoutUnitId, "Crunch Bauchbank"));
        database.exerciseSetDao().insert(exerciseSetList);
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public static ExerciseSetEntity createDefaultExerciseSet(int workoutUnitId, String exerciseInfoName)
    {
        switch (exerciseInfoName)
        {
            case "Cross-Walker":
                return new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 8, 0.F);
            case "Negativ-Crunch":
                return new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 20, 0.F);
            case "Klimmzug breit zur Brust":
                return new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 8, 0.F);
            case "Beinstrecker":
                return new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 15, 40.F);
            case "Beinbeuger":
                return new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 16, 40.F);
            case "Butterfly":
                return new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 17, 35.F);
            case "Wadenheben an der Beinpresse":
                return new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 19, 110.F);
            case "Duale Schrägband-Drückmaschine":
                return new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 17, 30.F);
            case "Bizepsmaschine":
                return new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 17, 35.F);
            case "Pushdown am Kabelzug":
                return new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 17, 20.F);
            case "Rückenstrecker":
                return new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 21, 0.F);
            case "Crunch Bauchbank":
                return new ExerciseSetEntity(workoutUnitId, exerciseInfoName, 19, 0.F);
            default:
                Log.e("createDefaultExercise", "Unrecognized exerciseInfoName (" + exerciseInfoName + ")");
        }
        return null;
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

                        // Insert first workout unit:
                        insertInitWorkoutUnit(database, 0);
                        // Insert first debugging workout unit:
                        insertInitWorkoutUnit(database, MainActivity.DEBUG_WORKOUT_MIN_ID);
                    });
                    // Notify that the database was created and is ready to be used:
                    database.setDatabaseCreated();
                });
            }
        };
        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).addCallback(callback).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build();
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