package com.romanbrunner.apps.fitnesstracker.Viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.romanbrunner.apps.fitnesstracker.BasicApp;
import com.romanbrunner.apps.fitnesstracker.DataRepository;


public class ExerciseViewModel extends AndroidViewModel
{
    // --------------------
    // Functional code
    // --------------------

    public ExerciseViewModel(@NonNull Application application, DataRepository repository)
    {
        super(application);
    }

    /**
     * A creator is used to inject the product ID into the ViewModel
     * <p>
     * This creator is to showcase how to inject dependencies into ViewModels. It's not
     * actually necessary in this case, as the product ID can be passed in a public method.
     */
    // TODO: replace/simplify
    public static class Factory extends ViewModelProvider.NewInstanceFactory
    {

        @NonNull
        private final Application mApplication;

        private final DataRepository mRepository;

        public Factory(@NonNull Application application)
        {
            mApplication = application;
            mRepository = ((BasicApp)application).getRepository();
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass)
        {
            return (T) new ExerciseViewModel(mApplication, mRepository);
        }
    }
}