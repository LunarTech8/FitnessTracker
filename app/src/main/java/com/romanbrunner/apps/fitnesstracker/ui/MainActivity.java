package com.romanbrunner.apps.fitnesstracker.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.romanbrunner.apps.fitnesstracker.DataRepository;
import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutUnitEntity;
import com.romanbrunner.apps.fitnesstracker.databinding.WorkoutScreenBinding;
import com.romanbrunner.apps.fitnesstracker.viewmodels.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity
{
    // --------------------
    // Data code
    // --------------------

    public static final boolean TEST_MODE_ACTIVE = true;
    public static final int DEBUG_WORKOUT_MIN_ID = 10000;
    public static final int DEBUG_LOG_MODE = 4;


    // --------------------
    // Functional code
    // --------------------

    public static boolean isEditModeActive = false;

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
            WorkoutUnitEntity workoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
            WorkoutInfoEntity workoutInfo = (WorkoutInfoEntity)binding.getWorkoutInfo();
            // Get newest workout info version:
            DataRepository.executeOnceForLiveData(viewModel.getNewestWorkoutInfo(workoutUnit.getWorkoutInfoName()), newestWorkoutInfo ->
            {
                assert newestWorkoutInfo != null;
                List<ExerciseInfoEntity> exerciseInfo = adapter.getExerciseInfo();
                // Check if a new workout info version is required:
                String exerciseInfoNames = exerciseInfo.stream().map(ExerciseInfoEntity::getName).collect(Collectors.joining(";")) + ";";
                if (!Objects.equals(exerciseInfoNames, workoutInfo.getExerciseInfoNames()))
                {
                    Log.d("onCreate", "old exercise info names: " + workoutInfo.getExerciseInfoNames());  // DEBUG:
                    Log.d("onCreate", "new exercise info names: " + exerciseInfoNames);  // DEBUG:
                    int newVersion = newestWorkoutInfo.getVersion() + 1;
                    // Adjust workout info:
                    workoutInfo.setVersion(newVersion);
                    workoutInfo.setExerciseInfoNames(exerciseInfoNames);
                    Log.d("onCreate", "new workout info version created: V" + workoutInfo.getVersion());  // DEBUG:
                    // Adjust workout unit:
                    workoutUnit.setWorkoutInfoVersion(newVersion);
                }
                // Store current info data:
                viewModel.storeWorkoutInfo(Collections.singletonList(workoutInfo));
                viewModel.storeExerciseInfo(exerciseInfo);
                // Finish workout:
                viewModel.finishWorkout(workoutUnit, adapter.getExerciseSets());
            });
        });
        binding.nextWorkoutButton.setOnClickListener((View view) ->
        {
            // TODO: implement, maybe instead make a changeWorkout dropdown or similar (with name and version as separate sliders)
        });
        binding.editModeButton.setOnClickListener((View view) ->
        {
            isEditModeActive = !isEditModeActive;
            binding.setIsEditModeActive(isEditModeActive);
            adapter.notifyDataSetChanged();
        });
        binding.debugLogButton.setOnClickListener((View view) -> viewModel.printDebugLog());  // Button only visible in debugging build
        binding.debugResetButton.setOnClickListener((View view) -> viewModel.removeDebugWorkoutUnits());  // Button only visible in debugging build
        subscribeUi(viewModel);
    }

    // Update the layout binding when the data in the view model changes:
    private void subscribeUi(final MainViewModel viewModel)
    {
        // Current entry:
        viewModel.getCurrentWorkoutUnit().observe(this, (@Nullable WorkoutUnitEntity workoutUnit) ->
        {
            if (workoutUnit != null)
            {
                Log.d("subscribeUi", "getCurrentWorkoutUnit observed: " + workoutUnit.getWorkoutInfoName() + " V" + workoutUnit.getWorkoutInfoVersion());  // DEBUG: for sortExerciseInfo new exerciseInfo name not found
                binding.setWorkoutUnit(workoutUnit);

                DataRepository.executeOnceForLiveData(viewModel.getWorkoutInfo(workoutUnit.getWorkoutInfoName(), workoutUnit.getWorkoutInfoVersion()), workoutInfo ->
                {
                    assert workoutInfo != null;
                    Log.d("subscribeUi", "current getWorkoutInfo exercise info names: " + workoutInfo.getExerciseInfoNames());  // DEBUG:
                    binding.setWorkoutInfo(workoutInfo);
                    DataRepository.executeOnceForLiveData(viewModel.getExerciseSets(workoutUnit), exerciseSetList -> exerciseSetList != null && !exerciseSetList.isEmpty(), exerciseSetList ->
                    {
                        assert exerciseSetList != null;
                        Log.d("subscribeUi", "current getExerciseSets: " + exerciseSetList.stream().map(ExerciseSetEntity::getExerciseInfoName).collect(Collectors.joining(", ")));  // DEBUG:
                        Set<String> exerciseInfoNames = new HashSet<>();
                        for (ExerciseSetEntity exerciseSet: exerciseSetList)
                        {
                            exerciseInfoNames.add(exerciseSet.getExerciseInfoName());
                        }
                        DataRepository.executeOnceForLiveData(viewModel.getExerciseInfo(exerciseInfoNames), exerciseInfoList ->
                        {
                            assert exerciseInfoList != null;
                            Log.d("subscribeUi", "current getExerciseInfo exercise info names: " + exerciseInfoList.stream().map(ExerciseInfoEntity::getName).collect(Collectors.joining(", ")));  // DEBUG:
                            adapter.setExercise(binding.getWorkoutInfo().getExerciseInfoNames(), exerciseInfoList, exerciseSetList);
                            binding.setIsWorkoutLoading(false);
                            binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
                        });
                    });
                });
            }
            else
            {
                binding.setIsWorkoutLoading(true);
                binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
            }
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