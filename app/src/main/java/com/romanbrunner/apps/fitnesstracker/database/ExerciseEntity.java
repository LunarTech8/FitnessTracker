package com.romanbrunner.apps.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.Exercise;

import java.util.Objects;


@Entity(tableName = "exercises",
        foreignKeys =
        {
            @ForeignKey(entity = WorkoutEntity.class, parentColumns = "id", childColumns = "workoutId", onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = ExerciseInfoEntity.class, parentColumns = "name", childColumns = "exerciseInfoName", onDelete = ForeignKey.RESTRICT)
        },
        indices =
        {
            @Index(value = "workoutId"),
            @Index(value = "exerciseInfoName")
        })
public class ExerciseEntity implements Exercise
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int workoutId;
    private String exerciseInfoName;
    private int repeats;
    private float weight;
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
    public String getExerciseInfoName()
    {
        return exerciseInfoName;
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
    public void setExerciseInfoName(String exerciseInfoName)
    {
        if (!Objects.equals(this.exerciseInfoName, exerciseInfoName)) this.exerciseInfoName = exerciseInfoName;
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
    public ExerciseEntity(int workoutId, String exerciseInfoName, int repeats, float weight)
    {
        this.workoutId = workoutId;
        this.exerciseInfoName = exerciseInfoName;
        this.repeats = repeats;
        this.weight = weight;
        done = false;
    }
    @Ignore
    public ExerciseEntity(Exercise exercise, int workoutId)
    {
        this(workoutId, exercise.getExerciseInfoName(), exercise.getRepeats(), exercise.getWeight());
    }

    public static boolean isContentTheSame(Exercise exerciseA, Exercise exerciseB)
    {
        return exerciseA.getId() == exerciseB.getId()
                && exerciseA.isDone() == exerciseB.isDone()
                && exerciseA.getRepeats() == exerciseB.getRepeats()
                && Float.compare(exerciseA.getWeight(), exerciseB.getWeight()) == 0
                && exerciseA.getWorkoutId() == exerciseB.getWorkoutId()
                && Objects.equals(exerciseA.getExerciseInfoName(), exerciseB.getExerciseInfoName());
    }
}