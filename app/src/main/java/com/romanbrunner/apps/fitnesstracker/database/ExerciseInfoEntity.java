package com.romanbrunner.apps.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.ExerciseInfo;

import java.util.Objects;


@Entity(tableName = "exerciseInfos")
public class ExerciseInfoEntity implements ExerciseInfo
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String token;
    private String remarks;

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public String getName()
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
    public void setId(int id)
    {
        if (this.id != id) this.id = id;
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
    public ExerciseInfoEntity(String name, String token)
    {
        this.name = name;
        this.token = token;
    }
    @Ignore
    public ExerciseInfoEntity(String name, String token, String remarks)
    {
        this(name, token);
        this.remarks = remarks;
    }
    @Ignore
    public ExerciseInfoEntity(ExerciseInfo exercise, int workoutId)
    {
        this(exercise.getName(), exercise.getToken(), exercise.getRemarks());
    }

    public static boolean isContentTheSame(ExerciseInfo exerciseA, ExerciseInfo exerciseB)
    {
        return exerciseA.getId() == exerciseB.getId()
                && Objects.equals(exerciseA.getRemarks(), exerciseB.getRemarks())
                && Objects.equals(exerciseA.getName(), exerciseB.getName())
                && Objects.equals(exerciseA.getToken(), exerciseB.getToken());
    }
}