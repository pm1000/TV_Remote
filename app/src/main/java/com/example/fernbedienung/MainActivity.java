package com.example.fernbedienung;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    public static final String MESSAGE_KEY = "";
    private TV_Server tv;
    private Handler handler;
    private ChannelAdapter adapter;
    private final ArrayList<Channel> channels = new ArrayList<>();
    private long time = 0;
    private int volume; //muss später durch persistente daten angepasst werden
    private boolean muted = false;
    private boolean standby = false;
    private ListView listview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.volume = readVolume("Volume.txt");
        this.channels.add(new Channel("Lade Kanäle..."));
        this.adapter = new ChannelAdapter(this, channels, R.color.light);

        //INIT TV-Server
        this.handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                //evaluate msg
                Log.i("NetChannelLoader", "Started!");
                try {
                    //get correct JSON Object
                    tv.showToastBeginChannelScan();
                    JSONObject full_obj = new JSONObject(msg.getData().getString(MainActivity.MESSAGE_KEY));
                    Log.i("NetChannelLoader", "Got Objects!");
                    Log.i("NetChannelLoader", "Length is "+full_obj.length());
                    if(full_obj.has("channels")) {
                        //if channels do exist, evaluate them
                        Log.i("NetChannelLoader", "channels item found!");
                        JSONArray channellist = full_obj.getJSONArray("channels");
                        Log.i("NetChannelLoader", "Found "+channellist.length()+" channels!");
                        for(int i = 0; i<channellist.length(); i++) {
                            //create single channel
                            MainActivity.this.channels.add(new Channel(channellist.getJSONObject(i)));
                        }
                        Log.i("NetChannelLoader", "After adding Mainactivity.channels holds " + MainActivity.this.channels.size() + " elements!");
                        MainActivity.this.adapter = new ChannelAdapter(MainActivity.this, MainActivity.this.channels, R.color.light);
                    }
                    tv.showToastFinishedChannelScan();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("NetChannelLoader", "Done!");
            }
        };
        this.tv = new TV_Server(getApplicationContext(), this.handler);
        this.tv.setContext(getApplicationContext());

        /*The next line should be replaced by loading channels from file*/
        this.startTV_Server();


        //TV-server initialized
        //Setting up content view
        setContentView(R.layout.activity_main);
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("tvNOW");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        this.listview = (ListView) findViewById(R.id.channel_list);
        this.listview.setAdapter(this.adapter);
        this.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Channel channel = MainActivity.this.channels.get(position);
                //SENDER UMSCHALTEN
                TV_Server tv = new TV_Server(getApplicationContext(), handler);
                String[] command = new String[1];
                command[0] ="channelMain=" + channel.getChannel();
                tv.execute(command);
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
                tv.execute(command);
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

        //volume bar
        final SeekBar volSeekBar = (SeekBar) findViewById(R.id.volumeSeekBar);
        volSeekBar.setMax(100);
        volSeekBar.setProgress(this.volume);
        volSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    TV_Server tv = new TV_Server(getApplicationContext(), handler);
                    String[] command = new String[1];
                    command[0] = "volume=" + progress;
                    tv.execute(command);
                    MainActivity.this.setVolume(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
                if (MainActivity.this.getVolume() < 100){
                    newVolume = MainActivity.this.getVolume() + 1;
                }else
                    newVolume = 100;
                command[0] = "volume=" + newVolume ;
                tv.execute(command);
                MainActivity.this.setVolume(newVolume);
                volSeekBar.setProgress(newVolume);
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
                if (MainActivity.this.getVolume() > 0){
                    newVolume = MainActivity.this.getVolume() - 1;
                }else
                    newVolume = 0;
                command[0] = "volume=" + newVolume ;
                tv.execute(command);
                MainActivity.this.setVolume(newVolume);
                volSeekBar.setProgress(newVolume);
            }
        });

        //mute
        Button muteBtn = (Button) findViewById(R.id.btn_volume_mute);
        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.this.getMuted()) {
                    TV_Server tv = new TV_Server(getApplicationContext(), handler);
                    String[] command = new String[1];
                    command[0] = "volume=" + MainActivity.this.getVolume();
                    tv.execute(command);
                    MainActivity.this.setMuted(false);
                    volSeekBar.setProgress(MainActivity.this.getVolume());
                }else
                {
                    TV_Server tv = new TV_Server(getApplicationContext(), handler);
                    String[] command = new String[1];
                    command[0] = "volume=0";
                    tv.execute(command);
                    MainActivity.this.setMuted(true);
                    volSeekBar.setProgress(0);
                }
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();

        writeVolume("Volume.txt");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        writeVolume("Volume.txt");
    }

    public int readVolume(String filename) {
        int volume = 0;
        try {
            InputStream inputStream = this.openFileInput(filename);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                if ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                    volume = Integer.parseInt(stringBuilder.toString());
                }



                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return volume;
    }


    public void writeVolume(String filename) {
        try {
            String output = this.volume + "";
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.flush();
            outputStreamWriter.write(output);
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startTV_Server() {
        this.tv.execute("scanChannels");
        /*AsyncTask.execute(new Runnable() {

            @Override
            public void run() {
                JSONObject response = tv.doInBackground(new String[] {"scanChannels"});
                Log.i("TMP", response.toString());
            }
        });*/

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
                this.standby = !this.standby;
                if(this.standby) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject response = tv.doInBackground(new String[]{"standby=1"});
                        }
                    });
                } else {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject response = tv.doInBackground(new String[]{"standby=0"});
                        }
                    });
                }
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
        return this.volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
    public void setMuted(boolean muted){this.muted = muted;}
    public boolean getMuted(){return this.muted;}
}
