package com.romanbrunner.apps.fitnesstracker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity
public class Exercise
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "token")
    public String token;

    @ColumnInfo(name = "remarks")
    public String remarks;

    @ColumnInfo(name = "repeats")
    public int repeats;

    @ColumnInfo(name = "weight")
    public float weight;

    @ColumnInfo(name = "done")
    public boolean done;

    @Ignore
    Exercise(String name, String token, int repeats, float weight)
    {
        this.name = name;
        this.token = token;
        this.repeats = repeats;
        this.weight = weight;
        done = false;
    }
    Exercise(String name, String token, int repeats, float weight, String remarks)
    {
        this(name, token, repeats, weight);
        this.remarks = remarks;
    }
}