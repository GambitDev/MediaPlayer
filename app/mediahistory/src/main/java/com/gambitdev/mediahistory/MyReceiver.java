package com.gambitdev.mediahistory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String songTitle = intent.getStringExtra("song_title");
        String timePlayed = intent.getStringExtra("time_played");
        if (songTitle != null && timePlayed != null) {
            Toast.makeText(context, context + songTitle + timePlayed, Toast.LENGTH_LONG).show();
        }
    }
}
