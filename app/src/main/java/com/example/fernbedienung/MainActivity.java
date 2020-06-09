package com.example.fernbedienung;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
       toolbar.setTitle("tvNOW");
       toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

       final ArrayList<Channel> channels = new ArrayList<>();

            channels.add(new Channel("ZDF"));
            channels.add(new Channel("ARD"));
            channels.add(new Channel("RTL"));
            channels.add(new Channel("NDR"));
            channels.add(new Channel("BAYERN 3"));

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
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    }
