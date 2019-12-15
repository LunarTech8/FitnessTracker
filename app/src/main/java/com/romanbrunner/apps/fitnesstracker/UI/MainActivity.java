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
import com.romanbrunner.apps.fitnesstracker.ViewModels.ExercisesViewModel;
import com.romanbrunner.apps.fitnesstracker.databinding.ActivityMainBinding;

import java.util.List;


public class MainActivity extends AppCompatActivity
{
    // --------------------
    // Functional code
    // --------------------

    public static RecyclerViewAdapter adapter;  // TODO: ugly/unclean public, find a better way

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)  // Is called every time the activity is recreated (eg. when rotating the screen)
    {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LightTheme);  // TODO: make "R.style.DarkTheme" work
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        if (savedInstanceState == null)
        {
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RecyclerViewAdapter();
            recyclerView.setAdapter(adapter);
        }

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Recreated activities receive the same ExerciseListViewModel instance created by the first activity.
        // "this" activity is the ViewModelStoreOwner of the view model, thus the recycling is linked between these two.
        final ExercisesViewModel viewModel = new ViewModelProvider(this).get(ExercisesViewModel.class);
        subscribeUi(viewModel.getAllExercises());
    }

    private void subscribeUi(LiveData<List<ExerciseEntity>> liveData)
    {
        // Update the list when the data changes:
        liveData.observe(this, (@Nullable List<ExerciseEntity> exercises) ->
        {
            if (exercises != null)
            {
                binding.setIsLoading(false);
                adapter.setExercises(exercises);
            }
            else
            {
                binding.setIsLoading(true);
            }
            // Espresso does not know how to wait for data binding's loop so we execute changes sync:
            binding.executePendingBindings();
        });
    }
}