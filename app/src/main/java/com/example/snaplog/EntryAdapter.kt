package com.example.snaplog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snaplog.databinding.ItemEntryBinding

class EntryAdapter(
    private val onClick: (Entry) -> Unit,
    private val onLongPress: (Entry) -> Unit
) : RecyclerView.Adapter<EntryAdapter.VH>() {

    private var items: List<Entry> = emptyList()

    fun submitList(newItems: List<Entry>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemEntryBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = items[position]
        holder.b.tvCaption.text = e.caption
        holder.b.tvQuote.text = e.quote

        Glide.with(holder.itemView.context)
            .load(e.imageUrl)
            .centerCrop()
            .into(holder.b.ivThumb)

        holder.itemView.setOnClickListener { onClick(e) }
        holder.itemView.setOnLongClickListener {
            onLongPress(e)
            true
        }
    }

    override fun getItemCount(): Int = items.size
}
