package com.romanbrunner.apps.fitnesstracker.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;


@Entity(tableName = "exercises")
public class ExerciseEntity
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey(autoGenerate = true)
    public int id;

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

    ExerciseEntity() {}
    @Ignore
    ExerciseEntity(String name, String token, int repeats, float weight)
    {
        this.name = name;
        this.token = token;
        this.repeats = repeats;
        this.weight = weight;
        done = false;
    }
    @Ignore
    ExerciseEntity(String name, String token, int repeats, float weight, String remarks)
    {
        this(name, token, repeats, weight);
        this.remarks = remarks;
    }

    public static boolean isContentTheSame(ExerciseEntity exerciseA, ExerciseEntity exerciseB)
    {
        return exerciseA.id == exerciseB.id
            && Objects.equals(exerciseA.name, exerciseB.name)
            && Objects.equals(exerciseA.token, exerciseB.token)
            && Objects.equals(exerciseA.remarks, exerciseB.remarks)
            && exerciseA.repeats == exerciseB.repeats
            && Float.compare(exerciseA.weight, exerciseB.weight) == 0
            && exerciseA.done == exerciseB.done;
    }
}