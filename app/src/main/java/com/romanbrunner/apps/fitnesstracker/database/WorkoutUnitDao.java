/*-------------------------------------------------------------
// Description:
Dao = Data access object.
Interface that defines how the Workout database in AppDatabase can be accessed.
The function implementations are generated by Room with the annotation settings.
(https://developer.android.com/training/data-storage/room/accessing-data.html)
-------------------------------------------------------------*/
package com.romanbrunner.apps.fitnesstracker.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface WorkoutUnitDao
{
    // --------------------
    // Functional code
    // --------------------

    @Query("SELECT * FROM WorkoutUnits ORDER BY date DESC LIMIT 1")
    LiveData<WorkoutUnitEntity> loadNewest();

    @Query("SELECT * FROM WorkoutUnits WHERE studio = :searchStudio ORDER BY date DESC LIMIT 1")
    LiveData<WorkoutUnitEntity> loadNewestByStudio(String searchStudio);

    @Query("SELECT * FROM WorkoutUnits WHERE studio = :searchStudio AND name = :searchName ORDER BY date DESC LIMIT 1")
    LiveData<WorkoutUnitEntity> loadNewestByStudioAndName(String searchStudio, String searchName);

    @Query("SELECT * FROM WorkoutUnits WHERE date < (SELECT MAX(date) FROM WorkoutUnits) ORDER BY date DESC LIMIT 1")
    LiveData<WorkoutUnitEntity> loadLast();

    @Query("SELECT * FROM WorkoutUnits WHERE studio = :searchStudio")
    LiveData<List<WorkoutUnitEntity>> loadAllByStudio(String searchStudio);

    @Query("SELECT * FROM WorkoutUnits WHERE studio = :searchStudio AND name = :searchName")
    LiveData<List<WorkoutUnitEntity>> loadAllByStudioAndName(String searchStudio, String searchName);

    @Query("SELECT * FROM WorkoutUnits")
    LiveData<List<WorkoutUnitEntity>> loadAll();

    @Insert
    void insert(WorkoutUnitEntity... workoutUnits);

    @Update
    void update(WorkoutUnitEntity... workoutUnits);

    @Delete
    void delete(WorkoutUnitEntity... workoutUnit);
    @Delete
    void delete(List<WorkoutUnitEntity> workoutUnits);
}