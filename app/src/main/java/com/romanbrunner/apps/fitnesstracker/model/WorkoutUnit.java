package com.romanbrunner.apps.fitnesstracker.model;

import java.util.Date;


public interface WorkoutUnit
{
    int getId();
    String getWorkoutInfoName();
    Date getDate();

    void setId(int id);
    void setWorkoutInfoName(String workoutInfoName);
    void setDate(Date date);
}