package com.romanbrunner.apps.fitnesstracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(R.style.DarkTheme);
        setTheme(R.style.LightTheme);  // TODO: make DarkTheme work
        setContentView(R.layout.activity_main);
    }
}
