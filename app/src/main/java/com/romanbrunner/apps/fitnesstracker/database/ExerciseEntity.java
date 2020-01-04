package com.romanbrunner.apps.fitnesstracker.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.Exercise;

import java.util.Objects;


@Entity(tableName = "exercises",
        foreignKeys = { @ForeignKey(entity = WorkoutEntity.class, parentColumns = "id", childColumns = "workoutId", onDelete = ForeignKey.CASCADE) },
        indices = { @Index(value = "workoutId") })
public class ExerciseEntity implements Exercise
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "workoutId")
    private int workoutId;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "token")
    private String token;
    @ColumnInfo(name = "remarks")
    private String remarks;
    @ColumnInfo(name = "repeats")
    private int repeats;
    @ColumnInfo(name = "weight")
    private float weight;
    @ColumnInfo(name = "done")
    private boolean done;

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public int getWorkoutId()
    {
        return workoutId;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getToken()
    {
        return token;
    }

    @Override
    public String getRemarks()
    {
        return remarks;
    }

    @Override
    public int getRepeats()
    {
        return repeats;
    }

    @Override
    public float getWeight()
    {
        return weight;
    }

    @Override
    public boolean isDone()
    {
        return done;
    }

    @Override
    public void setId(int id)
    {
        if (this.id != id) this.id = id;
    }

    @Override
    public void setWorkoutId(int workoutId)
    {
        if (this.workoutId != workoutId) this.workoutId = workoutId;
    }

    @Override
    public void setName(String name)
    {
        if (!Objects.equals(this.name, name)) this.name = name;
    }

    @Override
    public void setToken(String token)
    {
        if (!Objects.equals(this.token, token)) this.token = token;
    }

    @Override
    public void setRemarks(String remarks)
    {
        if (!Objects.equals(this.remarks, remarks)) this.remarks = remarks;
    }

    @Override
    public void setRepeats(int repeats)
    {
        if (this.repeats != repeats) this.repeats = repeats;
    }

    @Override
    public void setWeight(float weight)
    {
        if (Float.compare(this.weight, weight) != 0) this.weight = weight;
    }

    @Override
    public void setDone(boolean done)
    {
        if (this.done != done) this.done = done;
    }

    public ExerciseEntity() {}
    @Ignore
    public ExerciseEntity(int workoutId, String name, String token, int repeats, float weight)
    {
        this.workoutId = workoutId;
        this.name = name;
        this.token = token;
        this.repeats = repeats;
        this.weight = weight;
        done = false;
    }
    @Ignore
    public ExerciseEntity(int workoutId, String name, String token, int repeats, float weight, String remarks)
    {
        this(workoutId, name, token, repeats, weight);
        this.remarks = remarks;
    }
    @Ignore
    public ExerciseEntity(Exercise exercise, int workoutId)
    {
        this(workoutId, exercise.getName(), exercise.getToken(), exercise.getRepeats(), exercise.getWeight(), exercise.getRemarks());
    }

    public static boolean isContentTheSame(Exercise exerciseA, Exercise exerciseB)
    {
        return exerciseA.getId() == exerciseB.getId()
            && exerciseA.getWorkoutId() == exerciseB.getWorkoutId()
            && Objects.equals(exerciseA.getName(), exerciseB.getName())
            && Objects.equals(exerciseA.getToken(), exerciseB.getToken())
            && Objects.equals(exerciseA.getRemarks(), exerciseB.getRemarks())
            && exerciseA.getRepeats() == exerciseB.getRepeats()
            && Float.compare(exerciseA.getWeight(), exerciseB.getWeight()) == 0
            && exerciseA.isDone() == exerciseB.isDone();
    }
}