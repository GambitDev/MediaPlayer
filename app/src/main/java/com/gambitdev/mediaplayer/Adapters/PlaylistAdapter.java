package com.gambitdev.mediaplayer.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.gambitdev.mediaplayer.Interfaces.Playable;
import com.gambitdev.mediaplayer.Model.Song;
import com.gambitdev.mediaplayer.R;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.SongViewHolder> {

    private int songPlayingPos;
    private ArrayList<Song> playlist;
    private Playable listener;

    private static final int PLAYING = 0;
    private static final int NOT_PLAYING = 1;

    public void setSongPlayingPos(int songPlayingPos) {
        this.songPlayingPos = songPlayingPos;
        notifyDataSetChanged();
    }

    public void setPlaylist(ArrayList<Song> playlist) {
        this.playlist = playlist;
    }

    public void setListener(Playable listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (songPlayingPos == position) return PLAYING;
        else return NOT_PLAYING;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == PLAYING)
            view = inflater.inflate(R.layout.playing_song_layout, parent, false);
        else
            view = inflater.inflate(R.layout.song_layout, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song currentSong = playlist.get(position);
        holder.songTitle.setText(currentSong.getSongName());
        holder.bandName.setText(currentSong.getBandName());
        holder.root.setOnClickListener(v ->
                listener.onSongClicked(position));
    }

    @Override
    public int getItemCount() {
        return playlist.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout root;
        TextView songTitle, bandName;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.root);
            songTitle = itemView.findViewById(R.id.song_title_tv);
            bandName = itemView.findViewById(R.id.band_name_tv);
        }
    }
}
