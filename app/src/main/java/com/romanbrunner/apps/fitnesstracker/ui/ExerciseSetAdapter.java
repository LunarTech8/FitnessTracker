package com.romanbrunner.apps.fitnesstracker.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.database.ExerciseSetEntity;
import com.romanbrunner.apps.fitnesstracker.databinding.ExerciseSetCardBinding;
import com.romanbrunner.apps.fitnesstracker.model.ExerciseSet;

import java.util.ArrayList;
import java.util.List;


public class ExerciseSetAdapter extends RecyclerView.Adapter<ExerciseSetAdapter.ExerciseSetViewHolder>
{
    // --------------------
    // Data code
    // --------------------

    public static final int WEIGHTED_EXERCISE_REPEATS_MIN = 15;
    private static final int WEIGHTED_EXERCISE_REPEATS_MAX = 20;
    private static final float WEIGHTED_EXERCISE_WEIGHT_INCREMENT = 5F;


    // --------------------
    // Functional code
    // --------------------

    public interface CallbackExerciseSets
    {
        void remove(int position);
    }

    public interface CallbackStatus
    {
        void update();
    }

    public interface CallbackFocus
    {
        void set(View view, boolean hasFocus);
    }

    private final CallbackExerciseSets exerciseSetsCb;
    private final CallbackStatus exerciseStatusCb;
    private final CallbackFocus editTextFocusCb;
    private List<? extends ExerciseSet> exerciseSets;

    static class ExerciseSetViewHolder extends RecyclerView.ViewHolder
    {
        private final ExerciseSetCardBinding binding;

        ExerciseSetViewHolder(ExerciseSetCardBinding binding, ExerciseSetAdapter exerciseSetAdapter)
        {
            super(binding.getRoot());
            binding.setIsEditModeActive(MainActivity.isEditModeActive);
            binding.exerciseIncrementButton.setOnClickListener((View view) ->
            {
                if (!binding.exerciseDoneCheckbox.isChecked())
                {
                    binding.exerciseDoneCheckbox.setChecked(true);
                    exerciseSetAdapter.exerciseStatusCb.update();
                }
                int repeats = Integer.parseInt(binding.exerciseRepeatsField.getText().toString()) + 1;
                float weight = Float.parseFloat(binding.exerciseWeightField.getText().toString());
                if (repeats > WEIGHTED_EXERCISE_REPEATS_MAX && weight > 0F)
                {
                    repeats = WEIGHTED_EXERCISE_REPEATS_MIN;
                    binding.exerciseWeightField.setText(String.valueOf(weight + WEIGHTED_EXERCISE_WEIGHT_INCREMENT));
                }
                binding.exerciseRepeatsField.setText(String.valueOf(repeats));
            });
            binding.exerciseDoneCheckbox.setOnClickListener((View view) -> exerciseSetAdapter.exerciseStatusCb.update());
            binding.exerciseRepeatsField.setOnFocusChangeListener(exerciseSetAdapter.editTextFocusCb::set);
            binding.exerciseWeightField.setOnFocusChangeListener(exerciseSetAdapter.editTextFocusCb::set);
            binding.removeExerciseSetButton.setOnClickListener((View view) -> exerciseSetAdapter.exerciseSetsCb.remove(getAdapterPosition()));
            this.binding = binding;
        }
    }

    ExerciseSetAdapter(CallbackExerciseSets exerciseSetsCb, CallbackStatus exerciseStatusCb, CallbackFocus editTextFocusCb)
    {
        this.exerciseSetsCb = exerciseSetsCb;
        this.exerciseStatusCb = exerciseStatusCb;
        this.editTextFocusCb = editTextFocusCb;
        exerciseSets = null;
    }

    @Override
    public int getItemCount()
    {
        return exerciseSets == null ? 0 : exerciseSets.size();
    }

    @Override
    public long getItemId(int position)
    {
        return exerciseSets.get(position).getId();
    }

    @Override
    public @NonNull ExerciseSetViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        ExerciseSetCardBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.exercise_set_card, viewGroup, false);
        return new ExerciseSetViewHolder(binding, this);
    }

    @Override
    /* Is called when an exercise_set_card is reloaded (that was previously not visible) */
    public void onBindViewHolder(ExerciseSetViewHolder exerciseSetViewHolder, int position)
    {
        // Adjust changeable values of the view fields by the current exercises list:
        exerciseSetViewHolder.binding.setExerciseSet(exerciseSets.get(position));
        exerciseSetViewHolder.binding.setIsEditModeActive(MainActivity.isEditModeActive);
        exerciseSetViewHolder.binding.executePendingBindings();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setExerciseSets(@NonNull final List<? extends ExerciseSet> newExerciseSets)
    {
        if (this.exerciseSets == null)
        {
            // Add all entries:
            exerciseSets = new ArrayList<>(newExerciseSets);
            notifyItemRangeInserted(0, this.exerciseSets.size());
        }
        else
        {
            // Update changed entries:
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback()
            {
                @Override
                public int getOldListSize()
                {
                    return exerciseSets.size();
                }

                @Override
                public int getNewListSize()
                {
                    return newExerciseSets.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition)
                {
                    return exerciseSets.get(oldItemPosition).getId() == newExerciseSets.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition)
                {
                    return ExerciseSetEntity.isContentTheSame(newExerciseSets.get(newItemPosition), exerciseSets.get(oldItemPosition));
                }
            });
            exerciseSets = new ArrayList<>(newExerciseSets);
            result.dispatchUpdatesTo(this);
        }
    }
}