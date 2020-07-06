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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FavoriteActivity extends AppCompatActivity {
    public static final String MESSAGE_KEY = "";
    private Handler handler;
    private ArrayList<Channel> channels;
    private long time = 0;
    private int volume;
    private boolean muted = false;
    private boolean standby = false;
    private String activeChannel;
    private ChannelArray channelArray = ChannelArray.getInstance();

    private int x = 0;
    private int y = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.channelArray.setContext(this);
        this.volume = readInt("Volume.txt");
        this.standby = readInt("standby.txt") != 0;
        this.muted = readInt("muted.txt") != 0;
        this.activeChannel = readString("activeChannel.txt");
        //Setting up content view
        setContentView(R.layout.activity_favorite);
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("tvNOW");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        ArrayList<Channel> fav = new ArrayList<>();
        for (Channel c: channelArray.getChannels()) {
            if(c.getFavorite()) {
                fav.add(c);
            }
        }
        this.channels = fav;
        if(channels.isEmpty()) {
            channels.add(new Channel("Keine Favoriten vorhanden!"));
            channels.get(0).setFavorite(true);
        }
        ChannelAdapter adapter = new ChannelAdapter(this, channels, R.color.light);

        final ListView listView = (ListView) findViewById(R.id.channel_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activeChannel = channels.get(position).getChannel();
                //SENDER UMSCHALTEN
                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                String[] command = new String[1];
                command[0] ="channelMain=" + activeChannel;
                tv.execute(command);
            }

        });




        //sender zappen
        Button upBtn = (Button) findViewById(R.id.btn_channel_up);
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                if(activeChannel == "") {
                    activeChannel = channels.get(0).getChannel();
                } else {
                    int i = 0;
                    while (i < channels.size() && channels.get(i++).getChannel() != activeChannel);
                    if(i == channels.size()) {
                        i=0;
                    }
                    activeChannel = channels.get(i).getChannel();
                }
                String[] command = new String[1];
                command[0] ="channelMain="+ activeChannel;
                tv.execute(command);
            }
        });

        Button downBtn = (Button) findViewById(R.id.btn_channel_down);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                if(activeChannel == "") {
                    activeChannel = channels.get(0).getChannel();
                } else {
                    int i = channels.size()-1;
                    while (i >= 0 && channels.get(i--).getChannel() != activeChannel);
                    if(i == -1) {
                        i=channels.size()-1;
                    }
                    activeChannel = channels.get(i).getChannel();
                }
                String[] command = new String[1];
                command[0] ="channelMain="+ activeChannel;
                tv.execute(command);
            }
        });

        //timeshift
        Button pauseBtn = (Button) findViewById(R.id.btn_pause);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                String[] command = new String[1];
                command[0] = "timeShiftPause=";
                tv.execute(command);
                FavoriteActivity.this.setTime(System.currentTimeMillis());
                String tmp = "" + FavoriteActivity.this.time;
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
                    TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                    String[] command = new String[1];
                    command[0] = "volume=" + progress;
                    tv.execute(command);
                    FavoriteActivity.this.setVolume(progress);
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
                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                String[] command = new String[1];
                int newVolume;
                if (FavoriteActivity.this.getVolume() < 100){
                    newVolume = FavoriteActivity.this.getVolume() + 1;
                }else
                    newVolume = 100;
                command[0] = "volume=" + newVolume ;
                tv.execute(command);
                FavoriteActivity.this.setVolume(newVolume);
                volSeekBar.setProgress(newVolume);
            }
        });

        //volume down
        Button downVolButton = (Button) findViewById(R.id.btn_volume_down);
        downVolButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                String[] command = new String[1];
                int newVolume;
                if (FavoriteActivity.this.getVolume() > 0){
                    newVolume = FavoriteActivity.this.getVolume() - 1;
                }else
                    newVolume = 0;
                command[0] = "volume=" + newVolume ;
                tv.execute(command);
                FavoriteActivity.this.setVolume(newVolume);
                volSeekBar.setProgress(newVolume);
            }
        });

        //mute
        Button muteBtn = (Button) findViewById(R.id.btn_volume_mute);
        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FavoriteActivity.this.getMuted()) {
                    TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                    String[] command = new String[1];
                    command[0] = "volume=" + FavoriteActivity.this.getVolume();
                    tv.execute(command);
                    FavoriteActivity.this.setMuted(false);
                    volSeekBar.setProgress(FavoriteActivity.this.getVolume());
                }else
                {
                    TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                    String[] command = new String[1];
                    command[0] = "volume=0";
                    tv.execute(command);
                    FavoriteActivity.this.setMuted(true);
                    volSeekBar.setProgress(0);
                }
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        writeVolume("Volume.txt");
        writeBool("standby.txt", this.standby);
        writeBool("muted.txt", this.muted);
        writeDataToFile("activeChannel.txt", this.activeChannel);
        channelArray.writeChanges();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        writeVolume("Volume.txt");
        writeBool("standby.txt", this.standby);
        writeBool("muted.txt", this.muted);
        writeDataToFile("activeChannel.txt", this.activeChannel);
        channelArray.writeChanges();
    }

    private ArrayList<Channel> getFaveChannels(){
        ArrayList<Channel> al = new ArrayList<Channel>();
        boolean cont = true;
        try {
            FileInputStream fis = this.openFileInput("faveChannel");
            ObjectInputStream ois = new ObjectInputStream(fis);
            while(cont){
                Channel obj =null;
                try {
                    obj = (Channel) ois.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if(obj != null)
                    al.add(obj);
                else
                    cont = false;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return al;
    }


    public int readInt(String filename) {
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

    public String readString(String filename) {
        String value = "";
        try {
            InputStream inputStream = this.openFileInput(filename);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                if ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                    value = stringBuilder.toString();
                }
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

    public void writeVolume(String filename) {
        writeInt(filename, this.volume);
    }

    public void writeInt(String filename, int value) {
        writeDataToFile(filename, Integer.toString(value));
    }
    public void writeBool(String filename, boolean value) {
        writeInt(filename, (value)?1:0);
    }

    public void writeDataToFile(String filename, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.flush();
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                            TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                            tv.execute(new String[]{"standby=1"});
                        }
                    });
                } else {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                            tv.execute(new String[]{"standby=0"});
                        }
                    });
                }
                return true;
            case R.id.button_picInPic:
                //Change to activity_picinpic
                channelArray.writeChanges();
                changeIntent = new Intent(this, PicInPicActivity.class);
                startActivity(changeIntent);
                return true;
            case R.id.button_homeScreen:
                //Change to activity_main
                channelArray.writeChanges();
                changeIntent = new Intent(this, MainActivity.class);
                startActivity(changeIntent);
                return true;
            case R.id.button_favorites:
                //Change to activity_favorite
                channelArray.writeChanges();
                changeIntent = new Intent(this, FavoriteActivity.class);
                startActivity(changeIntent);
                return true;
            case R.id.button_settings:
                //Change to activity_settings
                channelArray.writeChanges();
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
