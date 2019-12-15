package com.romanbrunner.apps.fitnesstracker;

import android.app.Application;

import com.romanbrunner.apps.fitnesstracker.Database.AppDatabase;


public class BasicApp extends Application
{
    // --------------------
    // Functional code
    // --------------------

    private AppExecutors appExecutors;

    @Override
    public void onCreate()
    {
        super.onCreate();
        appExecutors = new AppExecutors();
    }

    public AppDatabase getDatabase()
    {
        return AppDatabase.getInstance(this, appExecutors);
    }

    public DataRepository getRepository()
    {
        return DataRepository.getInstance(getDatabase());
    }
}