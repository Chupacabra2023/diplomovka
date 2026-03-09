package com.example.diplomovka_kotlin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.diplomovka_kotlin.databinding.ItemInvitationBinding

data class InvitationItem(val title: String, val sender: String, val status: String)

class InvitationAdapter(
    private val items: List<InvitationItem>,
    private val onAction: (InvitationItem, String) -> Unit
) : RecyclerView.Adapter<InvitationAdapter.InvitationViewHolder>() {

    inner class InvitationViewHolder(val binding: ItemInvitationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        val binding = ItemInvitationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InvitationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvTitle.text = item.title
        holder.binding.tvSender.text = "Od: ${item.sender}"
        holder.binding.tvStatus.text = "Stav: ${item.status}"

        holder.binding.btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add("✅ Prijať")
            popup.menu.add("❌ Odmietnuť")
            popup.setOnMenuItemClickListener { menuItem ->
                onAction(item, menuItem.title.toString())
                true
            }
            popup.show()
        }
    }

    override fun getItemCount() = items.size
}