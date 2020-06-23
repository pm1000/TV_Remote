package com.example.fernbedienung;

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



    public String getName() {

        return program;
    }
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
    public boolean getFavorite() {

        return favorite;
    }

}
