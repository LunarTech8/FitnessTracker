package com.romanbrunner.apps.fitnesstracker.ui;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.database.WorkoutUnitEntity;
import com.romanbrunner.apps.fitnesstracker.databinding.ExerciseCardBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


class ExerciseInfoAdapter extends RecyclerView.Adapter<ExerciseInfoAdapter.ExerciseInfoViewHolder>
{
    // --------------------
    // Functional code
    // --------------------

    private static Map<String, List<ExerciseSetEntity>> exerciseInfo2SetsMap;
    private final ExerciseSetAdapter.CallbackStatus exerciseStatusCb;
    private final ExerciseSetAdapter.CallbackFocus editTextFocusCb;
    private List<ExerciseInfoEntity> exerciseInfo;
    private List<ExerciseSetAdapter> adapters;

    static class ExerciseInfoViewHolder extends RecyclerView.ViewHolder
    {
        private final ExerciseCardBinding binding;
        private String oldExerciseInfoName = null;

        ExerciseInfoViewHolder(ExerciseCardBinding binding, ExerciseInfoAdapter exerciseInfoAdapter)
        {
            super(binding.getRoot());
            this.binding = binding;
            binding.setIsEditModeActive(MainActivity.isEditModeActive);
            binding.setsBoard.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
            binding.moveUpButton.setOnClickListener((View view) ->
            {
                final int currentPosition = getAdapterPosition();
                if (currentPosition > 0)
                {
                    exerciseInfoAdapter.swapExercisePositions(currentPosition, currentPosition - 1);
                }
            });
            binding.moveDownButton.setOnClickListener((View view) ->
            {
                final int currentPosition = getAdapterPosition();
                if (currentPosition < exerciseInfoAdapter.getItemCount() - 1)
                {
                    exerciseInfoAdapter.swapExercisePositions(currentPosition, currentPosition + 1);
                }
            });
            binding.addSetButton.setOnClickListener((View view) ->
            {
                final String exerciseInfoName = binding.getExerciseInfo().getName();
                final List<ExerciseSetEntity> exerciseSets = exerciseInfo2SetsMap.get(exerciseInfoName);
                assert exerciseSets != null : "object cannot be null";
                final ExerciseSetEntity lastExerciseSet = exerciseSets.get(exerciseSets.size() - 1);
                exerciseSets.add(new ExerciseSetEntity(lastExerciseSet.getWorkoutUnitDate(), exerciseInfoName, lastExerciseSet.getRepeats(), lastExerciseSet.getWeight()));
                exerciseInfo2SetsMap.put(exerciseInfoName, exerciseSets);
                exerciseInfoAdapter.notifyItemChanged(getAdapterPosition());
            });
            binding.exerciseNameField.setOnFocusChangeListener((view, hasFocus) ->
            {
                if (hasFocus)
                {
                    // Memorise current name when tapping into name field:
                    oldExerciseInfoName = binding.getExerciseInfo().getName();
                }
                else if (oldExerciseInfoName != null)
                {
                    // Adjust name when tapping out of name field:
                    final String newExerciseInfoName = binding.getExerciseInfo().getName();
                    final List<ExerciseSetEntity> exerciseSets = exerciseInfo2SetsMap.get(oldExerciseInfoName);
                    if (exerciseSets != null)
                    {
                        // Remove old data:
                        exerciseInfo2SetsMap.remove(oldExerciseInfoName);
                        // Adjust exercise info name of linked exercise sets:
                        for (ExerciseSetEntity exerciseSet : exerciseSets)
                        {
                            exerciseSet.setExerciseInfoName(newExerciseInfoName);
                        }
                        // Merge exercise sets list if new exercise info name already existed:
                        final List<ExerciseSetEntity> exerciseSetsWithNewName = exerciseInfo2SetsMap.getOrDefault(newExerciseInfoName, null);
                        if (exerciseSetsWithNewName != null)
                        {
                            exerciseSets.addAll(exerciseSetsWithNewName);
                        }
                        // Add adjusted data:
                        exerciseInfo2SetsMap.put(newExerciseInfoName, exerciseSets);
                    }
                    oldExerciseInfoName = null;
                }
                exerciseInfoAdapter.editTextFocusCb.set(view, hasFocus);
            });
            binding.exerciseRemarksField.setOnFocusChangeListener(exerciseInfoAdapter.editTextFocusCb::set);
            binding.exerciseTokenField.setOnFocusChangeListener(exerciseInfoAdapter.editTextFocusCb::set);
        }
    }

    ExerciseInfoAdapter(ExerciseSetAdapter.CallbackStatus exerciseStatusCb, ExerciseSetAdapter.CallbackFocus editTextFocusCb)
    {
        this.exerciseStatusCb = exerciseStatusCb;
        this.editTextFocusCb = editTextFocusCb;
        exerciseInfo = null;
        adapters = null;
    }

    private void removeExerciseSet(int exerciseInfoPosition, int exerciseSetPosition)
    {
        final String exerciseInfoName = exerciseInfo.get(exerciseInfoPosition).getName();
        final List<ExerciseSetEntity> exerciseSets = exerciseInfo2SetsMap.get(exerciseInfoName);
        assert exerciseSets != null : "object cannot be null";
        if (exerciseSetPosition < 0 || exerciseSetPosition >= exerciseSets.size())
        {
            Log.e("removeExerciseSet", "Invalid exercise set position (" + exerciseSetPosition + ")");
        }
        if (exerciseSets.size() <= 1)
        {
            Log.d("removeExerciseSet", "removed exercise info: " + exerciseInfoPosition);  // DEBUG:
            exerciseInfo2SetsMap.remove(exerciseInfoName);
            exerciseInfo.remove(exerciseInfoPosition);
            adapters.remove(exerciseInfoPosition);
            notifyItemRemoved(exerciseInfoPosition);
        }
        else
        {
            Log.d("removeExerciseSet", "removed exercise set: " + exerciseSetPosition);  // DEBUG:
            exerciseSets.remove(exerciseSetPosition);
            exerciseInfo2SetsMap.put(exerciseInfoName, exerciseSets);
            notifyItemChanged(exerciseInfoPosition);
        }
    }

    private void swapExercisePositions(int oldPosition, int newPosition)
    {
        Collections.swap(exerciseInfo, oldPosition, newPosition);
        notifyItemMoved(oldPosition, newPosition);
    }

    List<ExerciseInfoEntity> getExerciseInfo()
    {
        return new ArrayList<>(exerciseInfo);
    }

    List<ExerciseSetEntity> getExerciseSets()
    {
        List<ExerciseSetEntity> orderedExerciseSets = new LinkedList<>();
        for (ExerciseInfoEntity exerciseInfoEntity : exerciseInfo)
        {
            orderedExerciseSets.addAll(Objects.requireNonNull(exerciseInfo2SetsMap.get(exerciseInfoEntity.getName())));
        }
        return orderedExerciseSets;
    }

    void setExercise(@NonNull final String exerciseInfoNames, @NonNull final List<ExerciseInfoEntity> exerciseInfo, @NonNull final List<ExerciseSetEntity> exerciseSets)
    {
        // Get ordered list:
        final List<ExerciseInfoEntity> orderedExerciseInfo = new ArrayList<>(getItemCount());
        for (String exerciseName : WorkoutUnitEntity.exerciseNames2NameSet(exerciseInfoNames))
        {
            boolean targetNotFound = true;
            for (ExerciseInfoEntity exerciseInfoEntity : exerciseInfo)
            {
                if (Objects.equals(exerciseInfoEntity.getName(), exerciseName))
                {
                    orderedExerciseInfo.add(exerciseInfoEntity);
                    targetNotFound = false;
                    break;
                }
            }
            if (targetNotFound)
            {
                Log.e("setExercise", "Could not find " + exerciseName + " in exerciseInfo");
            }
        }
        // Set exerciseInfo2SetsMap:
        exerciseInfo2SetsMap = new HashMap<>();
        for (ExerciseSetEntity exerciseSet: exerciseSets)
        {
            final String exerciseInfoName = exerciseSet.getExerciseInfoName();
            final List<ExerciseSetEntity> exerciseSetList = exerciseInfo2SetsMap.getOrDefault(exerciseInfoName, null);
            if (exerciseSetList != null)
            {
                exerciseSetList.add(exerciseSet);
            }
            else
            {
                exerciseInfo2SetsMap.put(exerciseInfoName, new LinkedList<>(Collections.singletonList(exerciseSet)));
            }
        }
        // Set exerciseInfo:
        this.exerciseInfo = orderedExerciseInfo;
        // Create exercise set adapters:
        final int exerciseInfoCount = orderedExerciseInfo.size();
        adapters = new ArrayList<>(exerciseInfoCount);
        for (int i = 0; i < exerciseInfoCount; i++)
        {
            int exerciseInfoPosition = i;
            adapters.add(new ExerciseSetAdapter((int exerciseSetPosition) -> removeExerciseSet(exerciseInfoPosition, exerciseSetPosition), exerciseStatusCb, editTextFocusCb));
        }
        // Load/Reload all views:
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount()
    {
        return exerciseInfo == null ? 0 : exerciseInfo.size();
    }

    @Override
    public long getItemId(int position)
    {
        return exerciseInfo.get(position).getName().hashCode();
    }

    @Override
    public @NonNull ExerciseInfoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        ExerciseCardBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.exercise_card, viewGroup, false);
        return new ExerciseInfoViewHolder(binding, this);
    }

    @Override
    /* Is called when an exercise_card is loaded/reloaded (that was previously not visible) */
    public void onBindViewHolder(ExerciseInfoViewHolder exerciseInfoViewHolder, int position)
    {
        // Adjust changeable values of the view fields of targeted exercise info:
        ExerciseInfoEntity exerciseInfoEntity = this.exerciseInfo.get(position);
        exerciseInfoViewHolder.binding.setExerciseInfo(exerciseInfoEntity);
        exerciseInfoViewHolder.binding.setIsEditModeActive(MainActivity.isEditModeActive);
        exerciseInfoViewHolder.binding.exerciseNameField.setFocusable(MainActivity.isEditModeActive);
        exerciseInfoViewHolder.binding.exerciseNameField.setEnabled(MainActivity.isEditModeActive);
        exerciseInfoViewHolder.binding.exerciseNameField.setFocusableInTouchMode(MainActivity.isEditModeActive);
        exerciseInfoViewHolder.binding.executePendingBindings();

        // Adjust adapter for exercise sets of targeted exercise info:
        ExerciseSetAdapter adapter = adapters.get(position);
        adapter.setExerciseSets(Objects.requireNonNull(exerciseInfo2SetsMap.get(exerciseInfoEntity.getName())));
        exerciseInfoViewHolder.binding.setsBoard.setAdapter(adapter);
    }

    @Override
    public void onViewRecycled(@NonNull ExerciseInfoViewHolder exerciseInfoViewHolder)
    {
        super.onViewRecycled(exerciseInfoViewHolder);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }
}