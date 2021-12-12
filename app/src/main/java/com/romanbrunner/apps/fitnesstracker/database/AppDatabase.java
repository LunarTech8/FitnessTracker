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


@Database(entities = {WorkoutUnitEntity.class, WorkoutInfoEntity.class, ExerciseSetEntity.class, ExerciseInfoEntity.class}, version = 4)
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

            database.execSQL("CREATE TABLE `exerciseInfo` (`name` TEXT NOT NULL, `token` TEXT, `remarks` TEXT, `defaultValues` TEXT, PRIMARY KEY(`name`))");
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
            database.execSQL("UPDATE `workoutInfo` SET `exerciseNames` = REPLACE(`exerciseNames`, '" + WorkoutInfoEntity.EXERCISE_NAMES_DELIMITER + "', '" + WorkoutInfoEntity.EXERCISE_NAMES_SEPARATOR + "1" + WorkoutInfoEntity.EXERCISE_NAMES_DELIMITER + "')");
            database.execSQL("DROP TABLE `workoutInfo_old`");
        }
    };
    private static final Migration MIGRATION_3_4 = new Migration(3, 4)
    {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database)
        {
            database.execSQL("ALTER TABLE `workoutInfo` RENAME TO `workoutInfo_old`");
            database.execSQL("ALTER TABLE `workoutUnits` RENAME TO `workoutUnits_old`");

            database.execSQL("CREATE TABLE `workoutInfo` (`studio` TEXT NOT NULL, `name` TEXT NOT NULL, `version` INTEGER NOT NULL, `description` TEXT, `exerciseNames` TEXT, PRIMARY KEY(`studio`, `name`, `version`))");
            database.execSQL("INSERT INTO `workoutInfo` (`studio`, `name`, `version`, `description`, `exerciseNames`) SELECT 'McFit', 'HIT full-body', `version`, 'High intensity training full-body', `exerciseNames` FROM `workoutInfo_old` WHERE `name` = 'HIT full-body (McFit)'");
            database.execSQL("INSERT INTO `workoutInfo` (`studio`, `name`, `version`, `description`, `exerciseNames`) SELECT 'Body+Souls', 'HIT full-body', 1, 'High intensity training full-body', `exerciseNames` FROM `workoutInfo_old` WHERE `name` = 'HIT full-body (Body+Souls)' AND `version` = 1");
            database.execSQL("INSERT INTO `workoutInfo` (`studio`, `name`, `version`, `description`, `exerciseNames`) SELECT 'Body+Souls', 'HIT full-body Bicycle', 1, 'High intensity training full-body with a bicycle tour as warm-up', `exerciseNames` FROM `workoutInfo_old` WHERE `name` = 'HIT full-body (Body+Souls)' AND `version` = 2");

            database.execSQL("CREATE TABLE `workoutUnits` (`id` INTEGER NOT NULL, `workoutInfoStudio` TEXT, `workoutInfoName` TEXT, `workoutInfoVersion` INTEGER NOT NULL, `date` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`workoutInfoStudio`, `workoutInfoName`, `workoutInfoVersion`) REFERENCES `workoutInfo`(`studio`, `name`, `version`) ON UPDATE NO ACTION ON DELETE CASCADE)");
            database.execSQL("CREATE INDEX `index_workoutUnits_workoutInfoStudio_workoutInfoName_workoutInfoVersion` ON `workoutUnits` (`workoutInfoStudio`, `workoutInfoName`, `workoutInfoVersion`)");
            database.execSQL("INSERT INTO `workoutUnits` (`id`, `workoutInfoStudio`, `workoutInfoName`, `workoutInfoVersion`, `date`) SELECT `id`, 'McFit', 'HIT full-body', `workoutInfoVersion`, `date` FROM `workoutUnits_old` WHERE `workoutInfoName` = 'HIT full-body (McFit)'");
            database.execSQL("INSERT INTO `workoutUnits` (`id`, `workoutInfoStudio`, `workoutInfoName`, `workoutInfoVersion`, `date`) SELECT `id`, 'Body+Souls', 'HIT full-body', 1, `date` FROM `workoutUnits_old` WHERE `workoutInfoName` = 'HIT full-body (Body+Souls)' AND `workoutInfoVersion` = 1");
            database.execSQL("INSERT INTO `workoutUnits` (`id`, `workoutInfoStudio`, `workoutInfoName`, `workoutInfoVersion`, `date`) SELECT `id`, 'Body+Souls', 'HIT full-body Bicycle', 1, `date` FROM `workoutUnits_old` WHERE `workoutInfoName` = 'HIT full-body (Body+Souls)' AND `workoutInfoVersion` = 2");

            database.execSQL("DROP TABLE `workoutInfo_old`");
            database.execSQL("DROP TABLE `workoutUnits_old`");
        }
    };


    private static void insertDefaultWorkoutInfo(@NonNull SupportSQLiteDatabase database)
    {
        final String delimiter = WorkoutInfoEntity.EXERCISE_NAMES_DELIMITER;
        final String separator = WorkoutInfoEntity.EXERCISE_NAMES_SEPARATOR;
        String exerciseNames;
        exerciseNames = "Cross-Walker" + separator + 1 + delimiter;
        exerciseNames += "Negativ-Crunch" + separator + 1 + delimiter;
        exerciseNames += "Klimmzug breit zur Brust" + separator + 3 + delimiter;
        exerciseNames += "Beinstrecker" + separator + 1 + delimiter;
        exerciseNames += "Beinbeuger" + separator + 1 + delimiter;
        exerciseNames += "Butterfly" + separator + 1 + delimiter;
        exerciseNames += "Wadenheben an der Beinpresse" + separator + 1 + delimiter;
        exerciseNames += "Duale Schrägband-Drückmaschine" + separator + 1 + delimiter;
        exerciseNames += "Bizepsmaschine" + separator + 1 + delimiter;
        exerciseNames += "Pushdown am Kabelzug" + separator + 1 + delimiter;
        exerciseNames += "Rückenstrecker" + separator + 1 + delimiter;
        exerciseNames += "Crunch Bauchbank" + separator + 1 + delimiter;
        database.execSQL("INSERT INTO `workoutInfo` (`studio`, `name`, `version`, `description`, `exerciseNames`) VALUES('McFit', 'HIT full-body', 1, 'High intensity training full-body', '" + exerciseNames + "')");
        exerciseNames = "CROSS WALKER" + separator + 1 + delimiter;
        exerciseNames += "KLIMMZUG BREIT ZUR BRUST" + separator + 3 + delimiter;
        exerciseNames += "PUSHDOWN AM KABELZUG" + separator + 1 + delimiter;
        exerciseNames += "BEINSTRECKER" + separator + 1 + delimiter;
        exerciseNames += "BEINBEUGER LIEGEND" + separator + 1 + delimiter;
        exerciseNames += "BUTTERFLY" + separator + 1 + delimiter;
        exerciseNames += "BIZEPSMASCHINE" + separator + 1 + delimiter;
        exerciseNames += "BEINPRESSE" + separator + 1 + delimiter;
        exerciseNames += "RUECKENSTRECKER" + separator + 1 + delimiter;
        exerciseNames += "CRUNCH BAUCHBANK" + separator + 1 + delimiter;
        exerciseNames += "OVERHEAD PRESS" + separator + 1 + delimiter;
        database.execSQL("INSERT INTO `workoutInfo` (`studio`, `name`, `version`, `description`, `exerciseNames`) VALUES('Body+Souls', 'HIT full-body', 1, 'High intensity training full-body', '" + exerciseNames + "')");
        exerciseNames = "BICYCLE" + separator + 1 + delimiter;
        exerciseNames += "KLIMMZUG BREIT ZUR BRUST" + separator + 3 + delimiter;
        exerciseNames += "PUSHDOWN AM KABELZUG" + separator + 1 + delimiter;
        exerciseNames += "BEINSTRECKER" + separator + 1 + delimiter;
        exerciseNames += "BEINBEUGER LIEGEND" + separator + 1 + delimiter;
        exerciseNames += "BUTTERFLY" + separator + 1 + delimiter;
        exerciseNames += "BIZEPSMASCHINE" + separator + 1 + delimiter;
        exerciseNames += "BEINPRESSE" + separator + 1 + delimiter;
        exerciseNames += "RUECKENSTRECKER" + separator + 1 + delimiter;
        exerciseNames += "CRUNCH BAUCHBANK" + separator + 1 + delimiter;
        exerciseNames += "OVERHEAD PRESS" + separator + 1 + delimiter;
        database.execSQL("INSERT INTO `workoutInfo` (`studio`, `name`, `version`, `description`, `exerciseNames`) VALUES('Body+Souls', 'HIT full-body Bicycle', 1, 'High intensity training full-body with a bicycle tour as warm-up', '" + exerciseNames + "')");
    }

    private static void insertDefaultExerciseInfo(@NonNull SupportSQLiteDatabase database)
    {
        final String delimiter = ExerciseInfoEntity.DEFAULT_VALUES_DELIMITER;
        final String separator = ExerciseInfoEntity.DEFAULT_VALUES_SEPARATOR;
        String defaultValues;
        defaultValues = 8 + separator + 0.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('Cross-Walker', '', 'Laufwiderstand: 10; Repeats in Minuten', '" + defaultValues + "')");
        defaultValues = 20 + separator + 0.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('Negativ-Crunch', '', '', '" + defaultValues + "')");
        defaultValues = 8 + separator + 0.F + delimiter;
        defaultValues += 6 + separator + 0.F + delimiter;
        defaultValues += 4 + separator + 0.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('Klimmzug breit zur Brust', '', '', '" + defaultValues + "')");
        defaultValues = 15 + separator + 40.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('Beinstrecker', 'E04', 'Fuß: 3; Beine: 11; Sitz: 1,5', '" + defaultValues + "')");
        defaultValues = 16 + separator + 40.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('Beinbeuger', 'E05', 'Fuß: 6; Beine: 12', '" + defaultValues + "')");
        defaultValues = 17 + separator + 35.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('Butterfly', 'A02', '', '" + defaultValues + "')");
        defaultValues = 19 + separator + 110.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('Wadenheben an der Beinpresse', 'E01', 'Rücken: 2; Sitz: 5', '" + defaultValues + "')");
        defaultValues = 17 + separator + 30.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('Duale Schrägband-Drückmaschine', 'C02', 'Sitz: 1', '" + defaultValues + "')");
        defaultValues = 17 + separator + 35.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('Bizepsmaschine', 'D01', '', '" + defaultValues + "')");
        defaultValues = 17 + separator + 20.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('Pushdown am Kabelzug', 'B06', '', '" + defaultValues + "')");
        defaultValues = 21 + separator + 0.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('Rückenstrecker', 'B03', 'Beine: 4', '" + defaultValues + "')");
        defaultValues = 19 + separator + 0.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('Crunch Bauchbank', 'F01', 'Beine: 3', '" + defaultValues + "')");
        defaultValues = 8 + separator + 0.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('CROSS WALKER', '', 'Laufwiderstand: 10; >12km/h; Repeats in Minuten', '" + defaultValues + "')");
        defaultValues = 8 + separator + 0.F + delimiter;
        defaultValues += 6 + separator + 0.F + delimiter;
        defaultValues += 4 + separator + 0.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('KLIMMZUG BREIT ZUR BRUST', '', '', '" + defaultValues + "')");
        defaultValues = 15 + separator + 39.4F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('BEINSTRECKER', '151', 'Fuß: 1; Rücken: 4', '" + defaultValues + "')");
        defaultValues = 16 + separator + 39.4F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('BEINBEUGER LIEGEND', '153', 'Fuß: 1; Beine: 5', '" + defaultValues + "')");
        defaultValues = 17 + separator + 34.3F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('BUTTERFLY', '141', 'Sitz: 5; Arme: 1', '" + defaultValues + "')");
        defaultValues = 19 + separator + 108.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('BEINPRESSE', '150', 'Sitz: 6; Mit Zehnspitzen', '" + defaultValues + "')");
        defaultValues = 15 + separator + 31.5F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('OVERHEAD PRESS', '40', 'Sitz: 3', '" + defaultValues + "')");
        defaultValues = 15 + separator + 34.3F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('BIZEPSMASCHINE', '145', 'Sitz: 7', '" + defaultValues + "')");
        defaultValues = 18 + separator + 18.1F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('PUSHDOWN AM KABELZUG', '', '', '" + defaultValues + "')");
        defaultValues = 21 + separator + 0.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('RUECKENSTRECKER', '34', 'Beine: 4', '" + defaultValues + "')");
        defaultValues = 21 + separator + 0.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('CRUNCH BAUCHBANK', '58', 'Beine: 3', '" + defaultValues + "')");
        defaultValues = 40 + separator + 0.F + delimiter;
        database.execSQL("INSERT INTO `exerciseInfo` (`name`, `token`, `remarks`, `defaultValues`) VALUES('BICYCLE', '', 'Repeats in Minuten', '" + defaultValues + "')");
    }

    private static void insertInitWorkoutUnit(final AppDatabase database, final int workoutUnitId)
    {
        database.workoutUnitDao().insert(new WorkoutUnitEntity(workoutUnitId, "Body+Souls", "HIT full-body", 1));
        final List<ExerciseSetEntity> exerciseSetList = new ArrayList<>(13);
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "CROSS WALKER", 8, 0.F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "KLIMMZUG BREIT ZUR BRUST", 7, 0.F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "KLIMMZUG BREIT ZUR BRUST", 6, 0.F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "KLIMMZUG BREIT ZUR BRUST", 5, 0.F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "PUSHDOWN AM KABELZUG", 20, 21.25F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "BEINSTRECKER", 15, 39.4F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "BEINBEUGER LIEGEND", 16, 39.4F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "BUTTERFLY", 17, 34.3F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "BIZEPSMASCHINE", 15, 34.3F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "BEINPRESSE", 17, 112.5F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "RUECKENSTRECKER", 23, 0.F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "CRUNCH BAUCHBANK", 28, 0.F));
        exerciseSetList.add(new ExerciseSetEntity(workoutUnitId, "OVERHEAD PRESS", 16, 31.5F));
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
        return Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).addCallback(callback).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build();
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
}