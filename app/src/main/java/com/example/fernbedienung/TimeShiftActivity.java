package com.example.fernbedienung;

import androidx.annotation.BinderThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import java.sql.Time;

public class TimeShiftActivity extends AppCompatActivity {

    public static final String MESSAGE_KEY = "";
    private Handler handler;
    private long time;
    private long currentPlayTime = 0;
    private int volume;
    private boolean muted = false;
    private boolean standby = false;
    private String activeChannel;
    private ChannelArray channelArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.time = System.currentTimeMillis() / 1000;
        this.channelArray = ChannelArray.getInstance();
        this.channelArray.setContext(this);
        this.channelArray.readChannels();
        this.volume = readInt("Volume.txt");
        this.standby = readInt("standby.txt") != 0;
        this.muted = readInt("muted.txt") != 0;
        this.activeChannel = readString("activeChannel.txt");

        //Setting up content view
        setContentView(R.layout.activity_time_shift);
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("tvNOW");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        if(channelArray.channelsSize() == 0) {
            channelArray.addChannel(new Channel("Keine Kanäle vorhanden!\nBitte Kanalscan durchführen!"));
        }
        final ChannelAdapter adapter = new ChannelAdapter(this, channelArray.getChannels(), R.color.light);

        final ListView listView = (ListView) findViewById(R.id.channel_list);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activeChannel = channelArray.getChannelAt(position).getChannel();
                //SENDER UMSCHALTEN
                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                String[] command = new String[1];
                command[0] = "channelMain=" + activeChannel;
                tv.execute(command);
            }
        });

        //spulbar
        final SeekBar spulBar = (SeekBar) findViewById(R.id.timeshiftSeekBar);
        spulBar.setMax(100);
        spulBar.setProgress(0);
        spulBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    long currentTime = System.currentTimeMillis() / 1000;
                    long diff = currentTime - TimeShiftActivity.this.getTime();
                    long timeInSec = (long) Math.floor(diff * ((progress) / 100.0));

                    TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                    String[] command = new String[1];
                    command[0] = "timeShiftPlay=" + timeInSec;
                    tv.execute(command);
                    TimeShiftActivity.this.setCurrentPlayTime(timeInSec);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
                    if (progress != 0) {
                        TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                        String[] command = new String[1];
                        command[0] = "volume=" + progress;
                        tv.execute(command);
                        TimeShiftActivity.this.setVolume(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //timedown
        Button timedown = (Button) findViewById(R.id.btn_rewind);
        timedown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long playTime = TimeShiftActivity.this.getCurrentPlayTime();
                if (playTime > 1) {
                        playTime = playTime - 1;
                }

                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                String[] command = new String[1];
                command[0] = "timeShiftPlay=" + playTime;
                tv.execute(command);
                TimeShiftActivity.this.setCurrentPlayTime(playTime);
            }
        });

        //timeup
        Button timeup = (Button) findViewById(R.id.btn_forward);
        timeup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long playTime = TimeShiftActivity.this.getCurrentPlayTime();
                long diff = System.currentTimeMillis() / 1000 - TimeShiftActivity.this.getTime();
                if (playTime < diff) {
                    playTime = playTime + 1;
                }

                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                String[] command = new String[1];
                command[0] = "timeShiftPlay=" + playTime;
                tv.execute(command);
                TimeShiftActivity.this.setCurrentPlayTime(playTime);
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
                if (TimeShiftActivity.this.getVolume() < 100){
                    newVolume = TimeShiftActivity.this.getVolume() + 1;
                }else
                    newVolume = 100;
                command[0] = "volume=" + newVolume ;
                tv.execute(command);
                TimeShiftActivity.this.setVolume(newVolume);
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
                if (TimeShiftActivity.this.getVolume() > 0){
                    newVolume = TimeShiftActivity.this.getVolume() - 1;
                }else
                    newVolume = 0;
                command[0] = "volume=" + newVolume ;
                tv.execute(command);
                TimeShiftActivity.this.setVolume(newVolume);
                volSeekBar.setProgress(newVolume);
            }
        });

        //play
        Button play = (Button) findViewById(R.id.btn_play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                String[] command = new String[1];
                command[0] = "timeShiftPlay=0";
                tv.execute(command);
                Intent changeIntent;
                changeIntent = new Intent(TimeShiftActivity.this, MainActivity.class);
                startActivity(changeIntent);
            }
        });

        //mute
        Button muteBtn = (Button) findViewById(R.id.btn_volume_mute);
        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TimeShiftActivity.this.getMuted()) {
                    TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                    String[] command = new String[1];
                    command[0] = "volume=" + TimeShiftActivity.this.getVolume();
                    tv.execute(command);
                    TimeShiftActivity.this.setMuted(false);
                    volSeekBar.setProgress(TimeShiftActivity.this.getVolume());
                }else
                {
                    TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                    String[] command = new String[1];
                    command[0] = "volume=0";
                    tv.execute(command);
                    TimeShiftActivity.this.setMuted(true);
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

    public long getTime(){
        return this.time;
    }

    public long getCurrentPlayTime(){
        return this.getCurrentPlayTime();
    }

    public void setCurrentPlayTime(long time){
        this.currentPlayTime = time;
    }
}
