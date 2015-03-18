package com.locationlocationlocation.accelereomteranalyser;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class AccelerometerActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccerlerometer;
    private float x,y,z;
    private TextView acceleration;
    private FileWriter writer;
    private Button startButton;
    private static String TAG = "Just det";
    private static String STARTED_RECORDING = "Started recording, press stop to stop the recording and save the results to a file";
    private static String STOPPED_RECORDING = "Stopped recording, the results has been saved to a file at: ";
    private static String FAULTY_SETTINGS = "You need to choose a sample rate and an activity";
    private enum Activities {NONE, SITTING, STANDING, WALKING, CYCLING, GOING_BY_CAR, GOING_BY_BUS, GOING_BY_TRAIN}
    private enum SamplingRates {NONE, Hz23, Hz46, Hz100, Hz200}
    private Activities currentActivity;
    private SamplingRates currentSamplingRate;
    private Context context;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccerlerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        acceleration = (TextView)findViewById(R.id.acceleration);

        Spinner spinner = (Spinner) findViewById(R.id.activity_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        Spinner activitySpinner = (Spinner) findViewById(R.id.activity_spinner);

        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(this, R.array.activities_array, android.R.layout.simple_spinner_item);

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
        getMenuInflater().inflate(R.menu.menu_accelerometer, menu);
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
            Toast.makeText(this, AccelerometerActivity.STARTED_RECORDING, Toast.LENGTH_LONG).show();
            startScan();

        }else{
            Toast.makeText(this, AccelerometerActivity.FAULTY_SETTINGS, Toast.LENGTH_LONG).show();
        }
    }

    public void stopRecording(View view) {
        ((Button)findViewById(R.id.start_button)).setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
        stopScan();
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];
        acceleration.setText("X: " + x + "\nY: " + y + "\nZ: " + z);
        try {
            writer.write(x + "," + y + "," + z + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void startScan(){
        Log.d(TAG, "Samlingrate: " + currentSamplingRate.name());
        mSensorManager.registerListener(this, mAccerlerometer, hzToMys(currentSamplingRate.name()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_dd_MM_HH_mm_ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        String timestamp = sdf.format(calendar.getTime());
        String folderPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/AccelerometerData";
        if(folderExists(folderPath)) {
            try {
                filePath = folderPath + "/" + timestamp + "_" + currentActivity.name() + "_acc.txt";
                writer = new FileWriter(filePath, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int hzToMys(String hzString) {
        int hz = Integer.valueOf(hzString.substring(2));
        return 1000000/hz;
    }

    private void stopScan(){
        mSensorManager.unregisterListener(this, mAccerlerometer);
        if(writer != null) {
            try {
                Toast.makeText(this, AccelerometerActivity.STOPPED_RECORDING + "\n" +  filePath, Toast.LENGTH_LONG).show();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateUserInput(){
        boolean validated = false;
        if(currentActivity != Activities.NONE && currentSamplingRate != SamplingRates.NONE){
            validated = true;
        }

        return validated;
    }


    private boolean folderExists(String folderPath){
        File folder = new File(folderPath);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        return success;
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
