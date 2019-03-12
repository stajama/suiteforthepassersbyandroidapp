package com.example.android.sftp2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.VolumeShaper;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static int TRANSITION_TIME = 6 * 1000;
    private static String TAG = MainActivity.class.getName();

    private Button start;
    private boolean startToggle;
    private TextView title;
    private TextView sculpture;
    private BroadcastReceiver broadcastReceiver;
    private TextView testBox;
    private boolean is26AndUp;

    // to simplify fading one piece out and another fading in, the audio1 and  audio2 MusicObjs here
    // are to be used in toggle with each other.
    private MusicObj audio1 = null;
    private MusicObj audio2 = null;
    private GPSMaths gpsMaths;
    private boolean transitioning = false;
    private boolean stop = false;


    // onResume() restarts the Broadcast Receive if the app has been allowed to die or if the
    // orientation of the phone changes.
    @Override
    protected void onResume() {
        super.onResume();

        startCreateBroadcaster();
    }

    //onDestroy() destroys the Broadcast Receiver to prevent data leakage. Also stops all music
    //immediately.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
        stopThePresses();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        // transfer over an necessary data from any previous state (phone locked or unlocked, change
        //in orientation of the phone.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // is26AndUp controls is the VolumeShaper library is used in MusicObj's.
        is26AndUp = Build.VERSION.SDK_INT >= 26;

        start = (Button) findViewById(R.id.button1);
        startToggle = false;

        title = (TextView) findViewById(R.id.currently_playing);
        sculpture = (TextView) findViewById(R.id.sculpture);
        testBox = (TextView) findViewById(R.id.test_box);

        // A class of fixed properties representing each sculpture
        gpsMaths = new GPSMaths();

        // This if break ensures the user has provided GPS position access.
        Log.d(TAG, "onCreate: pre-check");
        if(!runtime_permissions()) {
            enable_buttons();
        }
        startCreateBroadcaster();
    }



    private void enable_buttons() {
        Log.d(TAG, "enable_buttons: start of function");

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: start of onclick");
                if (!startToggle) {
                    Log.i(TAG, "onClick: Start clicked");
                    Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                    startService(i);
                    startToggle = true;
                    start.setText("Stop");
                    Log.d(TAG, "onClick: end of Start clicked, BROADCASTER = " + broadcastReceiver.toString());
                } else {
                    Log.d(TAG, "onClick: Stop clicked");
                    Intent i = new Intent(getApplicationContext(),GPS_Service.class);
                    stopService(i);
                    startToggle = false;
                    start.setText("Start");
                    stopThePresses();
                }
            }
        });

    }

    // setText() sets the text for the title and scuplture textviews
    public void setText(String titleText, String sculptureText) {
        title.setText(titleText);
        sculpture.setText(sculptureText);
        title.setVisibility(View.VISIBLE);
        sculpture.setVisibility(View.VISIBLE);
    }



    // runtime_permissions() requests access to GPS location data from the user. Code copied from
    // a tutorial; unsure exactly how it works.
    private boolean runtime_permissions() {
        Log.d(TAG, "runtime_permissions: ");
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }

    // to ensure access to the GPS coordinates even when the app is not active or when the phone is
    // locked, a Broadcast Receiver is required. This Broadcast Receiver is started with
    // startCreateBroadcasterr(). It will poll for GPS data as frequently as set. Once data is
    // acquired, the GPS target location of each sculpture is tested for target length from the
    // sculpture. If the user is in range, this starts the piece-switching process.
    private void startCreateBroadcaster() {
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Log.i(TAG, "broadcastReceiver.onReceive: " + Double.toString(intent.getDoubleExtra("Xcoords", 0)) + ", " + Double.toString(intent.getDoubleExtra("Ycoords", 0)));
                    testBox.setText(Double.toString(intent.getDoubleExtra("Xcoords", 0)) + ", " + Double.toString(intent.getDoubleExtra("Ycoords", 0)));
                    Sculpture playThis = gpsMaths.playWhat(intent.getDoubleExtra("Xcoords", 0), intent.getDoubleExtra("Ycoords", 0));
                    // if not currently transitioning, use these if-statements to switch to the
                    // appropiate piece.
                    if (!transitioning) {
                        setText(playThis.getTitle(), playThis.getName());
                        if (audio1 == null && audio2 == null) {
                            new startMusic().execute(playThis);
                        } else if (audio1 == null) {
                            if (!audio2.title.equals(playThis.getTitle())) {
                                new transition().execute(playThis);
                            } else {
                                return;
                            }
                        } else {
                            if (!audio1.title.equals(playThis.getTitle())) {
                                new transition().execute(playThis);
                            } else {
                                return;
                            }
                        }
                    }

                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }


    // causing permission for GPS location data to open infinitely when user refusing the access.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                enable_buttons();
            }else {
                runtime_permissions();
            }
        }
    }

    // Stop all music immedately
    public void stopThePresses() {
        if (audio1 != null) {
            audio1.mediaPlayer.stop();
            audio1.destroyMusicObj();
            audio1 = null;
        }
        if (audio2 != null) {
            audio2.mediaPlayer.stop();
            audio2.destroyMusicObj();
            audio2 = null;
        }
    }



    // Asynchronous function to start the application playing so the Promenade starts playing. Such
    // tasks cannot be performed on the main UI thread.
    public class startMusic extends AsyncTask<Sculpture, Void, Void> {

        @SuppressLint("NewApi")
        @Override
        protected Void doInBackground(Sculpture... sculptures) {
            Log.i(TAG, "startMusic.doInBackground: ");
            if (is26AndUp) {
                audio1 = new MusicObj(sculptures[0].getPiece(), TRANSITION_TIME, getApplicationContext(), sculptures[0].getTitle(), sculptures[0].getStartTime());
                audio1.mediaPlayer.start();
                audio1.shaper.apply(VolumeShaper.Operation.PLAY);
                while (audio1.getVolume() != 1.0) {
//                Log.i(TAG, "doInBackground: startMusic: audio1 volume: " + Float.toString(audio1.getVolume()));
//                try {
//                    wait(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                }
                return null;
            } else {
                audio1 = new MusicObj(sculptures[0].getPiece(), getApplicationContext(), sculptures[0].getTitle(), sculptures[0].getStartTime());
                audio1.mediaPlayer.start();
                long lastTimeRun = System.nanoTime();
                long start = System.nanoTime();
                while (audio1.getVolume() < 1) {
                    long currentTime = System.nanoTime();
                    Log.d(TAG, "startMusic.doInBackground: volume at " + Float.toString(audio1.getVolume()));
                    audio1.noShaperFadeIn((float) ((currentTime - lastTimeRun) / 1000000));
                    lastTimeRun = currentTime;
                }
                Log.i(TAG, "doInBackground: fade time = " + Long.toString((System.nanoTime() - start) / 1000000));
                audio1.mediaPlayer.setVolume(1,1);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            transitioning = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            transitioning = true;
        }
    }

    // transition takes a moment to record any change in MusicObj.startTime
    public class transition extends AsyncTask<Sculpture, Void, Integer> {

        public String titleToStore;

        @SuppressLint("NewApi")
        @Override
        protected Integer doInBackground(Sculpture... sculptures) {
            if (is26AndUp) {
                if (audio1 == null) {
                    titleToStore = audio2.title;
                    audio1 = new MusicObj(sculptures[0].getPiece(), TRANSITION_TIME, getApplicationContext(), sculptures[0].getTitle(), sculptures[0].getStartTime());
                    audio1.mediaPlayer.start();
                    audio1.shaper.apply(VolumeShaper.Operation.PLAY);
                    audio2.shaper.apply(VolumeShaper.Operation.REVERSE);
                    while (audio1.getVolume() != 1.0 && audio2.getVolume() != 0) {
                        //                    Log.i(TAG, "doInBackground: transition: audio1 volume: " + Float.toString(audio1.getVolume()) + " audio2 volume: " + Float.toString(audio2.getVolume()));
                        //                    try {
                        //                        wait(500);
                        //                    } catch (InterruptedException e) {
                        //                        e.printStackTrace();
                        //                    }
                    }
                    audio2.mediaPlayer.pause();
                    return audio2.getTimeStamp();
                } else {
                    titleToStore = audio1.title;
                    audio2 = new MusicObj(sculptures[0].getPiece(), TRANSITION_TIME, getApplicationContext(), sculptures[0].getTitle(), sculptures[0].getStartTime());
                    audio2.mediaPlayer.start();
                    audio2.shaper.apply(VolumeShaper.Operation.PLAY);
                    audio1.shaper.apply(VolumeShaper.Operation.REVERSE);
                    while (audio2.getVolume() != 1.0 && audio1.getVolume() != 0) {
                        //                    Log.i(TAG, "doInBackground: transition: audio2 volume: " + Float.toString(audio1.getVolume()) + " audio1 volume: " + Float.toString(audio2.getVolume()));
                        //                    try {
                        //                        wait(500);
                        //                    } catch (InterruptedException e) {
                        //                        e.printStackTrace();
                        //                    }
                    }
                    audio1.mediaPlayer.pause();
                    return audio1.getTimeStamp();
                }
            } else {
                if (audio1 == null) {
                    titleToStore = audio2.title;
                    audio1 = new MusicObj(sculptures[0].getPiece(), getApplicationContext(), sculptures[0].getTitle(), sculptures[0].getStartTime());
                    audio1.mediaPlayer.start();
                    long lastTimeRun = System.nanoTime();
                    while (audio1.getVolume() <= 1.0 && audio2.getVolume() >= 0) {
                        long currentTime = System.nanoTime();
                        if (audio1.getVolume() < 1) {
                            audio1.noShaperFadeIn((float) ((currentTime - lastTimeRun) / 1000000));
                        } else {
                            audio1.mediaPlayer.setVolume(1, 1);
                        }
                        if (audio2.getVolume() > 0) {
                            audio2.noShaperFadeOut((float) ((currentTime - lastTimeRun) / 1000000));
                        } else {
                            audio2.mediaPlayer.setVolume(0, 0);
                        }
                        lastTimeRun = currentTime;
                    }
                    audio2.mediaPlayer.pause();
                    return audio2.getTimeStamp();
                } else {
                    titleToStore = audio1.title;
                    audio2 = new MusicObj(sculptures[0].getPiece(), getApplicationContext(), sculptures[0].getTitle(), sculptures[0].getStartTime());
                    audio2.mediaPlayer.start();
                    long lastTimeRun = System.nanoTime();
                    while (audio2.getVolume() <= 1.0 && audio1.getVolume() >= 0) {
                        long currentTime = System.nanoTime();
                        if (audio2.getVolume() < 1) {
                            audio2.noShaperFadeIn((float) ((currentTime - lastTimeRun) / 1000000));
                        } else {
                            audio2.mediaPlayer.setVolume(1, 1);
                        }
                        if (audio1.getVolume() > 0) {
                            audio1.noShaperFadeOut((float) ((currentTime - lastTimeRun) / 1000000));
                        } else {
                            audio1.mediaPlayer.setVolume(0, 0);
                        }
                        lastTimeRun = currentTime;
                    }
                    audio1.mediaPlayer.pause();
                    return audio1.getTimeStamp();
                }
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            transitioning = false;
            switch (titleToStore) {
                case "Headless Figures":
                    gpsMaths.standingFigures.setStartTime(integer);
                    break;
                case "Love is a Madman":
                    gpsMaths.rumi.setStartTime(integer);
                    break;
                case "Broken":
                    gpsMaths.ferment.setStartTime(integer);
                    break;
                case "Oda a La Vanguardia":
                    gpsMaths.shuttlecockN.setStartTime(integer);
                    break;
                case "Fireflies in the Garden":
                    gpsMaths.shuttlecockS.setStartTime(integer);
                    break;
                case "Both light and shadow":
                    gpsMaths.shuttleTransition.setStartTime(integer);
                    break;
                case "Promenade":
                    gpsMaths.promenade.setStartTime(integer);
                    break;
                case "Big Muddy":
                    gpsMaths.rooftop.setStartTime(integer);
                    break;
                case "To Cast Four Motives":
                    gpsMaths.fourmotives.setStartTime(integer);
                    break;
                default:
                    Log.e(TAG, "transition.onPostExecute: something is wrong with timestamping");
            }
            if (audio1.title == titleToStore) {
                audio1.destroyMusicObj();
                audio1 = null;
            } else {
                audio2.destroyMusicObj();
                audio2 = null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            transitioning = true;
        }

    }
}
