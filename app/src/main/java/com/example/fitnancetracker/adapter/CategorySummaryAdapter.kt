package com.example.fitnancetracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnancetracker.R
import com.example.fitnancetracker.model.CategorySummary

class CategorySummaryAdapter(private val summaries: List<CategorySummary>) :
    RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val category: TextView = view.findViewById(R.id.tvCategory)
        val amount: TextView = view.findViewById(R.id.tvAmount)
        val percentage: TextView = view.findViewById(R.id.tvPercentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val summary = summaries[position]
        holder.category.text = summary.category
        holder.amount.text = "Rs. %.2f".format(summary.totalAmount)
        holder.percentage.text = "${summary.percentage}%"
    }

    override fun getItemCount() = summaries.size
}
