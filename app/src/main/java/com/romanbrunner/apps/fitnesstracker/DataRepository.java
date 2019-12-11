package com.romanbrunner.apps.fitnesstracker;


import com.romanbrunner.apps.fitnesstracker.Database.AppDatabase;

public class DataRepository
{
    // --------------------
    // Functional code
    // --------------------

    private static DataRepository instance;
    private final AppDatabase mDatabase;

    private DataRepository(final AppDatabase database)
    {
        mDatabase = database;
    }

    public static DataRepository getInstance(final AppDatabase database)
    {
        if (instance == null)
        {
            synchronized (DataRepository.class)
            {
                if (instance == null)
                {
                    instance = new DataRepository(database);
                }
            }
        }
        return instance;
    }
}