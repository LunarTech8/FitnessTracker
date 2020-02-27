package com.romanbrunner.apps.fitnesstracker.model;

public interface ExerciseInfo
{
    String getName();
    int getWorkoutId();
    String getToken();
    String getRemarks();

    void setName(String name);
    void setWorkoutId(int workoutId);
    void setToken(String token);
    void setRemarks(String remarks);
}