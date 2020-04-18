package com.romanbrunner.apps.fitnesstracker.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.ExerciseInfo;

import java.util.Objects;


@Entity(tableName = "exerciseInfo")
public class ExerciseInfoEntity implements ExerciseInfo
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey @NonNull
    private String name = "InitNonNullName";
    private String token;
    private String remarks;

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
    public void setName(String name)
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

    ExerciseInfoEntity() {}

    public static boolean isContentTheSame(ExerciseInfo exerciseInfoA, ExerciseInfo exerciseInfoB)
    {
        return Objects.equals(exerciseInfoA.getName(), exerciseInfoB.getName())
            && Objects.equals(exerciseInfoA.getToken(), exerciseInfoB.getToken())
            && Objects.equals(exerciseInfoA.getRemarks(), exerciseInfoB.getRemarks());
    }
}