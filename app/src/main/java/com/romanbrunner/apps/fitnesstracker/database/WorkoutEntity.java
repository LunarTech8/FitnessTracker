package com.romanbrunner.apps.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.Workout;

import java.util.Date;
import java.util.Objects;


@Entity(tableName = "workouts")
public class WorkoutEntity implements Workout
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey
    private int id;
    private Date date;
    private String name;
    private String description;

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public Date getDate()
    {
        return date;
    }

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
    public void setId(int id)
    {
        if (this.id != id) this.id = id;
    }

    @Override
    public void setDate(Date date)
    {
        if (date != null && (this.date == null || this.date.compareTo(date) != 0)) this.date = date;
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

    public WorkoutEntity() {}
    @Ignore
    public WorkoutEntity(int id, String name, String description)
    {
        this.id = id;
        date = new Date();  // Current date
        this.name = name;
        this.description = description;
    }
    @Ignore
    public WorkoutEntity(Workout workout)
    {
        this(workout.getId() + 1, workout.getName(), workout.getDescription());  // Increment Id by one
    }

    public static boolean isContentTheSame(Workout workoutA, Workout workoutB)
    {
        return workoutA.getId() == workoutB.getId()
            && workoutA.getDate().compareTo(workoutB.getDate()) == 0
            && Objects.equals(workoutA.getName(), workoutB.getName())
            && Objects.equals(workoutA.getDescription(), workoutB.getDescription());
    }
}