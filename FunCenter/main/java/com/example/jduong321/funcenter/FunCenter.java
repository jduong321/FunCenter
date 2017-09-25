package com.example.jduong321.funcenter;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;

public class FunCenter extends Service {

    private MediaPlayer mPlayer;

    private int[] tracks;
    private String[] images;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mPlayer = new MediaPlayer();

        // set up the music track IDs
        tracks = new int[3];
        tracks[0] = R.raw.track1;
        tracks[1] = R.raw.track2;
        tracks[2] = R.raw.track3;
        images = new String[3];
        images[0] = "https://cdn.feel.moe/cs/uploads/sites/11/2012/11/Kawaii-Pikachu.jpg";
        images[1] = "https://s-media-cache-ak0.pinimg.com/736x/01/71/fc/0171fc94d452137b0f0ac2282776f171.jpg";
        images[2] = "https://s-media-cache-ak0.pinimg.com/736x/22/e1/e9/22e1e9a2524350df8d3fc4020b6e51cb.jpg";
    }

    private final FunInterface.Stub mBinder = new FunInterface.Stub() {

        @Override
        public void stop() throws RemoteException { mPlayer.stop(); }

        @Override
        public void pause() throws RemoteException { mPlayer.pause(); }

        @Override
        public void resume() throws RemoteException { mPlayer.start(); }

        @Override
        public String display(int n) throws RemoteException{
            return images[n-1];
        }

        public int getPosition()throws RemoteException
        {
            return mPlayer.getCurrentPosition();
        }

        public boolean getStatus() throws RemoteException
        {
            /**
             * Have it so we know the state of the player.
             * true:Playing
             * false:Is not playing
             */

            return mPlayer.isPlaying();
        }

        @Override
        public void play(int n) throws RemoteException {


            // if the media player has been instantiated already
            if(mPlayer != null) {
                mPlayer.stop();     // stop any possible music playing
                mPlayer.release();  // release the player's resources
                mPlayer = null;     // set the player to null
            }

            // create a new MediaPlayer based on the n value and begin
            // playing the music
            mPlayer = MediaPlayer.create(FunCenter.this, tracks[n-1]);
            mPlayer.setLooping(false);
            mPlayer.start();
        }


    };

    public FunCenter() {
    }

    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // Don't automatically restart this Service if it is killed
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        // release the media player's resources if it exists
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
        stopSelf();
    }


}
