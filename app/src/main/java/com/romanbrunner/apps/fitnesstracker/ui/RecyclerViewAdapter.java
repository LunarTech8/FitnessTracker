package com.romanbrunner.apps.fitnesstracker.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.romanbrunner.apps.fitnesstracker.database.ExerciseEntity;
import com.romanbrunner.apps.fitnesstracker.model.Exercise;
import com.romanbrunner.apps.fitnesstracker.R;
import com.romanbrunner.apps.fitnesstracker.databinding.ItemBinding;

import java.util.List;


class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ExerciseViewHolder>
{
    // --------------------
    // Functional code
    // --------------------

    private List<? extends Exercise> exercises;

    static class ExerciseViewHolder extends RecyclerView.ViewHolder
    {
        final ItemBinding binding;

        ExerciseViewHolder(ItemBinding binding)
        {
            super(binding.getRoot());
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
        return exercises == null ? 0 : exercises.size();
    }

    @Override
    public long getItemId(int position)
    {
        return exercises.get(position).getId();
    }

    @Override
    public @NonNull ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        ItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.item, viewGroup, false);
        return new ExerciseViewHolder(binding);
    }

    @Override
    /* Is called when an item is reloaded (that was previously not visible) */
    public void onBindViewHolder(ExerciseViewHolder exerciseViewHolder, int position)
    {
        // Adjust changeable values of the view fields by the current exercises list:
        exerciseViewHolder.binding.setExercise(exercises.get(position));
        exerciseViewHolder.binding.executePendingBindings();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setExercises(@NonNull final List<? extends Exercise> exercises)
    {
        if (this.exercises == null)
        {
            // Add all entries:
            this.exercises = exercises;
            notifyItemRangeInserted(0, exercises.size());
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
            result.dispatchUpdatesTo(this);
        }
    }
}