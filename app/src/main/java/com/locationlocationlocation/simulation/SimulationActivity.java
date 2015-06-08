/*
 * Copyright (C) 2015 Robert Bagge
 * Copyright (C) 2015 William Martinsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.locationlocationlocation.simulation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class SimulationActivity extends Activity {
    private static String TAG = "Just det";
    private static String STARTED_RECORDING = "Started recording, press stop to stop the recording and save the results to a file";
    private static String STOPPED_RECORDING = "Stopped recording, the results has been saved to a file at: ";
    private static String FAULTY_SETTINGS = "You need to choose a sample rate and an activity";
    private Context context;
    private String appFolderPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation);
        appFolderPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeviceSimulation";
        folderExists(appFolderPath);

        context = getApplicationContext();

    }

    public void startRecording(View view) {

        EditText runName = (EditText)findViewById(R.id.run_name);
        String runNameStr = runName.getText().toString();
        File folder = new File(appFolderPath + "/" + runNameStr);
        if(folder.exists() || runNameStr == ""){
            Toast.makeText(this, "You need to type run name", Toast.LENGTH_SHORT).show();
        }else{
            ((Button)findViewById(R.id.stop_button)).setVisibility(View.VISIBLE);
            view.setVisibility(View.GONE);
            Toast.makeText(this, "startScan()", Toast.LENGTH_SHORT).show();
            folder.mkdir();
            startScan(folder.getAbsolutePath());
            //Start service
        }
        //startScan();
    }


    public void stopRecording(View view) {
        ((Button) findViewById(R.id.start_button)).setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
        Toast.makeText(this, "stopScan()", Toast.LENGTH_SHORT).show();
        stopScan();
    }

    public void startScan(String folderName){
        Intent intent = new Intent(getApplicationContext(), SensorService.class );
        intent.putExtra("folder_name", folderName);
        startService(intent);

    }

    public void stopScan(){
        Toast.makeText(this, "stopScan()", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, SimulationActivity.STOPPED_RECORDING, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), SensorService.class);
        stopService(intent);
    }

    public boolean validateRunName(String runNameStr){
        File folder = new File(appFolderPath + "/" + runNameStr);
        if(folder.exists() || runNameStr == ""){
            return false;
        }else{
            return true;
        }
    }

    private boolean folderExists(String folderPath){
        File folder = new File(folderPath);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        return success;
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
}
