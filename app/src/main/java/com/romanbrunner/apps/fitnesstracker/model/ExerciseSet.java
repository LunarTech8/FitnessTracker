package com.romanbrunner.apps.fitnesstracker.model;


public interface ExerciseSet
{
    int getId();
    int getWorkoutUnitId();
    String getExerciseInfoName();
    int getRepeats();
    float getWeight();
    boolean isDone();

    void setId(int id);
    void setWorkoutUnitId(int workoutUnitId);
    void setExerciseInfoName(String exerciseInfoName);
    void setRepeats(int repeats);
    void setWeight(float weight);
    void setDone(boolean done);
}