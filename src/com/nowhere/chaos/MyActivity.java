package com.nowhere.chaos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;

import static android.view.View.*;

public class MyActivity extends Activity implements OnClickListener, OnKeyListener,
        TextView.OnEditorActionListener {


    public static final String TAG = "pukach";          // log tag
    public static EditText inputString;
    public Menu myMenu;
    final int DIALOG_ABOUT = 1;
    final int DIALOG_ERASE = 2;

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_1);

        Button btOk, btCancel, button2Act, button3Act;

        Log.d(TAG, "1 activity created");

        // find all my View

        inputString = (EditText) findViewById(R.id.editText1);
        btOk = (Button) findViewById(R.id.buttonOk);
        btCancel = (Button) findViewById(R.id.buttonCancel);
        button2Act = (Button) findViewById(R.id.button2Act);
        button3Act = (Button) findViewById(R.id.button3Act);

        // set MyActivity as onKey and onClick handler for input window buttons
        // ... set hardware keyboard handler
        inputString.setOnKeyListener(this);
        // ... set software keyboard handler
        inputString.setOnEditorActionListener(this);
        btOk.setOnClickListener(this);
        btCancel.setOnClickListener(this);
        button2Act.setOnClickListener(this);
        button3Act.setOnClickListener(this);

    }

    public void onClick(View v) {

        Intent intent;
        switch (v.getId()) {

            // check for input rules, write to file if ok and clean input
            case R.id.buttonOk:
                Log.d(TAG, "Presed ОК button");
                textCheck(inputString.getText().toString());
                inputString.setText(null);
                break;

            // clean input and set hint
            case R.id.buttonCancel:
                Log.d(TAG, "Pressed Cancel button");
                inputString.setText(null);
                inputString.setHint(getString(R.string.short_prompt));
                break;

            // call 2 screen and clean input
            case R.id.button2Act:
                Log.d(TAG, "Pressed Strings button");
                intent = new Intent(this, MyActivity2.class);
                startActivity(intent);
                inputString.setText(null);
                break;
            // call 3 screen and clean input
            case R.id.button3Act:
                Log.d(TAG, "Pressed URL button");
                intent = new Intent(this, MyActivity3.class);
                startActivity(intent);
                inputString.setText(null);
                break;


        }


    }

    // handle hardware keyboard "Enter"
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        Log.d(TAG, "OnKey called (Hardware keyboard)");

        // if "Enter" pressed check text for validity and clean input
        if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            Log.d(TAG, "OnKey catched Enter");
            textCheck(inputString.getText().toString());
            inputString.setText(null);
            return true;
        }
        return false;
    }


    // check text for input criteria, if ok write it to file
    private void textCheck(String str) {

        Resources res = getResources();
        int limit = res.getInteger(R.integer.string_size_limit);
        String pattern = res.getString(R.string.pattern);

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
        Log.d(TAG, "1 activity paused");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "1 activity resumed");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "1 activity stoped");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "1 activity restarted");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "1 activity started");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "1 activity destroyed");
    }


    private void writeToFile(String str) {

        Resources res = getResources();
        String file = res.getString(R.string.str_file_name);
        BufferedWriter bw = null;
        //       File f;

        Log.d(TAG, "passed string: " + str);
        Log.d(TAG, "file name: " + file);

        // did not used "try with resources" for compatibility with API lower than 19
        try {
            bw = new BufferedWriter(new OutputStreamWriter(openFileOutput(file, Context.MODE_APPEND)));
            bw.write(str + "\n");
            // here I'm just find where is my file stored
//            f = getFilesDir();
//            Log.d(TAG, "file stored in: " + f.getCanonicalPath());

        } catch (FileNotFoundException exc) {
            Log.d(TAG, "File not found");
            Toast.makeText(this, res.getString(R.string.err_file_not_found), Toast.LENGTH_LONG).show();
        } catch (IOException exc) {
            Log.d(TAG, "IO Exception");
            Toast.makeText(this, res.getString(R.string.err_io), Toast.LENGTH_LONG).show();
        }
        finally {
            if(bw != null)
                try{
                    bw.close();
                }catch (IOException exc) {
                    Log.d(TAG, "IO Exception");
                    Toast.makeText(this, res.getString(R.string.err_io), Toast.LENGTH_LONG).show();
                }
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {

        Resources res = getResources();

        menu.add(0, 1, 0, res.getString(R.string.menu_delete));
        menu.add(0, 2, 1, res.getString(R.string.menu_about));

        return super.onCreateOptionsMenu(menu);

    }

    public boolean onOptionsItemSelected(MenuItem mi) {

        Resources res = getResources();

        switch (Integer.valueOf(mi.getItemId())) {

            case (1):
                Log.d(TAG, "Menu selected: " + res.getString(R.string.menu_delete));

                // show prompt "delete or not delete" :)
                showDialog(DIALOG_ERASE);
                break;
            case (2):
                Log.d(TAG, "Menu selected: " + res.getString(R.string.menu_about));
                showDialog(DIALOG_ABOUT);
                break;

        }

        return super.onOptionsItemSelected(mi);

    }

    // since I've yet did not know fragments I'll use deprecated methods
    protected Dialog onCreateDialog(int id) {

        Resources res = getResources();
        AlertDialog.Builder adb;

        switch (id) {
            case (DIALOG_ABOUT):
                adb = new AlertDialog.Builder(this);
                adb.setMessage(res.getString(R.string.about_text));
                adb.setTitle(res.getString(R.string.menu_about));
                adb.setNeutralButton(res.getString(R.string.button_ok), menuClickHandler);
                return adb.create();

            case (DIALOG_ERASE):
                adb = new AlertDialog.Builder(this);
                adb.setMessage(res.getString(R.string.msg_delete_promtp));
                adb.setTitle(res.getString(R.string.menu_delete));
                adb.setPositiveButton(res.getString(R.string.button_ok), menuClickHandler);
                adb.setNegativeButton(res.getString(R.string.button_cancel), menuClickHandler);
                return adb.create();
        }
        return super.onCreateDialog(id);
    }
    //   1l0O


    DialogInterface.OnClickListener menuClickHandler = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Resources res = getResources();
            switch (which) {
                case Dialog.BUTTON_NEUTRAL:
                    dialog.dismiss();
                    break;
                case Dialog.BUTTON_POSITIVE:
                    // just try to delete file and make the toash with result message 0
                    if (deleteFile(res.getString(R.string.str_file_name))) {
                        Log.d(TAG, "file chaos.dat deleted");
                        Toast.makeText(MyActivity.this, res.getString(R.string.msg_deleted), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MyActivity.this, res.getString(R.string.msg_not_deleted), Toast.LENGTH_LONG).show();
                        Log.d(TAG, "file chaos.dat was not deleted");
                    }
                    dialog.dismiss();
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        }
    };



    // handle "Enter" from software keyboard. don't forget that event can be null!
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        Log.d(TAG, "onEditorAction (Software keyboard) actionId: " + actionId+ " event: " + event);

        // if "Enter" pressed check text for validity and clean input
        if ((actionId == EditorInfo.IME_NULL) || (actionId == EditorInfo.IME_ACTION_DONE)) {
            Log.d(TAG, "onEditorAction catched Done");
            textCheck(inputString.getText().toString());
            inputString.setText(null);
            return true;
        }
        return false;
    }


}


