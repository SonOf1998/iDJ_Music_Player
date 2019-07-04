package me.me.balancekt

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView


class AlbumCoverCarousel : PagerAdapter()
{
    private val layoutInflater = MainActivity.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    val albumCovers = mutableListOf<Int>(
        R.drawable.cutler,
        R.drawable.dog,
        R.drawable.rofl,
        R.drawable.vsit
    )



    override fun instantiateItem(container: ViewGroup, position: Int) : Any
    {
        val itemView =  layoutInflater.inflate(R.layout.album_carousel_element, container, false)

        val imageView = itemView.findViewById<ImageView>(R.id.albumCover)
        imageView.setImageResource(R.drawable.default_album_cover)
        container.addView(itemView)

        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any)
    {
        container.removeView(obj as ConstraintLayout)
    }

    override fun getCount() = albumCovers.size
    override fun isViewFromObject(view: View, obj: Any) = view == obj as ConstraintLayout
}