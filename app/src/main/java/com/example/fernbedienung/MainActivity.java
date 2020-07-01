package com.example.fernbedienung;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    public static final String MESSAGE_KEY = "";
    private TV_Server tv;
    private Handler handler;
    private ArrayList<Channel> channels;
    private long time = 0;
    private int volume = 0; //muss später durch persistente daten angepasst werden
    private boolean muted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //INIT TV-Server
        this.handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                //evaluate msg
                try {
                    //get correct JSON Object
                    JSONObject full_obj = new JSONObject(msg.getData().getString(MainActivity.MESSAGE_KEY));
                    if(full_obj.has("channels")) {
                        //if channels do exist, evaluate them
                        JSONArray channellist = full_obj.getJSONArray("channels");
                        for(int i = 0; i<channellist.length(); i++) {
                            JSONObject element = channellist.getJSONObject(i);
                            //create single channel
                            channels.add(new Channel(element.getString("program")));
                            channels.get(channels.size()-1);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        this.tv = new TV_Server(getApplicationContext(), handler);
        this.tv.setHandler(handler);
        this.tv.setContext(getApplicationContext());

        //TV-server initialialized
        //Setting up content view
        setContentView(R.layout.activity_main);
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("tvNOW");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        //Get the ChannelList, jetzt überflüssig
        /*try {
            startTV_Server();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        //Bundle msg = this.handler.obtainMessage().;
        //Log.i("TMP",msg.getString("channels"));


        final ArrayList<Channel> channels = new ArrayList<>();
        channels.add(new Channel("Lade Kanäle..."));

        ChannelAdapter adapter = new ChannelAdapter(this, channels, R.color.light);

        final ListView listView = (ListView) findViewById(R.id.channel_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Channel channel = channels.get(position);
                //SENDER UMSCHALTEN
            }
        });

        //sender zappen
        Button upBtn = (Button) findViewById(R.id.btn_channel_up);
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TV_Server tv = new TV_Server(getApplicationContext(), handler);
                //methode für nächsten channel auswählen benötigt
                String[] command = new String[1];
                command[0] ="channelMain="; //+ nummer
                tv.execute(command);
            }
        });

        Button downBtn = (Button) findViewById(R.id.btn_channel_down);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TV_Server tv = new TV_Server(getApplicationContext(), handler);
                //methode für nächsten channel auswählen benötigt
                String[] command = new String[1];
                command[0] ="channelMain="; //- nummer
                tv.doInBackground(command);
            }
        });

        //timeshift
        Button pauseBtn = (Button) findViewById(R.id.btn_pause);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TV_Server tv = new TV_Server(getApplicationContext(), handler);
                String[] command = new String[1];
                command[0] = "timeShiftPause=";
                tv.execute(command);
                MainActivity.this.setTime(System.currentTimeMillis());
                String tmp = "" + MainActivity.this.time;
                //layout muss angepasst werden
            }
        });

        //mute
        Button muteBtn = (Button) findViewById(R.id.btn_volume_mute);
        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.this.muted) {
                    TV_Server tv = new TV_Server(getApplicationContext(), handler);
                    String[] command = new String[1];
                    command[0] = "volume=" + MainActivity.this.getVolume();
                    tv.execute(command);
                    MainActivity.this.muted = false;
                }else
                {
                    TV_Server tv = new TV_Server(getApplicationContext(), handler);
                    String[] command = new String[1];
                    command[0] = "volume=0";
                    tv.execute(command);
                    MainActivity.this.muted = true;
                }
            }
        });

        //volume up
        Button upVolButton = (Button) findViewById(R.id.btn_volume_up);
        upVolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TV_Server tv = new TV_Server(getApplicationContext(), handler);
                String[] command = new String[1];
                int newVolume;
                if (MainActivity.this.getVolume() <=100){
                    newVolume = MainActivity.this.getVolume() + 1;
                }else
                    newVolume = 100;
                command[0] = "volume=" + newVolume ;
                tv.execute(command);
                MainActivity.this.setVolume(newVolume);
            }
        });

        //volume down
        Button downVolButton = (Button) findViewById(R.id.btn_volume_down);
        downVolButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                TV_Server tv = new TV_Server(getApplicationContext(), handler);
                String[] command = new String[1];
                int newVolume;
                if (MainActivity.this.getVolume() >= 0){
                    newVolume = MainActivity.this.getVolume() - 1;
                }else
                    newVolume = 0;
                command[0] = "volume=" + newVolume ;
                tv.execute(command);
                MainActivity.this.setVolume(newVolume);
            }
        });



        //volume bar
        SeekBar volSeekBar = (SeekBar) findViewById(R.id.volumeSeekBar);
        volSeekBar.setMax(100);
        volSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TV_Server tv = new TV_Server(getApplicationContext(), handler);
                String[] command = new String[1];
                int newVolume = seekBar.getScrollX();
                command[0] = "volume=" + progress;
                tv.execute(command);
                MainActivity.this.setVolume(newVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void startTV_Server() throws IOException, JSONException {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                JSONObject response = tv.doInBackground(new String[] {"scanChannels"});
                Log.i("TMP", response.toString());
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

    public void setTime(long time){
        this.time = time;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
