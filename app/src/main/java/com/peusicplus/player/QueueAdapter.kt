package com.peusicplus.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QueueAdapter(
    private val onPlay: (Int) -> Unit,
    private val onRemove: (Int) -> Unit,
    private val onMoveUp: (Int) -> Unit,
    private val onMoveDown: (Int) -> Unit
) : RecyclerView.Adapter<QueueAdapter.QueueViewHolder>() {

    private var items: List<QueueItem> = emptyList()

    class QueueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.queueTitle)
        val artist: TextView = view.findViewById(R.id.queueArtist)
        val up: ImageButton = view.findViewById(R.id.moveUpButton)
        val down: ImageButton = view.findViewById(R.id.moveDownButton)
        val remove: ImageButton = view.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_queue, parent, false)
        return QueueViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.artist.text = item.artist
        holder.itemView.alpha = if (item.isCurrent) 1f else 0.75f
        holder.itemView.setOnClickListener { onPlay(position) }
        holder.up.setOnClickListener { onMoveUp(position) }
        holder.down.setOnClickListener { onMoveDown(position) }
        holder.remove.setOnClickListener { onRemove(position) }
    }

    override fun getItemCount(): Int = items.size

    fun submitQueue(newItems: List<QueueItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}