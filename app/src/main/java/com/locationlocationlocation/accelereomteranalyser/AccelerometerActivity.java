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
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class AccelerometerActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccerlerometer;
    private float x,y,z;
    private TextView acceleration;
    private FileWriter writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccerlerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        acceleration = (TextView)findViewById(R.id.acceleration);
    }

    public void onStartClick(View view) {
        //Send with SENSOR DELAY INTEGERS
        mSensorManager.registerListener(this, mAccerlerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onStopClick(View view) {
        mSensorManager.unregisterListener(this);
    }
    protected void onResume() {
        super.onResume();
        try {
            writer = new FileWriter("myfile.txt",true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onPause() {
        super.onPause();

        if(writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];
        acceleration.setText("X: " + x + "\nY: " + y + "\nZ: " + z);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private boolean folderExists(String folderPath){
        File folder = new File(folderPath);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        return success;
    }
}
