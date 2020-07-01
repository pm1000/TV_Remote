package com.example.fernbedienung;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Channel {

    private int frequency;
    private String channel;
    private int quality;
    private String program;
    private String provider;

    private boolean favorite = false;

    public Channel(String name) {
        this.program = name;
    }
    public Channel(JSONObject json) {
        try {
            this.program = json.getString("program");
            this.channel = json.getString("channel");
            this.frequency = json.getInt("frequency");
            this.quality = json.getInt("quality");
            this.provider = json.getString("provider");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return program;
    }
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
    public boolean getFavorite() {
        return favorite;
    }

    public String getChannel() {
        return channel;
    }
}
