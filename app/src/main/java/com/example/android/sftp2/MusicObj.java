package com.example.android.sftp2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.VolumeShaper;
import android.net.Uri;
import android.util.Log;

/**
 * Created by staja on 10/20/2018.
 */

// The MusicObj is a class wrapper for all audio-related functionality. Packages the MediaPlayer for
    // for the sound file and the VolumeShaper attached to it. If the device does not use API 26,
    // alternative fade in and fade out functions are provided.

public class MusicObj {
    private String TAG = MusicObj.class.getName();

    MediaPlayer mediaPlayer;
    VolumeShaper shaper;
    String title;
    float volume;
    float speed;

    // Overloaded constructor for MusicObj object. This call applies to API26 and greater devices,
    // attaches a VolumeShaper to the MediaPlayer to control fade. Errors suppressed to allow gradle
    // build of app for lower API devices.
    @SuppressLint("NewApi")
    public MusicObj(String piece, int transitionTime, Context context, String xtitle, int xstartTime) {
        volume = 0.0f;
        speed = 0.0f;
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
        @SuppressLint({"NewApi", "LocalSuppress"}) VolumeShaper.Configuration configuration = new VolumeShaper.Configuration.Builder()
                .setDuration(transitionTime)
                .setCurve(new float[] {0.f, 1.f}, new float[] {0.f, 1.f})
                .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_LINEAR)
                .build();
        shaper = mediaPlayer.createVolumeShaper(configuration);
        mediaPlayer.seekTo(xstartTime);
        Log.d(TAG, "MusicObj: " + piece + ":" + xtitle + " starting at " + Integer.toString(xstartTime));
        title = xtitle;
    }

    // Overloaded constructor for MusicObj object. This call applies to API25 and lesser devices.
    // Does not apply a VolumeShaper object to the MediaPlayer
    public MusicObj(String piece, Context context, String xtitle, int xstartTime) {
        volume = 0.0f;
        speed = 0.0007f;
        Log.i(TAG, "MusicObj2: What is coming in: " + piece + " - " + xtitle);
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
        shaper = null;
        mediaPlayer.seekTo(xstartTime);
        mediaPlayer.setVolume(volume, volume);
        Log.d(TAG, "MusicObj: " + piece + ":" + xtitle + " starting at " + Integer.toString(xstartTime));
        title = xtitle;
    }


    public int getTimeStamp() {
        return mediaPlayer.getCurrentPosition();
    }

    // Method to stop, close, and destroy a MusicObj object to prevent data leakage.
    public void destroyMusicObj() {
        mediaPlayer.release();
        if (shaper != null) {
            shaper.close();
        }
    }

    // getVolume() returns the current volume of the MediaPlayer object. Used to signify in
    // MainActivity that fade has completed. Errors suppressed to allow gradle
    // build of app for lower API devices.
    @SuppressLint("NewApi")
    public float getVolume(){
        if (shaper != null) {
            Log.d(TAG, "getVolume: IS using shapers");
            return shaper.getVolume();
        } else {
            Log.d(TAG, "getVolume: is NOT using shapers");
            return volume;
        }
    }

    // Alternative fade solutions for API25 and lesser devices. Based on code at
    // https://forums.xamarin.com/discussion/16871/fading-in-and-out-a-music-file-using-mediaplayer.
    // I am not sure if it is implemented correctly.
    public void noShaperFadeIn(float transitionDelta) {
        mediaPlayer.setVolume(volume,volume);
        Log.i(TAG, "noShaperFadeIn: " + Float.toString(speed) + " - " + Float.toString(transitionDelta) + " - " + Float.toString(speed * transitionDelta));
        volume += speed * transitionDelta;
    }

    public void noShaperFadeOut(float transitionDelta) {
        mediaPlayer.setVolume(volume,volume);
        volume -= speed * transitionDelta;
    }


}
