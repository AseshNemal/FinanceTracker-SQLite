package com.example.fitnancetracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnancetracker.CategorySummary
import com.example.fitnancetracker.R

class CategorySummaryAdapter(private val summaryList: List<CategorySummary>) :
    RecyclerView.Adapter<CategorySummaryAdapter.SummaryViewHolder>() {

    class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvTotalAmount: TextView = itemView.findViewById(R.id.tvTotalAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_summary, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        val summary = summaryList[position]
        holder.tvCategory.text = summary.category
        holder.tvTotalAmount.text = "Rs. %.2f".format(summary.totalAmount)

        
    }

    override fun getItemCount(): Int = summaryList.size
}
