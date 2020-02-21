package com.romanbrunner.apps.fitnesstracker.model;

public interface ExerciseInfo
{
    int getId();
    String getName();
    String getToken();
    String getRemarks();

    void setId(int id);
    void setName(String name);
    void setToken(String token);
    void setRemarks(String remarks);
}