package com.romanbrunner.apps.fitnesstracker.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.ExerciseInfo;

import java.util.Objects;


@Entity(tableName = "exerciseInfo",
        foreignKeys = {@ForeignKey(entity = WorkoutInfoEntity.class, parentColumns = "name", childColumns = "workoutInfoName", onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "workoutInfoName")}
        )
public class ExerciseInfoEntity implements ExerciseInfo
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey @NonNull
    private String name = "InitNonNullName";
    private String workoutInfoName;
    private String token;
    private String remarks;

    @Override
    public @NonNull String getName()
    {
        return name;
    }

    @Override
    public String getWorkoutInfoName()
    {
        return workoutInfoName;
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
    public void setName(String name)
    {
        if (!Objects.equals(this.name, name)) this.name = name;
    }

    @Override
    public void setWorkoutInfoName(String workoutInfoName)
    {
        if (!Objects.equals(this.workoutInfoName, workoutInfoName)) this.workoutInfoName = workoutInfoName;
    }

    @Override
    public void setToken(String token)
    {
        if (!Objects.equals(this.token, token)) this.token = token;
    }

    @Override
    public void setRemarks(String remarks)
    {
        if (!Objects.equals(this.remarks, remarks)) this.remarks = remarks;
    }

    public ExerciseInfoEntity() {}
    @Ignore
    public ExerciseInfoEntity(@NonNull String name, String workoutInfoName, String token)
    {
        this.name = name;
        this.workoutInfoName = workoutInfoName;
        this.token = token;
    }
    @Ignore
    public ExerciseInfoEntity(@NonNull String name, String workoutInfoName, String token, String remarks)
    {
        this(name, workoutInfoName, token);
        this.remarks = remarks;
    }

    public static boolean isContentTheSame(ExerciseInfo exerciseInfoA, ExerciseInfo exerciseInfoB)
    {
        return Objects.equals(exerciseInfoA.getName(), exerciseInfoB.getName())
            && Objects.equals(exerciseInfoA.getWorkoutInfoName(), exerciseInfoB.getWorkoutInfoName())
            && Objects.equals(exerciseInfoA.getToken(), exerciseInfoB.getToken())
            && Objects.equals(exerciseInfoA.getRemarks(), exerciseInfoB.getRemarks());
    }
}