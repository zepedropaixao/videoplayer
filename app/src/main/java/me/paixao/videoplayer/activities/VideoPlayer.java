package me.paixao.videoplayer.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.ArrayList;

import me.paixao.videoplayer.R;
import me.paixao.videoplayer.ui.customviews.VideoControllerView;

public class VideoPlayer extends BaseActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener, VideoControllerView.MediaPlayerControl {

    String currentVideoSource;
    SurfaceView videoSurface;
    MediaPlayer player;
    VideoControllerView controller;

    ArrayList<String> allMedia;
    int myCurrentVideoIndex = 0;

    public boolean mIsPlayerRelease = true;
    boolean fullScreen = false;

    public String getFileName(String result) {
        int cut = result.lastIndexOf('/');
        if (cut != -1) {
            result = result.substring(cut + 1);
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_2);
        String videoPlace = null;
        if (getIntent().hasExtra("uri"))
            videoPlace = getIntent().getStringExtra("uri");

        if (videoPlace == null)
            finish();

        setTitle(getFileName(videoPlace));

        allMedia = getAllMedia();
        for (String media : allMedia) {
            if (media.equals(videoPlace))
                break;
            myCurrentVideoIndex++;
        }

        controller = new VideoControllerView(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // NEXT
                if (myCurrentVideoIndex == 0) {
                    myCurrentVideoIndex = allMedia.size() - 1;
                } else {
                    myCurrentVideoIndex--;
                }
                String uri = allMedia.get(myCurrentVideoIndex);
                Intent intent = new Intent(_this, VideoPlayer.class);
                intent.putExtra("uri", uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // PREVIOUS
                if (myCurrentVideoIndex == allMedia.size() - 1) {
                    myCurrentVideoIndex = 0;
                } else {
                    myCurrentVideoIndex++;
                }
                String uri = allMedia.get(myCurrentVideoIndex);
                Intent intent = new Intent(_this, VideoPlayer.class);
                intent.putExtra("uri", uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
            }
        });

        videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.addCallback(this);

        if (player == null)
            createPlayer(videoPlace);
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

    @Override
    public void refresh() {
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

        // Get the SurfaceView layout parameters
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
    Integer videoWidth, videoHeight = null;
    float videoProportion;

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
