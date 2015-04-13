package com.locationlocationlocation.simulation;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SensorService extends Service {
    private static String TAG;
    private static String SERVICE_TAG = "Service";

    private static String FOLDER_NAME_ACCELEROMETER = "accelerometer_data";
    private static String FOLDER_NAME_WIFI = "wifi_data";

    private final static String WIFI_DATA_FILE = "wifi_data.txt";
    private final static String ACCELEROMETER_DATA_FILE = "accelerometer_data.txt";
    OutputStreamWriter mOutputWifi;
    OutputStreamWriter mOutputAccelerometer;

    private String mFolderPathWifi;
    private String mFolderPathAccelerometer;

    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiReceiver;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mMagnetometer;
    private SensorEventListener mAccelerometerListener, mMagnetometerListener;
    float[] mAcceleration, mGeomagnetic;
    private FileWriter writer;
    private String currentActivityName;
    private StringBuffer sb;
    private static int ACCELEROMETER_FREQUENCY = 100;
    private Handler wHandler;
    private Timer timer;
    private TimerTask doAsynchronousTask;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TAG = SensorService.this.getClass().getSimpleName();
        Log.e(TAG, "onStartCommand()");
        /*mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometerListener = new AccelerometerEventListener();
        mMagnetometerListener = new MagnetometerEventListener();
        mSensorManager.registerListener(mAccelerometerListener, mAccelerometer, this.ACCELEROMETER_FREQUENCY);
        mSensorManager.registerListener(mMagnetometerListener, mMagnetometer, this.ACCELEROMETER_FREQUENCY);
        this.sb = new StringBuffer();*/

        String sourceFolderName = intent.getStringExtra("folder_name");
        mFolderPathWifi = sourceFolderName + "/" + this.FOLDER_NAME_WIFI;
        mFolderPathAccelerometer = sourceFolderName + "/" + this.FOLDER_NAME_ACCELEROMETER;
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //mWifiReceiver = new WifiScanReceiver();


        try{
            File mWifiOutputFile = new File(sourceFolderName + "/" + WIFI_DATA_FILE);
            FileOutputStream mWifiOutputStream = new FileOutputStream(mWifiOutputFile);
            mOutputWifi = new OutputStreamWriter(mWifiOutputStream);
            mOutputWifi.write("Run initiated" + System.lineSeparator());
            Log.e(TAG, "Writing to mOutputWifi initiated");
        }catch(FileNotFoundException e1){
            e1.printStackTrace();
            Log.e(TAG, "Aborted, can't save to file");
        }catch(IOException e2){
            e2.printStackTrace();
        }

        try{
            File mAccelerometerOutputFile = new File(sourceFolderName + "/" + ACCELEROMETER_DATA_FILE);
            FileOutputStream mAccelerometerOutputStream = new FileOutputStream(mAccelerometerOutputFile);
            mOutputAccelerometer = new OutputStreamWriter(mAccelerometerOutputStream);
            mOutputAccelerometer.write("Run initiated" + System.lineSeparator());
            Log.e(TAG, "Writing to mOutputWifi initiated");
        }catch(FileNotFoundException e1){
            e1.printStackTrace();
            Log.e(TAG, "Aborted, can't save to file");
        }catch(IOException e2){
            e2.printStackTrace();
        }

        folderExists(mFolderPathWifi);
        folderExists(mFolderPathAccelerometer);

        Toast.makeText(this, sourceFolderName, Toast.LENGTH_LONG).show();
        AsyncTimer();


        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Log.e(TAG, "onDestroy()");
        timer.cancel();
        //mSensorManager.unregisterListener(mAccelerometerListener);
        //mSensorManager.unregisterListener(mMagnetometerListener);
        //unregisterReceiver(mWifiReceiver);

        //writeToFile();
        try {
            Log.e(TAG, "closing mOutputWifi");
            mOutputWifi.write("Run Closed" + System.lineSeparator());
            mOutputWifi.close();
        } catch (IOException e) {
            Log.e(TAG, "io exception when closing wifi output stream");
            e.printStackTrace();
        }
    }

    public void AsyncTimer() {
        wHandler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {

            @Override
            public void run() {
                wHandler.post(new Runnable() {
                    public void run() {
                        try {

                            long scanId = System.currentTimeMillis();
                            Log.e(TAG, "Timer task running: " + Long.toString(scanId));

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_dd_MM_HH_mm_ss");
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(scanId);
                            String timestamp = sdf.format(calendar.getTime());

                            WifiScan wifiScan = new WifiScan(mFolderPathWifi + "/" + timestamp + ".txt");
                            wifiScan.execute();
                        }catch (Exception e) {}
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 30000); // Repeat in every 30000 sec
    }

    // Async task - wifi scan
    public class WifiScan extends AsyncTask<Void, String, Void> {

        String filePath;

        public WifiScan(String filePath){
            this.filePath = filePath;
        }

        protected void onPreExecute() {

        }

        protected void onPostExecute(Void results) {

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.e(TAG, "Writing to wifi output stream new scan");
                mOutputWifi.write("===========================" + System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace();
            }
            registerReceiver(new WifiScanReceiver(this.filePath), new IntentFilter(
                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            mWifiManager.startScan();
            return null;
        }
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

    public class WifiScanReceiver extends BroadcastReceiver {
        String filePath;

        public WifiScanReceiver(String filePath){
            super();
            this.filePath = filePath;
        }
        @Override
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = mWifiManager.getScanResults();
            try {
                mOutputWifi.write(filePath + System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileWriter writer = null;
            try {
                writer = new FileWriter(filePath, true);

                List<String[]> data = new ArrayList<String[]>();
                ScanResult row = null;
                for(int i = 0; i < wifiScanList.size(); i++){
                    row = wifiScanList.get(i);
                    writer.write(row.BSSID + ", " + row.SSID + ", " + String.valueOf(convertFrequencyToChannel(row.frequency)) + ", " + String.valueOf(row.level) + System.lineSeparator());
                    //data.add(new String[] {row.BSSID, row.SSID, String.valueOf(convertFrequencyToChannel(row.frequency)), String.valueOf(row.level)});
                }
                /*for (int i = 0; i < wifiScanList.size(); i++) {
                    ScanResult sr1 = mWifiManager.getScanResults().get(i);
                    System.out.println("BSSID: " + sr1.BSSID);
                    System.out.println("RSSI: " + sr1.level);

                    String str = "BSSID: " + sr1.BSSID + " " + "Level: " + sr1.level;

                    try {
                        Log.e(TAG, "Writing to mOutputWifi onReceive");
                        mOutputWifi.write(str.toString() + System.lineSeparator());
                        writer.write(str.toString());
                    } catch (IOException e) {
                        Log.e(TAG, "Error when writing wifi scanResults to file");
                        e.printStackTrace();
                    }
                }*/
                //writer.writeAll(data);
                writer.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            unregisterReceiver(this);
        }
    }

    private static int convertFrequencyToChannel(int freq) {
        if (freq >= 2412 && freq <= 2484) {
            return (freq - 2412) / 5 + 1;
        } else if (freq >= 5170 && freq <= 5825) {
            return (freq - 5170) / 5 + 34;
        } else {
            return -1;
        }
    }
}
