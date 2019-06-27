package me.me.balancekt


import android.media.AudioManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.SeekBar
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.* // Make findViewById redundant

class MainActivity : AppCompatActivity()
{
    private var volumeLeft : Float? = null;
    private var volumeRight : Float? = null;


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initChannels()
        init()
    }


    private fun initChannels()
    {
        if(volumeLeft != null && volumeRight != null)
        {
            sbLeft.progress = (volumeLeft!! * 10).toInt()
            sbRight.progress = (volumeRight!! * 10).toInt()

        }
        else
        {
            val am : AudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            val volume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

            volumeLeft = volume.toFloat() / maxVolume
            volumeRight = volumeLeft;
            initChannels()
        }
    }


    private fun init()
    {
        sbLeft.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener
            {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
                {
                    Toast.makeText(applicationContext, "BRUH", Toast.LENGTH_SHORT).show()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?)
                {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?)
                {

                }
            }
        )
    }


    public override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean
    {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
            val topVolume : Float = Math.max(volumeLeft!!, volumeRight!!)

            if(topVolume + 1 <= 10.0f)
            {
                volumeLeft?.inc()
                volumeRight?.inc()

                initChannels()
            }

            return true
        }

        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            val bottomVolume : Float = Math.min(volumeLeft!!, volumeRight!!)

            if(bottomVolume - 1 >= 0.0f)
            {
                volumeLeft?.dec()
                volumeRight?.dec()

                initChannels()
                Toast.makeText(applicationContext, "WTF", Toast.LENGTH_LONG).show()
            }

            return true
        }

        return super.onKeyDown(keyCode, event)
    }



}
