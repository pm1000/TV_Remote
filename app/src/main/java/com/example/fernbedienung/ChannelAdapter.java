package com.example.fernbedienung;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
    private ChannelArray channelArray = ChannelArray.getInstance();

    public ChannelAdapter(Activity context, ArrayList<Channel> channels, int colorResourceID){
        super(context,0, channels);
        this.context = context;
        this.colorResourceID = colorResourceID;
    }
    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
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


        final ImageView channelImageView = (ImageView) listItemView.findViewById(R.id.favorite_image);


        channelImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = position;
                int count = -1;
                if (context.getClass().toString().equals("class com.example.fernbedienung.FavoriteActivity")){
                    int x = 0;
                    while (count != pos){
                        if (channelArray.getChannelAt(x).getFavorite())
                            count++;
                        x++;
                    }
                    pos = --x;
                }

                if (channelArray.getChannelAt(pos).getFavorite()){
                    channelArray.getChannelAt(pos).setFavorite(false);
                    channelImageView.setImageResource(notFavoriteChannel);
                }else {
                    channelArray.getChannelAt(pos).setFavorite(true);
                    channelImageView.setImageResource(favoriteChannel);
                }

                updateChannels();
            }
        });

        if (currentChannel.getFavorite()) {
            channelImageView.setImageResource(favoriteChannel);
        } else {
            channelImageView.setImageResource(notFavoriteChannel);
        }

        return listItemView;
    }
    public void updateChannels() {

        if (context.getClass().toString().equals("class com.example.fernbedienung.FavoriteActivity")) {
            this.clear();
            channelArray = ChannelArray.getInstance();
            ArrayList<Channel> fav = new ArrayList<>();
            for (Channel c : channelArray.getChannels()) {
                if (c.getFavorite()) {
                    fav.add(c);
                }
            }

            if (fav.isEmpty()) {
                fav.add(new Channel("Keine Favoriten vorhanden!"));
                fav.get(0).setFavorite(true);
            }
            this.addAll(fav);
        }
        this.notifyDataSetChanged();
    }

}
