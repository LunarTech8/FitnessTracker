package com.romanbrunner.apps.fitnesstracker.UI;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.romanbrunner.apps.fitnesstracker.R;


public class MainActivity extends AppCompatActivity
{
    // --------------------
    // Functional code
    // --------------------

    public static RecyclerViewAdapter adapter;  // TODO: ugly/unclean, find a better way

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.LightTheme);  // TODO: make "R.style.DarkTheme" work
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null)
        {
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new RecyclerViewAdapter();
            recyclerView.setAdapter(adapter);

//            final ExerciseListViewModel viewModel = new ViewModelProvider(this).get(ExerciseListViewModel.class);
            // TODO: implement
        }
    }
}