package com.romanbrunner.apps.fitnesstracker;

import androidx.room.Database;
import androidx.room.RoomDatabase;


@Database(entities = {Exercise.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase
{
    // --------------------
    // Functional code
    // --------------------

    public abstract ExerciseDao exerciseDao();
}