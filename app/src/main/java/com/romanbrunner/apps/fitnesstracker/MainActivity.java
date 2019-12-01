package com.romanbrunner.apps.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LightTheme);  // TODO: make "R.style.DarkTheme" work
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecyclerViewAdapter(initializeData()));
    }
}