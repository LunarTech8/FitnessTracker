package com.romanbrunner.apps.fitnesstracker.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.ExerciseSet;

import java.util.List;
import java.util.Objects;


@Entity(tableName = "exerciseSets",
        foreignKeys =
        {
            @ForeignKey(entity = WorkoutUnitEntity.class, parentColumns = "id", childColumns = "workoutUnitId", onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = ExerciseInfoEntity.class, parentColumns = "name", childColumns = "exerciseInfoName", onDelete = ForeignKey.RESTRICT)
        },
        indices =
        {
            @Index(value = "workoutUnitId"),
            @Index(value = "exerciseInfoName")
        })
public class ExerciseSetEntity implements ExerciseSet
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int workoutUnitId;
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
    public int getWorkoutUnitId()
    {
        return workoutUnitId;
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
        this.id = id;
    }

    @Override
    public void setWorkoutUnitId(int workoutUnitId)
    {
        this.workoutUnitId = workoutUnitId;
    }

    @Override
    public void setExerciseInfoName(String exerciseInfoName)
    {
        this.exerciseInfoName = exerciseInfoName;
    }

    @Override
    public void setRepeats(int repeats)
    {
        this.repeats = repeats;
    }

    @Override
    public void setWeight(float weight)
    {
        this.weight = weight;
    }

    @Override
    public void setDone(boolean done)
    {
        this.done = done;
    }

    ExerciseSetEntity() {}
    @Ignore
    public ExerciseSetEntity(int workoutUnitId, String exerciseInfoName, int repeats, float weight)
    {
        this.workoutUnitId = workoutUnitId;
        this.exerciseInfoName = exerciseInfoName;
        this.repeats = repeats;
        this.weight = weight;
        done = false;
    }
    @Ignore
    public ExerciseSetEntity(ExerciseSet exerciseSet, int workoutUnitId)
    {
        this(workoutUnitId, exerciseSet.getExerciseInfoName(), exerciseSet.getRepeats(), exerciseSet.getWeight());
    }

    public static boolean isContentTheSame(ExerciseSet exerciseSetA, ExerciseSet exerciseSetB)
    {
        return exerciseSetA.getId() == exerciseSetB.getId()
            && exerciseSetA.isDone() == exerciseSetB.isDone()
            && exerciseSetA.getRepeats() == exerciseSetB.getRepeats()
            && Float.compare(exerciseSetA.getWeight(), exerciseSetB.getWeight()) == 0
            && exerciseSetA.getWorkoutUnitId() == exerciseSetB.getWorkoutUnitId()
            && Objects.equals(exerciseSetA.getExerciseInfoName(), exerciseSetB.getExerciseInfoName());
    }
    public static boolean isContentTheSame(List<ExerciseSetEntity> exerciseSetListA, List<ExerciseSetEntity> exerciseSetListB)
    {
        if (exerciseSetListA.size() != exerciseSetListB.size())
        {
            return false;
        }
        for (int i = 0; i < exerciseSetListA.size(); i++)
        {
            if (!isContentTheSame(exerciseSetListA.get(i), exerciseSetListB.get(i)))
            {
                return false;
            }
        }
        return true;
    }
}