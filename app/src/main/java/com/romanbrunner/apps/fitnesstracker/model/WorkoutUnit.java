package com.romanbrunner.apps.fitnesstracker.model;

import java.util.Date;


public interface WorkoutUnit
{
    int getId();
    String getWorkoutInfoName();
    int getWorkoutInfoVersion();
    Date getDate();

    void setId(int id);
    void setWorkoutInfoName(String workoutInfoName);
    void setWorkoutInfoVersion(int workoutInfoVersion);
    void setDate(Date date);
}