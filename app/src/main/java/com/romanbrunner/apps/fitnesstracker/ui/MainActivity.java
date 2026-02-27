package com.romanbrunner.apps.fitnesstracker.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.romanbrunner.apps.fitnesstracker.DataRepository;
import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutUnitEntity;
import com.romanbrunner.apps.fitnesstracker.databinding.WorkoutScreenBinding;
import com.romanbrunner.apps.fitnesstracker.viewmodels.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
    private final static long LONG_PRESS_DURATION_MS = 5000;


    // --------------------
    // Functional code
    // --------------------

    private ExerciseInfoAdapter adapter;
    private ArrayAdapter<String> studioSpinnerAdapter;
    private ArrayAdapter<String> workoutSpinnerAdapter;
    private WorkoutScreenBinding binding;
    private MainViewModel viewModel;
    private int nextUnfinishedAbovePosition = -1;
    private int nextUnfinishedBelowPosition = -1;

    private static int currentThemeId = 0;
    public static boolean isEditModeActive = false;
    public static int debugLogMode = 7;

    private static int determineNamePostfixCounter(List<ExerciseInfoEntity> exerciseInfoList, String exerciseName)
    {
        final var existingNames = exerciseInfoList.stream().map(ExerciseInfoEntity::getName).collect(Collectors.toSet());
        int namePostfixCounter = 1;
        while (existingNames.contains(exerciseName + namePostfixCounter))
        {
            namePostfixCounter++;
        }
        return namePostfixCounter;
    }

    private void finishWorkout()
    {
        final List<ExerciseInfoEntity> exerciseInfo = adapter.getExerciseInfo();
        final List<ExerciseSetEntity> orderedExerciseSets = adapter.getExerciseSets();
        viewModel.storeExerciseInfo(exerciseInfo);
        viewModel.finishWorkout((WorkoutUnitEntity)binding.getWorkoutUnit(), orderedExerciseSets);
    }

    private void showRemoveExerciseDialog(String exerciseInfoName)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove exercise");
        builder.setMessage("Do you really want to remove \"" + exerciseInfoName + "\" from all workouts and the database?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> viewModel.removeExerciseCompletely(exerciseInfoName));
        builder.setNegativeButton(android.R.string.no, null);
        builder.show();
    }

    private void hideKeyboard(View view)
    {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(inputMethodManager).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void updateFinishedExercises()
    {
        final var exercisesDone = adapter.getExerciseSets().stream().collect(Collectors.groupingBy(ExerciseSetEntity::getExerciseInfoName)).values().stream().filter(sets -> sets.stream().allMatch(ExerciseSetEntity::isDone)).count();
        final var exercisesTotal = adapter.getExerciseSets().stream().map(ExerciseSetEntity::getExerciseInfoName).distinct().count();
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
        adapter.refreshDoneStates();
        binding.exercisesBoard.post(this::updateUnfinishedIndicators);
    }

    private void updateUnfinishedIndicators()
    {
        final var layoutManager = (LinearLayoutManager)binding.exercisesBoard.getLayoutManager();
        if (layoutManager == null || adapter.getItemCount() == 0)
        {
            binding.unfinishedAboveIndicator.setText(String.valueOf(0));
            binding.unfinishedBelowIndicator.setText(String.valueOf(0));
            binding.unfinishedAboveIndicator.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null), PorterDuff.Mode.MULTIPLY);
            binding.unfinishedBelowIndicator.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null), PorterDuff.Mode.MULTIPLY);
            binding.nextUnfinishedAbovePreview.getRoot().setVisibility(View.GONE);
            binding.nextUnfinishedBelowPreview.getRoot().setVisibility(View.GONE);
            return;
        }
        int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
        if (firstVisiblePosition == RecyclerView.NO_POSITION)
        {
            firstVisiblePosition = 0;
            lastVisiblePosition = adapter.getItemCount() - 1;
        }
        int unfinishedAbove = 0;
        for (int i = 0; i < firstVisiblePosition; i++)
        {
            if (!adapter.isExerciseFinishedAtPosition(i)) { unfinishedAbove++; }
        }
        int unfinishedBelow = 0;
        for (int i = lastVisiblePosition + 1; i < adapter.getItemCount(); i++)
        {
            if (!adapter.isExerciseFinishedAtPosition(i)) { unfinishedBelow++; }
        }
        binding.unfinishedAboveIndicator.setText(String.valueOf(unfinishedAbove));
        binding.unfinishedBelowIndicator.setText(String.valueOf(unfinishedBelow));
        binding.unfinishedAboveIndicator.getBackground().clearColorFilter();
        binding.unfinishedBelowIndicator.getBackground().clearColorFilter();
        if (unfinishedAbove == 0)
        {
            binding.unfinishedAboveIndicator.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null), PorterDuff.Mode.MULTIPLY);
        }
        if (unfinishedBelow == 0)
        {
            binding.unfinishedBelowIndicator.getBackground().setColorFilter(ResourcesCompat.getColor(getResources(), R.color.colorAccent, null), PorterDuff.Mode.MULTIPLY);
        }
        updateExercisePreviews(firstVisiblePosition, lastVisiblePosition);
    }

    private void updateExercisePreviews(int firstVisiblePosition, int lastVisiblePosition)
    {
        // Find next unfinished exercise above visible area:
        nextUnfinishedAbovePosition = -1;
        for (int i = firstVisiblePosition - 1; i >= 0; i--)
        {
            if (!adapter.isExerciseFinishedAtPosition(i))
            {
                nextUnfinishedAbovePosition = i;
                break;
            }
        }
        // Find next unfinished exercise below visible area:
        nextUnfinishedBelowPosition = -1;
        for (int i = lastVisiblePosition + 1; i < adapter.getItemCount(); i++)
        {
            if (!adapter.isExerciseFinishedAtPosition(i))
            {
                nextUnfinishedBelowPosition = i;
                break;
            }
        }
        // Update above preview:
        if (nextUnfinishedAbovePosition >= 0)
        {
            final var info = adapter.getExerciseInfoAtPosition(nextUnfinishedAbovePosition);
            binding.nextUnfinishedAbovePreview.setExerciseInfo(info);
            binding.nextUnfinishedAbovePreview.setIsHidden(true);
            binding.nextUnfinishedAbovePreview.setIsDone(false);
            binding.nextUnfinishedAbovePreview.setIsEditModeActive(false);
            binding.nextUnfinishedAbovePreview.exerciseNameField.setFocusable(false);
            binding.nextUnfinishedAbovePreview.exerciseNameField.setFocusableInTouchMode(false);
            binding.nextUnfinishedAbovePreview.exerciseNameField.setCursorVisible(false);
            binding.nextUnfinishedAbovePreview.exerciseNameField.setLongClickable(false);
            binding.nextUnfinishedAbovePreview.exerciseTokenField.setFocusable(false);
            binding.nextUnfinishedAbovePreview.exerciseTokenField.setFocusableInTouchMode(false);
            binding.nextUnfinishedAbovePreview.exerciseTokenField.setCursorVisible(false);
            binding.nextUnfinishedAbovePreview.exerciseTokenField.setLongClickable(false);
            binding.nextUnfinishedAbovePreview.executePendingBindings();
            binding.nextUnfinishedAbovePreview.getRoot().setVisibility(View.VISIBLE);
        }
        else
        {
            binding.nextUnfinishedAbovePreview.getRoot().setVisibility(View.GONE);
        }
        // Update below preview:
        if (nextUnfinishedBelowPosition >= 0)
        {
            final var info = adapter.getExerciseInfoAtPosition(nextUnfinishedBelowPosition);
            binding.nextUnfinishedBelowPreview.setExerciseInfo(info);
            binding.nextUnfinishedBelowPreview.setIsHidden(true);
            binding.nextUnfinishedBelowPreview.setIsDone(false);
            binding.nextUnfinishedBelowPreview.setIsEditModeActive(false);
            binding.nextUnfinishedBelowPreview.exerciseNameField.setFocusable(false);
            binding.nextUnfinishedBelowPreview.exerciseNameField.setFocusableInTouchMode(false);
            binding.nextUnfinishedBelowPreview.exerciseNameField.setCursorVisible(false);
            binding.nextUnfinishedBelowPreview.exerciseNameField.setLongClickable(false);
            binding.nextUnfinishedBelowPreview.exerciseTokenField.setFocusable(false);
            binding.nextUnfinishedBelowPreview.exerciseTokenField.setFocusableInTouchMode(false);
            binding.nextUnfinishedBelowPreview.exerciseTokenField.setCursorVisible(false);
            binding.nextUnfinishedBelowPreview.exerciseTokenField.setLongClickable(false);
            binding.nextUnfinishedBelowPreview.executePendingBindings();
            binding.nextUnfinishedBelowPreview.getRoot().setVisibility(View.VISIBLE);
        }
        else
        {
            binding.nextUnfinishedBelowPreview.getRoot().setVisibility(View.GONE);
        }
    }

    private void scrollToOutermostUnfinishedExercise(boolean scrollUp)
    {
        if (adapter.getItemCount() == 0) { return; }
        if (scrollUp)
        {
            for (int i = 0; i < adapter.getItemCount(); i++)
            {
                if (!adapter.isExerciseFinishedAtPosition(i))
                {
                    binding.exercisesBoard.smoothScrollToPosition(i);
                    return;
                }
            }
        }
        else
        {
            for (int i = adapter.getItemCount() - 1; i >= 0; i--)
            {
                if (!adapter.isExerciseFinishedAtPosition(i))
                {
                    binding.exercisesBoard.smoothScrollToPosition(i);
                    return;
                }
            }
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

    private void addNewExercise()
    {
        String newExerciseName = ExerciseInfoEntity.NEW_EXERCISE_NAME_PREFIX;
        final List<ExerciseInfoEntity> newExerciseInfoList = adapter.getExerciseInfo();
        newExerciseName += determineNamePostfixCounter(newExerciseInfoList, newExerciseName);
        ExerciseInfoEntity newExerciseInfo = new ExerciseInfoEntity(newExerciseName);
        newExerciseInfoList.add(newExerciseInfo);
        viewModel.storeExerciseInfo(Collections.singletonList(newExerciseInfo));
        final List<ExerciseSetEntity> newExerciseSetsList = adapter.getExerciseSets();
        newExerciseSetsList.add(new ExerciseSetEntity(Objects.requireNonNull(viewModel.getCurrentWorkoutUnit().getValue()).getDate(), newExerciseName, ExerciseSetAdapter.WEIGHTED_EXERCISE_REPEATS_MIN, 0F));
        // Add new exercise to workout unit and exercise adapter:
        final WorkoutUnitEntity workoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
        workoutUnit.setExerciseNames(WorkoutUnitEntity.exerciseSets2exerciseNames(newExerciseSetsList));
        adapter.setExercise(workoutUnit.getExerciseNames(), newExerciseInfoList, newExerciseSetsList);
        binding.exercisesBoard.smoothScrollToPosition(adapter.getItemCount());
    }

    private void addExistingExercise(String exerciseInfoName)
    {
        // Load exercise info by name:
        DataRepository.executeOnceForLiveData(viewModel.getExerciseInfoByNames(Collections.singleton(exerciseInfoName)), exerciseInfoList ->
        {
            assert exerciseInfoList != null : "object cannot be null";
            // Load template exercise sets:
            DataRepository.executeOnceForLiveData(viewModel.getTemplateExerciseSets(exerciseInfoName), templateSets ->
            {
                assert templateSets != null : "object cannot be null";
                final Date currentDate = Objects.requireNonNull(viewModel.getCurrentWorkoutUnit().getValue()).getDate();
                final List<ExerciseInfoEntity> newExerciseInfoList = adapter.getExerciseInfo();
                final List<ExerciseSetEntity> newExerciseSetsList = adapter.getExerciseSets();
                for (ExerciseInfoEntity info : exerciseInfoList)
                {
                    if (Objects.equals(info.getName(), exerciseInfoName))
                    {
                        newExerciseInfoList.add(info);
                        break;
                    }
                }
                // Clone template sets with current workout date (or create default if no templates exist):
                if (templateSets.isEmpty())
                {
                    newExerciseSetsList.add(new ExerciseSetEntity(currentDate, exerciseInfoName, ExerciseSetAdapter.WEIGHTED_EXERCISE_REPEATS_MIN, 0F));
                }
                else
                {
                    for (ExerciseSetEntity set : templateSets)
                    {
                        newExerciseSetsList.add(new ExerciseSetEntity(currentDate, exerciseInfoName, set.getRepeats(), set.getWeight()));
                    }
                }
                // Add exercise to workout unit and exercise adapter:
                final WorkoutUnitEntity workoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
                workoutUnit.setExerciseNames(WorkoutUnitEntity.exerciseSets2exerciseNames(newExerciseSetsList));
                adapter.setExercise(workoutUnit.getExerciseNames(), newExerciseInfoList, newExerciseSetsList);
                binding.exercisesBoard.smoothScrollToPosition(adapter.getItemCount());
            });
        });
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
                    assert exerciseSetList != null : "object cannot be null";
                    DataRepository.executeOnceForLiveData(viewModel.getExerciseInfo(exerciseSetList), exerciseInfoList ->
                    {
                        assert exerciseInfoList != null : "object cannot be null";
                        binding.setWorkoutUnit(workoutUnit);
                        adapter.setExercise(workoutUnit.getExerciseNames(), exerciseInfoList, exerciseSetList);
                        binding.setIsWorkoutLoading(false);
                        binding.executePendingBindings();
                        updateFinishedExercises();
                    });
                });
                DataRepository.executeOnceForLiveData(viewModel.getAllWorkoutUnits(), workoutUnits -> workoutUnits != null && !workoutUnits.isEmpty(), workoutUnits ->
                {
                    assert workoutUnits != null : "object cannot be null";
                    // Get all workout studios and workout names of current studio:
                    var workoutStudios = new LinkedHashSet<String>();
                    var workoutNames = new LinkedHashSet<String>();
                    for (WorkoutUnitEntity workout: workoutUnits)
                    {
                        workoutStudios.add(workout.getStudio());  // Duplicate names will automatically be ignored in a Set
                        if (Objects.equals(workout.getStudio(), workoutUnit.getStudio()))
                        {
                            workoutNames.add(workout.getName());  // Duplicate names will automatically be ignored in a Set
                        }
                    }
                    // Update spinner adapter entries:
                    studioSpinnerAdapter.clear();
                    studioSpinnerAdapter.addAll(new ArrayList<>(workoutStudios));
                    studioSpinnerAdapter.notifyDataSetChanged();
                    workoutSpinnerAdapter.clear();
                    workoutSpinnerAdapter.addAll(new ArrayList<>(workoutNames));
                    workoutSpinnerAdapter.notifyDataSetChanged();
                    // Set correct spinner selections to match current workout:
                    final int studioIndex = studioSpinnerAdapter.getPosition(workoutUnit.getStudio());
                    if (studioIndex >= 0) { binding.studioSpinner.setSelection(studioIndex); }
                    final int workoutIndex = workoutSpinnerAdapter.getPosition(workoutUnit.getName());
                    if (workoutIndex >= 0) { binding.workoutSpinner.setSelection(workoutIndex); }
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
        binding.exercisesBoard.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);
                updateUnfinishedIndicators();
            }
        });

        // Setup spinner adapters:
        studioSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        studioSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.studioSpinner.setAdapter(studioSpinnerAdapter);
        workoutSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        workoutSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.workoutSpinner.setAdapter(workoutSpinnerAdapter);

        // Setup layout data binding and add listeners and observers:
        binding.setIsTopBoxMinimized(true);
        binding.setIsDebugModeActive(DEBUG_MODE_ACTIVE);
        updateEditMode();
        binding.studioSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                final String selectedStudio = (String)parent.getItemAtPosition(position);
                final WorkoutUnitEntity currentWorkoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
                if (currentWorkoutUnit != null && Objects.equals(selectedStudio, currentWorkoutUnit.getStudio()))
                {
                    return;
                }
                // Change to new workout:
                DataRepository.executeOnceForLiveData(viewModel.getNewestWorkoutUnit(selectedStudio), baseWorkoutUnit ->
                {
                    assert baseWorkoutUnit != null : "object cannot be null";
                    viewModel.changeWorkout(baseWorkoutUnit);
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        binding.workoutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                final String selectedWorkout = (String)parent.getItemAtPosition(position);
                final WorkoutUnitEntity currentWorkoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
                if (currentWorkoutUnit != null && Objects.equals(selectedWorkout, currentWorkoutUnit.getName()))
                {
                    return;
                }
                if (currentWorkoutUnit == null) { return; }
                // Change to new workout:
                DataRepository.executeOnceForLiveData(viewModel.getNewestWorkoutUnit(currentWorkoutUnit.getStudio(), selectedWorkout), baseWorkoutUnit ->
                {
                    assert baseWorkoutUnit != null : "object cannot be null";
                    viewModel.changeWorkout(baseWorkoutUnit);
                });
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        binding.optionsButton.setOnClickListener((View view) -> binding.setIsTopBoxMinimized(!binding.getIsTopBoxMinimized()));
        binding.unfinishedAboveIndicator.setOnClickListener((View view) -> scrollToOutermostUnfinishedExercise(true));
        binding.unfinishedBelowIndicator.setOnClickListener((View view) -> scrollToOutermostUnfinishedExercise(false));
        binding.nextUnfinishedAbovePreview.cardContent.setOnClickListener((View view) ->
        {
            if (nextUnfinishedAbovePosition >= 0) { binding.exercisesBoard.smoothScrollToPosition(nextUnfinishedAbovePosition); }
        });
        binding.nextUnfinishedAbovePreview.exerciseNameField.setOnClickListener((View view) ->
        {
            if (nextUnfinishedAbovePosition >= 0) { binding.exercisesBoard.smoothScrollToPosition(nextUnfinishedAbovePosition); }
        });
        binding.nextUnfinishedAbovePreview.exerciseTokenField.setOnClickListener((View view) ->
        {
            if (nextUnfinishedAbovePosition >= 0) { binding.exercisesBoard.smoothScrollToPosition(nextUnfinishedAbovePosition); }
        });
        binding.nextUnfinishedBelowPreview.cardContent.setOnClickListener((View view) ->
        {
            if (nextUnfinishedBelowPosition >= 0) { binding.exercisesBoard.smoothScrollToPosition(nextUnfinishedBelowPosition); }
        });
        binding.nextUnfinishedBelowPreview.exerciseNameField.setOnClickListener((View view) ->
        {
            if (nextUnfinishedBelowPosition >= 0) { binding.exercisesBoard.smoothScrollToPosition(nextUnfinishedBelowPosition); }
        });
        binding.nextUnfinishedBelowPreview.exerciseTokenField.setOnClickListener((View view) ->
        {
            if (nextUnfinishedBelowPosition >= 0) { binding.exercisesBoard.smoothScrollToPosition(nextUnfinishedBelowPosition); }
        });
        binding.studioText.setOnFocusChangeListener(this::setEditTextFocusInTopBox);
        binding.workoutText.setOnFocusChangeListener(this::setEditTextFocusInTopBox);
        binding.workoutDateText.setOnFocusChangeListener(this::setEditTextFocusInTopBox);
        binding.workoutDescriptionText.setOnFocusChangeListener(this::setEditTextFocusInTopBox);
        binding.editModeButton.setOnClickListener((View view) ->
        {
            isEditModeActive = !isEditModeActive;
            if (!isEditModeActive)
            {
                final var currentWorkoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
                final var currentExerciseSets = adapter.getExerciseSets();
                currentWorkoutUnit.setExerciseNames(WorkoutUnitEntity.exerciseSets2exerciseNames(currentExerciseSets));
                viewModel.storeExerciseInfo(adapter.getExerciseInfo());
                viewModel.storeWorkout(currentWorkoutUnit, currentExerciseSets);
                // Update spinner entries in case studio or workout name was changed in edit mode:
                final String newStudio = currentWorkoutUnit.getStudio();
                final String newName = currentWorkoutUnit.getName();
                if (studioSpinnerAdapter.getPosition(newStudio) < 0)
                {
                    studioSpinnerAdapter.add(newStudio);
                    studioSpinnerAdapter.notifyDataSetChanged();
                }
                binding.studioSpinner.setSelection(studioSpinnerAdapter.getPosition(newStudio));
                if (workoutSpinnerAdapter.getPosition(newName) < 0)
                {
                    workoutSpinnerAdapter.add(newName);
                    workoutSpinnerAdapter.notifyDataSetChanged();
                }
                binding.workoutSpinner.setSelection(workoutSpinnerAdapter.getPosition(newName));
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
            // Build list of existing exercises not in current workout:
            final List<String> currentExerciseNames = adapter.getExerciseInfo().stream().map(ExerciseInfoEntity::getName).collect(Collectors.toList());
            DataRepository.executeOnceForLiveData(viewModel.getAllExerciseInfo(), allExerciseInfo ->
            {
                assert allExerciseInfo != null : "object cannot be null";
                final List<String> menuItems = new ArrayList<>();
                menuItems.add("Add new exercise");
                for (ExerciseInfoEntity exerciseInfo : allExerciseInfo)
                {
                    if (!currentExerciseNames.contains(exerciseInfo.getName()) && !exerciseInfo.getName().startsWith(ExerciseInfoEntity.NEW_EXERCISE_NAME_PREFIX))
                    {
                        menuItems.add("Add \"" + exerciseInfo.getName() + "\"");
                    }
                }
                menuItems.add("Cancel");
                // Show dialog with exercise list:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final ArrayAdapter<String> menuAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItems);
                final boolean[] longPressTriggered = {false};
                final int[] longPressItemPosition = {-1};
                final Handler longPressHandler = new Handler(Looper.getMainLooper());
                builder.setAdapter(menuAdapter, (dialogInterface, selectedIndex) ->
                {
                    if (!longPressTriggered[0])
                    {
                        if (selectedIndex == 0)
                        {
                            addNewExercise();
                        }
                        else if (selectedIndex < menuItems.size() - 1)
                        {
                            addExistingExercise(menuItems.get(selectedIndex).replaceFirst("^Add \"", "").replaceFirst("\"$", ""));
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                // Set up long press detection for exercise removal:
                dialog.getListView().setOnTouchListener((v, event) ->
                {
                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                            longPressTriggered[0] = false;
                            longPressItemPosition[0] = dialog.getListView().pointToPosition((int) event.getX(), (int) event.getY());
                            if (longPressItemPosition[0] > 0 && longPressItemPosition[0] < menuItems.size() - 1)
                            {
                                longPressHandler.postDelayed(() ->
                                {
                                    longPressTriggered[0] = true;
                                    dialog.dismiss();
                                    showRemoveExerciseDialog(menuItems.get(longPressItemPosition[0]).replaceFirst("^Add \"", "").replaceFirst("\"$", ""));
                                }, LONG_PRESS_DURATION_MS);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            longPressHandler.removeCallbacksAndMessages(null);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (dialog.getListView().pointToPosition((int) event.getX(), (int) event.getY()) != longPressItemPosition[0])
                            {
                                longPressHandler.removeCallbacksAndMessages(null);
                                longPressItemPosition[0] = -1;
                            }
                            break;
                    }
                    return false;
                });
            });
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