package com.locationlocationlocation.accelereomteranalyser;


import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

    //private ImageButton stopButton;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner = (Spinner) findViewById(R.id.activity_spinner);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.activities_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        //stopButton = (ImageButton)findViewById(R.id.stop_button);
        startButton = (Button)findViewById(R.id.start_button);
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
        ((Button)findViewById(R.id.stop_button)).setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
        Toast.makeText(this, MainActivity.STARTED_RECORDING, Toast.LENGTH_LONG).show();
    }

    public void stopRecording(View view) {
        ((Button)findViewById(R.id.start_button)).setVisibility(View.VISIBLE);
        view.setVisibility(View.GONE);
        Toast.makeText(this, MainActivity.STOPPED_RECORDING, Toast.LENGTH_LONG).show();
    }
}
