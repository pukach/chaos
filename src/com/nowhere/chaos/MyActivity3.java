package com.nowhere.chaos;

import android.app.Activity;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

/**
 * Created by pukach on 4/17/15.
 */
public class MyActivity3 extends Activity implements View.OnKeyListener {

    private static EditText inputUrl;
    private static TextView tw;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_3);

        inputUrl = (EditText) findViewById(R.id.editUrl);
        tw = (TextView) findViewById(R.id.md5TextView);

        inputUrl.setOnKeyListener(this);
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {

        Resources res = getResources();


        // if "Enter" pressed check text for validity, get page md5 and clean input
        if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {

            String str = inputUrl.getText().toString();

            Log.d(MyActivity.TAG, "onKey: " + str);

            try {
                // ...
                switch (new CheckUrl().execute(str).get()) {
                    case 0:
                        PageInfo pi = new PageInfo(str);
                        tw.append(pi.url + "\n");
                        tw.append(pi.md5sum + "\n");
                        inputUrl.setText(null);
                        break;
                    case 1:
                        Toast.makeText(MyActivity3.this, res.getString(R.string.err_bad_url), Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        Toast.makeText(MyActivity3.this, res.getString(R.string.err_cant_open_url), Toast.LENGTH_LONG).show();
                        break;
                }
                return true;

            } catch (InterruptedException ex) {
                Toast.makeText(MyActivity3.this, res.getString(R.string.err_interrupted), Toast.LENGTH_LONG).show();
                Log.d(MyActivity.TAG, "InterruptedException catched");
            } catch (ExecutionException ex) {
                Toast.makeText(MyActivity3.this, res.getString(R.string.err_interrupted), Toast.LENGTH_LONG).show();
                Log.d(MyActivity.TAG, "ExecutionException catched");
            }
        }

        return false;
    }


    // mostly for understanding activity life cycle
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(MyActivity.TAG, "3 activity paused");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(MyActivity.TAG, "3 activity resumed");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(MyActivity.TAG, "3 activity stoped");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(MyActivity.TAG, "3 activity restarted");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(MyActivity.TAG, "3 activity started");
    }

    // mostly for understanding activity life cycle
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(MyActivity.TAG, "3 activity destroyed");
    }


/*

    private void writeToFile(String str) {

        Resources res = getResources();
        String file = res.getString(R.string.tmp_file_name);
        BufferedWriter bw = null;
        Log.d(MyActivity.TAG, "passed string: " + str);
        Log.d(MyActivity.TAG, "file name: " + file);

        // did not used "try with resources" for compatibility with API lower than 19
        try {
            bw = new BufferedWriter(new OutputStreamWriter(openFileOutput(file, Context.MODE_APPEND))); // open file
            bw.write(str);
            // here I'm just find where is my file stored
//            f = getFilesDir();
//            Log.d(TAG, "file stored in: " + f.getCanonicalPath());

        } catch (FileNotFoundException exc) {
            Log.d(MyActivity.TAG, "File not found");
            Toast.makeText(this, res.getString(R.string.err_file_not_found), Toast.LENGTH_LONG).show();
        } catch (IOException exc) {
            Log.d(MyActivity.TAG, "IO Exception");
            Toast.makeText(this, res.getString(R.string.err_io), Toast.LENGTH_LONG).show();
        } finally {
            if (bw != null)
                try {
                    bw.close(); // try to close file
                } catch (IOException exc) {
                    Log.d(MyActivity.TAG, "IO Exception");
                    Toast.makeText(this, res.getString(R.string.err_io), Toast.LENGTH_LONG).show();
                }
        }

    }

*/
}

// just shell for PageCrawler
// create PageCrawler instance and wait until in make all work in background
class PageInfo {

    String url = null;
    String md5sum = null;
    //static boolean isPageChanged(); // check is page on specific url is changed
    //static boolean savePageInfo();  // write to file
    //static boolean retrievePageInfo(); // read from file
    //static boolean updatePageInfo(); //

    PageInfo(String str) {
        PageCrawler pc = new PageCrawler(str);
        try {
            pc.thread.join();
            Log.d(MyActivity.TAG, "PageInfo joined to PageCrawler");
        } catch (InterruptedException ex) {
            Log.d(MyActivity.TAG, "InterruptedException catched");
        }
        url = str;
        md5sum = pc.md5;
    }

}

// Read content of page by URL in separate thread and create MD5 hash
// used as inner mechanism for PageInfo to get all work done in background thread
class PageCrawler implements Runnable {

    Thread thread;
    String url_str;
    String md5;

    PageCrawler(String u) {
        thread = new Thread(this);
        url_str = u;
        Log.d(MyActivity.TAG, "PageCrawler created");
        thread.start();
    }

    public void run() {

        String page_content = null;
        BufferedReader reader = null;

        Log.d(MyActivity.TAG, "PageCrawler run() started");

        // read content of the page and store in to String
        try {
            URL url = null;
            String line = null;

            StringBuilder sb = new StringBuilder();
            // ... should add url control to avoid malformedurlexception
            url = new URL(this.url_str);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            while ((line = reader.readLine()) != null) sb.append(line);
            page_content = sb.toString();

            Log.d(MyActivity.TAG, "PageCrawler finished page reading");
        } catch (MalformedURLException ex) {
            Log.d(MyActivity.TAG, "MalformedURLException catched");
        } catch (IOException ex) {
            Log.d(MyActivity.TAG, "IOException catched");
        } catch (NullPointerException ex) {
            Log.d(MyActivity.TAG, "NullPointerException catched");

        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException ex) {
                    Log.d(MyActivity.TAG, "IOException catched");
                } catch (NullPointerException ex) {
                    Log.d(MyActivity.TAG, "NullPointerException catched");
                }
        }

        // get MD5 hashcode of page content
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            // ... should implement check for empty page_content
            md.update(page_content.getBytes("utf-8"));
            byte[] md5hash = md.digest();
            String hash_string = new BigInteger(1, md5hash).toString(16);
            Log.d(MyActivity.TAG, "hash_string: " + hash_string);
            StringBuilder sb = new StringBuilder(32);
            // ... put leading zero if necessary
            for (int i = 0; i < (32 - hash_string.length()); i++) sb.append("0");
            // ... store in 32-chars hexadecimal string
            md5 = sb.append(hash_string).toString();
            Log.d(MyActivity.TAG, "url: " + url_str);
            Log.d(MyActivity.TAG, "md5: " + sb.toString());

        } catch (NoSuchAlgorithmException ex) {
            Log.d(MyActivity.TAG, "NoSuchAlgorithmExceptioncatched catched");
        } catch (UnsupportedEncodingException ex) {
            Log.d(MyActivity.TAG, "UnsupportedEncodingException catched");
        } catch (NullPointerException ex) {
            Log.d(MyActivity.TAG, "NullPointerException catched");
        }
        Log.d(MyActivity.TAG, "PageCrawler run() exits");
    }


}

// check input for valid URL format and try to open resource for reading
// set state to 0 if all ok, 1 if malformed url, 2 if open/close/network error
class CheckUrl extends AsyncTask<String, Void, Integer> {

    URL url;
    int state = 0;
    BufferedReader reader;

    @Override
    protected Integer doInBackground(String... s) {

        Log.d(MyActivity.TAG, "doInBack started: " + s[0]);

        try {
            url = new URL(s[0]);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

        } catch (MalformedURLException ex) {
            Log.d(MyActivity.TAG, "MalformedURLException catched");
            return 1;
        } catch (IOException ex) {
            Log.d(MyActivity.TAG, "Can\'t open URL");
            return 2;
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException ex) {
                    Log.d(MyActivity.TAG, "IOException catched");
                    return 2;
                }
        }

        return 0;
    }

    @Override
    protected void onPostExecute(Integer i) {
        state = (int) i;
    }
}





