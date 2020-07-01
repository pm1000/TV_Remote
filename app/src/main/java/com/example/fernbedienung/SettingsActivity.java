package com.example.fernbedienung;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class SettingsActivity  extends AppCompatActivity {

    public static final String MESSAGE_KEY = "";
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //INIT TV-Server
        this.handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                try {
                    ArrayList<Channel> channels = new ArrayList<>();
                    JSONObject channelObject = new JSONObject(msg.getData().getString(SettingsActivity.this.MESSAGE_KEY));
                    Log.i("test", channelObject.toString());

                    if (channelObject.has("channels")) {
                        JSONArray channellist = channelObject.getJSONArray("channels");
                        for (int i = 0; i < channellist.length(); i++) {
                            JSONObject element = channellist.getJSONObject(i);
                            //create single channel
                            channels.add(new Channel(element.getInt("frequency"), element.getString("channel"), element.getInt("quality"), element.getString("program"), element.getString("provider")));
                            //channels.get(channels.size() - 1);
                        }

                        File path = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_MOVIES);
                        File file = new File(path, "/" + "channels");
                        FileOutputStream fos = openFileOutput("channels", Context.MODE_PRIVATE);
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        for (int x = 0; x < channellist.length(); x++){
                                oos.writeObject(channels.get(x));
                        }

                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

        };

        //TV-server initialialized
        setContentView(R.layout.activity_settings);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("tvNOW");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);


        //sendersuchlauf
        Button channelScanBtn = (Button) findViewById(R.id.channelScan_Btn);
        channelScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TV_Server tv = new TV_Server(getApplicationContext(), SettingsActivity.this.handler, true);
                String command[] = new String[1];
                command[0] = "scanChannels=";
                tv.execute(command);
                    /*AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            TV_Server tv = new TV_Server(getApplicationContext(), SettingsActivity.this.handler, true);
                            String command[] = new String[1];
                            command[0] = "scanChannels=";
                            JSONObject response = tv.doInBackground(command);
                            Log.i("test", response.toString());
                        }
                    });*/
            }
        });
    }
    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent changeIntent;
        switch (item.getItemId()) {
            case R.id.button_activate:
                //Send switch off signal
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                        JSONObject response = tv.doInBackground(new String[] {"standby=1"});
                    }
                });
                return true;
            case R.id.button_picInPic:
                //Change to activity_picinpic
                changeIntent = new Intent(this, PicInPicActivity.class);
                startActivity(changeIntent);
                return true;
            case R.id.button_homeScreen:
                //Change to activity_main
                changeIntent = new Intent(this, MainActivity.class);
                startActivity(changeIntent);
                return true;
            case R.id.button_favorites:
                //Change to activity_favorite
                changeIntent = new Intent(this, FavoriteActivity.class);
                startActivity(changeIntent);
                return true;
            case R.id.button_settings:
                //Change to activity_settings
                changeIntent = new Intent(this, SettingsActivity.class);
                startActivity(changeIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
