package com.romanbrunner.apps.fitnesstracker.database;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.ExerciseInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


@Entity(tableName = "exerciseInfo")
public class ExerciseInfoEntity implements ExerciseInfo
{
    // --------------------
    // Functional code
    // --------------------

    public static final String DEFAULT_VALUES_SEPARATOR = ",";  // Used for internal separation of data for each entry
    public static final String DEFAULT_VALUES_DELIMITER = ";";  // Used for external separation between entries

    @PrimaryKey @NonNull private String name = "InitNonNullName";
    private String token;
    private String remarks;
    private String defaultValues;  // Stores repeats, weight, count (indirectly) and order  // TODO: obsolete, instead newest exercise set values should be used

    @Override
    public @NonNull String getName()
    {
        return name;
    }

    @Override
    public String getToken()
    {
        return token;
    }

    @Override
    public String getRemarks()
    {
        return remarks;
    }

    @Override
    public String getDefaultValues()
    {
        return defaultValues;
    }

    @Override
    public void setName(@NonNull String name)
    {
        this.name = name;
    }

    @Override
    public void setToken(String token)
    {
        this.token = token;
    }

    @Override
    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    @Override
    public void setDefaultValues(String defaultValues)
    {
        this.defaultValues = defaultValues;
    }

    ExerciseInfoEntity() {}
    @Ignore
    public ExerciseInfoEntity(@NonNull String name)
    {
        this.name = name;
        token = "";
        remarks = "";
        defaultValues = "";
    }

    public static List<Pair<Integer, Float>> defaultValues2DataList(final String defaultValues)
    {
        final List<Pair<Integer, Float>> dataList = new LinkedList<>();
        for (String dataString : defaultValues.split(DEFAULT_VALUES_DELIMITER))
        {
            String[] dataStringParts = dataString.split(DEFAULT_VALUES_SEPARATOR);
            dataList.add(new Pair<>(Integer.valueOf(dataStringParts[0]), Float.valueOf(dataStringParts[1])));
        }
        return dataList;
    }

    public static String exerciseSets2defaultValues(final String exerciseInfoName, final List<ExerciseSetEntity> orderedExerciseSets)
    {
        // Transform ordered exercise data into exerciseNames string:
        final StringBuilder defaultValues = new StringBuilder();
        for (ExerciseSetEntity exerciseSet : orderedExerciseSets)
        {
            if (Objects.equals(exerciseInfoName, exerciseSet.getExerciseInfoName()))
            {
                defaultValues.append(exerciseSet.getRepeats()).append(DEFAULT_VALUES_SEPARATOR).append(exerciseSet.getWeight()).append(DEFAULT_VALUES_DELIMITER);
            }
        }
        return String.valueOf(defaultValues);
    }

    public static boolean isContentTheSame(ExerciseInfo exerciseInfoA, ExerciseInfo exerciseInfoB)
    {
        return Objects.equals(exerciseInfoA.getName(), exerciseInfoB.getName())
            && Objects.equals(exerciseInfoA.getToken(), exerciseInfoB.getToken())
            && Objects.equals(exerciseInfoA.getRemarks(), exerciseInfoB.getRemarks())
            && Objects.equals(exerciseInfoA.getDefaultValues(), exerciseInfoB.getDefaultValues());
    }
}