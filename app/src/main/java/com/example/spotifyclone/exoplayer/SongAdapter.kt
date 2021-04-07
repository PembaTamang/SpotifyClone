package com.example.spotifyclone.exoplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.model.BKMediaTrack
import kotlinx.android.synthetic.main.list_item.view.*
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
               clickListener!!.invoke(songs[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            tvPrimary.text = song.title
            tvSecondary.text = song.artist
            glide.load(song.imageUrl).into(ivItemImage)
        }
    }

    override fun getItemCount() = songs.size

    private var clickListener : ((BKMediaTrack)->Unit)? = null

    fun setClickListener(itemClick: (BKMediaTrack) -> Unit){
        clickListener = itemClick
    }

    private val diffCallback = object : DiffUtil.ItemCallback<BKMediaTrack>() {
        override fun areItemsTheSame(oldItem: BKMediaTrack, newItem: BKMediaTrack): Boolean {
            return oldItem.mediaID == newItem.mediaID
        }

        override fun areContentsTheSame(oldItem: BKMediaTrack, newItem: BKMediaTrack): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    private val differ = AsyncListDiffer(this, diffCallback)

    var songs: List<BKMediaTrack>
        get() = differ.currentList
        set(value) = differ.submitList(value)

}