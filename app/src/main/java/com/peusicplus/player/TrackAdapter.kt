package com.peusicplus.player

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class TrackAdapter(
    private val onTrackClick: (Int) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private var tracks: List<Track> = emptyList()
    private var selectedPosition = -1

    class TrackViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val artwork: ImageView = view.findViewById(R.id.itemArtwork)
        val title: TextView = view.findViewById(R.id.trackTitle)
        val artist: TextView = view.findViewById(R.id.trackArtist)
        val duration: TextView = view.findViewById(R.id.trackDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.title.text = track.title
        holder.artist.text = "${track.artist} • ${track.album}"
        holder.duration.text = formatDuration(track.durationMs)
        holder.artwork.load(track.albumArtUri) {
            crossfade(true)
            error(R.drawable.ic_music_note)
        }
        holder.itemView.isSelected = position == selectedPosition
        holder.itemView.setOnClickListener { onTrackClick(position) }
    }

    override fun getItemCount(): Int = tracks.size

    fun submitTracks(newTracks: List<Track>) {
        tracks = newTracks
        selectedPosition = if (tracks.isEmpty()) -1 else 0
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        val previous = selectedPosition
        selectedPosition = position
        if (previous != -1) notifyItemChanged(previous)
        notifyItemChanged(position)
    }

    private fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }
}