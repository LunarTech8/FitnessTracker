package com.romanbrunner.apps.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.WorkoutUnit;

import java.util.Date;
import java.util.Objects;


@Entity(tableName = "workoutUnits",
        foreignKeys = {@ForeignKey(entity = WorkoutInfoEntity.class, parentColumns = "name", childColumns = "workoutInfoName", onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "workoutInfoName")}
        )
public class WorkoutUnitEntity implements WorkoutUnit
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey
    private int id;
    private String workoutInfoName;
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
    public Date getDate()
    {
        return date;
    }

    @Override
    public void setId(int id)
    {
        if (this.id != id) this.id = id;
    }

    @Override
    public void setWorkoutInfoName(String workoutInfoName)
    {
        if (!Objects.equals(this.workoutInfoName, workoutInfoName)) this.workoutInfoName = workoutInfoName;
    }

    @Override
    public void setDate(Date date)
    {
        if (date != null && (this.date == null || this.date.compareTo(date) != 0)) this.date = date;
    }

    public WorkoutUnitEntity() {}
    @Ignore
    public WorkoutUnitEntity(int id, String workoutInfoName)
    {
        this.id = id;
        this.workoutInfoName = workoutInfoName;
        date = new Date();  // Current date
    }
    @Ignore
    public WorkoutUnitEntity(WorkoutUnit workoutUnit)
    {
        this(workoutUnit.getId() + 1, workoutUnit.getWorkoutInfoName());  // Increment Id by one
    }

    public static boolean isContentTheSame(WorkoutUnit workoutUnitA, WorkoutUnit workoutUnitB)
    {
        return workoutUnitA.getId() == workoutUnitB.getId()
            && Objects.equals(workoutUnitA.getWorkoutInfoName(), workoutUnitB.getWorkoutInfoName())
            && workoutUnitA.getDate().compareTo(workoutUnitB.getDate()) == 0;
    }
}