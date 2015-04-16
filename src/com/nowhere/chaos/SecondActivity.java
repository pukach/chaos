package com.nowhere.chaos;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by pukach on 4/13/15.
 */
public class SecondActivity extends Activity {

    public TextView tw; // output view on secondary screen

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secondary);

        Log.d(MyActivity.TAG, "Secondary activity created");

        tw = (TextView) findViewById(R.id.outputText);

    }

    // mostly for understanding activity life cycle
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(MyActivity.TAG, "Secondary activity started");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // let's set main screen's input string hint from "accepted" to prompt
        MyActivity.inputString.setHint(getString(R.string.short_prompt));
        Log.d(MyActivity.TAG, "Secondary activity destroyed");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(MyActivity.TAG, "Secondary activity paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(MyActivity.TAG, "Secondary activity resumed");
        // when set visible read all saved data from file and display it
        readFromFile();

    }

    // mostly for understanding activity life cycle
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(MyActivity.TAG, "Secondary activity stoped");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(MyActivity.TAG, "Secondary activity restarted");
    }


    private void readFromFile() {

        Resources res = getResources();
        String file = res.getString(R.string.file_name);
        String str;
        BufferedReader br;

        // did not used "try with resources" for compatibility with API lower than 19
        try {
            br = new BufferedReader(new InputStreamReader(openFileInput(file))); // open file for reading

            // read until end of file and write lines to secondary screen
            while ((str = br.readLine()) != null) {
                Log.d(MyActivity.TAG, "readed from file: " + str);
                tw.append(str + "\n");
            }
            br.close(); // try to close file

        } catch (FileNotFoundException exc) {
            Log.d(MyActivity.TAG, "File not found");
            Toast.makeText(this, res.getString(R.string.err_file_not_found), Toast.LENGTH_LONG).show();
        } catch (IOException exc) {
            Log.d(MyActivity.TAG, "IO Exception");
            Toast.makeText(this, res.getString(R.string.err_io), Toast.LENGTH_LONG).show();
        }
    }

}