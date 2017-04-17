package com.example.user.player;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class Play_activity extends AppCompatActivity implements View.OnClickListener {

    private AnimatedVectorDrawable mPlayDrawable;
    private AnimatedVectorDrawable mPauseDrawable;
    private boolean mPlayFlag;
    private ImageButton SoundButton1;
    private static final String TAG = "myLogs";

    boolean mBound = false;
    PlayerService mService;
    public static SeekBar seekBar;
    private Handler mHandler = new Handler();
    ArrayList<File> mySongs;
    private TextView textView;

    private Button btForw, btNext;
    ImageButton btPlay;
    TextView songCurrentDurationLabel;
    public static TextView songTotalDurationLabel;
    //MyTask mt;
    public static Uri u;
    public int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_activity);
        setTitle("MyPlayer on service with SeekBar");
        Log.d(TAG, "Создали второе активити");

        mPlayDrawable = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_triangle_animatable);
        mPauseDrawable = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_pause_animatable);
        SoundButton1 = (ImageButton)findViewById(R.id.btPlay);
        textView = (TextView) findViewById(R.id.name_song);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        btForw = (Button) findViewById(R.id.btForw);
        btNext = (Button) findViewById(R.id.btNext);
        btForw.setOnClickListener(this);
        btNext.setOnClickListener(this);

        Intent i = getIntent();
        Bundle b = i.getExtras();
        mySongs = (ArrayList) b.getParcelableArrayList("songslist");
        position = b.getInt("pos", 0);
        u = Uri.parse(mySongs.get(position).toString());
        textView.setText(mySongs.get(position).getName().replace(".mp3", ""));

        Intent intent = new Intent(this, PlayerService.class);
        startService(intent);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                if(fromTouch){
                    updateCurrentDuration();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateCurrentDuration);
                mService.mPlayer.seekTo(seekBar.getProgress());
                updateProgressBar();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, mConnection, 0);
        SoundButton1.setImageDrawable(mPauseDrawable);
        updateProgressBar();
//        mt = new MyTask();
//        mt.execute();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        mHandler.removeCallbacks(mUpdateTimeTask);
        if (mBound) {
            unbindService(mConnection);
            Log.d(TAG, "(onStop) Unbind from service");
            mBound = false;
        }
        Log.d(TAG, "(onStop) Stop play_activity");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateTimeTask);
        Log.d(TAG, "Desrtoy play_activity");

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    public void playClick(View view){
        if (mService.mPlayer.isPlaying()) {
            SoundButton1.setImageDrawable(mPauseDrawable);
            mPauseDrawable.start();
            mService.mPlayer.pause();
            mHandler.removeCallbacks(mUpdateTimeTask);
        } else {
            SoundButton1.setImageDrawable(mPlayDrawable);
            mPlayDrawable.start();
            mService.mPlayer.start();
            updateProgressBar();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btNext:
                seekBar.setProgress(0);
                mService.mPlayer.stop();
                mService.mPlayer.release();
                position = (position+1)%mySongs.size();
                u = Uri.parse(mySongs.get(position).toString());
                mService.mStart();
                updateProgressBar();
                SoundButton1.setImageDrawable(mPlayDrawable);
                //mPlayDrawable.start();
                textView.setText(mySongs.get(position).getName().replace(".mp3", ""));
                break;
            case R.id.btForw:
                seekBar.setProgress(0);
                mService.mPlayer.stop();
                mService.mPlayer.release();
                if (position-1<0){
                    position = mySongs.size()-1;
                }else{
                    position = position - 1;
                }
                u = Uri.parse(mySongs.get(position).toString());
                mService.mStart();
                updateProgressBar();
                SoundButton1.setImageDrawable(mPlayDrawable);
                //mPlayDrawable.start();
                textView.setText(mySongs.get(position).getName().replace(".mp3", ""));
                break;
        }
    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 500);
    }

    public void updateCurrentDuration() { mHandler.post(mUpdateCurrentDuration);  }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try {
                Log.d(TAG, "Trying invoke virtual method ");
                int progress = mService.mPlayer.getCurrentPosition();
                ///long totalDuration;
                long currentDuration;
                //totalDuration = mService.mPlayer.getDuration();
                currentDuration = mService.mPlayer.getCurrentPosition();

                seekBar.setProgress(progress);
                songCurrentDurationLabel.setText(milliSecondsToTimer(currentDuration));
                //songTotalDurationLabel.setText(milliSecondsToTimer(totalDuration));
                // Running this thread after 500 milliseconds
                mHandler.postDelayed(this, 500);
            }
            catch (NullPointerException npe){
                mHandler.postDelayed(this, 500);
                Log.d(TAG, "catch (NullPointerException npe ");
            }
        }
    };

    private Runnable mUpdateCurrentDuration = new Runnable() {
        public void run() {
            long currentDuration = seekBar.getProgress();
            songCurrentDurationLabel.setText(milliSecondsToTimer(currentDuration));
        }
    };

    public static String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }
        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }


//    class MyTask extends AsyncTask<Void, Void, Long> {
//
//        TextView songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
//        //long totalDuration;
//        long currentDuration;
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//        }
//
//        @Override
//        protected Long doInBackground(Void... params) {
//            try {
//                //totalDuration = mService.mPlayer.getDuration();
//                currentDuration = mService.mPlayer.getCurrentPosition();
//                //songCurrentDurationLabel.setText();
//                TimeUnit.SECONDS.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return currentDuration;
//        }
//
//        @Override
//        protected void onPostExecute(final Long l) {
//            super.onPostExecute(l);
//            songCurrentDurationLabel.setText(milliSecondsToTimer(currentDuration));
//        }
//    }
}
