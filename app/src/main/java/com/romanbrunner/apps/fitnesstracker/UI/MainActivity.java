package com.romanbrunner.apps.fitnesstracker.UI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.romanbrunner.apps.fitnesstracker.Database.ExerciseEntity;
import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.Viewmodel.ExerciseListViewModel;

import java.util.List;


public class MainActivity extends AppCompatActivity
{
    // --------------------
    // Functional code
    // --------------------

    public static RecyclerViewAdapter adapter;  // TODO: ugly/unclean public, find a better way

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LightTheme);  // TODO: make "R.style.DarkTheme" work
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null)
        {
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RecyclerViewAdapter();
            recyclerView.setAdapter(adapter);
        }

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Re-created activities receive the same ExerciseListViewModel instance created by the first activity.
        final ExerciseListViewModel viewModel = new ViewModelProvider(this).get(ExerciseListViewModel.class);
        subscribeUi(viewModel.getAllExercises());
    }

    private void subscribeUi(LiveData<List<ExerciseEntity>> liveData)
    {
        // Update the list when the data changes:
        liveData.observe(this, (@Nullable List<ExerciseEntity> exercises) ->
        {
            if (exercises != null)
            {
                adapter.setExercises(exercises);
            }
        });
    }
    // TODO: test if this works or if the one below has to be used
//    private void subscribeUi(LiveData<List<ExerciseEntity>> liveData)
//    {
//        // Update the list when the data changes:
//        liveData.observe(this, new Observer<List<ExerciseEntity>>()
//        {
//            @Override
//            public void onChanged(@Nullable List<ExerciseEntity> exercises)
//            {
//                if (exercises != null)
//                {
//                    adapter.setExercises(exercises);
//                }
//            }
//        });
//    }
}