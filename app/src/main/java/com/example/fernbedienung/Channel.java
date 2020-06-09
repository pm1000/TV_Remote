package com.example.fernbedienung;

public class Channel {

    private String name;
    private boolean favorite = false;

    public Channel(String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean getFavorite() {

        return favorite;
    }

}
