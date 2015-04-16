package com.nowhere.chaos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;

import static android.view.View.*;

public class MyActivity extends Activity implements OnClickListener, OnKeyListener {


    public static final String TAG = "pukach"; // log tag
    public static EditText inputString; // input window on main screen
    public Menu myMenu;
    final int DIALOG_ABOUT = 1;
    final int DIALOG_ERASE = 2;


    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button btOk, btCancel, buttonSecAct; // buttons on main screen

        Log.d(TAG, "Primary activity created");


        // find all my View
        inputString = (EditText) findViewById(R.id.editText1);
        btOk = (Button) findViewById(R.id.buttonOk);
        btCancel = (Button) findViewById(R.id.buttonCancel);
        buttonSecAct = (Button) findViewById(R.id.buttonSecAct);

        // set MyActivity as onKey and onClick handler for input window buttons
        inputString.setOnKeyListener(this);
        btOk.setOnClickListener(this);
        btCancel.setOnClickListener(this);
        buttonSecAct.setOnClickListener(this);



    }

    public void onClick(View v) {
        switch (v.getId()) {

            // check for input rules, write to file if ok and clean input
            case R.id.buttonOk:
                Log.d(TAG, "Presed ОК");
                textCheck(inputString.getText().toString());
                inputString.setText(null);
                break;

            // clean input and set hint
            case R.id.buttonCancel:
                Log.d(TAG, "Pressed Cancel");
                inputString.setText(null);
                inputString.setHint(getString(R.string.short_prompt));
                break;

            // call second screen and clean input
            case R.id.buttonSecAct:
                Log.d(TAG, "Pressed Второй экран");
                Intent intent = new Intent(this, SecondActivity.class);
                startActivity(intent);
                inputString.setText(null);
                break;


        }


    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {

        // if "Enter" pressed check text for validity and clean input
        if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {

            textCheck(inputString.getText().toString());
            inputString.setText(null);
            return true;
        }
        return false;
    }


    private void textCheck(String str) {

        Resources res = getResources();
        int limit = res.getInteger(R.integer.string_size_limit); // get string limit
        String pattern = res.getString(R.string.pattern); // get validity pattern

        Log.d(TAG, "limit == " + limit);
        Log.d(TAG, "pattern == " + pattern);
        Log.d(TAG, "isEmpty == " + Boolean.toString(str.isEmpty()));
        Log.d(TAG, "string length == " + str.length());
        Log.d(TAG, "string == \"" + str + "\"");
        Log.d(TAG, "string matches pattern == " + Boolean.toString(str.matches(pattern)));

        // if not empty, shorter than limit and pass validity pattern - write to file and set "Ok" input hint
        if (!str.isEmpty() && str.length() <= limit && str.matches(pattern)) {
            writeToFile(str);
            inputString.setHint(getString(R.string.accepted));
        } else {
            //set input window hint to initial prompt and make a toast with appropriate error message
            inputString.setHint(getString(R.string.short_prompt));

            if (str.isEmpty()) {
                Toast.makeText(this, res.getString(R.string.err_too_short), Toast.LENGTH_LONG).show();
            } else if (str.length() > limit) {
                Toast.makeText(this, res.getString(R.string.err_too_long), Toast.LENGTH_LONG).show();
            } else if (!str.matches(pattern)) {
                Toast.makeText(this, res.getString(R.string.err_illegal_input), Toast.LENGTH_LONG).show();
            }

        }


    }

    // mostly for understanding activity life cycle
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Primary activity paused");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Primary activity resumed");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Primary activity stoped");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "Primary activity restarted");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Primary activity started");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Primary activity destroyed");
    }


    private void writeToFile(String str) {

        Resources res = getResources();
        String file = res.getString(R.string.file_name);
        BufferedWriter bw;
 //       File f;

        Log.d(TAG, "passed string: " + str);
        Log.d(TAG, "file name: " + file);

        // did not used "try with resources" for compatibility with API lower than 19
        try {
            bw = new BufferedWriter(new OutputStreamWriter(openFileOutput(file, Context.MODE_APPEND))); // open file
            bw.write(str + "\n"); // write checked string + new line char
            // here I'm just find where is my file stored
//            f = getFilesDir();
//            Log.d(TAG, "file stored in: " + f.getCanonicalPath());
            bw.close(); // close file

        } catch (FileNotFoundException exc) {
            Log.d(TAG, "File not found");
            Toast.makeText(this, res.getString(R.string.err_file_not_found), Toast.LENGTH_LONG).show();
        } catch (IOException exc) {
            Log.d(TAG, "IO Exception");
            Toast.makeText(this, res.getString(R.string.err_io), Toast.LENGTH_LONG).show();
        }

    }

    public boolean onCreateOptionsMenu(Menu menu){

        Resources res = getResources();

        menu.add(0, 1, 0, res.getString(R.string.menu_delete));
        menu.add(0, 2, 1, res.getString(R.string.menu_about));

        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected( MenuItem mi){

        Resources res = getResources();

        switch (Integer.valueOf(mi.getItemId())){

            case (1):
                Log.d(TAG, "Menu selected: " + res.getString(R.string.menu_delete));
                // NOTE: file will be erased without prompt

                if(deleteFile(res.getString(R.string.file_name)))
                    Toast.makeText(this, res.getString(R.string.msg_deleted), Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this, res.getString(R.string.msg_not_deleted), Toast.LENGTH_LONG).show();

                break;
            case (2):
                Log.d(TAG, "Menu selected: " + res.getString(R.string.menu_about));
//                Toast.makeText(this, res.getString(R.string.about_text), Toast.LENGTH_LONG).show();
                showDialog(DIALOG_ABOUT);
                break;

        }

        return super.onOptionsItemSelected(mi);

    }

    // since I've yet did not know fragments I'll use deprecated methods
    protected Dialog onCreateDialog(int id){

        Resources res = getResources();

        switch (id){
            case (DIALOG_ABOUT):
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setMessage(res.getString(R.string.about_text));
                adb.setTitle(res.getString(R.string.menu_about));
//                adb.setNeutralButton("OK", menuClickHandler);
                return adb.create();
            case (DIALOG_ERASE):
                break;
        }
        return super.onCreateDialog(id);
    }

/*    OnClickListener menuClickHandler = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case Dialog.BUTTON_NEUTRAL:
                    finish();
                case Dialog.BUTTON_POSITIVE:
                    finish();
                case Dialog.BUTTON_NEGATIVE:
                    finish();
            }

        }
    };*/
}
