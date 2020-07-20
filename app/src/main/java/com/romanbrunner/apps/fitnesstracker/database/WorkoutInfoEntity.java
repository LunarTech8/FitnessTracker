package com.romanbrunner.apps.fitnesstracker.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import com.romanbrunner.apps.fitnesstracker.model.WorkoutInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Entity(primaryKeys = {"name", "version"}, tableName = "workoutInfo")
public class WorkoutInfoEntity implements WorkoutInfo
{
    // --------------------
    // Functional code
    // --------------------

    private static final String EXERCISE_NAMES_COUNT_SEPARATOR = ",";  // FIXME: count information isn't used yet anywhere
    public static final String EXERCISE_NAMES_DELIMITER = ";";

    @NonNull
    private String name = "InitNonNullName";
    private int version;
    private String description;
    private String exerciseNames;

    @Override
    public @NonNull String getName()
    {
        return name;
    }

    @Override
    public int getVersion()
    {
        return version;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public String getExerciseNames()
    {
        return exerciseNames;
    }

    @Override
    public void setName(@NonNull String name)
    {
        this.name = name;
    }

    @Override
    public void setVersion(int version)
    {
        this.version = version;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public void setExerciseNames(String exerciseNames)
    {
        this.exerciseNames = exerciseNames;
    }

    WorkoutInfoEntity() {}

    public static String[] exerciseNames2UniqueNamesArray(String exerciseNames)
    {
        String[] namesArray = exerciseNames.split(EXERCISE_NAMES_DELIMITER);
        for (int i = 0; i < namesArray.length; i++)
        {
            namesArray[i] = namesArray[i].split(EXERCISE_NAMES_COUNT_SEPARATOR)[0];
        }
        return namesArray;
    }

    public static String exerciseSets2exerciseNames(List<ExerciseSetEntity> exerciseSets)
    {
        final Map<String, Integer> exerciseName2exerciseSetCount = new HashMap<>();
        for (ExerciseSetEntity exerciseSet : exerciseSets)
        {
            String exerciseName = exerciseSet.getExerciseInfoName();
            //noinspection ConstantConditions
            exerciseName2exerciseSetCount.put(exerciseName, exerciseName2exerciseSetCount.getOrDefault(exerciseName, 0) + 1);
        }
        final StringBuilder exerciseNames = new StringBuilder();
        for (Map.Entry<String, Integer> entry : exerciseName2exerciseSetCount.entrySet())
        {
            exerciseNames.append(entry.getKey()).append(EXERCISE_NAMES_COUNT_SEPARATOR).append(entry.getValue()).append(EXERCISE_NAMES_DELIMITER);
        }
        return String.valueOf(exerciseNames);
    }

    public static boolean isContentTheSame(WorkoutInfo workoutInfoA, WorkoutInfo workoutInfoB)
    {
        return Objects.equals(workoutInfoA.getName(), workoutInfoB.getName())
            && workoutInfoA.getVersion() == workoutInfoB.getVersion()
            && Objects.equals(workoutInfoA.getDescription(), workoutInfoB.getDescription())
            && Objects.equals(workoutInfoA.getExerciseNames(), workoutInfoB.getExerciseNames());
    }
}