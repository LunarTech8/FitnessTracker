package com.romanbrunner.apps.fitnesstracker.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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


public class MainActivity extends AppCompatActivity
{
    // --------------------
    // Data code
    // --------------------

    private static final int[] THEMES = {R.style.LightTheme, R.style.DarkTheme};
    public static final boolean DEBUG_MODE_ACTIVE = true;
    public static final int DEBUG_LOG_MAX_MODES = 7;


    // --------------------
    // Functional code
    // --------------------

    private int exercisesDone = 0;
    private ExerciseInfoAdapter adapter;
    private WorkoutScreenBinding binding;
    private MainViewModel viewModel;

    private static int currentThemeId = 0;
    public static boolean isEditModeActive = false;
    public static int debugLogMode = 5;

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
        final int exercisesTotal = WorkoutUnitEntity.exerciseNames2Amount(binding.getWorkoutUnit().getExerciseNames());
        if (exercisesDone < 0 || exercisesDone > exercisesTotal)
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

    private void changeExerciseStatus(boolean done)
    {
        if (done)
        {
            exercisesDone += 1;
        }
        else
        {
            exercisesDone -= 1;
        }
        updateFinishedExercises();
    }

    // Update the layout binding when the data in the view model changes:
    private void subscribeUi(final MainViewModel viewModel)
    {
        // Current workout entry:
        viewModel.getCurrentWorkoutUnit().observe(this, (@Nullable WorkoutUnitEntity workoutUnit) ->
        {
            if (workoutUnit != null)  // workoutUnit will be null at first as long as observableWorkoutUnit isn't loaded from the database yet
            {
                binding.setWorkoutUnit(workoutUnit);
                DataRepository.executeOnceForLiveData(viewModel.getExerciseSets(workoutUnit), exerciseSetList -> exerciseSetList != null && !exerciseSetList.isEmpty(), exerciseSetList ->
                {
                    if (exerciseSetList == null) throw new AssertionError("object cannot be null");
                    DataRepository.executeOnceForLiveData(viewModel.getExerciseInfo(exerciseSetList), exerciseInfoList ->
                    {
                        if (exerciseInfoList == null) throw new AssertionError("object cannot be null");
                        adapter.setExercise(binding.getWorkoutUnit().getExerciseNames(), exerciseInfoList, exerciseSetList);
                        binding.setIsWorkoutLoading(false);
                        binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
                        exercisesDone = 0;
                        updateFinishedExercises();
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
                binding.setTotalWorkoutCount(String.valueOf(workoutUnits.size() - 1));
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

    /* Is called every time the activity is recreated (eg. when rotating the screen) */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTheme(THEMES[currentThemeId]);
        binding = DataBindingUtil.setContentView(this, R.layout.workout_screen);

        // Create a ViewModel the first time the system calls an activity's onCreate() method.
        // Recreated activities receive the same ExerciseListViewModel instance created by the first activity.
        // "this" activity is the ViewModelStoreOwner of the view model, thus the recycling is linked between these two.
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Setup recycle view adapter:
        adapter = new ExerciseInfoAdapter(this::changeExerciseStatus, this::setEditTextFocusInExercisesBoard);
        binding.exercisesBoard.setAdapter(adapter);
        binding.exercisesBoard.setLayoutManager(new LinearLayoutManager(this));

        // Setup layout data binding and add listeners and observers:
        binding.setIsTopBoxMinimized(true);
        binding.setIsEditModeActive(isEditModeActive);
        binding.setIsDebugModeActive(DEBUG_MODE_ACTIVE);
        binding.nextStudioButton.setOnClickListener((View view) -> DataRepository.executeOnceForLiveData(viewModel.getAllWorkoutUnits(), workoutUnits ->
        {
            if (workoutUnits == null) throw new AssertionError("object cannot be null");
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
                    if (iterator.hasNext())
                    {
                        newWorkoutStudio = iterator.next();
                    }
                    else
                    {
                        newWorkoutStudio = workoutStudios.iterator().next();
                    }
                    break;
                }
            }
            // Change to new workout:
            DataRepository.executeOnceForLiveData(viewModel.getNewestWorkoutUnit(newWorkoutStudio), baseWorkoutUnit ->
            {
                if (baseWorkoutUnit == null) throw new AssertionError("object cannot be null");
                DataRepository.executeOnceForLiveData(viewModel.changeWorkout(baseWorkoutUnit), newWorkoutUnit ->
                {
                    if (newWorkoutUnit == null) throw new AssertionError("object cannot be null");
                    binding.setWorkoutUnit(newWorkoutUnit);
                    binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
                });
            });
        }));
        binding.nextWorkoutButton.setOnClickListener((View view) ->
        {
            final WorkoutUnitEntity currentWorkoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
            DataRepository.executeOnceForLiveData(viewModel.getAllWorkoutUnits(currentWorkoutUnit.getStudio()), workoutUnits ->
            {
                if (workoutUnits == null) throw new AssertionError("object cannot be null");
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
                // Change to new workout:
                DataRepository.executeOnceForLiveData(viewModel.getNewestWorkoutUnit(currentWorkoutUnit.getStudio(), newWorkoutName), baseWorkoutUnit ->
                {
                    if (baseWorkoutUnit == null) throw new AssertionError("object cannot be null");
                    DataRepository.executeOnceForLiveData(viewModel.changeWorkout(baseWorkoutUnit), newWorkoutUnit ->
                    {
                        if (newWorkoutUnit == null) throw new AssertionError("object cannot be null");
                        binding.setWorkoutUnit(newWorkoutUnit);
                        binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
                    });
                });
            });
        });
        binding.optionsButton.setOnClickListener((View view) -> binding.setIsTopBoxMinimized(!binding.getIsTopBoxMinimized()));
        binding.workoutDateText.setOnFocusChangeListener(this::setEditTextFocusInTopBox);
        binding.workoutDescriptionText.setOnFocusChangeListener(this::setEditTextFocusInTopBox);
        binding.editModeButton.setOnClickListener((View view) ->
        {
            isEditModeActive = !isEditModeActive;
            binding.setIsEditModeActive(isEditModeActive);
            adapter.notifyDataSetChanged();
            if (!isEditModeActive)
            {
                viewModel.storeExerciseInfo(adapter.getExerciseInfo());  // TEST: check if this is needed
            }
            binding.executePendingBindings();  // Espresso does not know how to wait for data binding's loop so we execute changes sync
        });
        binding.themeButton.setOnClickListener((View view) ->
        {
            currentThemeId += 1;
            if (currentThemeId >= THEMES.length)
            {
                currentThemeId = 0;
            }
            recreate();
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
            newExerciseSetsList.add(new ExerciseSetEntity(Objects.requireNonNull(viewModel.getCurrentWorkoutUnit().getValue()).getDate(), newExerciseName, ExerciseSetAdapter.WEIGHTED_EXERCISE_REPEATS_MIN, 0F));
            // Add new exercise to workout unit and exercise adapter:
            final WorkoutUnitEntity workoutUnit = (WorkoutUnitEntity)binding.getWorkoutUnit();
            workoutUnit.setExerciseNames(WorkoutUnitEntity.exerciseSets2exerciseNames(newExerciseSetsList));
            adapter.setExercise(workoutUnit.getExerciseNames(), newExerciseInfoList, newExerciseSetsList);
            binding.exercisesBoard.smoothScrollToPosition(adapter.getItemCount());
        });
        binding.finishButton.setOnClickListener((View view) ->
        {
            if (exercisesDone < WorkoutUnitEntity.exerciseNames2Amount(binding.getWorkoutUnit().getExerciseNames()))
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

        subscribeUi(viewModel);

        // Add debugging button listeners:
        // (Buttons only visible in debugging build)
        binding.debugPrintLogButton.setOnClickListener((View view) -> viewModel.printDebugLog());
        binding.debugNextLogModeButton.setOnClickListener((View view) ->
        {
            if (++debugLogMode > DEBUG_LOG_MAX_MODES) { debugLogMode = 0; }
        });
        binding.debugRemoveWorkoutUnitsButton.setOnClickListener((View view) -> viewModel.removeAllWorkoutUnits());
    }
}