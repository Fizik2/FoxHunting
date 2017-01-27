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
import android.widget.TextView;

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

import java.util.Calendar;
import java.util.List;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {

    private final float MIN_HZ = 0.1f;
    private final float MAX_HZ = 3.0f;

    private ImageView image;
    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private static final String LOG_TAG = "MapActivity";

    private boolean continueCheckFox = false;
    private boolean isCreateLocation = false;
    //private boolean continueListenFox = false;


    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    private static final CharSequence[] MAP_TYPE_ITEMS = {"Дорожная карта", "Гибрид", "Спутниковая", "Рельеф"};

    private MediaPlayer mp;
    private Game game;

    private LatLng currentLatLng;

    private TextView currentMsg;
    CustomGauge gauge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mp = MediaPlayer.create(this, R.raw.s2);


        image = (ImageView) findViewById(R.id.imageViewCompass);
        currentMsg = (TextView) findViewById(R.id.currentMsg);
        gauge = (CustomGauge) findViewById(R.id.gauge1);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    private void init() {
        mMap.getUiSettings().setCompassEnabled(true);

        //boolean needSetPointer = game != null && game.isPointer || game == null && Settings.isPointer;
        boolean needSetPointer = Settings.isPointer;
        mMap.getUiSettings().setMyLocationButtonEnabled(needSetPointer);

        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        mMap.setMyLocationEnabled(needSetPointer);

        if (game != null) {
            LatLng[] latLngs = game.getAllFoxes();
            for (byte i = 0; i < latLngs.length; i++) {
                if (game.isFoundFox(i))
                    mMap.addMarker(new MarkerOptions().position(latLngs[i]));
            }

        }
    }


    public void onClickToStart(View view) {
        toStart();
    }

    public void onClickTypeMap(View view) {
        showMapTypeSelectorDialog();
    }

    private void msgAfterGiveUp() {
        long endTime = Calendar.getInstance().getTimeInMillis();
        long diff = endTime - game.startTime;
        int durationGameHours = (int) (diff / 1000 / 60 / 60);
        int durationGameMinutes = (int) (diff / 1000 / 60) - durationGameHours * 60;


        int foundFoxCount = 0;
        for(byte i = 0; i < game.getAllFoxes().length; i++){
            foundFoxCount += game.isFoundFox(i) ? 1 : 0;
        }

        String msg = "";
        if(foundFoxCount == 0)
            msg += "Вы не нашли ни одной лисы за ";
        else
            msg += "Вы нашли " + foundFoxCount + " лис(ы) за ";


        if (durationGameHours != 0)
            msg += durationGameHours + ":" + durationGameMinutes;
        else
            msg += durationGameMinutes + " минут(ы)";

        new AlertDialog.Builder(this)
                .setIcon(R.drawable.attention512)
                .setTitle("Игра закончена!")
                .setMessage(msg)
                .setPositiveButton("Ок", null)
                .show();


    }

    public void onClickGiveUp(View view) {
        if (game == null) return;

        final Activity activity = this;
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.attention512)
                .setTitle("Сдаться")
                .setMessage("Вы действительно хотите сдаться?")
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        msgAfterGiveUp();

                        LatLng[] latLngs = game.getAllFoxes();
                        for (LatLng latLng : latLngs) {
                            mMap.addMarker(new MarkerOptions().position(latLng));
                        }

                        continueCheckFox = false;
                        game.removeIt(activity);
                        game = null;

                    }
                })
                .setNegativeButton("Нет", null)
                .show();


    }

    private void finishGame() {
        long endTime = Calendar.getInstance().getTimeInMillis();
        long diff = endTime - game.startTime;
        int durationGameHours = (int) (diff / 1000 / 60 / 60);
        int durationGameMinutes = (int) (diff / 1000 / 60) - durationGameHours * 60;

        game.removeIt(this);
        game = null;
        continueCheckFox = false;


        String msg = "Поздравляем, Вы нашли всех лис за ";
        if (durationGameHours != 0)
            msg += durationGameHours + ":" + durationGameMinutes;
        else
            msg += durationGameMinutes + " минут(ы)";

        new AlertDialog.Builder(this)
                .setIcon(R.drawable.attention512)
                .setTitle("Игра закончена!")
                .setMessage(msg)
                .setPositiveButton("Ок", null)
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

        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
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

        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (!isCreateLocation) {
            isCreateLocation = true;
            continueCheckFox = true;
            listenFoxes();
            toStart();

        }

        //If you only need one location, unregister the listener
        if (game != null && game.areAllFound()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            finishGame();
        } else {
            if (game != null && !game.areAllFound()) {
                LatLng[] latLngs = game.getAllFoxes();
                for (byte i = 0; i < latLngs.length; i++) {
                    if (game.isFoundFox(i)) continue;
                    Log.e(LOG_TAG, "Distansce to " + i + " fox =" + game.getFoxDistance(latLngs[i], currentLatLng));
                    if (game.getFoxDistance(latLngs[i], currentLatLng) <= game.eraseDistance) {


                        try {
                            game.foxFound(i);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mMap.addMarker(new MarkerOptions().position(latLngs[i]));
                        if (game.areAllFound()) break;

                        new AlertDialog.Builder(this)
                                .setIcon(R.drawable.attention512)
                                .setTitle("Есть!")
                                .setMessage("Ура! Лиса найдена!")
                                .setPositiveButton("Ок", null)
                                .show();
                    }
                }
            }
        }
    }


    private void toStart() {


        //zoom to current position:
        if (game == null) return;
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

        if (game != null && !game.areAllFound()) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


        }

        game = Game.loadTheLastGame(this);

        continueCheckFox = true;
        if (game != null && isCreateLocation) {
            listenFoxes();
        }
        //if (game != null && game.isCompass || game == null && Settings.isCompass) {
        if (Settings.isCompass) {
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

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        if (mMap != null) {
            mMap.clear();
        }
        continueCheckFox = false;
        if (game != null)
            game.saveIt(this);

        //if (game != null && game.isCompass || game == null && Settings.isCompass) {
        if (Settings.isCompass) {
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

    private void listenFoxes() {
        new Thread() {
            @Override
            public void run() {
                super.run();

                while (continueCheckFox && game != null && !game.areAllFound()) {
                    //List<LatLng> latLngs = game.getCurrentFoxes();
                    LatLng[] latLngs = game.getAllFoxes();
                    for (byte i = 0; i < latLngs.length; i++) {
                        if (!continueCheckFox) break;
                        if (game.isFoundFox(i)) continue;
                        int ticks = 0;
                        showAFox(latLngs[i], i + 1);
                        float intense;
                        float hz;
                        if (game != null)
                            intense = game.getFoxIntensePercent(latLngs[i], currentLatLng);
                        else
                            intense = 0;


                        if (intense < 0) {
                            hz = MIN_HZ;
                            if (intense < -1)
                                intense = -1;
                        } else {
                            hz = MIN_HZ + (MAX_HZ - MIN_HZ) * intense; // 0.5 - 10
                        }


                        Log.e(LOG_TAG, "Current intense " + intense);
                        Log.e(LOG_TAG, "Current hz " + hz);
                        long mls = (long) (1000 * 1.0 / hz);
                        while (continueCheckFox && game != null && ticks < game.foxDuration * hz) {
                            if(ticks % hz == 0)
                                indicateFox(intense);

                            ticks++;
                            foxSound();
                            try {
                                Thread.sleep(mls);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    int seconds = 0;
                    showAFox(null, 0);
                    indicateFox(0);
                    while (continueCheckFox && game != null && seconds < game.foxDuration) {
                        seconds++;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
                //continueListenFox = false;
            }
        }.start();
    }

    private void showAFox(LatLng latLng, final int i) {
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                if (i != 0)
                    currentMsg.setText("Лиса №" + i);
                else
                    currentMsg.setText("");
            }
        };
        runOnUiThread(r1);
    }

    private void foxSound() {
        if (Settings.isAudiosignal)
            mp.start();
    }

    private void indicateFox(final float intense) {
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                if (intense < 0) {
                    gauge.setPointStartColor(getResources().getColor(R.color.blue));
                    gauge.setValue(-(int) (intense * 100));
                } else {
                    gauge.setPointStartColor(getResources().getColor(R.color.red));
                    gauge.setValue((int) (intense * 100));
                }
            }
        };
        runOnUiThread(r1);
    }

}
