package com.example.fernbedienung;

import android.app.Activity;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ChannelArray {
    private ArrayList<Channel> channels = new ArrayList<Channel>();
    private static ChannelArray Instance = null;
    private Activity context = null;

    private ChannelArray(){
        channels = new ArrayList<Channel>();
    }


     public static ChannelArray getInstance() {
        if(ChannelArray.Instance == null) {
            ChannelArray.Instance = new ChannelArray();
        }
        return ChannelArray.Instance;
    }

    public void setContext(Activity context){
        this.context = context;
    }

    public void readChannels(){
        ArrayList<Channel> al = new ArrayList<Channel>();
        boolean cont = true;
        try {
            FileInputStream fis = this.context.openFileInput("channels");
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

        this.channels = al;
    }

    public void writeChanges() {
        try {
            FileOutputStream fos = context.openFileOutput("channels", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            for (int x = 0; x < this.channels.size(); x++) {
                if (this.channels.get(x).getChannel() != null)
                    oos.writeObject(this.channels.get(x));
            }
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int channelsSize(){
        return channels.size();
    }
    public int size(){
        return channels.size();
    }

    public Channel getChannelAt(int index){
        return channels.get(index);
    }

    public Channel get(int index){
        return this.getChannelAt(index);
    }

    public void addChannel(Channel channel){
        this.channels.add(channel);
    }

    public ArrayList<Channel> getChannels(){
        return this.channels;
    }

}
