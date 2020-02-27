package com.romanbrunner.apps.fitnesstracker.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;

import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutEntity;
import com.romanbrunner.apps.fitnesstracker.viewmodels.MainViewModel;
import com.romanbrunner.apps.fitnesstracker.databinding.WorkoutScreenBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity
{
    // --------------------
    // Data code
    // --------------------

    public static final boolean TEST_MODE_ACTIVE = true;
    public static final int DEBUG_WORKOUT_MIN_ID = 10000;
    public static final int DEBUG_LOG_MODE = 2;  // 0="All workouts and all exercises", 1="Last workout and last exercises", 2="All workouts"


    // --------------------
    // Functional code
    // --------------------

    public static boolean isEditModeActive = false;

    private ExerciseAdapter adapter;
    private WorkoutScreenBinding binding;
    private MainViewModel viewModel;

    /* Is called every time the activity is recreated (eg. when rotating the screen) */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LightTheme);  // TODO: make "R.style.DarkTheme" work
        binding = DataBindingUtil.setContentView(this, R.layout.workout_screen);

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Recreated activities receive the same ExerciseListViewModel instance created by the first activity.
        // "this" activity is the ViewModelStoreOwner of the view model, thus the recycling is linked between these two.
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Setup recycle view adapter:
        adapter = new ExerciseAdapter();
        binding.exercisesBoard.setAdapter(adapter);
        binding.exercisesBoard.setHasFixedSize(true);  // True because recyclerView size shouldn't change because items aren't added/removed
        binding.exercisesBoard.setLayoutManager(new LinearLayoutManager(this));

        // Setup layout data binding and add listeners and observers:
        binding.setIsTopBoxMinimized(true);
        binding.setIsEditModeActive(isEditModeActive);
        binding.setIsTestModeActive(TEST_MODE_ACTIVE);
        binding.nameButton.setOnClickListener((View view) -> binding.setIsTopBoxMinimized(!binding.getIsTopBoxMinimized()));
        binding.finishButton.setOnClickListener((View view) ->
        {
            viewModel.finishExercises();
            adapter.notifyDataSetChanged();
        });
        binding.editModeButton.setOnClickListener((View view) ->
        {
            viewModel.setExerciseInfo(adapter.getUpdatedExerciseInfo());  // DEBUG: should probably called on finishButton click instead

            isEditModeActive = !isEditModeActive;
            binding.setIsEditModeActive(isEditModeActive);
            // TODO: reload adapter
        });
        binding.debugLogButton.setOnClickListener((View view) -> viewModel.printDebugLog(this));  // Button only visible in debugging build
        binding.debugResetButton.setOnClickListener((View view) -> viewModel.removeDebugWorkouts(this));  // Button only visible in debugging build
        subscribeUi(viewModel);
    }

    private void subscribeUi(final MainViewModel viewModel)
    {
        // Update the layout binding when the data in the view model changes:
        viewModel.getCurrentWorkout().observe(this, (@Nullable WorkoutEntity workout) ->
        {
            if (workout != null)
            {
                binding.setIsWorkoutLoading(false);
                binding.setWorkout(workout);
            }
            else
            {
                binding.setIsWorkoutLoading(true);
            }
            binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
        });
        viewModel.getLastWorkout().observe(this, (@Nullable WorkoutEntity workout) ->
        {
            if (workout != null)
            {
                binding.setLastWorkoutDate(SimpleDateFormat.getDateInstance().format(workout.getDate()));
                binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
            }
        });
        viewModel.getAllWorkouts().observe(this, (@Nullable List<WorkoutEntity> workouts) ->
        {
            if (workouts != null)
            {
                float averageInterval = 0F;
                for (int i = 1; i < workouts.size() - 1; i++)  // Start with second entry for diff and skip last entry because it isn't finished
                {
                    averageInterval += TimeUnit.DAYS.convert(workouts.get(i).getDate().getTime() - workouts.get(i - 1).getDate().getTime(), TimeUnit.MILLISECONDS);
                }
                binding.setAverageInterval(String.format(Locale.getDefault(), "%.2f", averageInterval / (workouts.size() - 2)));
                binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
            }
        });
        viewModel.getCurrentExerciseInfo().observe(this, (@Nullable List<ExerciseInfoEntity> exerciseInfoList) ->
        {
            if (exerciseInfoList != null)
            {
                binding.setIsExercisesLoading(!adapter.setExerciseInfo(exerciseInfoList));
            }
            else
            {
                binding.setIsExercisesLoading(true);
            }
            binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
        });
        viewModel.getCurrentExerciseSets().observe(this, (@Nullable List<ExerciseSetEntity> exerciseSets) ->
        {
            if (exerciseSets != null)
            {
                binding.setIsExercisesLoading(!adapter.setExerciseSets(exerciseSets));
            }
            else
            {
                binding.setIsExercisesLoading(true);
            }
            binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
        });
    }
}