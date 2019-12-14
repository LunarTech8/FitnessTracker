/*-------------------------------------------------------------
// Description:
Dao = Data access object.
Interface that defines how the Exercise database in AppDatabase can be accessed.
The function implementations are generated by Room with the annotation settings.
(https://developer.android.com/training/data-storage/room/accessing-data.html)
-------------------------------------------------------------*/
package com.romanbrunner.apps.fitnesstracker.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface ExerciseDao
{
    // --------------------
    // Functional code
    // --------------------

    @Query("SELECT * FROM exercises")
    LiveData<List<ExerciseEntity>> loadAll();

    @Query("SELECT * FROM exercises WHERE id LIKE :searchId LIMIT 1")
    LiveData<ExerciseEntity> loadById(int searchId);

    @Query("SELECT * FROM exercises WHERE id IN (:searchIds)")
    LiveData<List<ExerciseEntity>> loadByIds(int[] searchIds);

    @Query("SELECT * FROM exercises WHERE name LIKE :searchName AND " + "token LIKE :searchToken LIMIT 1")
    LiveData<ExerciseEntity> loadByNameAndToken(String searchName, String searchToken);

    @Insert
    void insert(ExerciseEntity... exercises);

    @Insert
    void insert(List<ExerciseEntity> exercises);

    @Update
    void update(ExerciseEntity... exercises);

    @Delete
    void delete(ExerciseEntity... exercises);
}