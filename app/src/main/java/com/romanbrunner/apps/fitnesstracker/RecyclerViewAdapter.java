package com.romanbrunner.apps.fitnesstracker;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ExerciseViewHolder>
{
    // --------------------
    // Data code
    // --------------------

    private List<Exercise> initializeData()
    {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("Cross-Walker", "-", 8, 0.F, "Repeats in Minuten"));
        exercises.add(new Exercise("Negativ-Crunch", "-", 20, 0.F));
        exercises.add(new Exercise("Klimmzug breit zur Brust", "-", 8, 0.F));
        exercises.add(new Exercise("Klimmzug breit zur Brust", "-", 6, 0.F));
        exercises.add(new Exercise("Klimmzug breit zur Brust", "-", 4, 0.F));
        exercises.add(new Exercise("Beinstrecker", "-", 19, 35.F));
        exercises.add(new Exercise("Beinbeuger", "-", 15, 40.F));
        exercises.add(new Exercise("Butterfly", "-", 16, 35.F));
        exercises.add(new Exercise("Wadenheben an der Beinpresse", "-", 16, 105.F, "5 Löcher vorne frei"));
        exercises.add(new Exercise("Duale Schrägband-Drückmaschine", "-", 18, 30.F));
        exercises.add(new Exercise("Bizepsmaschine", "-", 16, 35.F));
        exercises.add(new Exercise("Pushdown am Kabelzug", "-", 16, 20.F));
        exercises.add(new Exercise("Rückenstrecker", "-", 21, 0.F));
        exercises.add(new Exercise("Beinheben liegend", "-", 22, 0.F));
        return exercises;
    }


    // --------------------
    // Functional code
    // --------------------

    private List<Exercise> exercises;
    private AppDatabase database;

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

    RecyclerViewAdapter(AppDatabase database)
    {
        this.database = database;
        // Load or initialise database:
        exercises = this.database.exerciseDao().getAll();
        if (exercises.isEmpty()) {
            exercises = initializeData();
        }
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
        // Adjust changeable values of the view fields by the current exercises list:
        exerciseViewHolder.nameField.setText(exercises.get(exerciseNumber).name);
        exerciseViewHolder.tokenField.setText(exercises.get(exerciseNumber).token);
        exerciseViewHolder.repeatsField.setText(String.valueOf(exercises.get(exerciseNumber).repeats));
        exerciseViewHolder.weightField.setText(String.valueOf(exercises.get(exerciseNumber).weight));
        exerciseViewHolder.doneField.setChecked(exercises.get(exerciseNumber).done);
        exerciseViewHolder.remarksField.setText(exercises.get(exerciseNumber).remarks);

        // Add change listeners:
        exerciseViewHolder.nameField.addTextChangedListener(new TextWatcher()
        {
            // The user's changes are saved here:
            public void onTextChanged(CharSequence c, int start, int before, int count)
            {
                // TODO: find out how to implement listener
                Exercise exercise = database.exerciseDao().findByNameAndToken("", "");
                exercise.name = c.toString();  // Adjust value in database
                // TODO: update database -> replaceById()
                // TODO: refresh view
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {}

            public void afterTextChanged(Editable c) {}
        });
        // TODO: make fields iterable
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }
}