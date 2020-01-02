package com.romanbrunner.apps.fitnesstracker.model;

import java.util.Date;

public interface Exercise
{
    int getId();
    int getWorkoutId();
    String getName();
    String getToken();
    String getRemarks();
    int getRepeats();
    float getWeight();
    boolean isDone();

    void setId(int id);
    void setWorkoutId(int workoutId);
    void setName(String name);
    void setToken(String token);
    void setRemarks(String remarks);
    void setRepeats(int repeats);
    void setWeight(float weight);
    void setDone(boolean done);
}