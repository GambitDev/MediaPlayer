package com.gambitdev.mediaplayer;

import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gambitdev.mediaplayer.Adapters.PlaylistAdapter;
import com.gambitdev.mediaplayer.Interfaces.Playable;
import com.gambitdev.mediaplayer.Model.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Playable {

    private ArrayList<Song> playlist = new ArrayList<>();
    private int songPlayingPos;
    private Song songPlaying;
    private PlaylistAdapter adapter;
    private MediaPlayer player;
    private Handler progressUpdater = new Handler();
    private Handler timerHandler = new Handler();
    private SeekBar seekBar;
    private TextView playingTv, timer;
    private Runnable progressRunnable, timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        RecyclerView itemList = findViewById(R.id.playlist);
        itemList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlaylistAdapter();
        adapter.setPlaylist(playlist);
        adapter.setListener(this);
        itemList.setAdapter(adapter);

        songPlaying = playlist.get(0);
        player = MediaPlayer.create(this, songPlaying.getRawResId());
        playingTv = findViewById(R.id.song_title_tv);
        setPlayingTv();
        player.setOnCompletionListener(mp -> {
            if (songPlayingPos == playlist.size() - 1) {
                player.release();
                return;
            }
            switchDataSource(++songPlayingPos);
        });
        player.start();
        initMediaController();

        seekBar = findViewById(R.id.seek_bar);
        seekBar.setMax(player.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            boolean playerPaused = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (!player.isPlaying()) return;
                player.pause();
                playerPaused = true;
                progressUpdater.removeCallbacks(progressRunnable);
                timerHandler.removeCallbacks(timerRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!playerPaused) return;
                player.start();
                playerPaused = false;
                progressUpdater.postDelayed(progressRunnable, 100);
                timerHandler.postDelayed(timerRunnable, 1000);
            }
        });

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (player.isPlaying()) {
                    seekBar.setProgress(player.getCurrentPosition());
                    progressUpdater.postDelayed(this, 100);
                }
            }
        };
        progressUpdater.postDelayed(progressRunnable, 100);

        timer = findViewById(R.id.timer);
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (player.isPlaying()) {
                    timer.setText(getFormattedTime(player.getDuration(),
                            player.getCurrentPosition()));
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void initData() {
        playlist.add(new Song("Soundgarden",
                "Blow Up The Outside World",
                R.raw.blow_up_the_outside_world));
        playlist.add(new Song("Incubus",
                "Dig",
                R.raw.dig));
        playlist.add(new Song("Soundgarden",
                "Fell On Black Days",
                R.raw.fell_on_black_days));
        playlist.add(new Song("Audioslave",
                "Getaway Car",
                R.raw.getaway_car));
        playlist.add(new Song("Stone Temple Pilots",
                "Interstate Love Song",
                R.raw.interstate_love_song));
        playlist.add(new Song("Audioslave",
                "Light My Way",
                R.raw.light_my_way));
        playlist.add(new Song("Pearl Jam",
                "Once",
                R.raw.once));
        playlist.add(new Song("Weezer",
                "Say It Ain't So",
                R.raw.say_it_aint_so));
        playlist.add(new Song("Pearl Jam",
                "State Of Love And Trust",
                R.raw.state_of_love_and_trust));
        playlist.add(new Song("Tool",
                "The Pot",
                R.raw.the_pot));
    }

    private void initMediaController() {
        findViewById(R.id.prev_btn).setOnClickListener(v -> {
            if (songPlayingPos == 0) {
                player.seekTo(0);
            } else {
                switchDataSource(--songPlayingPos);
            }
        });
        findViewById(R.id.play_btn).setOnClickListener(v -> {
            if (player.isPlaying()) return;
            player.start();
            progressUpdater.postDelayed(progressRunnable, 100);
            timerHandler.postDelayed(timerRunnable, 1000);
        });
        findViewById(R.id.pause_btn).setOnClickListener(v -> {
            if (!player.isPlaying()) return;
            player.pause();
        });
        findViewById(R.id.stop_btn).setOnClickListener(v -> {
            seekBar.setProgress(0);
            player.seekTo(0);
            player.pause();
        });
        findViewById(R.id.next_btn).setOnClickListener(v -> {
            if (songPlayingPos == playlist.size() - 1) return;
            switchDataSource(++songPlayingPos);
        });
    }

    private Uri getSongUri(@RawRes int resRawId) {
        return Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE
                        + File.pathSeparator + File.separator + File.separator
                        + getPackageName()
                        + File.separator
                        + resRawId
        );
    }

    private void setPlayingTv(){
        String title = songPlaying.getBandName() + " - " + songPlaying.getSongName();
        playingTv.setText(title);
    }

    private void notifyReceivers(String songTitle, String timePlayed) {
        Intent intent = new Intent();
        intent.putExtra("song_title", songTitle);
        intent.putExtra("time_played", timePlayed);
        intent.setAction("com.gambitdev.mediaplayer.record");
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    @Override
    public void onSongClicked(int songPos) {
        switchDataSource(songPos);
    }

    private void switchDataSource(int newSongPos) {
        notifyReceivers(songPlaying.getBandName() + " - " + songPlaying.getSongName(),
                getFormattedTime(player.getDuration(), player.getCurrentPosition()));
        songPlayingPos = newSongPos;
        songPlaying = playlist.get(songPlayingPos);
        try {
            player.reset();
            player.setDataSource(this, getSongUri(songPlaying.getRawResId()));
            player.prepare();
            player.start();
            setPlayingTv();
            adapter.setSongPlayingPos(songPlayingPos);
        } catch (IOException e) {
            player.release();
            Toast.makeText(this,
                    "Something went wrong",
                    Toast.LENGTH_SHORT)
                    .show();
        }
        seekBar.setProgress(0);
        seekBar.setMax(player.getDuration());
    }

    private String getFormattedTime(long duration, long currentPos) {
        StringBuilder sb = new StringBuilder();

        int durationMinutes = (int) (duration/1000) / 60;
        int durationSeconds = (int) (duration/1000) % 60;

        int currentMinutes = (int) (currentPos/1000) / 60;
        int currentSeconds = (int) (currentPos/1000) % 60;

        sb.append(currentMinutes)
                .append(":");
        if (currentSeconds < 10) sb.append("0");
        sb.append(currentSeconds)
                .append(" / ")
                .append(durationMinutes)
                .append(":");
        if (durationSeconds < 10) sb.append("0");
        sb.append(durationSeconds);
        return sb.toString();
    }
}
