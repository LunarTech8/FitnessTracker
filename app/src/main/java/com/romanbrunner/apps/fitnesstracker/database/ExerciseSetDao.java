/*-------------------------------------------------------------
// Description:
Dao = Data access object.
Interface that defines how the Exercise database in AppDatabase can be accessed.
The function implementations are generated by Room with the annotation settings.
(https://developer.android.com/training/data-storage/room/accessing-data.html)
-------------------------------------------------------------*/
package com.romanbrunner.apps.fitnesstracker.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;


@Dao
public interface ExerciseSetDao
{
    // --------------------
    // Functional code
    // --------------------

    @Query("SELECT * FROM exerciseSets")
    LiveData<List<ExerciseSetEntity>> loadAll();

    @Query("SELECT * FROM exerciseSets WHERE workoutUnitDate = :searchWorkoutUnitDate")
    LiveData<List<ExerciseSetEntity>> loadByWorkoutUnitDate(Date searchWorkoutUnitDate);

    @Insert
    void insert(List<ExerciseSetEntity> exercises);

    @Update
    void update(List<ExerciseSetEntity> exercises);
}