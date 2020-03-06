package com.romanbrunner.apps.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.WorkoutInfo;

import java.util.Objects;


@Entity(tableName = "workoutInfo")
public class WorkoutInfoEntity implements WorkoutInfo
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey
    private String name;
    private String description;

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setName(String name)
    {
        if (!Objects.equals(this.name, name)) this.name = name;
    }

    @Override
    public void setDescription(String description)
    {
        if (!Objects.equals(this.description, description)) this.description = description;
    }

    public WorkoutInfoEntity() {}
    @Ignore
    public WorkoutInfoEntity(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public static boolean isContentTheSame(WorkoutInfo workoutInfoA, WorkoutInfo workoutInfoB)
    {
        return Objects.equals(workoutInfoA.getName(), workoutInfoB.getName())
            && Objects.equals(workoutInfoA.getDescription(), workoutInfoB.getDescription());
    }
}