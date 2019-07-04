package me.me.balancekt


import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.* // Make findViewById redundant
import java.util.*
import kotlin.math.ln
import kotlin.math.min
import android.view.MotionEvent
import android.widget.TextView
import java.lang.Thread.sleep
import java.lang.ref.Reference
import kotlin.math.abs
import kotlin.math.max


class MainActivity : AppCompatActivity()
{
    var dbScaleLeft = 0.0f
    var dbScaleRight = 0.0f

    lateinit var music : Music

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        activity = this
        setContentView(R.layout.activity_main)
        setOnNotchSupport() // modern androidos telefonokon a front kamera bevágása melletti területekre is akarunk rajzolni
        setUpAlbumCoverCarousel()
        checkRuntimePermissions()
        music = Music("goodguy")
        music.play()

        openBrowser()
        setUpVolumeChannels()
    }

    private fun setOnNotchSupport()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    private fun setUpAlbumCoverCarousel()
    {
        val acc = AlbumCoverCarousel()
        val accView = albumCoverCarousel

        accView.adapter = acc
        accView.currentItem = 0
    }

    private fun setUpVolumeChannels()
    {
        val sharedPreferences = applicationContext.getSharedPreferences("volumes", Context.MODE_PRIVATE)
        val leftChannelVol = sharedPreferences.getInt("leftVolume", 100)
        val rightChannelVol = sharedPreferences.getInt("rightVolume", 100)

        fun volToDb(vol : Int) : Float = min(1.0f - (ln(100.0 - vol) / ln(100.0)).toFloat(), 1.0f)
        dbScaleLeft = volToDb(leftChannelVol)
        dbScaleRight = volToDb(rightChannelVol)

        music.mp.setVolume(dbScaleLeft, dbScaleRight)
        tbLeftVolumeLabel.tag = "left"
        tbRightVolumeLabel.tag = "right"

        var swipeL : SwipeVolumeControlListener?
        val swipeR : SwipeVolumeControlListener?

        swipeL = SwipeVolumeControlListener(leftChannelVol, tbLeftVolumeLabel, music.mp, null)
        swipeR = SwipeVolumeControlListener(rightChannelVol, tbRightVolumeLabel, music.mp, swipeL)
        swipeL.otherSideController = swipeR

        val gestureDetectorL = GestureDetector(this, swipeL)
        gestureDetectorL.setIsLongpressEnabled(false)
        val gestureDetectorR = GestureDetector(this, swipeR)
        gestureDetectorR.setIsLongpressEnabled(false)

        vLeftChannel.setOnTouchListener { _, event -> gestureDetectorL.onTouchEvent(event) }
        vRightChannel.setOnTouchListener { _, event -> gestureDetectorR.onTouchEvent(event) }
    }

    class SwipeVolumeControlListener(
        private var volume : Int,
        private val label : TextView?,
        private val mp : MediaPlayer,
        var otherSideController : SwipeVolumeControlListener?
    ) : GestureDetector.SimpleOnGestureListener() {

        private val distanceFromZeroToMax = 600.0f
        private var currentProgress = 0.0f

        fun volToDb(vol : Int) : Float = min(1.0f - (ln(100.0 - vol) / ln(100.0)).toFloat(), 1.0f)

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean
        {
            if(label != null && e1 != null && e2 != null)
            {
                currentProgress += (100 * distanceY) / distanceFromZeroToMax
                if(abs(currentProgress) >= 1.0f)
                {
                    volume += currentProgress.toInt()
                    currentProgress = 0.0f
                }

                if(distanceY < 0)
                {
                    volume = max(volume, 0)
                }

                if(distanceY > 0)
                {
                    volume = min(volume, 100)
                }

                val sharedPreferences = getContext().getSharedPreferences("volumes", Context.MODE_PRIVATE )
                if(label.tag == "left")
                {
                    mp.setVolume(volToDb(volume), volToDb(otherSideController!!.volume))
                    with(sharedPreferences.edit())
                    {
                        putInt("leftVolume", volume)
                        apply()
                    }
                }
                else
                {
                    mp.setVolume(volToDb(otherSideController!!.volume), volToDb(volume))
                    with(sharedPreferences.edit())
                    {
                        putInt("rightVolume", volume)
                        apply()
                    }
                }




                label.text = "$volume%"
            }

            return false
        }


        override fun onDown(event: MotionEvent): Boolean {

            if(label != null)
            {
                label.visibility = View.VISIBLE
            }

            return true
        }

    }



    private fun openBrowser()
    {
        val fileBrowser = FileBrowser()
        fileBrowser.show(supportFragmentManager, "")
    }


    /** Android 6.0+ runtime permission request
     *
     *  Update this with manifest simultaneously
     */

    val PERMISSION_READ_WRITE = 0x00

    private fun checkRuntimePermissions()
    {
        val permissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        if (!permissionRead || !permissionWrite ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS))
            {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_READ_WRITE)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        if (requestCode == PERMISSION_READ_WRITE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Everything OK
            }
        }
    }

    companion object
    {
        private var activity : MainActivity? = null
        fun getContext() : Context = activity!!.applicationContext
    }
}
