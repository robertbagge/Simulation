package com.locationlocationlocation.simulation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.IBinder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SensorService extends Service {
    private static String SERVICE_TAG = "Service";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mMagnetometer;
    private SensorEventListener mAccelerometerListener, mMagnetometerListener;
    float[] mAcceleration, mGeomagnetic;
    private FileWriter writer;
    private String currentActivityName;
    private StringBuffer sb;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int hz = intent.getIntExtra("hz", SensorManager.SENSOR_DELAY_FASTEST);
        this.currentActivityName = intent.getStringExtra("currentactivity");
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometerListener = new AccelerometerEventListener();
        mMagnetometerListener = new MagnetometerEventListener();
        mSensorManager.registerListener(mAccelerometerListener, mAccelerometer, hz);
        mSensorManager.registerListener(mMagnetometerListener, mMagnetometer, hz);
        this.sb = new StringBuffer();


        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        mSensorManager.unregisterListener(mAccelerometerListener);
        mSensorManager.unregisterListener(mMagnetometerListener);
        writeToFile();
    }

    private void writeToFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_dd_MM_HH_mm_ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        String timestamp = sdf.format(calendar.getTime());
        String folderPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/AccelerometerData";
        if(folderExists(folderPath)) {
            try {
                String filePath = folderPath + "/" + timestamp + "_" + currentActivityName + ".txt";
                writer = new FileWriter(filePath, true);
                writer.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean folderExists(String folderPath){
        File folder = new File(folderPath);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        return success;
    }


    private class AccelerometerEventListener implements SensorEventListener {
        long recordingStartingTime = 0;
        @Override
        public void onSensorChanged(SensorEvent event){
            //if (recordingStartingTime == 0)
            //    recordingStartingTime = event.timestamp;

            mAcceleration = event.values;
            new SensorLoggerTask().execute(new SensorEventValueHolder(event.timestamp, mAcceleration[0], mAcceleration[1], mAcceleration[2], event.sensor.getType()));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private class MagnetometerEventListener implements SensorEventListener{
        long recordingStartingTime = 0;
        @Override
        public void onSensorChanged(SensorEvent event){

            //if (recordingStartingTime == 0)
            //    recordingStartingTime = event.timestamp;
            mGeomagnetic = event.values;
            if (mAcceleration != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mAcceleration, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);// orientation contains: azimut, pitch and roll
                    new SensorLoggerTask().execute(new SensorEventValueHolder(event.timestamp, 57.2957795f * orientation[0], 57.2957795f * orientation[1], 57.2957795f * orientation[2], event.sensor.getType()));
                }
            }

        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
    private class SensorEventValueHolder{
        long timestamp;
        float value1,value2,value3;
        int type;

        public SensorEventValueHolder(long timestamp, float value1, float value2, float value3, int type){
            this.timestamp = timestamp;
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
            this.type = type;
        }
    }

    private class SensorLoggerTask extends
            AsyncTask<SensorEventValueHolder, Void, Void> {
        @Override
        protected Void doInBackground(SensorEventValueHolder... events) {
            //Log.d(SERVICE_TAG, String.valueOf(events.length));
            for(SensorEventValueHolder holder: events) {
                sb.append(holder.timestamp + ", " + holder.value1 +
                        ", " + holder.value2 + ", " + holder.value3 + ", " + holder.type + "\n");
            }
            return null;
        }
    }
}
