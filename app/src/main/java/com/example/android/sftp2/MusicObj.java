package com.example.android.sftp2;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.VolumeShaper;
import android.net.Uri;
import android.util.Log;

/**
 * Created by staja on 10/20/2018.
 */

public class MusicObj {
    private String TAG = MusicObj.class.getName();

    MediaPlayer mediaPlayer;
    VolumeShaper shaper;
    String title;

    public MusicObj(String piece, int transitionTime, Context context, String xtitle, int xstartTime) {
        Log.i(TAG, "MusicObj: What is coming in: " + piece + " - " + xtitle + " - " + Integer.toString(transitionTime));
        String path = "android.resource://com.example.android.sftp2/raw/" + piece.substring(0, piece.indexOf("."));
        Uri uri = Uri.parse(path);
        try {
            Log.i(TAG, "MusicObj: " + uri.toString());
            mediaPlayer = MediaPlayer.create(context, uri);
            Log.i(TAG, "MusicObj: Where it fails: mediaPlayer == null: " + Boolean.toString(mediaPlayer == null));
            mediaPlayer.setLooping(true);
        } catch (Exception e) {
            Log.e(TAG, "MusicObj: " + e.toString());
        }
        VolumeShaper.Configuration configuration = new VolumeShaper.Configuration.Builder()
                .setDuration(transitionTime)
                .setCurve(new float[] {0.f, 1.f}, new float[] {0.f, 1.f})
                .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_LINEAR)
                .build();
        shaper = mediaPlayer.createVolumeShaper(configuration);
        mediaPlayer.seekTo(xstartTime);
        Log.d(TAG, "MusicObj: " + piece + ":" + xtitle + " starting at " + Integer.toString(xstartTime));
        title = xtitle;
    }


    public int getTimeStamp() {
        return mediaPlayer.getCurrentPosition();
    }

    public void destroyMusicObj() {
        mediaPlayer.release();
        shaper.close();
    }

    public float getVolume(){
        return shaper.getVolume();
    }
}
