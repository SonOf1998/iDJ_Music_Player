package me.me.balancekt

import android.app.Application
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.*
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
import android.widget.Button
import android.widget.ImageView
import kotlin.system.exitProcess

class FileBrowser : DialogFragment()
{
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.file_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val browserAdapter = BrowserAdapter(childFragmentManager)

        // initial state
        val leftButton = view.findViewById<Button>(R.id.bTabMusicBrowser)
        val midButton = view.findViewById<Button>(R.id.bTabAllMusic)
        val rightButton = view.findViewById<Button>(R.id.bTabPlaylists)
        leftButton.text = "Explorer"
        leftButton.setTextColor(Color.WHITE)
        midButton.text = "All music"
        midButton.setTextColor(Color.GRAY)
        rightButton.text = "Playlists"
        rightButton.setTextColor(Color.GRAY)

        leftButton.setBackgroundResource(R.drawable.left_button_selected)
        midButton.setBackgroundResource(R.drawable.mid_button_default)
        rightButton.setBackgroundResource(R.drawable.right_button_default)



        val vpBrowser = view.findViewById<ViewPager>(R.id.vpBrowser)

        leftButton.setOnClickListener { vpBrowser.currentItem = 0 }
        midButton.setOnClickListener { vpBrowser.currentItem = 1 }
        rightButton.setOnClickListener { vpBrowser.currentItem = 2 }

        vpBrowser.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(p0: Int) {
                when(p0)
                {
                    0 -> {
                        leftButton.setBackgroundResource(R.drawable.left_button_selected)
                        midButton.setBackgroundResource(R.drawable.mid_button_default)
                        rightButton.setBackgroundResource(R.drawable.right_button_default)
                        leftButton.setTextColor(Color.WHITE)
                        midButton.setTextColor(Color.GRAY)
                        rightButton.setTextColor(Color.GRAY)
                    }
                    1 -> {
                        leftButton.setBackgroundResource(R.drawable.left_button_default)
                        midButton.setBackgroundResource(R.drawable.mid_button_selected)
                        rightButton.setBackgroundResource(R.drawable.right_button_default)
                        leftButton.setTextColor(Color.GRAY)
                        midButton.setTextColor(Color.WHITE)
                        rightButton.setTextColor(Color.GRAY)
                    }
                    2 -> {
                        leftButton.setBackgroundResource(R.drawable.left_button_default)
                        midButton.setBackgroundResource(R.drawable.mid_button_default)
                        rightButton.setBackgroundResource(R.drawable.right_button_selected)
                        leftButton.setTextColor(Color.GRAY)
                        midButton.setTextColor(Color.GRAY)
                        rightButton.setTextColor(Color.WHITE)
                    }
                }
            }

        })
        vpBrowser.adapter = browserAdapter
        vpBrowser.currentItem = 0
    }

    override fun onResume() {
        super.onResume()
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.8).toInt()
        dialog.window?.setLayout(width, height)
    }
}
