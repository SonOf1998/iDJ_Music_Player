package me.me.balancekt

import android.media.MediaPlayer

class Music(val path : String, var isLooping : Boolean)
{
    var mp : MediaPlayer = MediaPlayer.create(MainActivity.getContext(), R.raw.garrix)

    init
    {
        mp.isLooping = isLooping;

    }

    constructor(path : String) : this(path, false)



    fun play(){
        mp.start()
    }

    fun pause() {
        mp.pause()
    }
}