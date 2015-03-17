package com.locationlocationlocation.accelereomteranalyser;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends Activity {

    private static String TAG = "Just det";
    private static String STARTED_RECORDING = "Started recording, press stop to stop the recording and save the results to a file";
    private static String STOPPED_RECORDING = "Stopped recording, the results has been saved to a file at: ";
    private static String FAULTY_SETTINGS = "You need to choose a sample rate and an activity";

    private enum Activities {NONE, SITTING, STANDING, WALKING, CYCLING, GOING_BY_CAR, GOING_BY_BUS, GOING_BY_TRAIN}
    private enum SamplingRates {NONE, Hz23, Hz46, Hz100, Hz200}
    private Activities currentActivity;
    private SamplingRates currentSamplingRate;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner activitySpinner = (Spinner) findViewById(R.id.activity_spinner);

        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(this,
                R.array.activities_array, android.R.layout.simple_spinner_item);

        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        activitySpinner.setAdapter(activityAdapter);
        activitySpinner.setOnItemSelectedListener(new ActivitiesOnItemSelectedListener());

        Spinner sampleRateSpinner = (Spinner) findViewById(R.id.sample_rate_spinner);

        ArrayAdapter<CharSequence> sampleRateAdapter = ArrayAdapter.createFromResource(this,
                R.array.sample_rate_array, android.R.layout.simple_spinner_item);

        sampleRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sampleRateSpinner.setAdapter(sampleRateAdapter);
        sampleRateSpinner.setOnItemSelectedListener(new SamplingRatesOnItemSelectedListener());

        currentActivity = Activities.NONE;
        currentSamplingRate = SamplingRates.NONE;
        context = getApplicationContext();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void startRecording(View view) {

        if (validateUserInput()){
            ((Button)findViewById(R.id.stop_button)).setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
            Toast.makeText(this, MainActivity.STARTED_RECORDING, Toast.LENGTH_LONG).show();
            startScan();

        }else{
            Toast.makeText(this, MainActivity.FAULTY_SETTINGS, Toast.LENGTH_LONG).show();
        }

    }

    public void stopRecording(View view) {
        ((Button)findViewById(R.id.start_button)).setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
        Toast.makeText(this, MainActivity.STOPPED_RECORDING, Toast.LENGTH_LONG).show();
    }

    private void startScan(){
        //TODO
    }

    private boolean validateUserInput(){
        boolean validated = false;
        if(currentActivity != Activities.NONE && currentSamplingRate != SamplingRates.NONE){
            validated = true;
        }

        return validated;
    }



    private class ActivitiesOnItemSelectedListener implements AdapterView.OnItemSelectedListener{

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            Log.d(TAG + "pos", Long.toString(parent.getItemIdAtPosition(pos)));
            Log.d(TAG + "pos pos ", Integer.toString(pos));
            //parent.getItemAtPosition(pos)
            currentActivity = Activities.values()[pos];
            Toast.makeText(context, currentActivity.name(), Toast.LENGTH_SHORT).show();
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }

    }

    private class SamplingRatesOnItemSelectedListener implements AdapterView.OnItemSelectedListener{

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            Log.d(TAG + "pos", Long.toString(parent.getItemIdAtPosition(pos)));
            Log.d(TAG + "pos pos ", Integer.toString(pos));
            //parent.getItemAtPosition(pos)
            currentSamplingRate = SamplingRates.values()[pos];
            Toast.makeText(context, currentSamplingRate.name(), Toast.LENGTH_SHORT).show();
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }

    }







}
