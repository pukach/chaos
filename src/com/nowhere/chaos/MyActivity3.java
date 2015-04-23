package com.nowhere.chaos;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by pukach on 4/17/15.
 */
public class MyActivity3 extends Activity implements View.OnKeyListener, View.OnClickListener, TextView.OnEditorActionListener {

    private static EditText inputUrl;
    private static ArrayList<ColoredString> urls;
    private static MyArrayAdapter adapter;
    private static ListView lv;
    public URLDBHelper url_db;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_activity_3);
        Button btnCheck;

        // find all MyActivity3 Views
        inputUrl = (EditText) findViewById(R.id.editUrl);
        btnCheck = (Button) findViewById(R.id.btChk);
        lv = (ListView) findViewById(R.id.sitelistView);

        // create list and adapter
        urls = new ArrayList<ColoredString>();
        adapter = new MyArrayAdapter(this, urls);

        // assign adapter and handlers
        lv.setAdapter(adapter);
        inputUrl.setOnKeyListener(this);
        inputUrl.setOnEditorActionListener(this);
        btnCheck.setOnClickListener(this);

        url_db = new URLDBHelper(this);

        SQLiteDatabase db = url_db.getReadableDatabase();
        Log.d(MyActivity.TAG, "Rows in DB: ");
        Cursor cursor = db.query(URLDBHelper.DB_TABLE, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {

            int id_column_index = cursor.getColumnIndex(URLDBHelper.KEY_ID);
            int url_column_index = cursor.getColumnIndex(URLDBHelper.KEY_URL);
            int md5_column_index = cursor.getColumnIndex(URLDBHelper.KEY_MD5);
            int changed_column_index = cursor.getColumnIndex(URLDBHelper.KEY_CHANGED);

            // fill our listview by url's from database
            do {
                Log.d(MyActivity.TAG, "ID " + cursor.getInt(id_column_index) +
                        "; URL " + cursor.getString(url_column_index) +
                        "; MD5 " + cursor.getString(md5_column_index) +
                        "; CHANGED " + cursor.getInt(changed_column_index));
                urls.add(0, new ColoredString(cursor.getString(url_column_index)));

            }
            while (cursor.moveToNext());
            adapter.notifyDataSetChanged();


        } else {
            Log.d(MyActivity.TAG, "BD empty ");
        }
        cursor.close();
        url_db.close();
    }



    class ColoredString {

        String string;
        int color;

        ColoredString(String string, int color) {

            this.string = string;
            this.color = color;
        }

        // constructor with default color
        ColoredString(String string) {
            this.string = string;
            this.color = getResources().getColor(R.color.primary_color);
        }

        // construct as copy
        ColoredString(ColoredString cs) {
            this.string = cs.string;
            this.color = cs.color;
        }

        public void changeColor(int new_color) {
            this.color = new_color;
        }

    }

    // extend ArrayAdapte only to override getView for manipulating with color of ArrayList elements
    class MyArrayAdapter extends ArrayAdapter<ColoredString> {

        private final Context context;
        private final ArrayList<ColoredString> values;


        MyArrayAdapter(Context context, ArrayList<ColoredString> values) {
            super(context, android.R.layout.simple_list_item_multiple_choice, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ColoredString cstr = getItem(position);

            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            }

            TextView textView = (TextView) rowView.findViewById(android.R.id.text1);
            textView.setText(cstr.string);
            textView.setTextColor(cstr.color);

            return rowView;

        }
    }


    public void onClick(View v) {

        Log.d(MyActivity.TAG, "Button CHK pressed");

        // prepare index for mapping since objects in ListView and DB stored in reversed order
        List<Integer> reverse_index = new ArrayList<Integer>();
        // ... i+1 since id's in DB starts from 1, not from 0
        for (int i = 0; i < urls.size(); i++) reverse_index.add(i + 1);
        Log.d(MyActivity.TAG, "initial index :" + reverse_index);
        Collections.sort(reverse_index, Collections.reverseOrder());
        Log.d(MyActivity.TAG, "reverse index :" + reverse_index);


        // get all checked positions in urls list
        SparseBooleanArray sb = lv.getCheckedItemPositions();
        for (int i = 0; i < sb.size(); i++) {
            int key = sb.keyAt(i);
            // change color and deselect item in ListView
            if (sb.get(key)) {
                Log.d(MyActivity.TAG, "ListView checked key: " + key + ", DB _id: " + reverse_index.get(key) + ", url: " + urls.get(key).string);

                try {
                    if ((new CheckIsPageChanged().execute(reverse_index.get(key)).get()) != 0) {
                        ColoredString cs = new ColoredString(urls.get(key));
                        cs.changeColor(getResources().getColor(R.color.changed_color));
                        urls.set(key, cs);
                    }
                } catch (InterruptedException ex) {
                    Toast.makeText(MyActivity3.this, getResources().getString(R.string.err_interrupted), Toast.LENGTH_LONG).show();
                    Log.d(MyActivity.TAG, "InterruptedException catched");
                } catch (ExecutionException ex) {
                    Toast.makeText(MyActivity3.this, getResources().getString(R.string.err_interrupted), Toast.LENGTH_LONG).show();
                    Log.d(MyActivity.TAG, "ExecutionException catched");

                }
                lv.setItemChecked(key, false);
            }
            adapter.notifyDataSetChanged();
        }
    }

    // handle "Enter" from software keyboard. don't forget that event can be null!
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        Log.d(MyActivity.TAG, "onEditor actionId: " + actionId+ " event: " + event);

        // if "Enter" pressed check text for validity and clean input
        if ((actionId == EditorInfo.IME_NULL) || (actionId == EditorInfo.IME_ACTION_DONE)) {
            Log.d(MyActivity.TAG, "onEditorAction catched Done");
            handleUrlInput();
            return true;
        }
        return false;
    }


    // handle hardware keyboard's "Enter"
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        // if "Enter" pressed check text for validity, get page md5 and clean input
        if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            handleUrlInput();
            return true;
        }
        return false;
    }

    // this code shared by onKey and onEditorAction to handle url input by software and hardware kbd's "Enter"
    private void handleUrlInput(){

        // check text for validity, get page md5, add to database and clean input
        String str = inputUrl.getText().toString();

        Log.d(MyActivity.TAG, "handleUrlInput: " + str);

        SQLiteDatabase db = url_db.getWritableDatabase();
        ContentValues content_value = new ContentValues();

        try {
            // ...
            switch (new CheckUrl().execute(str).get()) {
                case 0:
                    PageInfo pi = new PageInfo(str);
                    Toast.makeText(MyActivity3.this, pi.md5sum, Toast.LENGTH_LONG).show();
                    content_value.put(URLDBHelper.KEY_URL, pi.url);
                    content_value.put(URLDBHelper.KEY_MD5, pi.md5sum);
                    content_value.put(URLDBHelper.KEY_CHANGED, 0);
                    long row_id = db.insert(URLDBHelper.DB_TABLE, null, content_value);
                    Log.d(MyActivity.TAG, "DB insert on row " + row_id);

                    // add new string to the head of list, deselect all items and clear input string
                    ColoredString cstr = new ColoredString(str, getResources().getColor(R.color.accent_color));
                    urls.add(0, cstr);
                    for (int i = 0; i < urls.size(); i++) lv.setItemChecked(i, false);
                    inputUrl.setText(null);
                    adapter.notifyDataSetChanged();
                    break;
                case 1:
                    Toast.makeText(MyActivity3.this, getResources().getString(R.string.err_bad_url), Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    Toast.makeText(MyActivity3.this, getResources().getString(R.string.err_cant_open_url), Toast.LENGTH_LONG).show();
                    break;
            }

        } catch (InterruptedException ex) {
            Toast.makeText(MyActivity3.this, getResources().getString(R.string.err_interrupted), Toast.LENGTH_LONG).show();
            Log.d(MyActivity.TAG, "InterruptedException catched");
        } catch (ExecutionException ex) {
            Toast.makeText(MyActivity3.this, getResources().getString(R.string.err_interrupted), Toast.LENGTH_LONG).show();
            Log.d(MyActivity.TAG, "ExecutionException catched");
        } finally {
            url_db.close();
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


    // check is page md5 changed since initial add to database
    class CheckIsPageChanged extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... db_row_index) {


            SQLiteDatabase db = url_db.getReadableDatabase();
            Cursor cursor = db.query(URLDBHelper.DB_TABLE, null, " _id=" + db_row_index[0], null, null, null, null);

            if (cursor.moveToFirst()) {

                int id_column_index = cursor.getColumnIndex(URLDBHelper.KEY_ID);
                int url_column_index = cursor.getColumnIndex(URLDBHelper.KEY_URL);
                int md5_column_index = cursor.getColumnIndex(URLDBHelper.KEY_MD5);
                int changed_column_index = cursor.getColumnIndex(URLDBHelper.KEY_CHANGED);

                Log.d(MyActivity.TAG, "ChkIsPgChanged read from db: ID " + cursor.getInt(id_column_index) +
                        "; URL " + cursor.getString(url_column_index) +
                        "; MD5 " + cursor.getString(md5_column_index) +
                        "; CHANGED " + cursor.getInt(changed_column_index));
                PageInfo pi = new PageInfo(cursor.getString(url_column_index));
                if (pi.md5sum.equals(cursor.getString(md5_column_index))) {
                    Log.d(MyActivity.TAG, "MD5 equals: now " + pi.md5sum + ", was " + cursor.getString(md5_column_index));
                    cursor.close();
                    url_db.close();
                    return 0;
                } else {
                    Log.d(MyActivity.TAG, "MD5 changed: now " + pi.md5sum + ", was " + cursor.getString(md5_column_index));
                    cursor.close();
                    url_db.close();
                    return 1;
                }

            } else {
                Log.d(MyActivity.TAG, "BD empty ");
            }
            cursor.close();
            url_db.close();

            return 0;
        }


        @Override
        protected void onPostExecute(Integer i) {
            super.onPostExecute(i);
        }
    }


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
    String url_str = null;
    String md5 = null;

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

    URL url = null;
    //    int state = 0;
    BufferedReader reader = null;

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
        super.onPostExecute(i);
    }
}


class URLDBHelper extends SQLiteOpenHelper {

    //Resources res = Resources.getSystem();

    private static final String URL_DB_NAME = "chaosurl.db"; //res.getString(R.string.url_db_name);
    private static final int DB_VERSION = 1;

    protected static final String DB_TABLE = "URL_MD5_TABLE";
    protected static final String KEY_ID = "_id";
    protected static final String KEY_URL = "_URL";
    protected static final String KEY_MD5 = "_MD5";
    protected static final String KEY_CHANGED = "_CHANGED";

    private static final String DB_CREATE = "create table " + DB_TABLE + " (" +
            KEY_ID + " integer primary key autoincrement, " +
            KEY_URL + " text not null, " +
            KEY_MD5 + " text not null, " +
            KEY_CHANGED + " integer);";


    URLDBHelper(Context context) {
        super(context, URL_DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
        Log.d(MyActivity.TAG, "Upgrade DB from version " + old_version + new_version);
        db.execSQL("DROP TABLE IF IT EXISTS " + DB_TABLE);
        onCreate(db);
    }


}




