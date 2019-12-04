package com.romanbrunner.apps.fitnesstracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;


@Dao
public interface ExerciseDao
{
    // --------------------
    // Functional code
    // --------------------

    @Query("SELECT * FROM exercise")
    List<Exercise> getAll();

    @Query("SELECT * FROM exercise WHERE id IN (:exerciseIds)")
    List<Exercise> loadAllByIds(int[] exerciseIds);

    @Query("SELECT * FROM exercise WHERE name LIKE :searchName AND " + "token LIKE :searchToken LIMIT 1")
    Exercise findByNameAndToken(String searchName, String searchToken);

    @Insert
    void insertAll(Exercise... exercises);

    @Delete
    void delete(Exercise exercise);
}