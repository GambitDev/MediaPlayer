package com.gambitdev.mediaplayer.Model;

import androidx.annotation.RawRes;

public class Song {

    private String bandName;
    private String songName;

    @RawRes
    private int rawResId;

    public Song(String bandName, String songName, int rawResId) {
        this.bandName = bandName;
        this.songName = songName;
        this.rawResId = rawResId;
    }

    public String getBandName() {
        return bandName;
    }

    public String getSongName() {
        return songName;
    }

    public int getRawResId() {
        return rawResId;
    }
}
