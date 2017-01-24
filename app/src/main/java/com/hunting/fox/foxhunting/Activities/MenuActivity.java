package com.hunting.fox.foxhunting.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hunting.fox.foxhunting.Activities.MapsActivity;
import com.hunting.fox.foxhunting.Activities.SettingsActivity;
import com.hunting.fox.foxhunting.R;

/**
 * Created by fizik on 23.01.17.
 */

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    public void onPlay(View view) {
        startActivity(new Intent(this, MapsActivity.class));
    }

    public void onSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onExit(View view) {
        this.finish();
    }
}
