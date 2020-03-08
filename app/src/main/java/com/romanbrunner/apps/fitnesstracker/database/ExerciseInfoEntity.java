package com.romanbrunner.apps.fitnesstracker.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
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
        if (!Objects.equals(this.name, name)) this.name = name;
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
    public ExerciseInfoEntity(@NonNull String name, String token)
    {
        this.name = name;
        this.token = token;
    }
    @Ignore
    public ExerciseInfoEntity(@NonNull String name, String token, String remarks)
    {
        this(name, token);
        this.remarks = remarks;
    }

    public static boolean isContentTheSame(ExerciseInfo exerciseInfoA, ExerciseInfo exerciseInfoB)
    {
        return Objects.equals(exerciseInfoA.getName(), exerciseInfoB.getName())
            && Objects.equals(exerciseInfoA.getToken(), exerciseInfoB.getToken())
            && Objects.equals(exerciseInfoA.getRemarks(), exerciseInfoB.getRemarks());
    }
}