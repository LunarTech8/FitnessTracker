package com.romanbrunner.apps.fitnesstracker.database;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.WorkoutUnit;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Entity(tableName = "workoutUnits")
public class WorkoutUnitEntity implements WorkoutUnit
{
    // --------------------
    // Functional code
    // --------------------

    public static final String EXERCISE_NAMES_SEPARATOR = ",";  // Used for internal separation of data for each entry
    public static final String EXERCISE_NAMES_DELIMITER = ";";  // Used for external separation between entries

    @PrimaryKey private Date date;
    @NonNull private String studio = "InitNonNullStudio";
    @NonNull private String name = "InitNonNullName";
    private String description;
    private String exerciseNames;  // Stores exercise names, count and order

    @Override
    public Date getDate()
    {
        return date;
    }

    @Override
    public @NonNull String getStudio()
    {
        return studio;
    }

    @Override
    public @NonNull String getName()
    {
        return name;
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
    public void setStudio(@NonNull String studio)
    {
        this.studio = studio;
    }

    @Override
    public void setName(@NonNull String name)
    {
        this.name = name;
    }

    @Override
    public void setDate(Date date)
    {
        if (date != null) this.date = date;
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

    WorkoutUnitEntity() {}
    @Ignore
    public WorkoutUnitEntity(@NonNull String studio, @NonNull String name, String description, String exerciseNames, @NonNull Date date)
    {
        this.date = date;
        this.studio = studio;
        this.name = name;
        this.description = description;
        this.exerciseNames = exerciseNames;
    }
    @Ignore
    public WorkoutUnitEntity(@NonNull String studio, @NonNull String name, String description, String exerciseNames)
    {
        date = new Date();  // Current date
        this.studio = studio;
        this.name = name;
        this.description = description;
        this.exerciseNames = exerciseNames;
    }
    @Ignore
    public WorkoutUnitEntity(@NonNull WorkoutUnit workoutUnit, @NonNull Date date)
    {
        this(workoutUnit.getStudio(), workoutUnit.getName(), workoutUnit.getDescription(), workoutUnit.getExerciseNames(), date);
    }
    @Ignore
    public WorkoutUnitEntity(@NonNull WorkoutUnit workoutUnit)
    {
        this(workoutUnit.getStudio(), workoutUnit.getName(), workoutUnit.getDescription(), workoutUnit.getExerciseNames());
    }

    public static Set<String> exerciseNames2NameSet(final String exerciseNames)
    {
        final String[] dataStringEntries = exerciseNames.split(EXERCISE_NAMES_DELIMITER);
        final Set<String> nameList = new LinkedHashSet<>(dataStringEntries.length);
        for (String dataString : dataStringEntries)
        {
            nameList.add(dataString.split(EXERCISE_NAMES_SEPARATOR)[0]);
        }
        return nameList;
    }

    public static int exerciseNames2Amount(final String exerciseNames)
    {
        final String[] dataStringEntries = exerciseNames.split(EXERCISE_NAMES_DELIMITER);
        int amount = 0;
        for (String dataString : dataStringEntries)
        {
            amount += Integer.parseInt(dataString.split(EXERCISE_NAMES_SEPARATOR)[1]);
        }
        return amount;
    }

    public static String exerciseSets2exerciseNames(final List<ExerciseSetEntity> orderedExerciseSets)
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
            exerciseNames.append(exercise.first).append(EXERCISE_NAMES_SEPARATOR).append(exercise.second).append(EXERCISE_NAMES_DELIMITER);
        }
        return String.valueOf(exerciseNames);
    }

    public static boolean isContentTheSame(WorkoutUnit workoutUnitA, WorkoutUnit workoutUnitB)
    {
        return workoutUnitA.getDate().compareTo(workoutUnitB.getDate()) == 0
            && Objects.equals(workoutUnitA.getStudio(), workoutUnitB.getStudio())
            && Objects.equals(workoutUnitA.getName(), workoutUnitB.getName())
            && Objects.equals(workoutUnitA.getDescription(), workoutUnitB.getDescription())
            && Objects.equals(workoutUnitA.getExerciseNames(), workoutUnitB.getExerciseNames());
    }
}