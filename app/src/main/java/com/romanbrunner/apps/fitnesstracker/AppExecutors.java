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

    private final Executor mDiskIO;
    private final Executor mNetworkIO;
    private final Executor mMainThread;

    public Executor diskIO()  // TODO: rename to get...
    {
        return mDiskIO;
    }

    public Executor networkIO()  // TODO: rename to get...
    {
        return mNetworkIO;
    }

    public Executor mainThread()  // TODO: rename to get...
    {
        return mMainThread;
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
        this.mDiskIO = diskIO;
        this.mNetworkIO = networkIO;
        this.mMainThread = mainThread;
    }
    public AppExecutors()
    {
        this(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(3), new MainThreadExecutor());
    }
}