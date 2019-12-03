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
        EditText exerciseName;
        EditText exerciseToken;
        EditText exerciseRepeats;
        EditText exerciseWeight;
        CheckBox exerciseDone;
        EditText exerciseRemarks;

        ExerciseViewHolder(View itemView)
        {
            super(itemView);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            exerciseToken = itemView.findViewById(R.id.exerciseToken);
            exerciseRepeats = itemView.findViewById(R.id.exerciseRepeats);
            exerciseWeight = itemView.findViewById(R.id.exerciseWeight);
            exerciseDone = itemView.findViewById(R.id.exerciseDone);
            exerciseRemarks = itemView.findViewById(R.id.exerciseRemarks);
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
    public void onBindViewHolder(ExerciseViewHolder exerciseViewHolder, int exerciseId)
    {
        exerciseViewHolder.exerciseName.setText(exercises.get(exerciseId).name);
        exerciseViewHolder.exerciseToken.setText(exercises.get(exerciseId).token);
        exerciseViewHolder.exerciseRepeats.setText(String.valueOf(exercises.get(exerciseId).repeats));
        exerciseViewHolder.exerciseWeight.setText(String.valueOf(exercises.get(exerciseId).weight));
        exerciseViewHolder.exerciseDone.setChecked(exercises.get(exerciseId).done);
        exerciseViewHolder.exerciseRemarks.setText(exercises.get(exerciseId).remarks);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }
}