package com.romanbrunner.apps.fitnesstracker.Viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.romanbrunner.apps.fitnesstracker.BasicApp;
import com.romanbrunner.apps.fitnesstracker.DataRepository;


public class ExerciseListViewModel extends AndroidViewModel
{
    // --------------------
    // Functional code
    // --------------------

    private final DataRepository mRepository;

    public ExerciseListViewModel(Application application)
    {
        super(application);

        mRepository = ((BasicApp)application).getRepository();
    }
}