package com.romanbrunner.apps.fitnesstracker.database;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import com.romanbrunner.apps.fitnesstracker.model.WorkoutInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


@Entity(primaryKeys = {"name", "version"}, tableName = "workoutInfo")
public class WorkoutInfoEntity implements WorkoutInfo
{
    // --------------------
    // Functional code
    // --------------------

    public static final String EXERCISE_NAMES_COUNT_SEPARATOR = ",";
    public static final String EXERCISE_NAMES_DELIMITER = ";";

    @NonNull
    private String name = "InitNonNullName";
    private int version;
    private String description;
    private String exerciseNames;  // Stores exercise names, count and order

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

    public static List<Pair<String, Integer>> exerciseNames2DataList(String exerciseNames)
    {
        List<Pair<String, Integer>> dataList = new LinkedList<>();
        for (String dataString : exerciseNames.split(EXERCISE_NAMES_DELIMITER))
        {
            String[] dataStringParts = dataString.split(EXERCISE_NAMES_COUNT_SEPARATOR);
            dataList.add(new Pair<>(dataStringParts[0], Integer.valueOf(dataStringParts[1])));
        }
        return dataList;
    }

    public static String exerciseSets2exerciseNames(List<ExerciseSetEntity> orderedExerciseSets)
    {
        // Condense ordered exercise sets into ordered exercise data with unique entry names and their occurrence count:
        List<Pair<String, Integer>> orderedExercises = new ArrayList<>();
        for (ExerciseSetEntity exerciseSet : orderedExerciseSets)
        {
            String name = exerciseSet.getExerciseInfoName();
            int index = 0;
            while (index < orderedExercises.size() && !Objects.equals(orderedExercises.get(index).first, name))
            {
                index++;
            }
            if (index < orderedExercises.size())
            {
                orderedExercises.set(index, new Pair<>(name, orderedExercises.get(index).second + 1));
            }
            else
            {
                orderedExercises.add(new Pair<>(name, 1));
            }
        }
        // Transform ordered exercise data into exerciseNames string:
        final StringBuilder exerciseNames = new StringBuilder();
        for (Pair<String, Integer> exercise : orderedExercises)
        {
            exerciseNames.append(exercise.first).append(EXERCISE_NAMES_COUNT_SEPARATOR).append(exercise.second).append(EXERCISE_NAMES_DELIMITER);
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