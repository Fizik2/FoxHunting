package com.hunting.fox.foxhunting.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hunting.fox.foxhunting.Game;
import com.hunting.fox.foxhunting.R;
import com.hunting.fox.foxhunting.Settings;

import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {


    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private static final String LOG_TAG = "MapActivity";

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static final CharSequence[] MAP_TYPE_ITEMS = {"Дорожная карта", "Гибрид", "Спутниковая", "Рельеф"};

    private MediaPlayer mp;
    private Game game;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mp = MediaPlayer.create(this, R.raw.s2);


        image = (ImageView) findViewById(R.id.imageViewCompass);


        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    private void init() {
        mMap.getUiSettings().setCompassEnabled(true);

        boolean needSetPointer = game != null && game.isPointer || game == null && Settings.isPointer;
        mMap.getUiSettings().setMyLocationButtonEnabled(needSetPointer);
        mMap.setMyLocationEnabled(needSetPointer);
    }


    public void onClickToStart(View view) {
        toStart();
    }

    public void onClickTypeMap(View view) {
        showMapTypeSelectorDialog();
    }

    public void onClickGiveUp(View view) {
        if(game == null) return;

        final Activity activity = this;
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_delete)
                .setTitle("Сдаться")
                .setMessage("Вы действительно хотите сдаться?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LatLng[] latLngs = game.getAllFoxes();
                        for (LatLng latLng : latLngs) {
                            mMap.addMarker(new MarkerOptions().position(latLng));
                        }
                        game.removeIt(activity);
                        game = null;

                    }
                })
                .setNegativeButton("Нет", null)
                .show();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        init();

        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000); //5 seconds
        mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {

        if (game == null) {
            game = Game.loadTheLastGame(this);

            if (game == null)
                game = new Game(location);

        }

//        List<LatLng> latLngs = game.getCurrentFoxes();
//        float[] results = new float[1];
//        for (LatLng latLng : latLngs) {
//            Location.distanceBetween(game.getFirstLatLng().latitude, game.getFirstLatLng().longitude, latLng.latitude, latLng.longitude, results);
//            Log.d(LOG_TAG, "Distance in kilometers: " + results[0]);
//            mMap.addMarker(new MarkerOptions().position(latLng));
//
//        }


        toStart();
        //If you only need one location, unregister the listener
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void toStart() {
        mp.start();

        //zoom to current position:
        if(game == null) return;
        CameraPosition cameraPosition = new CameraPosition.Builder().target(game.getFirstLatLng()).zoom(14).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Select Map Type";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = mMap.getMapType() - 1;

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Locally create a finalised object.

                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 1:
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case 3:
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            default:
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        dialog.dismiss();
                    }
                }
        );

        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        game = Game.loadTheLastGame(this);

        if (game != null && game.isCompass || game == null && Settings.isCompass) {
            image.setVisibility(View.VISIBLE);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_GAME);
        } else {
            image.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (game != null)
            game.saveIt(this);

        if (game != null && game.isCompass || game == null && Settings.isCompass) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        ra.setDuration(210);
        ra.setFillAfter(true);
        image.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
}
