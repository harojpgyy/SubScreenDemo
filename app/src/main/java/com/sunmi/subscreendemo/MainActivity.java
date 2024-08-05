package com.sunmi.subscreendemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Presentation;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.hardware.display.DisplayManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.sunmi.subscreendemo.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "darren-MAct";

    private ActivityMainBinding mBinding;
    private SurfaceHolder mSurfaceHolder = null;
    private boolean mVideoPlayerInitialized = false;
    private MediaPlayer mVideoPlayer = null;

    private final Map<String, Presentation> mSubPresentation = new HashMap<>();
    private final Map<String, Integer> mSubDisplayState = new HashMap<>(); // 0: none, 1: video, 2: image
    private String[] mSubDisplaysName = null;
    private String mSubDisplayName = "";
    private String[] mVideosName;
    private String mVideoName = "video_bak.mp4";


    /**
     * host screen start play
     */
    private void startPlayer() {
        if (mVideoPlayerInitialized) {
            mVideoPlayer.setLooping(true);
            mVideoPlayer.prepareAsync();
            mVideoPlayer.start();
        } else {
            try {
                AssetFileDescriptor fd = getAssets().openFd(mVideoName);
                mVideoPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                AudioAttributes audioAttributes = new AudioAttributes.Builder().setLegacyStreamType(AudioAttributes.USAGE_MEDIA).build();
                mVideoPlayer.setAudioAttributes(audioAttributes);
                mVideoPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);//缩放模式
                mVideoPlayer.setLooping(true);
                mVideoPlayer.prepareAsync();
                mVideoPlayer.setOnPreparedListener(mp -> {
                    mp.start();
                    mVideoPlayerInitialized = true;
                });
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "MediaPlayer.setDataSource() failed.");
            }
        }
    }

    /**
     * host screen stop play
     */
    private void stopPlayer() {
        mVideoPlayer.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(LayoutInflater.from(getBaseContext()));
        setContentView(mBinding.getRoot());
        SubDisplayManager.init(getApplicationContext());
        // Sub screen start play button
        mBinding.subPlayVideo.setOnClickListener(v -> {
            Log.v(TAG, " -- show -- ");
            try {
                if (mSubDisplaysName.length == 0) {
                    initSpinner();
                }
                if (mSubDisplayName != null && !mSubDisplayName.isEmpty()) {
                    Display display = SubDisplayManager.getDisplayById(Integer.parseInt(mSubDisplayName.split("-")[0]));
                    if (display != null) {
                        AssetFileDescriptor fd = getAssets().openFd(mVideoName);
                        SubVideoPresentation subPresentation = new SubVideoPresentation(MainActivity.this, display, fd);
                        Log.v(TAG, " -- Real Show -- ");
                        subPresentation.show();
                        mSubPresentation.put(mSubDisplayName, subPresentation);
                        mBinding.subPlayVideo.setEnabled(false);
                        mBinding.subStopVideo.setEnabled(true);
                        mSubDisplayState.put(mSubDisplayName, 1);
                    } else {
                        Toast.makeText(getBaseContext(), "Not connected to the secondary screen", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // Sub screen stop play button
        mBinding.subStopVideo.setEnabled(false);
        mBinding.subStopVideo.setOnClickListener(v -> {
            Log.v(TAG, " -- hide -- ");
            if (mSubPresentation.get(mSubDisplayName) != null) {
                Log.v(TAG, " -- Real hide -- ");
                mSubPresentation.remove(mSubDisplayName).hide();
                mBinding.subPlayVideo.setEnabled(true);
                mBinding.subStopVideo.setEnabled(false);
                mSubDisplayState.put(mSubDisplayName, 0);
            }
        });
        //  host screen start play
        mVideoPlayer = new MediaPlayer();
        mBinding.mediaPlayerSurface.setKeepScreenOn(true);
        mBinding.mainPlayVideo.setOnClickListener(v -> {
            Log.v(TAG, " -- show -- ");
            mBinding.mediaPlayerSurface.setVisibility(View.VISIBLE);
            startPlayer();
            mBinding.mainPlayVideo.setEnabled(false);
            mBinding.mainStopVideo.setEnabled(true);
        });
        // host screen stop play
        mBinding.mainStopVideo.setEnabled(false);
        mBinding.mainStopVideo.setOnClickListener(v -> {
            Log.v(TAG, " -- hide -- ");
            mBinding.mediaPlayerSurface.setVisibility(View.GONE);
            stopPlayer();
            mBinding.mainPlayVideo.setEnabled(true);
            mBinding.mainStopVideo.setEnabled(false);
        });
        mSurfaceHolder = mBinding.mediaPlayerSurface.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mVideoPlayer.setDisplay(mSurfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mVideoPlayer.setDisplay(null);
            }
        });

        initSpinner();
        mBinding.subSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSubDisplayName = mSubDisplaysName[position];
                refreshSubDisplayState();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        initVideoSelect();
        SubDisplayManager.getDisplayManager().registerDisplayListener(new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {
                Log.d(TAG, "onDisplayAdded: " + displayId);
                initSpinner();
            }

            @Override
            public void onDisplayRemoved(int displayId) {
                Log.d(TAG, "onDisplayRemoved: "+ displayId);
                initSpinner();
            }

            @Override
            public void onDisplayChanged(int displayId) {
                Log.d(TAG, "onDisplayChanged: "+ displayId);
            }
        }, new Handler());
    }

    private void initVideoSelect() {
        mVideosName = getResources().getStringArray(R.array.spinner_video);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mVideosName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.videoName.setAdapter(adapter);
        mBinding.videoName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mVideoName = mVideosName[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initSpinner() {
        mSubDisplaysName = SubDisplayManager.getSubDisplayIdAndName();
        if (mSubDisplaysName.length > 0) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mSubDisplaysName);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mBinding.subSpinner.setAdapter(adapter);
            int index = Arrays.binarySearch(mSubDisplaysName, mSubDisplayName);
            if (index < 0 || index > mSubDisplaysName.length) {
                mBinding.subSpinner.setSelection(0);
                mSubDisplayName = mSubDisplaysName[0];
            } else {
                mBinding.subSpinner.setSelection(index);
            }
            refreshSubDisplayState();
        }
    }

    private void refreshSubDisplayState() {
        int state = mSubDisplayState.getOrDefault(mSubDisplayName, 0);
        switch (state) {
            case 0:
                mBinding.subPlayVideo.setEnabled(true);
                mBinding.subStopVideo.setEnabled(false);
                break;
            case 1:
                mBinding.subPlayVideo.setEnabled(false);
                mBinding.subStopVideo.setEnabled(true);
                break;
            case 2:
                mBinding.subPlayVideo.setEnabled(true);
                mBinding.subStopVideo.setEnabled(false);
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoPlayer.release();
        mVideoPlayer = null;
        SubDisplayManager.release();
    }
}