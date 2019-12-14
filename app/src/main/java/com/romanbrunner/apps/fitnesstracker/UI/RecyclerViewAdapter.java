package com.romanbrunner.apps.fitnesstracker.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.romanbrunner.apps.fitnesstracker.Database.ExerciseEntity;
import com.romanbrunner.apps.fitnesstracker.R;

import java.util.List;


class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ExerciseViewHolder>
{
    // --------------------
    // Functional code
    // --------------------

    private List<ExerciseEntity> exercises;

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder
    {
        EditText nameField;
        EditText tokenField;
        EditText repeatsField;
        EditText weightField;
        CheckBox doneField;
        EditText remarksField;

        ExerciseViewHolder(View itemView)
        {
            super(itemView);
            // Get view fields by id:
            nameField = itemView.findViewById(R.id.exerciseName);
            tokenField = itemView.findViewById(R.id.exerciseToken);
            repeatsField = itemView.findViewById(R.id.exerciseRepeats);
            weightField = itemView.findViewById(R.id.exerciseWeight);
            doneField = itemView.findViewById(R.id.exerciseDone);
            remarksField = itemView.findViewById(R.id.exerciseRemarks);
            // TODO: maybe better store the itemView instead of the individual fields
        }
    }

    RecyclerViewAdapter() {}

    @Override
    public int getItemCount()
    {
        return exercises == null ? 0 : exercises.size();
    }

    @Override
    public long getItemId(int position)
    {
        return exercises.get(position).id;
    }

    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExerciseViewHolder exerciseViewHolder, int position)
    {
        // Adjust changeable values of the view fields by the current exercises list:
        final ExerciseEntity exercise = exercises.get(position);
        exerciseViewHolder.nameField.setText(exercise.name);
        exerciseViewHolder.tokenField.setText(exercise.token);
        exerciseViewHolder.repeatsField.setText(String.valueOf(exercise.repeats));
        exerciseViewHolder.weightField.setText(String.valueOf(exercise.weight));
        exerciseViewHolder.doneField.setChecked(exercise.done);
        exerciseViewHolder.remarksField.setText(exercise.remarks);
        // TODO: make fields iterable

        // Add change listeners:
//        exerciseViewHolder.nameField.addTextChangedListener(new TextWatcher()  // TODO: find out how to implement listener -> check if this works
//        {
//            public void onTextChanged(CharSequence c, int start, int before, int count)
//            {
//                // Adjust value and update database
//                exercise.name = c.toString();
//                database.exerciseDao().update(exercise);
//                // TODO: refresh view -> check if it is required
//            }
//
//            public void beforeTextChanged(CharSequence c, int start, int count, int after) {}
//
//            public void afterTextChanged(Editable c) {}
//        });
        // TODO: make fields iterable
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setExercises(final List<ExerciseEntity> exercises)
    {
        if (this.exercises == null)
        {
            this.exercises = exercises;
            notifyItemRangeInserted(0, exercises.size());
        }
        else
        {
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
                    return RecyclerViewAdapter.this.exercises.get(oldItemPosition).id == exercises.get(newItemPosition).id;
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