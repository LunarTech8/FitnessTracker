package com.romanbrunner.apps.fitnesstracker;

import android.app.Application;

import com.romanbrunner.apps.fitnesstracker.Database.AppDatabase;


public class BasicApp extends Application
{
    // --------------------
    // Functional code
    // --------------------

    private AppExecutors mAppExecutors;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mAppExecutors = new AppExecutors();
    }

    public AppDatabase getDatabase()
    {
        return AppDatabase.getInstance(this, mAppExecutors);
    }

    public DataRepository getRepository()
    {
        return DataRepository.getInstance(getDatabase());
    }
}