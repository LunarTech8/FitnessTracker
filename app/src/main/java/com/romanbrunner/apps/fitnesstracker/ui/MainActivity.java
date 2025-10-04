package com.romanbrunner.apps.fitnesstracker.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.romanbrunner.apps.fitnesstracker.DataRepository;
import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutUnitEntity;
import com.romanbrunner.apps.fitnesstracker.databinding.WorkoutScreenBinding;
import com.romanbrunner.apps.fitnesstracker.viewmodels.MainViewModel;

import java.text.SimpleDateFormat;
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

    public final static boolean DEBUG_MODE_ACTIVE = true;
    public final static int DEBUG_LOG_MAX_MODES = 7;
    private final static String PREFS_NAME = "GlobalPreferences";


    // --------------------
    // Functional code
    // --------------------

    private ExerciseInfoAdapter adapter;
    private WorkoutScreenBinding binding;
    private MainViewModel viewModel;

    private static int currentThemeId = 0;
    public static boolean isEditModeActive = false;
    public static int debugLogMode = 1;

    private static int determineNamePostfixCounter(List<ExerciseInfoEntity> exerciseInfoList, String exerciseName)
    {
        int namePostfixCounter = 1;
        boolean nameNotFound = true;
        while (nameNotFound)
        {
            nameNotFound = false;
            for (ExerciseInfoEntity exerciseInfo: exerciseInfoList)
            {
                if (Objects.equals(exerciseInfo.getName(), exerciseName + namePostfixCounter))
                {
                    nameNotFound = true;
                    namePostfixCounter++;
                    break;
                }
            }
        }
        return namePostfixCounter;
    }

    private void finishWorkout()
    {
        final List<ExerciseInfoEntity> exerciseInfo = adapter.getExerciseInfo();
        final List<ExerciseSetEntity> orderedExerciseSets = adapter.getExerciseSets();
        viewModel.updateExerciseInfo(exerciseInfo, orderedExerciseSets);
        viewModel.storeExerciseInfo(exerciseInfo);
        viewModel.finishWorkout((WorkoutUnitEntity)binding.getWorkoutUnit(), orderedExerciseSets);
    }

    private void hideKeyboard(View view)
    {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(inputMethodManager).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void updateFinishedExercises()
    {
        final var exercisesDone = adapter.getExerciseSets().stream().filter(ExerciseSetEntity::isDone).count();
        final var exercisesTotal = WorkoutUnitEntity.exerciseNames2Amount(binding.getWorkoutUnit().getExerciseNames());
        if (exercisesDone > exercisesTotal)
        {
            Log.e("updateFinishedExercises", "Counter for finished exercises is invalid (" + exercisesDone + "/" + exercisesTotal + ")");
        }
        binding.setFinishedExercises(String.format(Locale.getDefault(), "%d/%d", exercisesDone, exercisesTotal));
        binding.finishButton.getBackground().clearColorFilter();
        if (exercisesDone == exercisesTotal)
        {
            binding.finishButton.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null), PorterDuff.Mode.MULTIPLY);
        }
    }

    private void updateEditMode()
    {
        binding.setIsEditModeActive(isEditModeActive);
        binding.studioText.setFocusable(isEditModeActive);
        binding.studioText.setEnabled(isEditModeActive);
        binding.studioText.setFocusableInTouchMode(isEditModeActive);
        binding.workoutText.setFocusable(isEditModeActive);
        binding.workoutText.setEnabled(isEditModeActive);
        binding.workoutText.setFocusableInTouchMode(isEditModeActive);
        adapter.notifyDataSetChanged();
        binding.executePendingBindings();
    }

    private void updateTheme()
    {
        switch (currentThemeId)
        {
            default:
                currentThemeId = 0;
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    private void setEditTextFocusInTopBox(View view, boolean hasFocus)
    {
        if (!hasFocus)
        {
            // Hide keyboard when tapping out of edit text:
            hideKeyboard(view);
        }
    }

    private void setEditTextFocusInExercisesBoard(View view, boolean hasFocus)
    {
        if (!hasFocus)
        {
            // Hide keyboard when tapping out of edit text:
            hideKeyboard(view);
        }
        else
        {
            // Minimize top box to have enough space for keyboard and edit text:
            binding.setIsTopBoxMinimized(true);
        }
    }

    // Update the layout binding when the data in the view model changes:
    private void subscribeUi()
    {
        // Current workout entry:
        viewModel.getCurrentWorkoutUnit().observe(this, (@Nullable WorkoutUnitEntity workoutUnit) -> {
            if (workoutUnit != null)  // workoutUnit will be null at first as long as observableWorkoutUnit isn't loaded from the database yet
            {
                DataRepository.executeOnceForLiveData(viewModel.getExerciseSets(workoutUnit), exerciseSetList -> exerciseSetList != null && !exerciseSetList.isEmpty(), exerciseSetList ->
                {
                    Log.d("subscribeUi", "exerciseSetList loaded");  // DEBUG:
                    assert exerciseSetList != null : "object cannot be null";
                    DataRepository.executeOnceForLiveData(viewModel.getExerciseInfo(exerciseSetList), exerciseInfoList ->
                    {
                        assert exerciseInfoList != null : "object cannot be null";
                        Log.d("subscribeUi", "--------");  // DEBUG:
                        Log.d("subscribeUi", "workoutUnit.getName() = " + workoutUnit.getName());  // DEBUG:
                        Log.d("subscribeUi", "workoutUnit.getExerciseNames() = " + workoutUnit.getExerciseNames());  // DEBUG:
                        Log.d("subscribeUi", "workoutUnit.getDate() = " + workoutUnit.getDate().toString());  // DEBUG:
                        Log.d("subscribeUi", "exerciseInfoList = " + exerciseInfoList.stream().map(element -> element.getName() + " " + element.getDefaultValues()).collect(Collectors.joining(", ")));  // DEBUG:
                        Log.d("subscribeUi", "exerciseSetList = " + exerciseSetList.stream().map(element -> element.getExerciseInfoName() + " " + element.getWorkoutUnitDate().toString()).collect(Collectors.joining(", ")));  // DEBUG:
                        binding.setWorkoutUnit(workoutUnit);
                        adapter.setExercise(workoutUnit.getExerciseNames(), exerciseInfoList, exerciseSetList);
                        binding.setIsWorkoutLoading(false);
                        binding.executePendingBindings();
                        updateFinishedExercises();
                    });
                });
            }
            else
            {
                binding.setIsWorkoutLoading(true);
                binding.executePendingBindings();
            }
        });
        // Entries for statistics:
        viewModel.getLastWorkoutUnit().observe(this, (@Nullable WorkoutUnitEntity workoutUnit) ->
        {
            if (workoutUnit != null)
            {
                binding.setLastWorkoutDate(SimpleDateFormat.getDateInstance().format(workoutUnit.getDate()));
                binding.executePendingBindings();
            }
        });
        viewModel.getAllWorkoutUnits().observe(this, (@Nullable List<WorkoutUnitEntity> workoutUnits) ->
        {
            if (workoutUnits != null)
            {
                binding.setTotalWorkoutCount(String.valueOf(workoutUnits.size() - 1));
                float averageInterval = 0F;
                for (int i = 1; i < workoutUnits.size() - 1; i++)  // Start with second entry for diff and skip last entry because it isn't finished
                {
                    averageInterval += TimeUnit.DAYS.convert(workoutUnits.get(i).getDate().getTime() - workoutUnits.get(i - 1).getDate().getTime(), TimeUnit.MILLISECONDS);
                }
                binding.setAverageInterval(String.format(Locale.getDefault(), "%.2f", averageInterval / (workoutUnits.size() - 2)));
                binding.executePendingBindings();
            }
        });
    }

    /* Is called every time the activity is recreated (eg. when rotating the screen) */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        currentThemeId = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt("currentThemeId", currentThemeId);
        updateTheme();
        binding = DataBindingUtil.setContentView(this, R.layout.workout_screen);

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Recreated activities receive the same ExerciseListViewModel instance created by the first activity.
        // "this" activity is the ViewModelStoreOwner of the view model, thus the recycling is linked between these two.
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Setup recycle view adapter:
        adapter = new ExerciseInfoAdapter(this::updateFinishedExercises, this::setEditTextFocusInExercisesBoard);
        binding.exercisesBoard.setAdapter(adapter);
        binding.exercisesBoard.setLayoutManager(new LinearLayoutManager(this));

        // Setup layout data binding and add listeners and observers:
        binding.setIsTopBoxMinimized(true);
        binding.setIsDebugModeActive(DEBUG_MODE_ACTIVE);
        updateEditMode();
        binding.nextStudioButton.setOnClickListener((View view) -> DataRepository.executeOnceForLiveData(viewModel.getAllWorkoutUnits(), workoutUnits ->
        {
            assert workoutUnits != null : "object cannot be null";
            final WorkoutUnitEntity currentWorkoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
            // Get all workout studios:
            Set<String> workoutStudios = new LinkedHashSet<>();
            for (WorkoutUnitEntity workoutUnit: workoutUnits)
            {
                workoutStudios.add(workoutUnit.getStudio());  // Duplicate names will automatically be ignored in a Set
            }
            // Get new workout studio:
            String newWorkoutStudio = currentWorkoutUnit.getStudio();
            Iterator<String> iterator = workoutStudios.iterator();
            while(iterator.hasNext())
            {
                if (Objects.equals(iterator.next(), currentWorkoutUnit.getStudio()))
                {
                    // If available find the next workout studio after the current one, otherwise wrap around to the first studio in the list:
                    newWorkoutStudio = iterator.hasNext() ? iterator.next() : workoutStudios.iterator().next();
                    break;
                }
            }
            // Change to new workout:
            Log.d("nextStudioButton", "newWorkoutStudio = " + newWorkoutStudio);  // DEBUG:
            DataRepository.executeOnceForLiveData(viewModel.getNewestWorkoutUnit(newWorkoutStudio), baseWorkoutUnit ->
            {
                assert baseWorkoutUnit != null : "object cannot be null";  // FIXME: baseWorkoutUnit = new when creating new studio
                // FIXME: crash when creating new studio and pressing next studio button without finishing the workout first
                viewModel.changeWorkout(baseWorkoutUnit);
            });
        }));
        binding.nextWorkoutButton.setOnClickListener((View view) ->
        {
            final WorkoutUnitEntity currentWorkoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
            DataRepository.executeOnceForLiveData(viewModel.getAllWorkoutUnits(currentWorkoutUnit.getStudio()), workoutUnits ->
            {
                assert workoutUnits != null : "object cannot be null";
                // Get all workout names of current studio:
                Set<String> workoutNames = new LinkedHashSet<>();
                for (WorkoutUnitEntity workoutUnit: workoutUnits)
                {
                    workoutNames.add(workoutUnit.getName());  // Duplicate names will automatically be ignored in a Set
                }
                // Get new workout name:
                String newWorkoutName = currentWorkoutUnit.getName();
                Iterator<String> iterator = workoutNames.iterator();
                while(iterator.hasNext())
                {
                    if (Objects.equals(iterator.next(), currentWorkoutUnit.getName()))
                    {
                        // If available find the next workout name after the current one, otherwise wrap around to the first name in the list:
                        newWorkoutName = iterator.hasNext() ? iterator.next() : workoutNames.iterator().next();
                        break;
                    }
                }
                // Change to new workout:
                Log.d("nextWorkoutButton", "newWorkoutName = " + newWorkoutName);  // DEBUG:
                DataRepository.executeOnceForLiveData(viewModel.getNewestWorkoutUnit(currentWorkoutUnit.getStudio(), newWorkoutName), baseWorkoutUnit ->
                {
                    assert baseWorkoutUnit != null : "object cannot be null";  // FIXME: baseWorkoutUnit = new when creating new workout
                    // FIXME: crash when creating new workout and pressing next workout button without finishing the workout first
                    Log.d("nextWorkoutButton", "baseWorkoutUnit.getName() = " + baseWorkoutUnit.getName());  // DEBUG:
                    Log.d("nextWorkoutButton", "baseWorkoutUnit.getExerciseNames() = " + baseWorkoutUnit.getExerciseNames());  // DEBUG:
                    Log.d("nextWorkoutButton", "baseWorkoutUnit.getDate() = " + baseWorkoutUnit.getDate().toString());  // DEBUG:
                    viewModel.changeWorkout(baseWorkoutUnit);
                });
            });
        });
        binding.optionsButton.setOnClickListener((View view) -> binding.setIsTopBoxMinimized(!binding.getIsTopBoxMinimized()));
        binding.studioText.setOnFocusChangeListener(this::setEditTextFocusInTopBox);
        binding.workoutText.setOnFocusChangeListener(this::setEditTextFocusInTopBox);
        binding.workoutDateText.setOnFocusChangeListener(this::setEditTextFocusInTopBox);
        binding.workoutDescriptionText.setOnFocusChangeListener(this::setEditTextFocusInTopBox);
        binding.editModeButton.setOnClickListener((View view) ->
        {
            isEditModeActive = !isEditModeActive;
            if (!isEditModeActive)  // DEBUG:
            {
                final var currentWorkoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();  // DEBUG:
                final var currentExerciseSets = adapter.getExerciseSets();  // DEBUG:
                currentWorkoutUnit.setExerciseNames(WorkoutUnitEntity.exerciseSets2exerciseNames(currentExerciseSets));  // DEBUG:
                Log.d("updateEditMode", "currentWorkoutUnit.getExerciseNames() = " + currentWorkoutUnit.getExerciseNames());  // DEBUG:
                Log.d("updateEditMode", "currentWorkoutUnit.getDate() = " + currentWorkoutUnit.getDate().toString());  // DEBUG:
                Log.d("updateEditMode", "currentExerciseSets = " + currentExerciseSets.stream().map(element -> element.getExerciseInfoName() + " " + element.getWorkoutUnitDate().toString()).collect(Collectors.joining(", ")));  // DEBUG:
            }
            updateFinishedExercises();
            updateEditMode();
        });
        binding.themeButton.setOnClickListener((View view) ->
        {
            currentThemeId += 1;
            updateTheme();
            final var editor = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            editor.putInt("currentThemeId", currentThemeId);
            editor.apply();
        });
        binding.addExerciseButton.setOnClickListener((View view) ->
        {
            // Create new exercise:
            String newExerciseName = "NewExerciseName";
            final List<ExerciseInfoEntity> newExerciseInfoList = adapter.getExerciseInfo();
            newExerciseName += determineNamePostfixCounter(newExerciseInfoList, newExerciseName);
            newExerciseInfoList.add(new ExerciseInfoEntity(newExerciseName));  // FIXME: new exercise info is not stored in database
            final List<ExerciseSetEntity> newExerciseSetsList = adapter.getExerciseSets();
            newExerciseSetsList.add(new ExerciseSetEntity(Objects.requireNonNull(viewModel.getCurrentWorkoutUnit().getValue()).getDate(), newExerciseName, ExerciseSetAdapter.WEIGHTED_EXERCISE_REPEATS_MIN, 0F));
            // Add new exercise to workout unit and exercise adapter:
            final WorkoutUnitEntity workoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
            workoutUnit.setExerciseNames(WorkoutUnitEntity.exerciseSets2exerciseNames(newExerciseSetsList));
            adapter.setExercise(workoutUnit.getExerciseNames(), newExerciseInfoList, newExerciseSetsList);
            binding.exercisesBoard.smoothScrollToPosition(adapter.getItemCount());
        });
        binding.finishButton.setOnClickListener((View view) ->
        {
            if (adapter.getExerciseSets().stream().filter(ExerciseSetEntity::isDone).count() < WorkoutUnitEntity.exerciseNames2Amount(binding.getWorkoutUnit().getExerciseNames()))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Incomplete workout!");
                builder.setMessage("Do you really want to finish the incomplete workout?");
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> finishWorkout());
                builder.setNegativeButton(android.R.string.no, null);
                builder.show();
            }
            else
            {
                finishWorkout();
            }
        });

        subscribeUi();

        // Add debugging button listeners:
        // (Buttons only visible in debugging build)
        binding.debugPrintLogButton.setOnClickListener((View view) -> viewModel.printDebugLog());
        binding.debugNextLogModeButton.setOnClickListener((View view) ->
        {
            if (++debugLogMode > DEBUG_LOG_MAX_MODES) { debugLogMode = 0; }
            Log.i("debugNextLogModeButton", "Changed debug mode to " + debugLogMode);
        });
        binding.debugRemoveWorkoutUnitsButton.setOnClickListener((View view) -> viewModel.removeAllWorkoutUnits());
    }
}