package com.romanbrunner.apps.fitnesstracker.model;

import java.util.Date;


public interface WorkoutUnit
{
    int getId();
    Date getDate();
    String getStudio();
    String getName();
    String getDescription();
    String getExerciseNames();

    void setId(int id);
    void setDate(Date date);
    void setStudio(String studio);
    void setName(String name);
    void setDescription(String description);
    void setExerciseNames(String exerciseNames);
}