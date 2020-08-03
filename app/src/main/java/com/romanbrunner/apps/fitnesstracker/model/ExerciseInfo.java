package com.romanbrunner.apps.fitnesstracker.model;


public interface ExerciseInfo
{
    String getName();
    String getToken();
    String getRemarks();
    String getDefaultValues();

    void setName(String name);
    void setToken(String token);
    void setRemarks(String remarks);
    void setDefaultValues(String defaultValues);
}