package com.romanbrunner.apps.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.ExerciseSet;

import java.util.Objects;


@Entity(tableName = "exerciseSets",
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
public class ExerciseSetEntity implements ExerciseSet
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

    public ExerciseSetEntity() {}
    @Ignore
    public ExerciseSetEntity(int workoutId, String exerciseInfoName, int repeats, float weight)
    {
        this.workoutId = workoutId;
        this.exerciseInfoName = exerciseInfoName;
        this.repeats = repeats;
        this.weight = weight;
        done = false;
    }
    @Ignore
    public ExerciseSetEntity(ExerciseSet exerciseSet, int workoutId)
    {
        this(workoutId, exerciseSet.getExerciseInfoName(), exerciseSet.getRepeats(), exerciseSet.getWeight());
    }

    public static boolean isContentTheSame(ExerciseSet exerciseSetA, ExerciseSet exerciseSetB)
    {
        return exerciseSetA.getId() == exerciseSetB.getId()
                && exerciseSetA.isDone() == exerciseSetB.isDone()
                && exerciseSetA.getRepeats() == exerciseSetB.getRepeats()
                && Float.compare(exerciseSetA.getWeight(), exerciseSetB.getWeight()) == 0
                && exerciseSetA.getWorkoutId() == exerciseSetB.getWorkoutId()
                && Objects.equals(exerciseSetA.getExerciseInfoName(), exerciseSetB.getExerciseInfoName());
    }
}