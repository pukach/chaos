package com.nowhere.chaos;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by pukach on 4/17/15.
 */
public class MyActivity3 extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_3);

//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
//        startActivity(intent);

        PageContent get = new PageContent("http://info.cern.ch/LMBrowser.html", "GETPAGE");
        Log.d(MyActivity.TAG, "After PageContent thread started");
        TextView tw = (TextView) findViewById(R.id.URLTextView);


        try {
            get.thrd.join();
            Log.d(MyActivity.TAG, "Activity 3 joined to GetPage");
        } catch (InterruptedException ex) {
            Log.d(MyActivity.TAG, "InterruptedException");
        }

        tw.setText("http://info.cern.ch/LMBrowser.html");
        tw.append(get.page_content + "\n\n\n");

        //get MD5 hash
        try {
            // set md5 algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            String str = get.page_content;
            // put my data to MessageDigest instance
            md.update(str.getBytes("utf-8"));
            // get hash, we can store this array for future or use hexadecimal string of 32 chars length
            byte[] md5_hash = md.digest();
            // prepare our hash as common hex string
            String hash_string = new BigInteger(1, md5_hash).toString(16);
            // put leading zero if necessary
            StringBuilder sb = new StringBuilder(32);
            for(int i = 0; i < (32-hash_string.length()); i++ ) sb.append("0");


            tw.append(sb.append(hash_string));

        }catch (NoSuchAlgorithmException ex){
            Log.d(MyActivity.TAG, "NoSuchAlgorithmExceptioncatched");
        }catch (UnsupportedEncodingException ex){
            Log.d(MyActivity.TAG, "UnsupportedEncodingException catched");
        }



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
}

class PageContent implements Runnable {

    Thread thrd;
    String page_content;
    String url;

    PageContent(String u, String n) {
        thrd = new Thread(this, n);
        url = u;
        Log.d(MyActivity.TAG, "PageContent created");
        thrd.start();
    }

    public void run() {

        BufferedReader reader = null;
        Log.d(MyActivity.TAG, "GetPageContent run() started");
        URL url = null;
        String line = null;
        StringBuilder sb = new StringBuilder();

        try {
            url = new URL(this.url);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

            while ((line = reader.readLine()) != null) {
                //Log.d(MyActivity.TAG, line + "\n");
                sb.append(line);
            }
            Log.d(MyActivity.TAG, "After getContent in run()");
        } catch (MalformedURLException ex) {
            Log.d(MyActivity.TAG, "MalformedURLException");
        } catch (IOException ex) {
            Log.d(MyActivity.TAG, "IOException");
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Log.d(MyActivity.TAG, "IOException");
            } catch (NullPointerException ex) {
                Log.d(MyActivity.TAG, "NullPointerException");
            }
        }
        page_content = sb.toString();
        Log.d(MyActivity.TAG, "PageContent run() exits");
    }

}


