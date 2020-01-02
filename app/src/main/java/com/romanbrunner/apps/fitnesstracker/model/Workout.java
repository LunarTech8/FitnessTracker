package com.romanbrunner.apps.fitnesstracker.model;

import java.util.Date;


public interface Workout
{
    int getId();
    Date getDate();
    String getName();
    String getDescription();

    void setId(int id);
    void setDate(Date date);
    void setName(String name);
    void setDescription(String token);
}