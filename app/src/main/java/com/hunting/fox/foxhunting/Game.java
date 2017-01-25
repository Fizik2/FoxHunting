package com.hunting.fox.foxhunting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by fizik on 25.01.17.
 */

public class Game {

    private static final String SAVE_NAME = "FoxGame";

    private final LatLng firstLatLng;
    private final Location firstLocation;
    private List<LatLng> currentFoxes;
    private LatLng[] allFoxes;
    private boolean[] foundFoxes;

    public boolean isCompass = Settings.isCompass;
    public boolean isPointer = Settings.isPointer;
    public boolean isAudiosignal = Settings.isAudiosignal;
    public float foxDuration = Settings.foxDuration; // In minutes
    public float foxDistance = Settings.foxDistance; // In kilometers
    public byte foxNumber = Settings.foxNumber;


    public Game(Location firstLocation) {
        this.firstLocation = firstLocation;
        this.firstLatLng = new LatLng(firstLocation.getLatitude(), firstLocation.getLongitude());
        this.allFoxes = generateRandomLocation(firstLatLng);
        this.foundFoxes = new boolean[Settings.foxNumber];
        this.currentFoxes = new ArrayList<LatLng>();

        for(int i = 0; i < foundFoxes.length; i++){
            foundFoxes[i] = false;
        }

        updateCurrentFox();
    }

    private void updateCurrentFox(){
        currentFoxes.clear();
        for(byte i = 0; i < Settings.foxNumber; i++){
            if(!isFoundFox(i))
                currentFoxes.add(allFoxes[i]);
        }
    }

    public boolean isFoundFox(byte index){
        return foundFoxes[index];
    }

    public void foxFound(byte index) throws Exception {
        if(foundFoxes[index])
            throw new Exception("That fox is already found!");
        foundFoxes[index] = true;
        updateCurrentFox();
    }

    private static LatLng[] generateRandomLocation(LatLng firstLatLng) {
        LatLng[] latLngs = new LatLng[Settings.foxNumber];
        for (int i = 0; i < Settings.foxNumber; i++) {
            latLngs[i] = getDestinationPoint(firstLatLng, new Random().nextFloat()*180 - 90, (2*new Random().nextFloat() - 1) *Settings.foxDistance);
        }
        return latLngs;
    }

    private static LatLng getDestinationPoint(LatLng source, double brng, double dist) {
        dist = dist / 6371;
        brng = Math.toRadians(brng);

        double lat1 = Math.toRadians(source.latitude), lon1 = Math.toRadians(source.longitude);
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist) + Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1), Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
        if (Double.isNaN(lat2) || Double.isNaN(lon2)) {
            return null;
        }
        return new LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2));
    }

    public Location getFirstLocation() {
        return firstLocation;
    }

    public LatLng getFirstLatLng(){
        return firstLatLng;
    }

    public LatLng[] getAllFoxes() {
        return allFoxes;
    }

    public List<LatLng> getCurrentFoxes() {
        return currentFoxes;
    }

    @SuppressLint("CommitPrefEdits")
    public void saveIt(Activity activity){
        SharedPreferences mPrefs = activity.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(this);
        prefsEditor.putString(SAVE_NAME, json);
        prefsEditor.commit();
    }

    public static Game loadTheLastGame(Activity activity){
        SharedPreferences mPrefs = activity.getPreferences(MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString(SAVE_NAME, "");
        return gson.fromJson(json, Game.class);
    }

    @SuppressLint("CommitPrefEdits")
    public void removeIt(Activity activity){
        SharedPreferences mPrefs = activity.getPreferences(MODE_PRIVATE);
        mPrefs.edit().remove(SAVE_NAME).commit();
    }
}
