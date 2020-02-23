package com.romanbrunner.apps.fitnesstracker.ui;

import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.romanbrunner.apps.fitnesstracker.database.ExerciseEntity;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseInfoEntity;
import com.romanbrunner.apps.fitnesstracker.model.Exercise;
import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.databinding.ExerciseCardBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ExerciseViewHolder>
{
    // --------------------
    // Data code
    // --------------------

    private static final int WEIGHTED_EXERCISE_REPEATS_MIN = 15;
    private static final int WEIGHTED_EXERCISE_REPEATS_MAX = 20;
    private static final float WEIGHTED_EXERCISE_WEIGHT_INCREMENT = 5F;


    // --------------------
    // Functional code
    // --------------------

    private static Map<String, ExerciseInfoEntity> exerciseInfoMap = null;
    private List<? extends Exercise> exercises;

    static class ExerciseViewHolder extends RecyclerView.ViewHolder
    {
        final ExerciseCardBinding binding;

        ExerciseViewHolder(ExerciseCardBinding binding)
        {
            super(binding.getRoot());
            binding.setIsEditModeActive(MainActivity.isEditModeActive);
            binding.exerciseIncrementButton.setOnClickListener((View view) ->
            {
                binding.exerciseDoneCheckbox.setChecked(true);
                int repeats = Integer.parseInt(binding.exerciseRepeatsField.getText().toString()) + 1;
                float weight = Float.parseFloat(binding.exerciseWeightField.getText().toString());
                if (repeats > WEIGHTED_EXERCISE_REPEATS_MAX && weight > 0F)
                {
                    repeats = WEIGHTED_EXERCISE_REPEATS_MIN;
                    binding.exerciseWeightField.setText(String.valueOf(weight + WEIGHTED_EXERCISE_WEIGHT_INCREMENT));
                }
                binding.exerciseRepeatsField.setText(String.valueOf(repeats));
            });
            this.binding = binding;
        }
    }

    RecyclerViewAdapter()
    {
        exercises = null;
    }

    @Override
    public int getItemCount()
    {
        return (exercises == null || exerciseInfoMap == null) ? 0 : exercises.size();
    }

    @Override
    public long getItemId(int position)
    {
        return exercises.get(position).getId();
    }

    @Override
    public @NonNull ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        ExerciseCardBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.exercise_card, viewGroup, false);
        return new ExerciseViewHolder(binding);
    }

    @Override
    /* Is called when an exercise_card is reloaded (that was previously not visible) */
    public void onBindViewHolder(ExerciseViewHolder exerciseViewHolder, int position)
    {
        // Adjust changeable values of the view fields by the current exercises list:
        Exercise exercise = exercises.get(position);
        exerciseViewHolder.binding.setExerciseInfo(exerciseInfoMap.get(exercise.getExerciseInfoName()));
        exerciseViewHolder.binding.setExercise(exercise);
        exerciseViewHolder.binding.executePendingBindings();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public List<ExerciseInfoEntity> getUpdatedExerciseInfo()
    {
        // Find changed names and update map:
        Map<String, String> changedKeyMap = new LinkedHashMap<>();
        final Set<String> keys = exerciseInfoMap.keySet();
        for (String oldKey: keys)  // DEBUG: fix ConcurrentModificationException
        {
            ExerciseInfoEntity exerciseInfo = exerciseInfoMap.get(oldKey);
            if (exerciseInfo != null && !Objects.equals(exerciseInfo.getName(), oldKey))
            {
                String newKey = exerciseInfo.getName();
                changedKeyMap.put(oldKey, newKey);
                exerciseInfoMap.remove(oldKey);
                exerciseInfoMap.put(newKey, exerciseInfo);
            }
        }
        // Update changed entries:
        final Set<String> changedKeys = changedKeyMap.keySet();
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback()
        {
            @Override
            public int getOldListSize()
            {
                return RecyclerViewAdapter.this.exercises.size();
            }

            @Override
            public int getNewListSize()
            {
                return exercises.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition)
            {
                return exercises.get(oldItemPosition).getId() == exercises.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition)
            {
                Exercise exercise = exercises.get(newItemPosition);
                if (changedKeys.contains(exercise.getExerciseInfoName()))
                {
                    exercise.setExerciseInfoName(changedKeyMap.get(exercise.getExerciseInfoName()));
                    return false;
                }
                return true;
            }
        });
        result.dispatchUpdatesTo(this);
        // Return updated exercise info list:
        return new ArrayList<>(exerciseInfoMap.values());
    }

    public boolean setExerciseInfo(@NonNull final List<ExerciseInfoEntity> exerciseInfoList)
    {
        if (exerciseInfoMap == null)
        {
            exerciseInfoMap = new HashMap<>();
            for (ExerciseInfoEntity exerciseInfo: exerciseInfoList)
            {
                exerciseInfoMap.put(exerciseInfo.getName(), exerciseInfo);
            }
            if (exercises != null)
            {
                notifyItemRangeInserted(0, exercises.size());
                return true;
            }
        }
        else
        {
            exerciseInfoMap.clear();
            for (ExerciseInfoEntity exerciseInfo: exerciseInfoList)
            {
                exerciseInfoMap.put(exerciseInfo.getName(), exerciseInfo);
            }
            if (exercises != null)
            {
                // Update changed entries:
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback()
                {
                    @Override
                    public int getOldListSize()
                    {
                        return exercises.size();
                    }

                    @Override
                    public int getNewListSize()
                    {
                        return exercises.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition)
                    {
                        return exercises.get(oldItemPosition).getId() == exercises.get(newItemPosition).getId();
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition)
                    {
                        ExerciseInfoEntity exerciseInfoA = exerciseInfoMap.get(exercises.get(newItemPosition).getExerciseInfoName());
                        ExerciseInfoEntity exerciseInfoB = exerciseInfoMap.get(exercises.get(oldItemPosition).getExerciseInfoName());
                        if (exerciseInfoA != null && exerciseInfoB != null)
                        {
                            return ExerciseInfoEntity.isContentTheSame(exerciseInfoA, exerciseInfoB);
                        }
                        else
                        {
                            return false;
                        }
                    }
                });
                result.dispatchUpdatesTo(this);
                return true;
            }
        }
        return false;
    }

    public boolean setExercises(@NonNull final List<? extends Exercise> exercises)
    {
        if (this.exercises == null)
        {
            // Add all entries:
            this.exercises = exercises;
            if (exerciseInfoMap != null)
            {
                notifyItemRangeInserted(0, exercises.size());
                return true;
            }
        }
        else
        {
            // Update changed entries:
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback()
            {
                @Override
                public int getOldListSize()
                {
                    return RecyclerViewAdapter.this.exercises.size();
                }

                @Override
                public int getNewListSize()
                {
                    return exercises.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition)
                {
                    return RecyclerViewAdapter.this.exercises.get(oldItemPosition).getId() == exercises.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition)
                {
                    return ExerciseEntity.isContentTheSame(exercises.get(newItemPosition), RecyclerViewAdapter.this.exercises.get(oldItemPosition));
                }
            });
            this.exercises = exercises;
            if (exerciseInfoMap != null)
            {
                result.dispatchUpdatesTo(this);
                return true;
            }
        }
        return false;
    }
}