package com.example.newsreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> ids;
    ArrayList<String> titles;
    ArrayList<String> newsUrls;
    int i;
    ArrayAdapter<String> adap;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView list = findViewById(R.id.list);
        DownloadTask task= new DownloadTask();
        ids= new ArrayList<>();
        titles= new ArrayList<>();
        newsUrls= new ArrayList<>();
        db= this.openOrCreateDatabase("NewsDatabase",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS NewsTable (INTEGER PRIMARY KEY, newsID INTEGER, newsTitle VARCHAR, newsURL VARCHAR )");
        i=0;
        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        adap = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        list.setAdapter(adap);
        updateListView();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ShowNews.class);
                intent.putExtra("url",newsUrls.get(position));
                startActivity(intent);
            }
        });
    }
    public void updateListView()
    {
        Cursor c= db.rawQuery("SELECT * FROM NewsTable",null);
        if(c.moveToFirst())
        {
            titles.clear();
            newsUrls.clear();
            do{
                titles.add(c.getString(2));
                newsUrls.add(c.getString(3));
            }while (c.moveToNext());
        }
        adap.notifyDataSetChanged();
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            URL url;
            HttpURLConnection urlConnection;
            String result = "";
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("Result", result);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONArray arr = new JSONArray(s);
                for (int i = 0; i < 30; i++) {
                    ids.add(arr.get(i).toString());
                }
                Log.i("ids", ids.toString());
                db.execSQL("DELETE FROM NewsTable");
                NewsDownloadTask task1 = new NewsDownloadTask();
                    try {
                        task1.execute("https://hacker-news.firebaseio.com/v0/item/" + ids.get(i) + ".json?print=pretty");
                        i++;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    public class NewsDownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            URL url;
            HttpURLConnection urlConnection;
            String result = "";
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject obj= new JSONObject(s);
                if(!obj.isNull("title") && !obj.isNull("url")) {
                    String url= obj.getString("url");
                    int id= obj.getInt("id");
                    String title= obj.getString("title");
                   String sql= "INSERT INTO NewsTable(newsID, newsTitle, newsURL) VALUES (?,?,?)";
                   SQLiteStatement statement= db.compileStatement(sql);
                   statement.bindDouble(1,id);
                   statement.bindString(2,title);
                   statement.bindString(3,url);
                   statement.execute();
                }



            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            NewsDownloadTask task1 = new NewsDownloadTask();
            try {
                    if(i<20) {
                        task1.execute("https://hacker-news.firebaseio.com/v0/item/" + ids.get(i) + ".json?print=pretty");
                        i++;
                    }

            }
            catch(Exception e)
                {
                e.printStackTrace();
            }
            updateListView();
        }
    }
}
