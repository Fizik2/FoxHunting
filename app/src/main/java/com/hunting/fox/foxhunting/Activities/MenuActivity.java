package com.hunting.fox.foxhunting.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.hunting.fox.foxhunting.Activities.MapsActivity;
import com.hunting.fox.foxhunting.Activities.SettingsActivity;
import com.hunting.fox.foxhunting.R;

/**
 * Created by fizik on 23.01.17.
 */

public class MenuActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 4;
    private static final String LOG_TAG = "MenuActivity";
    private View playView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }


    public void onPlay(View view) {
        playView = view;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

            Log.e(LOG_TAG, "Alert! There no permissions!");
            return;
        }

        startActivity(new Intent(this, MapsActivity.class));
    }

    public void onSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onExit(View view) {
        this.finish();
    }

    //TODO: Check rightness of checking permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPlay(playView);
                } else {
                    Log.e(LOG_TAG, "Alert! Denied permission of coarse location!");
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPlay(playView);
                } else {
                    Log.e(LOG_TAG, "Alert! Denied permission of fine location!");
                }
                return;
            }
        }
    }

}
