package com.example.fernbedienung;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;

import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;

public class PicInPicActivity extends AppCompatActivity {
    public static final String MESSAGE_KEY = "";
    private Handler handler;
    private long time = 0;
    private int volume;
    private boolean muted = false;
    private boolean standby = false;
    private String activeChannel;
    private String activePipChannel;
    private int zoomstate = 0;
    private boolean pipControlActive = true;
    private ChannelArray channels;

    private Switch swt_zoom_pip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.channels = ChannelArray.getInstance();
        this.volume = readInt("Volume.txt");
        this.pipControlActive = readInt("pipControl.txt") != 0;
        this.standby = readInt("standby.txt") != 0;
        this.muted = readInt("muted.txt") != 0;
        this.activeChannel = readString("activeChannel.txt");
        this.activePipChannel = readString("activePipChannel.txt");
        this.zoomstate = readInt("zoomstate.txt");

        //Show the PIP-window
        TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
        String[] command = new String[1];
        command[0] ="showPip=1";
        tv.execute(command);

        //Setting up content view
        setContentView(R.layout.activity_picinpic);
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("tvNOW");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        if(channels.getChannels().isEmpty()) {
            channels.addChannel(new Channel("Keine Kanäle vorhanden!\nBitte Kanalscan durchführen!"));
        }
        ChannelAdapter adapter = new ChannelAdapter(this, channels.getChannels(), R.color.light);

        //select either mainchannel or PIP-Channel
        Switch PIP_switch = (Switch) findViewById(R.id.swt_toggleControl);
        PIP_switch.setChecked(this.pipControlActive);
        PIP_switch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PicInPicActivity.this.pipControlActive = isChecked;
                PicInPicActivity.this.updateZoomButtonPosition();
            }
        });

        //zoom selected picture
        this.swt_zoom_pip = (Switch)findViewById(R.id.swt_zoom0);
        this.applyAllZoom(this.zoomstate);
        this.updateZoomButtonPosition();
        this.swt_zoom_pip.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setZoomstate(isChecked);
            }
        });

        final ListView listView = (ListView) findViewById(R.id.channel_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!PicInPicActivity.this.pipControlActive) {
                    PicInPicActivity.this.activeChannel = PicInPicActivity.this.channels.get(position).getChannel();
                } else {
                    PicInPicActivity.this.activePipChannel = PicInPicActivity.this.channels.get(position).getChannel();
                }
                //SENDER UMSCHALTEN
                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                String[] command = new String[1];
                if(PicInPicActivity.this.pipControlActive) {
                    command[0] ="channelPip="+ PicInPicActivity.this.activePipChannel;
                } else {
                    command[0] = "channelMain=" + PicInPicActivity.this.activeChannel;
                }
                tv.execute(command);
            }
        });

        //sender zappen
        Button upBtn = (Button) findViewById(R.id.btn_channel_up);
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                if(!PicInPicActivity.this.pipControlActive) {
                    if (PicInPicActivity.this.activeChannel == "") {
                        PicInPicActivity.this.activeChannel = PicInPicActivity.this.channels.get(0).getChannel();
                    } else {
                        int i = 0;
                        while (i < PicInPicActivity.this.channels.size() && PicInPicActivity.this.channels.get(i++).getChannel() != PicInPicActivity.this.activeChannel)
                            ;
                        if (i == PicInPicActivity.this.channels.size()) {
                            i = 0;
                        }
                        PicInPicActivity.this.activeChannel = PicInPicActivity.this.channels.get(i).getChannel();
                    }
                } else {
                    if (PicInPicActivity.this.activePipChannel == "") {
                        PicInPicActivity.this.activePipChannel = PicInPicActivity.this.channels.get(0).getChannel();
                    } else {
                        int i = 0;
                        while (i < PicInPicActivity.this.channels.size() && PicInPicActivity.this.channels.get(i++).getChannel() != PicInPicActivity.this.activePipChannel)
                            ;
                        if (i == PicInPicActivity.this.channels.size()) {
                            i = 0;
                        }
                        PicInPicActivity.this.activePipChannel = PicInPicActivity.this.channels.get(i).getChannel();
                    }
                }
                String[] command = new String[1];
                if(PicInPicActivity.this.pipControlActive) {
                    command[0] ="channelPip="+ PicInPicActivity.this.activePipChannel;
                } else {
                    command[0] = "channelMain=" + PicInPicActivity.this.activeChannel;
                }
                tv.execute(command);
            }
        });

        Button downBtn = (Button) findViewById(R.id.btn_channel_down);
        downBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                if(!PicInPicActivity.this.pipControlActive) {
                    if (PicInPicActivity.this.activeChannel.isEmpty()) {
                        PicInPicActivity.this.activeChannel = PicInPicActivity.this.channels.get(0).getChannel();
                    } else {
                        int i = PicInPicActivity.this.channels.size();
                        while (i >= 0 && PicInPicActivity.this.channels.get(i).getChannel().equals(activeChannel)) {
                            i--;
                        }
                        if (i == -1) {
                            i = PicInPicActivity.this.channels.size()-1;
                        }
                        PicInPicActivity.this.activeChannel = PicInPicActivity.this.channels.get(i).getChannel();
                    }
                } else {
                    if (PicInPicActivity.this.activePipChannel.isEmpty()) {
                        PicInPicActivity.this.activePipChannel = PicInPicActivity.this.channels.get(0).getChannel();
                    } else {
                        int i = PicInPicActivity.this.channels.size();
                        while (i >= 0 && PicInPicActivity.this.channels.get(i).getChannel().equals(PicInPicActivity.this.activePipChannel)) {
                            i--;
                        }
                        if (i == -1) {
                            i = PicInPicActivity.this.channels.size()-1;
                        }
                        PicInPicActivity.this.activePipChannel = PicInPicActivity.this.channels.get(i).getChannel();
                    }
                }
                String[] command = new String[1];
                if(PicInPicActivity.this.pipControlActive) {
                    command[0] = "channelPip="+ PicInPicActivity.this.activePipChannel;
                } else {
                    command[0] = "channelMain=" + PicInPicActivity.this.activeChannel;
                }
                tv.execute(command);
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
                    PicInPicActivity.this.setVolume(progress);
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
                if (PicInPicActivity.this.getVolume() < 100){
                    newVolume = PicInPicActivity.this.getVolume() + 1;
                } else {
                    newVolume = 100;
                }
                command[0] = "volume=" + newVolume ;
                tv.execute(command);
                PicInPicActivity.this.setVolume(newVolume);
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
                if (PicInPicActivity.this.getVolume() > 0){
                    newVolume = PicInPicActivity.this.getVolume() - 1;
                } else {
                    newVolume = 0;
                }
                command[0] = "volume=" + newVolume ;
                tv.execute(command);
                PicInPicActivity.this.setVolume(newVolume);
                volSeekBar.setProgress(newVolume);
            }
        });

        //mute
        Button muteBtn = (Button) findViewById(R.id.btn_volume_mute);
        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PicInPicActivity.this.getMuted()) {
                    TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                    String[] command = new String[1];
                    command[0] = "volume=" + PicInPicActivity.this.getVolume();
                    tv.execute(command);
                    PicInPicActivity.this.setMuted(false);
                    volSeekBar.setProgress(PicInPicActivity.this.getVolume());
                } else {
                    TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
                    String[] command = new String[1];
                    command[0] = "volume=0";
                    tv.execute(command);
                    PicInPicActivity.this.setMuted(true);
                    volSeekBar.setProgress(0);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveData();
    }

    private ArrayList<Channel> getChannels(String filename){
        ArrayList<Channel> al = new ArrayList<Channel>();
        boolean cont = true;
        try {
            //FileInputStream fis = new FileInputStream("channels");
            FileInputStream fis = this.openFileInput("channels");
            ObjectInputStream ois = new ObjectInputStream(fis);
            while(cont){
                Channel obj = null;
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
        //Hide the PIP-window
        TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
        String[] command = new String[1];
        command[0] ="showPip=0";
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
                saveData();
                tv.execute(command);
                changeIntent = new Intent(this, PicInPicActivity.class);
                startActivity(changeIntent);
                return true;
            case R.id.button_homeScreen:
                //Change to activity_main
                saveData();
                tv.execute(command);
                changeIntent = new Intent(this, MainActivity.class);
                startActivity(changeIntent);
                return true;
            case R.id.button_favorites:
                //Change to activity_favorite
                saveData();
                tv.execute(command);
                changeIntent = new Intent(this, FavoriteActivity.class);
                startActivity(changeIntent);
                return true;
            case R.id.button_settings:
                //Change to activity_settings
                saveData();
                tv.execute(command);
                changeIntent = new Intent(this, SettingsActivity.class);
                startActivity(changeIntent);
                return true;
            default:
                saveData();
                tv.execute(command);
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

    public void setZoomstate(boolean zoomButtonState) {
        if(this.pipControlActive) {
            if(((this.zoomstate & 2) == 0) == zoomButtonState) {
                toggleZoom();
            }
        } else {
            if(((this.zoomstate & 1) == 0) == zoomButtonState) {
                toggleZoom();
            }
        }
    }

    public void toggleZoom() {
        if(this.pipControlActive) {
            //target is PIP-View
            this.zoomstate = this.zoomstate ^ 2; // toggles second last digit: 01 => 11, 11 => 01, ...
            this.applyPipZoom(this.zoomstate);
        } else {
            //target is main view
            this.zoomstate = this.zoomstate ^ 1; // toggles second last digit: 01 => 11, 11 => 01, ...
            this.applyMainZoom(this.zoomstate);
        }
    }
    public void applyAllZoom(int zoomStatus) {
        this.applyMainZoom(zoomStatus);
        this.applyPipZoom(zoomStatus);
    }
    public void applyMainZoom(int zoomStatus) {
        TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
        String[] command = new String[1];
        command[0] = "zoomMain="+(zoomStatus%2);
        tv.execute(command);
    }
    public void applyPipZoom(int zoomStatus) {
        TV_Server tv = new TV_Server(getApplicationContext(), handler, false);
        String[] command = new String[1];
        command[0] = "zoomPip="+(zoomStatus/2);
        tv.execute(command);
    }
    public void updateZoomButtonPosition() {
        this.swt_zoom_pip.setChecked((this.pipControlActive) ? (this.zoomstate / 2 != 0) : (this.zoomstate % 2 != 0));
    }

    private void saveData(){
        writeVolume("Volume.txt");
        writeBool("standby.txt", this.standby);
        writeBool("muted.txt", this.muted);
        writeDataToFile("activeChannel.txt", this.activeChannel);
        writeDataToFile("activePipChannel.txt", this.activePipChannel);
        writeBool("pipControl.txt", this.pipControlActive);
        writeInt("zoomstate.txt", this.zoomstate);
        this.channels.writeChanges();
    }
}
