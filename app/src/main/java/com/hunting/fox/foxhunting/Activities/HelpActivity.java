package com.hunting.fox.foxhunting.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.EditText;

import com.hunting.fox.foxhunting.R;

/**
 * Created by fizik on 20.03.17.
 */
public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

    }

    public void onBack(View view) {
        finish();
    }

}
