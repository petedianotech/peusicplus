package com.peusicplus.player

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class LibraryAdapter(
    private val onEntryClick: (LibraryEntry) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder>() {

    private var entries: List<LibraryEntry> = emptyList()

    class LibraryViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val artwork: ImageView = view.findViewById(R.id.itemArtwork)
        val title: TextView = view.findViewById(R.id.itemTitle)
        val subtitle: TextView = view.findViewById(R.id.itemSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library, parent, false)
        return LibraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        val entry = entries[position]
        holder.title.text = entry.title
        holder.subtitle.text = entry.subtitle

        if (entry.albumId != -1L) {
            holder.artwork.load(entry.albumArtUri(entry.albumId)) {
                crossfade(true)
                error(R.drawable.ic_music_note)
            }
        } else {
            holder.artwork.setImageResource(R.drawable.ic_music_note)
        }

        holder.itemView.setOnClickListener { onEntryClick(entry) }
    }

    override fun getItemCount(): Int = entries.size

    fun submitEntries(newEntries: List<LibraryEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    private fun LibraryEntry.albumArtUri(albumId: Long): Uri {
        return Uri.parse("content://media/external/audio/albumart/$albumId")
    }
}