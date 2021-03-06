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

    public interface CallbackAction
    {
        void execute();
    }

    public interface CallbackStatus
    {
        void change(boolean done);
    }

    public interface CallbackFocus
    {
        void set(View view, boolean hasFocus);
    }

    private final CallbackAction checkForEmptyExercisesCb;
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
                    exerciseSetAdapter.exerciseStatusCb.change(true);
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
            binding.exerciseDoneCheckbox.setOnClickListener((View view) -> exerciseSetAdapter.exerciseStatusCb.change(binding.exerciseDoneCheckbox.isChecked()));
            binding.exerciseRepeatsField.setOnFocusChangeListener(exerciseSetAdapter.editTextFocusCb::set);
            binding.exerciseWeightField.setOnFocusChangeListener(exerciseSetAdapter.editTextFocusCb::set);
            binding.removeExerciseSetButton.setOnClickListener((View view) ->
            {
                final int position = getAdapterPosition();
                exerciseSetAdapter.exerciseSets.remove(position);
                exerciseSetAdapter.notifyItemRemoved(position);
                if (exerciseSetAdapter.exerciseSets.size() <= 0)
                {
                    exerciseSetAdapter.checkForEmptyExercisesCb.execute();
                }
            });
            this.binding = binding;
        }
    }

    ExerciseSetAdapter(CallbackAction checkForEmptyExercisesCb, CallbackStatus exerciseStatusCb, CallbackFocus editTextFocusCb)
    {
        this.checkForEmptyExercisesCb = checkForEmptyExercisesCb;
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

    public void setExerciseSets(@NonNull final List<? extends ExerciseSet> exerciseSets)
    {
        if (this.exerciseSets == null)
        {
            // Add all entries:
            this.exerciseSets = exerciseSets;
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
                    return ExerciseSetAdapter.this.exerciseSets.size();
                }

                @Override
                public int getNewListSize()
                {
                    return exerciseSets.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition)
                {
                    return ExerciseSetAdapter.this.exerciseSets.get(oldItemPosition).getId() == exerciseSets.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition)
                {
                    return ExerciseSetEntity.isContentTheSame(exerciseSets.get(newItemPosition), ExerciseSetAdapter.this.exerciseSets.get(oldItemPosition));
                }
            });
            this.exerciseSets = exerciseSets;
            result.dispatchUpdatesTo(this);
        }
    }
}