package com.hunting.fox.foxhunting.Activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.EditText;

import com.hunting.fox.foxhunting.R;
import com.hunting.fox.foxhunting.Settings;

/**
 * Created by fizik on 23.01.17.
 */

public class SettingsActivity extends AppCompatActivity {

    private AppCompatCheckBox cbCompass;
    private AppCompatCheckBox cbPointer;
    private AppCompatCheckBox cbAudioSignal;
    private EditText edtFoxNumber;
    private EditText edtFoxDuration;
    private EditText edtFoxDistance;
    private EditText edtEraseDistance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        cbCompass = (AppCompatCheckBox) findViewById(R.id.cbCompass);
        cbPointer = (AppCompatCheckBox) findViewById(R.id.cbPointer);
        cbAudioSignal = (AppCompatCheckBox) findViewById(R.id.cbAudioSignal);
        edtFoxNumber = (EditText) findViewById(R.id.edtFoxNumber);
        edtFoxDuration = (EditText) findViewById(R.id.edtFoxDuration);
        edtFoxDistance = (EditText) findViewById(R.id.edtFoxDistance);
        edtEraseDistance = (EditText) findViewById(R.id.edtEraseDistance);

    }

    public void onBack(View view) {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Settings.isCompass = cbCompass.isChecked();
        Settings.isPointer = cbPointer.isChecked();
        Settings.isAudiosignal = cbAudioSignal.isChecked();

        try {
            byte foxNumber = Byte.parseByte(edtFoxNumber.getText().toString());
            if (foxNumber > 0 && foxNumber < 50)
                Settings.foxNumber = foxNumber;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            int foxDuration = Integer.parseInt(edtFoxDuration.getText().toString());
            if (foxDuration > 1 && foxDuration < 600)
                Settings.foxDuration = foxDuration;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            float foxDistance = Float.parseFloat(edtFoxDistance.getText().toString());
            if (foxDistance > 0.1 && foxDistance < 20)
                Settings.foxDistance = foxDistance;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            float eraseDistance = Float.parseFloat(edtEraseDistance.getText().toString());
            if(eraseDistance > 0 && eraseDistance < 500)
                Settings.eraseDistance = eraseDistance;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Settings.saveSettings(this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();

        Settings.loadSettings(this);

        cbCompass.setChecked(Settings.isCompass);
        cbPointer.setChecked(Settings.isPointer);
        cbAudioSignal.setChecked(Settings.isAudiosignal);

        edtFoxNumber.setText("" + Settings.foxNumber);
        edtFoxDuration.setText("" + Settings.foxDuration);
        edtFoxDistance.setText("" + Settings.foxDistance);
        edtEraseDistance.setText("" + Settings.eraseDistance);

    }

}
