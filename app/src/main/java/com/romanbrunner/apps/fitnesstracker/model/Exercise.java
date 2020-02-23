package com.romanbrunner.apps.fitnesstracker.model;

public interface Exercise
{
    int getId();
    int getWorkoutId();
    String getExerciseInfoName();
    int getRepeats();
    float getWeight();
    boolean isDone();

    void setId(int id);
    void setWorkoutId(int workoutId);
    void setExerciseInfoName(String exerciseInfoName);
    void setRepeats(int repeats);
    void setWeight(float weight);
    void setDone(boolean done);
}