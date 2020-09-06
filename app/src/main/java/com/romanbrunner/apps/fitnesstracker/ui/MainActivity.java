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
import java.util.Iterator;
import java.util.LinkedHashSet;
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

    public static final boolean DEBUG_MODE_ACTIVE = false;
    public static final int DEBUG_WORKOUT_MIN_ID = 10000;
    public static final int DEBUG_LOG_MAX_MODES = 5;


    // --------------------
    // Functional code
    // --------------------

    public static boolean isEditModeActive = false;
    public static int debugLogMode = 4;

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
        binding.setIsDebugModeActive(DEBUG_MODE_ACTIVE);
        binding.nextStudioButton.setOnClickListener((View view) -> {});  // TODO: implement
        binding.workoutInfoButton.setOnClickListener((View view) -> binding.setIsTopBoxMinimized(!binding.getIsTopBoxMinimized()));
        binding.nextWorkoutButton.setOnClickListener((View view) -> DataRepository.executeOnceForLiveData(viewModel.getAllWorkoutInfo(), allWorkoutInfo ->
        {
            if (allWorkoutInfo == null) throw new AssertionError("object cannot be null");
            final WorkoutInfoEntity currentWorkoutInfo = (WorkoutInfoEntity)binding.getWorkoutInfo();
            // Get all workout names:
            Set<String> workoutNames = new LinkedHashSet<>();
            for (WorkoutInfoEntity workoutInfo: allWorkoutInfo)
            {
                workoutNames.add(workoutInfo.getName());  // Duplicate names will automatically be ignored in a Set
            }
            // Get new workout name:
            String newWorkoutName = currentWorkoutInfo.getName();
            Iterator<String> iterator = workoutNames.iterator();
            while(iterator.hasNext())
            {
                if (Objects.equals(iterator.next(), currentWorkoutInfo.getName()))
                {
                    if (iterator.hasNext())
                    {
                        newWorkoutName = iterator.next();
                    }
                    else
                    {
                        newWorkoutName = workoutNames.iterator().next();
                    }
                    break;
                }
            }
            Log.d("onCreate", "new workout info name: " + newWorkoutName);  // DEBUG:
            // Change to new workout:
            DataRepository.executeOnceForLiveData(viewModel.getNewestWorkoutInfo(currentWorkoutInfo.getStudio(), newWorkoutName), newWorkoutInfo ->
            {
                if (newWorkoutInfo == null) throw new AssertionError("object cannot be null");
                DataRepository.executeOnceForLiveData(viewModel.changeWorkout(newWorkoutInfo), newWorkoutUnit ->
                {
                    if (newWorkoutUnit == null) throw new AssertionError("object cannot be null");
                    binding.setWorkoutInfo(newWorkoutInfo);
                    binding.setWorkoutUnit(newWorkoutUnit);
                    binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
                });
            });
        }));
        binding.editModeButton.setOnClickListener((View view) ->
        {
            isEditModeActive = !isEditModeActive;
            binding.setIsEditModeActive(isEditModeActive);
            adapter.notifyDataSetChanged();
            if (!isEditModeActive)
            {
                WorkoutUnitEntity workoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
                // Get newest workout info version:
                DataRepository.executeOnceForLiveData(viewModel.getNewestWorkoutInfo(workoutUnit.getWorkoutInfoStudio(), workoutUnit.getWorkoutInfoName()), newestWorkoutInfo ->
                {
                    final WorkoutInfoEntity workoutInfo = (WorkoutInfoEntity)binding.getWorkoutInfo();
                    // Check for requirement of a new version:
                    final String newExerciseNames = WorkoutInfoEntity.exerciseSets2exerciseNames(adapter.getExerciseSets());
                    int newVersion = -1;
                    if (newestWorkoutInfo == null)
                    {
                        newVersion = 0;
                    }
                    else if (!Objects.equals(newExerciseNames, workoutInfo.getExerciseNames()))
                    {
                        newVersion = newestWorkoutInfo.getVersion() + 1;
                    }
                    // Create new workout info version if required:
                    if (newVersion >= 0)
                    {
                        Log.d("onCreate", "old exercise info names: " + workoutInfo.getExerciseNames());  // DEBUG:
                        Log.d("onCreate", "new exercise info names: " + newExerciseNames);  // DEBUG:
                        // Adjust workout info:
                        workoutInfo.setVersion(newVersion);
                        workoutInfo.setExerciseNames(newExerciseNames);
                        binding.setWorkoutInfo(workoutInfo);
                        Log.d("onCreate", "new workout info version created: V" + workoutInfo.getVersion());  // DEBUG:
                        // Adjust workout unit:
                        workoutUnit.setWorkoutInfoVersion(newVersion);
                    }
                    // Store current info data:
                    viewModel.storeWorkoutInfo(Collections.singletonList(workoutInfo));
                    viewModel.storeExerciseInfo(adapter.getExerciseInfo());
                });
            }
            binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
        });
        binding.addExerciseButton.setOnClickListener((View view) ->
        {
            // Create new exercise:
            String newExerciseName = "NewExerciseName";
            final List<ExerciseInfoEntity> newExerciseInfoList = adapter.getExerciseInfo();
            int namePostfixCounter = 1;
            boolean nameNotFound = true;
            while (nameNotFound)
            {
                nameNotFound = false;
                for (ExerciseInfoEntity exerciseInfo: newExerciseInfoList)
                {
                    if (Objects.equals(exerciseInfo.getName(), newExerciseName + namePostfixCounter))
                    {
                        nameNotFound = true;
                        namePostfixCounter++;
                        break;
                    }
                }
            }
            newExerciseName += namePostfixCounter;
            newExerciseInfoList.add(new ExerciseInfoEntity(newExerciseName));
            final List<ExerciseSetEntity> newExerciseSetsList = adapter.getExerciseSets();
            newExerciseSetsList.add(new ExerciseSetEntity(Objects.requireNonNull(viewModel.getCurrentWorkoutUnit().getValue()).getId(), newExerciseName, ExerciseSetAdapter.WEIGHTED_EXERCISE_REPEATS_MIN, 0F));
            // Add new exercise to workout info and exercise adapter:
            final WorkoutInfoEntity workoutInfo = (WorkoutInfoEntity)binding.getWorkoutInfo();
            Log.d("onCreate", "old exercise info names: " + workoutInfo.getExerciseNames());  // DEBUG:
            workoutInfo.setExerciseNames(WorkoutInfoEntity.exerciseSets2exerciseNames(newExerciseSetsList));
            adapter.setExercise(workoutInfo.getExerciseNames(), newExerciseInfoList, newExerciseSetsList);
            Log.d("onCreate", "new exercise info names: " + binding.getWorkoutInfo().getExerciseNames());  // DEBUG:
            // TODO: the view should be scrolled to the bottom so that the new exercise entry is shown
        });
        binding.finishButton.setOnClickListener((View view) ->
        {
            final List<ExerciseInfoEntity> exerciseInfo = adapter.getExerciseInfo();
            final List<ExerciseSetEntity> orderedExerciseSets = adapter.getExerciseSets();
            viewModel.updateExerciseInfo(exerciseInfo, orderedExerciseSets);
            viewModel.storeExerciseInfo(exerciseInfo);
            viewModel.finishWorkout((WorkoutUnitEntity)binding.getWorkoutUnit(), orderedExerciseSets);
        });
        subscribeUi(viewModel);

        // Add debugging button listeners:
        // (Buttons only visible in debugging build)
        binding.debugPrintLogButton.setOnClickListener((View view) -> viewModel.printDebugLog());
        binding.debugNextLogModeButton.setOnClickListener((View view) ->
        {
            if (++debugLogMode > DEBUG_LOG_MAX_MODES) { debugLogMode = 0; }
        });
        binding.debugRemoveWorkoutUnitsButton.setOnClickListener((View view) -> viewModel.removeDebugWorkoutUnits());
        binding.debugResetWorkoutButton.setOnClickListener((View view) ->
        {
            viewModel.removeDebugWorkoutUnits();
            final WorkoutInfoEntity workoutInfo = (WorkoutInfoEntity)binding.getWorkoutInfo();
            viewModel.resetWorkoutInfo(workoutInfo.getStudio(), workoutInfo.getName());
        });
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

                DataRepository.executeOnceForLiveData(viewModel.getWorkoutInfo(workoutUnit.getWorkoutInfoStudio(), workoutUnit.getWorkoutInfoName(), workoutUnit.getWorkoutInfoVersion()), workoutInfo ->
                {
                    if (workoutInfo == null) throw new AssertionError("object cannot be null");
                    Log.d("subscribeUi", "current getWorkoutInfo exercise info names: " + workoutInfo.getExerciseNames());  // DEBUG:
                    binding.setWorkoutInfo(workoutInfo);
                    DataRepository.executeOnceForLiveData(viewModel.getExerciseSets(workoutUnit), exerciseSetList -> exerciseSetList != null && !exerciseSetList.isEmpty(), exerciseSetList ->
                    {
                        if (exerciseSetList == null) throw new AssertionError("object cannot be null");
                        Log.d("subscribeUi", "current getExerciseSets: " + exerciseSetList.stream().map(ExerciseSetEntity::getExerciseInfoName).collect(Collectors.joining(", ")));  // DEBUG:
                        DataRepository.executeOnceForLiveData(viewModel.getExerciseInfo(exerciseSetList), exerciseInfoList ->
                        {
                            if (exerciseInfoList == null) throw new AssertionError("object cannot be null");
                            Log.d("subscribeUi", "current getExerciseInfo exercise info names: " + exerciseInfoList.stream().map(ExerciseInfoEntity::getName).collect(Collectors.joining(", ")));  // DEBUG:
                            adapter.setExercise(binding.getWorkoutInfo().getExerciseNames(), exerciseInfoList, exerciseSetList);
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