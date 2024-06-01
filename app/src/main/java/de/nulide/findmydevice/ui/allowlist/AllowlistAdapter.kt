package de.nulide.findmydevice.ui.allowlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import de.nulide.findmydevice.R
import de.nulide.findmydevice.data.Allowlist

class AllowlistAdapter(
    private val onDeleteClicked: (String) -> Unit,
) : ListAdapter<AllowlistItem, AllowlistViewHolder>(AllowlistDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllowlistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_allowlist, parent, false)
        return AllowlistViewHolder(itemView, onDeleteClicked)
    }

    override fun onBindViewHolder(holder: AllowlistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun submitList(allowlist: Allowlist) {
        val list = allowlist.map { contact -> AllowlistItem(contact.name, contact.number) }
        submitList(list)
    }

    object AllowlistDiffCallback : DiffUtil.ItemCallback<AllowlistItem>() {
        override fun areItemsTheSame(oldItem: AllowlistItem, newItem: AllowlistItem): Boolean {
            return oldItem.number == newItem.number
        }

        override fun areContentsTheSame(oldItem: AllowlistItem, newItem: AllowlistItem): Boolean {
            return oldItem == newItem
        }
    }
}