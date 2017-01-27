package com.hunting.fox.foxhunting;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by fizik on 24.01.17.
 */

public class Settings {
    public static boolean isCompass = true;
    public static boolean isPointer = false;
    public static boolean isAudiosignal = true;
    public static int foxDuration = 1; // In seconds
    public static float foxDistance = 5.0f; // In kilometers
    public static float eraseDistance = 100f; // In meters
    public static byte foxNumber = 5;


    public static void loadSettings(Activity activity){
        SharedPreferences settings = activity.getSharedPreferences(activity.getString(R.string.settingsReference), 0);

        isCompass = settings.getBoolean(activity.getString(R.string.compassSettings), isCompass);
        isAudiosignal = settings.getBoolean(activity.getString(R.string.audioSignalSettings), isAudiosignal);
        isPointer = settings.getBoolean(activity.getString(R.string.pointerSettings), isPointer);

        foxDuration = settings.getInt(activity.getString(R.string.foxDurationSettings), foxDuration);
        foxDistance = settings.getFloat(activity.getString(R.string.foxDistanceSettings), foxDistance);
        eraseDistance = settings.getFloat(activity.getString(R.string.eraseDistanceSettings), eraseDistance);
        foxNumber = (byte) settings.getInt(activity.getString(R.string.foxNumberSettings), foxNumber);

    }

    public static  void saveSettings(Activity activity){
        SharedPreferences settings = activity.getSharedPreferences(activity.getString(R.string.settingsReference), 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(activity.getString(R.string.compassSettings), isCompass);
        editor.putBoolean(activity.getString(R.string.audioSignalSettings), isAudiosignal);
        editor.putBoolean(activity.getString(R.string.pointerSettings), isPointer);

        editor.putInt(activity.getString(R.string.foxDurationSettings), foxDuration);
        editor.putFloat(activity.getString(R.string.foxDistanceSettings), foxDistance);
        editor.putFloat(activity.getString(R.string.eraseDistanceSettings), eraseDistance);

        editor.putInt(activity.getString(R.string.foxNumberSettings), foxNumber);

        editor.commit();
    }
}
