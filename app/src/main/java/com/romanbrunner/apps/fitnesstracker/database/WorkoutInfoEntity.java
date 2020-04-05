package com.romanbrunner.apps.fitnesstracker.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import com.romanbrunner.apps.fitnesstracker.model.WorkoutInfo;

import java.util.Objects;


@Entity(primaryKeys = {"name", "version"}, tableName = "workoutInfo")
public class WorkoutInfoEntity implements WorkoutInfo
{
    // --------------------
    // Functional code
    // --------------------
    @NonNull
    private String name = "InitNonNullName";
    private int version;
    private String description;
    private String exerciseInfoNames;

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
    public String getExerciseInfoNames()
    {
        return exerciseInfoNames;
    }

    @Override
    public void setName(@NonNull String name)
    {
        if (!Objects.equals(this.name, name)) this.name = name;
    }

    @Override
    public void setVersion(int version)
    {
        if (this.version != version) this.version = version;
    }

    @Override
    public void setDescription(String description)
    {
        if (!Objects.equals(this.description, description)) this.description = description;
    }

    @Override
    public void setExerciseInfoNames(String exerciseInfoNames)
    {
        if (!Objects.equals(this.exerciseInfoNames, exerciseInfoNames)) this.exerciseInfoNames = exerciseInfoNames;
    }

    WorkoutInfoEntity() {}

    public static boolean isContentTheSame(WorkoutInfo workoutInfoA, WorkoutInfo workoutInfoB)
    {
        return Objects.equals(workoutInfoA.getName(), workoutInfoB.getName())
            && workoutInfoA.getVersion() == workoutInfoB.getVersion()
            && Objects.equals(workoutInfoA.getDescription(), workoutInfoB.getDescription())
            && Objects.equals(workoutInfoA.getExerciseInfoNames(), workoutInfoB.getExerciseInfoNames());
    }
}