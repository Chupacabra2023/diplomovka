package com.example.diplomovka_kotlin.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.diplomovka_kotlin.R
import com.example.diplomovka_kotlin.databinding.ItemFaqBinding

data class FaqItem(val question: String, val answer: String)

class FaqAdapter(private val items: List<FaqItem>) :
    RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {

    inner class FaqViewHolder(val binding: ItemFaqBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ItemFaqBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvQuestion.text = item.question
        holder.binding.tvAnswer.text = item.answer

        holder.binding.llQuestion.setOnClickListener {
            val isVisible = holder.binding.tvAnswer.visibility == View.VISIBLE
            holder.binding.tvAnswer.visibility = if (isVisible) View.GONE else View.VISIBLE
            holder.binding.ivArrow.setImageResource(
                if (isVisible) R.drawable.ic_expand_more else R.drawable.ic_expand_less
            )
        }
    }

    override fun getItemCount() = items.size
}