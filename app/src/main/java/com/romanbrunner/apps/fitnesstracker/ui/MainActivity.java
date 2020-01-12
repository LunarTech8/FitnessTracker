package com.romanbrunner.apps.fitnesstracker.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.View;

import com.romanbrunner.apps.fitnesstracker.database.ExerciseEntity;
import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutEntity;
import com.romanbrunner.apps.fitnesstracker.viewmodels.MainViewModel;
import com.romanbrunner.apps.fitnesstracker.databinding.ActivityMainBinding;

import java.util.List;


public class MainActivity extends AppCompatActivity
{
    // --------------------
    // Functional code
    // --------------------

    private RecyclerViewAdapter adapter;
    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)  // Is called every time the activity is recreated (eg. when rotating the screen)
    {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LightTheme);  // TODO: make "R.style.DarkTheme" work

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        adapter = new RecyclerViewAdapter();
        binding.recyclerView.setAdapter(adapter);
        // TODO: check if needed
//        binding.recyclerView.setHasFixedSize(true);
//        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Recreated activities receive the same ExerciseListViewModel instance created by the first activity.
        // "this" activity is the ViewModelStoreOwner of the view model, thus the recycling is linked between these two.
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding.finishButton.setOnClickListener((View view) ->
        {
            viewModel.finishExercises();
            adapter.notifyDataSetChanged();
        });
        binding.debugButton.setOnClickListener((View view) -> viewModel.printDebugLog(this));  // DEBUG: Button only visible in debug build
        subscribeUi(viewModel);
    }

    @Override
    protected void onPause()
    {
        viewModel.saveCurrentData();
        super.onPause();
    }

    private void subscribeUi(final MainViewModel viewModel)
    {
        // Update the layout binding when the data in the view model changes:
        viewModel.getCurrentWorkout().observe(this, (@Nullable WorkoutEntity workout) ->
        {
            if (workout != null)
            {
                binding.setIsWorkoutLoading(false);
                binding.setWorkout(workout);  // Update displayed data
            }
            else
            {
                binding.setIsWorkoutLoading(true);
            }
            binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
        });
        viewModel.getCurrentExercises().observe(this, (@Nullable List<ExerciseEntity> exercises) ->
        {
            if (exercises != null)
            {
                binding.setIsExercisesLoading(false);
                adapter.setExercises(exercises);  // Update displayed data
            }
            else
            {
                binding.setIsExercisesLoading(true);
            }
            binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
        });
    }
}