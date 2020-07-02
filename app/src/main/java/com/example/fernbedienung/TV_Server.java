package com.example.fernbedienung;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


/**
 * Created by zander on 14.06.17.
 */

public class TV_Server extends AsyncTask<String, Void, JSONObject> {

    public static final String TAG = "ScanChannelsTask";

    private Context context;
    private HttpRequest request;
    private boolean running;
    private Handler handler;
    //private static TV_Server Instance =null;

    public TV_Server(Context context, @Nullable Handler handler) {
        this.request = new HttpRequest("192.168.173.1", 1000);;
        this.context = context;
        this.handler = handler;
    }

    /*public static TV_Server getInstance() {
        if(TV_Server.Instance == null) {
            TV_Server.Instance = new TV_Server(null, null);
        }
        return TV_Server.Instance;
    }*/

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
    public void setContext(Context context) {
        this.context = context;
    }


    @Override
    protected JSONObject doInBackground(String[] params) {
        JSONObject obj = null;

        if (params[0] != null) {
            try {
//                Log.e(TAG, params[0] + " - " + this.request.getIpAddress());
                obj = this.request.sendHttp(params[0]);
//                Log.e(TAG, "Channel Scan almost finished...");
                Log.e(TAG, obj.toString());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }
        return obj;
    }


    @Override
    protected void onPreExecute() {
//        super.onPreExecute();
        Log.e(TAG, "onPreExecute() was called...");
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.MESSAGE_KEY, jsonObject.toString());
        msg.setData(bundle);
        this.handler.sendMessage(msg);
    }
    public void showToastBeginChannelScan() {
        Toast.makeText(this.context, "Started Channel Scan", Toast.LENGTH_SHORT).show();
    }
    public void showToastFinishedChannelScan() {
        Toast.makeText(this.context, "Channel Scan finished", Toast.LENGTH_SHORT).show();
    }
}
