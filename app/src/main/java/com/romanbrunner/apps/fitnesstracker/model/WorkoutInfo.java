package com.romanbrunner.apps.fitnesstracker.model;


public interface WorkoutInfo
{
    String getStudio();
    String getName();
    int getVersion();
    String getDescription();
    String getExerciseNames();

    void setStudio(String studio);
    void setName(String name);
    void setVersion(int version);
    void setDescription(String description);
    void setExerciseNames(String exerciseNames);
}