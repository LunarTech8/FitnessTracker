package com.romanbrunner.apps.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.WorkoutUnit;

import java.util.Date;
import java.util.Objects;


@Entity(tableName = "workoutUnits", foreignKeys = @ForeignKey(entity = WorkoutInfoEntity.class, parentColumns = {"name", "version"}, childColumns = {"workoutInfoName", "workoutInfoVersion"}, onDelete = ForeignKey.CASCADE), indices = @Index(value = {"workoutInfoName", "workoutInfoVersion"}))
public class WorkoutUnitEntity implements WorkoutUnit
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey
    private int id;
    private String workoutInfoName;
    private int workoutInfoVersion;
    private Date date;

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public String getWorkoutInfoName()
    {
        return workoutInfoName;
    }

    @Override
    public int getWorkoutInfoVersion()
    {
        return workoutInfoVersion;
    }

    @Override
    public Date getDate()
    {
        return date;
    }

    @Override
    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public void setWorkoutInfoName(String workoutInfoName)
    {
        this.workoutInfoName = workoutInfoName;
    }

    @Override
    public void setWorkoutInfoVersion(int workoutInfoVersion)
    {
        this.workoutInfoVersion = workoutInfoVersion;
    }

    @Override
    public void setDate(Date date)
    {
        if (date != null) this.date = date;
    }

    WorkoutUnitEntity() {}
    @Ignore
    public WorkoutUnitEntity(int id, String workoutInfoName, int workoutInfoVersion)
    {
        this.id = id;
        this.workoutInfoName = workoutInfoName;
        this.workoutInfoVersion = workoutInfoVersion;
        date = new Date();  // Current date
    }
    @Ignore
    public WorkoutUnitEntity(WorkoutUnit workoutUnit)
    {
        this(workoutUnit.getId() + 1, workoutUnit.getWorkoutInfoName(), workoutUnit.getWorkoutInfoVersion());  // Increment Id by one
    }

    public static boolean isContentTheSame(WorkoutUnit workoutUnitA, WorkoutUnit workoutUnitB)
    {
        return workoutUnitA.getId() == workoutUnitB.getId()
            && Objects.equals(workoutUnitA.getWorkoutInfoName(), workoutUnitB.getWorkoutInfoName())
            && workoutUnitA.getWorkoutInfoVersion() == workoutUnitB.getWorkoutInfoVersion()
            && workoutUnitA.getDate().compareTo(workoutUnitB.getDate()) == 0;
    }
}