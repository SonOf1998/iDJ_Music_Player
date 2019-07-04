package me.me.balancekt

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.mpatric.mp3agic.Mp3File
import java.io.File
import java.lang.ref.WeakReference
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class AllMusicFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.fragment_all_music, container, false);
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val listView = view.findViewById<ListView>(R.id.lvAllMusic)

        val asyncAddingItems = AsyncAddingItems(activity, listView)
        asyncAddingItems.execute()
    }
}

class AsyncAddingItems(val activity : FragmentActivity?, val listView : ListView) : AsyncTask<Void, Void, MutableList<String>>()
{
    private val listByteArray = mutableListOf<ByteArray?>()

    override fun doInBackground(vararg params: Void?): MutableList<String>?
    {
        val mutableList = mutableListOf<String>()

        fun recursiveFileSystemTraversal(f : File)
        {
            val files = f.listFiles()
            for(file in files)
            {
                if(file.isDirectory)
                {
                    recursiveFileSystemTraversal(file)
                }
                if(file.isFile && file.path.endsWith(".mp3"))
                {
                    // Name of mp3 file
                    val filePath = file.toString()
                    mutableList.add(filePath.substring(filePath.lastIndexOf('/') + 1).replace(".mp3", ""))

                    // Duration of mp3 file
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(filePath)
                    val durationStr : String = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    mediaMetadataRetriever.release()
                    val duration = durationStr.toLong()
                    val formatter = if((duration / 1000) < 60 * 60) {
                        DateTimeFormatter.ofPattern("mm:ss").withZone(ZoneId.systemDefault())
                    } else {
                        DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
                    }
                    val instant = Instant.ofEpochMilli(duration)
                    mutableList.add(formatter.format(instant))

                    // Size of mp3 file
                    val fileSizeInBytes = file.length()
                    val kB = fileSizeInBytes / 1024.0

                    val decimalFormat = DecimalFormat("#.##", DecimalFormatSymbols.getInstance( Locale.ENGLISH ))
                    decimalFormat.roundingMode = RoundingMode.HALF_UP

                    if(kB < 1024)
                    {
                        mutableList.add(decimalFormat.format(kB) + " kB")
                    }
                    else
                    {
                        mutableList.add(decimalFormat.format(kB / 1024) + " MB")
                    }

                    // cache album covers
                    val musicFile = Mp3File(file)
                    if(musicFile.hasId3v2Tag())
                    {
                        val albumCoverInBytes = musicFile.id3v2Tag.albumImage
                        listByteArray.add(albumCoverInBytes)
                    }
                }
            }
        }
        recursiveFileSystemTraversal(Environment.getExternalStorageDirectory())

        return mutableList
    }

    override fun onPostExecute(result: MutableList<String>?)
    {
        // Runs on UI thread
        super.onPostExecute(result)

        val listAdapter = ListAdapter(activity?.applicationContext)


        if (result != null) {
            for(i in 0 until result.size)
            {
                when(i % 3)
                {
                    0 -> listAdapter.titles.add(result[i])
                    1 -> listAdapter.durations.add(result[i])
                    2 -> listAdapter.sizes.add(result[i])
                }
            }

            for(imageBytes in listByteArray)
            {
                listAdapter.imageBytes.add(imageBytes)
            }
        }


        listView.adapter = listAdapter
    }
}

class ListAdapter(val context : Context?) : BaseAdapter()
{
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    val titles = mutableListOf<String>()
    val durations = mutableListOf<String>()
    val sizes = mutableListOf<String>()
    val imageBytes = mutableListOf<ByteArray?>()

    /**
     * structure of data:
     *
     *  song title
     *  song duration
     *  song size
     *  song last modified
     */

    class ViewHolder()
    {
        lateinit var title : TextView
        lateinit var duration : TextView
        lateinit var size : TextView
        lateinit var albumCover : ImageView

        fun clear()
        {
            title.text = null
            duration.text = null
            size.text = null

            albumCover.setImageBitmap(null)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view : View? = convertView
        convertView ?: layoutInflater.inflate(R.layout.item_all_music_list, parent, false)

        val viewHolder : ViewHolder

        if(view == null)
        {
            view = layoutInflater.inflate(R.layout.item_all_music_list, parent, false)
            viewHolder = ViewHolder()


            viewHolder.title = view.findViewById<TextView>(R.id.tvAllMusicItemTitle)
            viewHolder.duration = view.findViewById<TextView>(R.id.tvAllMusicItemDuration)
            viewHolder.size = view.findViewById<TextView>(R.id.tvAllMusicItemSize)
            viewHolder.albumCover = view.findViewById<ImageView>(R.id.ivAllMusicItemAlbumCover)

            view.tag = viewHolder
        }
        else
        {
            viewHolder = view.tag as ViewHolder
            viewHolder.clear()
        }

        if(view != null)
        {
            viewHolder.title.text = titles[position]
            viewHolder.title.setTextColor(Color.WHITE)
            viewHolder.duration.text = Html.fromHtml("<font color='#DEDEDE'>Duration: ${durations[position]}</font>", Html.FROM_HTML_MODE_LEGACY)
            viewHolder.size.text = Html.fromHtml("<font color='#DEDEDE'>Size: ${sizes[position]}</font>", Html.FROM_HTML_MODE_LEGACY)

            viewHolder.albumCover.tag = position
            val asyncSetMiniAlbumCovers = AsyncSetMiniAlbumCovers(viewHolder.albumCover, imageBytes[position])
            asyncSetMiniAlbumCovers.execute()
        }

        return view!!
    }

    class AsyncSetMiniAlbumCovers(imageView: ImageView, private val albumCoverInBytes: ByteArray?) : AsyncTask<File, Void, Bitmap>()
    {
        private val iv = WeakReference(imageView)
        private val tag = iv.get()?.tag

        override fun doInBackground(vararg params: File?): Bitmap? {

            if(albumCoverInBytes != null)
            {
                var bmp = BitmapFactory.decodeByteArray(albumCoverInBytes, 0, albumCoverInBytes.count())
                bmp = Bitmap.createScaledBitmap(bmp, iv.get()?.measuredWidth!!, iv.get()?.measuredHeight!!, false)
                return bmp
            }

            return null
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)

            if(iv.get()?.tag != tag)
            {
                return
            }

            if(result != null)
            {
                val origin = iv.get()
                origin?.setImageBitmap(result)
            }
            else
            {
                iv.get()?.setBackgroundResource(R.drawable.default_album_cover)
            }


        }
    }


    override fun getItem(position: Int): Any {
        return Any()   // flawed, dummy stuff
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount() = titles.size

}