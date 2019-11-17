package edu.cs4730.floatingcubes;

import android.content.Context;
import android.media.MediaPlayer;

public class MosquitoPlayer{
    Context myContext;
    MediaPlayer mediaPlayer;

    MosquitoPlayer(Context context){
        myContext = context;
        mediaPlayer = MediaPlayer.create(myContext, R.raw.mosquito_1_1);
        mediaPlayer.start();
    }
}
