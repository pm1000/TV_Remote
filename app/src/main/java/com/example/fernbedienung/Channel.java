package com.example.fernbedienung;

import java.io.Serializable;

public class Channel implements Serializable {

    private int frequency;
    private String channel;
    private int quality;
    private String program;
    private String provider;
    private boolean favorite = false;

    public Channel(){

    }

    public Channel(String name) {
        this.program = name;
    }

    public Channel(int frequency, String channel, int quality, String program, String provider){
        this.frequency = frequency;
        this.channel = channel;
        this.quality = quality;
        this.program = program;
        this.provider = provider;
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
    public String getChannel() { return this.channel; }

}
