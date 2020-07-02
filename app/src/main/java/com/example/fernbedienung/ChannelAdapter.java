package com.example.fernbedienung;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class ChannelAdapter extends ArrayAdapter<Channel>  {

    private int colorResourceID;
    private int favoriteChannel = R.drawable.star_favorite;
    private int notFavoriteChannel = R.drawable.star_not_favorite;
    private Activity context;
    private ViewGroup parent;

    public ChannelAdapter(Activity context, ArrayList<Channel> channels, int colorResourceID){
        super(context,0, channels);
        this.context = context;
        this.colorResourceID = colorResourceID;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        this.parent = parent;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, this.parent, false);
        }

        // Get the {@link AndroidFlavor} object located at this position in the list

        Channel currentChannel = getItem(position);

        View textContainer = listItemView.findViewById(R.id.channelTextView);
        int color = ContextCompat.getColor(getContext(), colorResourceID);
        textContainer.setBackgroundColor(color);

        TextView channelTextView = (TextView) listItemView.findViewById(R.id.channel_name);
        channelTextView.setText(currentChannel.getName());


        ImageView channelImageView = (ImageView) listItemView.findViewById(R.id.favorite_image);

        if (currentChannel.getFavorite()) {
            channelImageView.setImageResource(favoriteChannel);
        } else {
            channelImageView.setImageResource(notFavoriteChannel);
        }

        return listItemView;
    }
    public void updateChannels(ArrayList<Channel> channels) {
        this.clear();
        this.addAll(channels);
        this.notifyDataSetChanged();
    }
}
