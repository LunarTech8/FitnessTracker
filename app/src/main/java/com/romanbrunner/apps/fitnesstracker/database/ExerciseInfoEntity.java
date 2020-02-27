package com.romanbrunner.apps.fitnesstracker.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.romanbrunner.apps.fitnesstracker.model.ExerciseInfo;

import java.util.Objects;


@Entity(tableName = "exerciseInfo",
        foreignKeys = {@ForeignKey(entity = WorkoutEntity.class, parentColumns = "id", childColumns = "workoutId", onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "workoutId")}
        )
public class ExerciseInfoEntity implements ExerciseInfo
{
    // --------------------
    // Functional code
    // --------------------

    @PrimaryKey @NonNull
    private String name = "InitNonNullName";
    private int workoutId;
    private String token;
    private String remarks;

    @Override
    public @NonNull String getName()
    {
        return name;
    }

    @Override
    public int getWorkoutId()
    {
        return workoutId;
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
    public void setName(String name)
    {
        if (!Objects.equals(this.name, name)) this.name = name;
    }

    @Override
    public void setWorkoutId(int workoutId)
    {
        if (this.workoutId != workoutId) this.workoutId = workoutId;
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

    public ExerciseInfoEntity() {}
    @Ignore
    public ExerciseInfoEntity(@NonNull String name, int workoutId, String token)
    {
        this.name = name;
        this.token = token;
    }
    @Ignore
    public ExerciseInfoEntity(@NonNull String name, int workoutId, String token, String remarks)
    {
        this(name, workoutId, token);
        this.remarks = remarks;
    }

    public static boolean isContentTheSame(ExerciseInfo exerciseInfoA, ExerciseInfo exerciseInfoB)
    {
        return Objects.equals(exerciseInfoA.getName(), exerciseInfoB.getName())
                && exerciseInfoA.getWorkoutId() == exerciseInfoB.getWorkoutId()
                && Objects.equals(exerciseInfoA.getToken(), exerciseInfoB.getToken())
                && Objects.equals(exerciseInfoA.getRemarks(), exerciseInfoB.getRemarks());
    }
}