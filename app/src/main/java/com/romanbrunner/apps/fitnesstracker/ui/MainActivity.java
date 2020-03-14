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
import com.romanbrunner.apps.fitnesstracker.database.WorkoutInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutUnitEntity;
import com.romanbrunner.apps.fitnesstracker.viewmodels.MainViewModel;
import com.romanbrunner.apps.fitnesstracker.databinding.WorkoutScreenBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

    private static boolean isExerciseInfoLoading = true;
    private static boolean isExerciseSetsLoading = true;
    private static boolean isWorkoutInfoLoading = true;
    private static boolean isWorkoutUnitsLoading = true;

    private ExerciseInfoAdapter adapter;
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
        adapter = new ExerciseInfoAdapter();
        binding.exercisesBoard.setAdapter(adapter);
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
//            viewModel.setExerciseInfo(adapter.getUpdatedExerciseInfo());  // DEBUG: should probably called on finishButton click instead
            isEditModeActive = !isEditModeActive;
            binding.setIsEditModeActive(isEditModeActive);
            adapter.reloadViews();
        });
        binding.debugLogButton.setOnClickListener((View view) -> viewModel.printDebugLog(this));  // Button only visible in debugging build
        binding.debugResetButton.setOnClickListener((View view) -> viewModel.removeDebugWorkoutUnits());  // Button only visible in debugging build
        subscribeUi(viewModel);
    }

    // Update the layout binding when the data in the view model changes:
    private void subscribeUi(final MainViewModel viewModel)
    {
        // Info entries:
        viewModel.getWorkoutInfo().observe(this, (@Nullable List<WorkoutInfoEntity> workoutInfoList) ->
        {
            if (workoutInfoList != null)
            {
                isWorkoutInfoLoading = false;
                if (!isWorkoutUnitsLoading)
                {
                    final String name = binding.getWorkoutUnit().getWorkoutInfoName();
                    final int version = binding.getWorkoutUnit().getWorkoutInfoVersion();
                    for (WorkoutInfoEntity workoutInfo: workoutInfoList)
                    {
                        if (Objects.equals(workoutInfo.getName(), name) && workoutInfo.getVersion() == version)
                        {
                            binding.setWorkoutInfo(workoutInfo);
                            break;
                        }
                    }
                }
            }
            else
            {
                isWorkoutInfoLoading = true;
            }
            binding.setIsWorkoutLoading(isWorkoutInfoLoading || isWorkoutUnitsLoading);
            binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
        });
        viewModel.getExerciseInfo().observe(this, (@Nullable List<ExerciseInfoEntity> exerciseInfoList) ->
        {
            if (exerciseInfoList != null)
            {
                isExerciseInfoLoading = false;
                adapter.setExerciseInfo(exerciseInfoList);
            }
            else
            {
                isExerciseInfoLoading = true;
            }
            binding.setIsExercisesLoading(isExerciseInfoLoading || isExerciseSetsLoading);
            binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
        });
        // Current entries:
        viewModel.getCurrentWorkoutUnit().observe(this, (@Nullable WorkoutUnitEntity workoutUnit) ->
        {
            if (workoutUnit != null)
            {
                isWorkoutUnitsLoading = false;
                binding.setWorkoutUnit(workoutUnit);
                List<WorkoutInfoEntity> workoutInfoList = viewModel.getWorkoutInfo().getValue();
                if (!isWorkoutInfoLoading && workoutInfoList != null)
                {
                    final String name = workoutUnit.getWorkoutInfoName();
                    final int version = workoutUnit.getWorkoutInfoVersion();
                    for (WorkoutInfoEntity workoutInfo: workoutInfoList)
                    {
                        if (Objects.equals(workoutInfo.getName(), name) && workoutInfo.getVersion() == version)
                        {
                            binding.setWorkoutInfo(workoutInfo);
                            break;
                        }
                    }
                }
            }
            else
            {
                isWorkoutUnitsLoading = true;
            }
            binding.setIsWorkoutLoading(isWorkoutInfoLoading || isWorkoutUnitsLoading);
            binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
        });
        viewModel.getCurrentExerciseSets().observe(this, (@Nullable List<ExerciseSetEntity> exerciseSetList) ->
        {
            if (exerciseSetList != null)
            {
                isExerciseSetsLoading = false;
                adapter.setExerciseSets(exerciseSetList);
            }
            else
            {
                isExerciseSetsLoading = true;
            }
            binding.setIsExercisesLoading(isExerciseInfoLoading || isExerciseSetsLoading);
            binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
        });
        // Entries for statistics:
        viewModel.getLastWorkoutUnit().observe(this, (@Nullable WorkoutUnitEntity workoutUnit) ->
        {
            if (workoutUnit != null)
            {
                binding.setLastWorkoutDate(SimpleDateFormat.getDateInstance().format(workoutUnit.getDate()));
                binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
            }
        });
        viewModel.getAllWorkoutUnits().observe(this, (@Nullable List<WorkoutUnitEntity> workoutUnits) ->
        {
            if (workoutUnits != null)
            {
                float averageInterval = 0F;
                for (int i = 1; i < workoutUnits.size() - 1; i++)  // Start with second entry for diff and skip last entry because it isn't finished
                {
                    averageInterval += TimeUnit.DAYS.convert(workoutUnits.get(i).getDate().getTime() - workoutUnits.get(i - 1).getDate().getTime(), TimeUnit.MILLISECONDS);
                }
                binding.setAverageInterval(String.format(Locale.getDefault(), "%.2f", averageInterval / (workoutUnits.size() - 2)));
                binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
            }
        });
    }
}