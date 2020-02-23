package com.romanbrunner.apps.fitnesstracker.model;

public interface ExerciseInfo
{
    String getName();
    String getToken();
    String getRemarks();

    void setName(String name);
    void setToken(String token);
    void setRemarks(String remarks);
}