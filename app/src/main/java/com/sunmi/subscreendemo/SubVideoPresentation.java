package com.sunmi.subscreendemo;

import android.annotation.SuppressLint;
import android.app.Presentation;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;

import com.sunmi.subscreendemo.databinding.PresentationVideoBinding;

import java.io.IOException;

/**
 * 副屏异显实现
 */
public class SubVideoPresentation extends Presentation {
    private static final String TAG = "darren-video";

    private AssetFileDescriptor mFd;
    private PresentationVideoBinding mBinding = null;
    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private boolean mPlayerInitialized = false;

    public SubVideoPresentation(Context context, Display display, AssetFileDescriptor fd) {
        super(context, display);
        mFd = fd;
    }

    private void startPlayer() {
        try {
            mMediaPlayer.setDataSource(mFd.getFileDescriptor(), mFd.getStartOffset(), mFd.getLength());
            mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);//缩放模式
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                mPlayerInitialized = true;
            });
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "MediaPlayer.setDataSource() failed.");
        }
    }

    private void stopPlayer() {
        mMediaPlayer.stop();
    }

    private void initData() {
        Log.v(TAG, "SubVideoPresentation....initData");
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = PresentationVideoBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(mBinding.getRoot());

        initData();
        Log.v(TAG, "SubVideoPresentation....savedInstanceState");

        mMediaPlayer = new MediaPlayer();
        mBinding.mediaPlayerSurface.setKeepScreenOn(true);

        mSurfaceHolder = mBinding.mediaPlayerSurface.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mMediaPlayer.setDisplay(mSurfaceHolder);
                startPlayer();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mMediaPlayer.setDisplay(null);
                stopPlayer();
            }
        });
        mBinding.mediaPlayerSurface.setOnTouchListener((v, event) -> {
            Log.v(TAG, "event.getX() =" + event.getX() + ";event.getX() =" + event.getY());
            if (mPlayerInitialized) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                } else {
                    mMediaPlayer.start();
                }
            } else {
                if (!mSurfaceHolder.isCreating()) {
                    startPlayer();
                }
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "SubVideoPresentation....onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "SubVideoPresentation....onStop");
    }

    @Override
    public void show() {
        super.show();
        Log.v(TAG, "SubVideoPresentation....show");
    }

    @Override
    public void hide() {
        super.hide();
        Log.v(TAG, "SubVideoPresentation....hide");
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Log.v(TAG, "SubVideoPresentation....dismiss");
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

}
