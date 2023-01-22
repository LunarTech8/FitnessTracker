package com.romanbrunner.apps.fitnesstracker.model;


import java.util.Date;

public interface ExerciseSet
{
    int getId();
    Date getWorkoutUnitDate();
    String getExerciseInfoName();
    int getRepeats();
    float getWeight();
    boolean isDone();

    void setId(int id);
    void setWorkoutUnitDate(Date workoutUnitDate);
    void setExerciseInfoName(String exerciseInfoName);
    void setRepeats(int repeats);
    void setWeight(float weight);
    void setDone(boolean done);
}