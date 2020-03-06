package com.romanbrunner.apps.fitnesstracker.model;


public interface ExerciseInfo
{
    String getName();
    String getWorkoutInfoName();
    String getToken();
    String getRemarks();

    void setName(String name);
    void setWorkoutInfoName(String workoutInfoName);
    void setToken(String token);
    void setRemarks(String remarks);
}