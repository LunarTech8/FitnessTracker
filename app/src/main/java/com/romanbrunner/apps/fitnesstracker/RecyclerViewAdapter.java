package com.romanbrunner.apps.fitnesstracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ExerciseViewHolder>
{
    // --------------------
    // Functional code
    // --------------------

    private List<Exercise> exercises;

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
            nameField = itemView.findViewById(R.id.exerciseName);
            tokenField = itemView.findViewById(R.id.exerciseToken);
            repeatsField = itemView.findViewById(R.id.exerciseRepeats);
            weightField = itemView.findViewById(R.id.exerciseWeight);
            doneField = itemView.findViewById(R.id.exerciseDone);
            remarksField = itemView.findViewById(R.id.exerciseRemarks);
        }
    }

    RecyclerViewAdapter(List<Exercise> exercises)
    {
        this.exercises = exercises;
    }

    @Override
    public int getItemCount()
    {
        return exercises.size();
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
        exerciseViewHolder.nameField.setText(exercises.get(exerciseNumber).name);
        exerciseViewHolder.tokenField.setText(exercises.get(exerciseNumber).token);
        exerciseViewHolder.repeatsField.setText(String.valueOf(exercises.get(exerciseNumber).repeats));
        exerciseViewHolder.weightField.setText(String.valueOf(exercises.get(exerciseNumber).weight));
        exerciseViewHolder.doneField.setChecked(exercises.get(exerciseNumber).done);
        exerciseViewHolder.remarksField.setText(exercises.get(exerciseNumber).remarks);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }
}