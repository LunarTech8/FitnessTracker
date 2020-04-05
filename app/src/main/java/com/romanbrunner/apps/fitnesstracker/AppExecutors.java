/*-------------------------------------------------------------
// Description:
Global executor pools for the whole application.
Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind webservice requests).
-------------------------------------------------------------*/
package com.romanbrunner.apps.fitnesstracker;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class AppExecutors
{
    // --------------------
    // Functional code
    // --------------------

    private final Executor diskIO;
    private final Executor networkIO;
    private final Executor mainThread;

    public Executor getDiskIO()
    {
        return diskIO;
    }

    private static class MainThreadExecutor implements Executor
    {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command)
        {
            mainThreadHandler.post(command);
        }
    }

    private AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread)
    {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }
    AppExecutors()
    {
        this(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(3), new MainThreadExecutor());
    }
}