package me.paixao.videoplayer.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.raizlabs.android.dbflow.sql.language.Select;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.paixao.videoplayer.R;
import me.paixao.videoplayer.db.models.Playlist;
import me.paixao.videoplayer.db.models.Playlist_Table;
import me.paixao.videoplayer.db.models.Video;
import me.paixao.videoplayer.db.models.Video_Table;
import me.paixao.videoplayer.ui.customviews.VideoControllerView;

public class VideoPlayer extends BaseActivity
        implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        VideoControllerView.MediaPlayerControl {

    // When a playlist is opened, this var is defined
    String playlist_uuid;

    // URL to the current playing video
    String currentVideoSource;

    SurfaceView videoSurface;
    MediaPlayer player;

    // Video proportions vars
    Integer videoWidth, videoHeight = null;
    float videoProportion;

    // Custom view with the desired controls
    VideoControllerView controller;

    // Array of URLs to run in sequence
    ArrayList<String> allMedia;

    // Auxiliary index used to know in the list which video I'm currently playing
    int myCurrentVideoIndex = 0;

    public boolean mIsPlayerRelease = true;
    boolean fullScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_player_2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        currentVideoSource = null;
        playlist_uuid = null;
        if (getIntent().hasExtra("uri"))
            currentVideoSource = getIntent().getStringExtra("uri");

        if (getIntent().hasExtra("playlist"))
            playlist_uuid = getIntent().getStringExtra("playlist");

        if (currentVideoSource == null)
            finish();

        // Define title of the Activity
        Playlist pl = null;
        if (playlist_uuid != null)
            pl = new Select().from(Playlist.class)
                    .where(Playlist_Table.uuid.eq(playlist_uuid))
                    .querySingle();
        if (pl != null) {
            setTitle("#" + pl.getName() + " - " + getFileName(currentVideoSource));
        } else {
            setTitle(getFileName(currentVideoSource));
        }

        super.onCreate(savedInstanceState);
    }

    public void createPlayer(String videoPlace) {
        currentVideoSource = videoPlace;

        if (player != null) {
            player.stop();
            mIsPlayerRelease = true;
            player.release();
        }
        player = new MediaPlayer();
        mIsPlayerRelease = false;

        try {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(this, Uri.parse(videoPlace));
            player.setOnPreparedListener(this);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    goToNextVideo();
                }
            });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFileName(String result) {
        int cut = result.lastIndexOf('/');
        if (cut != -1) {
            result = result.substring(cut + 1);
        }
        return result;
    }

    public void goToPreviousVideo() {
        if (myCurrentVideoIndex == 0) {
            myCurrentVideoIndex = allMedia.size() - 1;
        } else {
            myCurrentVideoIndex--;
        }
        String uri = allMedia.get(myCurrentVideoIndex);
        Intent intent = new Intent(_this, VideoPlayer.class);
        intent.putExtra("uri", uri);
        if (playlist_uuid != null)
            intent.putExtra("playlist", playlist_uuid);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public void goToNextVideo() {
        if (myCurrentVideoIndex == allMedia.size() - 1) {
            myCurrentVideoIndex = 0;
        } else {
            myCurrentVideoIndex++;
        }
        String uri = allMedia.get(myCurrentVideoIndex);
        Intent intent = new Intent(_this, VideoPlayer.class);
        intent.putExtra("uri", uri);
        if (playlist_uuid != null)
            intent.putExtra("playlist", playlist_uuid);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public ArrayList<String> getAllPlaylistMedia() {
        if (playlist_uuid != null) {
            List<Video> videoList = new Select().from(Video.class)
                    .where(Video_Table.playlist.eq(playlist_uuid))
                    .orderBy(Video_Table.order, true)
                    .queryList();
            ArrayList<String> output = new ArrayList<>();
            for (Video vid : videoList)
                output.add(vid.getUri());
            return output;
        } else {
            return null;
        }
    }

    @Override
    public void refresh() {
        if (playlist_uuid == null) {
            allMedia = getAllMedia();
        } else {
            allMedia = getAllPlaylistMedia();
        }
        for (String media : allMedia) {
            if (media.equals(currentVideoSource))
                break;
            myCurrentVideoIndex++;
        }

        controller = new VideoControllerView(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextVideo();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPreviousVideo();
            }
        });

        videoSurface = findViewById(R.id.videoSurface);
        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);

        if (player == null)
            createPlayer(currentVideoSource);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.show();
        return false;
    }

    // Implement SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        FrameLayout vsc = findViewById(R.id.videoSurfaceContainer);

        FrameLayout.LayoutParams videoSurfaceParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        videoSurfaceParams.gravity = Gravity.CENTER;

        // Get the dimensions of the video
        if (videoWidth == null) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap bmp = null;
            videoWidth = player.getVideoWidth();
            videoHeight = player.getVideoHeight();
            try {
                retriever.setDataSource(currentVideoSource);
                bmp = retriever.getFrameAtTime();
                videoHeight = bmp.getHeight();
                videoWidth = bmp.getWidth();
            } catch (Exception e) {
                e.printStackTrace();
            }
            videoProportion = (float) videoWidth / (float) videoHeight;
        }

        // Get the width of the screen
        int screenWidth = vsc.getWidth();
        int screenHeight = vsc.getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;

        int width, height;
        // Get the SurfaceView layout parameters
        if (videoProportion > screenProportion) {
            width = screenWidth;
            height = (int) ((float) screenWidth / videoProportion);
        } else {
            width = (int) (videoProportion * (float) screenHeight);
            height = screenHeight;
        }
        videoSurfaceParams.width = width;
        videoSurfaceParams.height = height;

        // Commit the layout parameters
        videoSurface.getHolder().setFixedSize(width, height);
        videoSurface.setLayoutParams(videoSurfaceParams);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        player.setDisplay(holder);
        player.prepareAsync();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        player.stop();
    }

    // End SurfaceHolder.Callback

    // Implement MediaPlayer.OnPreparedListener
    @Override
    public void onPrepared(MediaPlayer mp) {
        controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
        player.start();
    }

    // Implement VideoMediaController.MediaPlayerControl
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (!mIsPlayerRelease)
            return player.getCurrentPosition();
        return 0;
    }

    @Override
    public int getDuration() {
        if (!mIsPlayerRelease)
            return player.getDuration();
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if (!mIsPlayerRelease)
            return player.isPlaying();
        return false;
    }

    @Override
    public void pause() {
        if (!mIsPlayerRelease)
            player.pause();
    }

    @Override
    public void seekTo(int i) {
        if (!mIsPlayerRelease)
            player.seekTo(i);
    }

    @Override
    public void start() {
        if (!mIsPlayerRelease)
            player.start();
    }

    @Override
    public boolean isFullScreen() {
        return fullScreen;
    }

    @Override
    public void toggleFullScreen() {
        if (fullScreen) {
            fullScreen = false;
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().show();
        } else {
            fullScreen = true;
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().hide();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.release();
        mIsPlayerRelease = true;
    }
}
