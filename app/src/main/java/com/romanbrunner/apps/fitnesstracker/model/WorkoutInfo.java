package com.romanbrunner.apps.fitnesstracker.model;


public interface WorkoutInfo
{
    String getName();
    int getVersion();
    String getDescription();
    String getExerciseInfoNames();

    void setName(String name);
    void setVersion(int version);
    void setDescription(String description);
    void setExerciseInfoNames(String exerciseInfoNames);
}