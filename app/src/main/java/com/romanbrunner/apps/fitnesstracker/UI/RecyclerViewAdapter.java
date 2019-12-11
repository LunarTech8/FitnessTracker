package com.romanbrunner.apps.fitnesstracker.UI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.romanbrunner.apps.fitnesstracker.Database.ExerciseEntity;
import com.romanbrunner.apps.fitnesstracker.R;

import java.util.List;


class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ExerciseViewHolder>
{
    // --------------------
    // Functional code
    // --------------------

    private List<ExerciseEntity> exerciseEntities;

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
        }
    }

    RecyclerViewAdapter() {}

    @Override
    public int getItemCount()
    {
        return exerciseEntities.size();  // FIXME: exercises isn't initialized at startup yet
    }

    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        ExerciseViewHolder exerciseViewHolder = new ExerciseViewHolder(view);
        return exerciseViewHolder;
    }

    @Override
    public void onBindViewHolder(ExerciseViewHolder exerciseViewHolder, int exerciseNumber)
    {
        // Adjust changeable values of the view fields by the current exercises list:
        final ExerciseEntity exerciseEntity = exerciseEntities.get(exerciseNumber);
        exerciseViewHolder.nameField.setText(exerciseEntity.name);
        exerciseViewHolder.tokenField.setText(exerciseEntity.token);
        exerciseViewHolder.repeatsField.setText(String.valueOf(exerciseEntity.repeats));
        exerciseViewHolder.weightField.setText(String.valueOf(exerciseEntity.weight));
        exerciseViewHolder.doneField.setChecked(exerciseEntity.done);
        exerciseViewHolder.remarksField.setText(exerciseEntity.remarks);
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
}