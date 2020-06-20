package com.example.fernbedienung;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class FavoriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("tvNOW");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
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