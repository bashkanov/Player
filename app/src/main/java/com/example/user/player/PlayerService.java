package com.example.user.player;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class PlayerService extends Service {

    private final IBinder mBinder = new LocalBinder();
    protected MediaPlayer mPlayer;
    private static final String TAG = "myLogs";

    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "(OnCreateServise)");
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        Log.e(TAG, "(onStartCommand)");
        if (mPlayer!=null){
            Log.e(TAG, "(OnBind) mPlayer is not null");
            mPlayer.stop();
            mPlayer.release();
            mStart();
        }
        return(START_NOT_STICKY);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        mStart();
//        if (mPlayer!=null){
//            Log.e(TAG, "(OnBind) mPlayer is not null");
//            mPlayer.stop();
//            mPlayer.release();
//            mStart();
//        }
        Log.e(TAG, "(OnBind)");
        return mBinder;
    }

    public class LocalBinder extends Binder {
        PlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlayerService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
     //   mPlayer.stop();
      //  mPlayer.release();
        Log.e(TAG, "Servise is stopped");
    }

    /** method for clients */
    public void mStart(){
        Log.e(TAG, "Custom method start");
        mPlayer = MediaPlayer.create(this, Play_activity.u);
        Play_activity.songTotalDurationLabel.setText(Play_activity.milliSecondsToTimer(mPlayer.getDuration()));
        Play_activity.seekBar.setMax(mPlayer.getDuration());
        mPlayer.setLooping(false);
        mPlayer.start();
    }
}
