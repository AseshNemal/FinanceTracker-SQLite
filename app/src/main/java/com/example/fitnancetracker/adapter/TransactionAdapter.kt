package com.example.fitnancetracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnancetracker.R
import com.example.fitnancetracker.model.Transaction

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onDelete: (Int) -> Unit,
    private val onEdit: (Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.itemTitle)
        val amount: TextView = view.findViewById(R.id.itemAmount)
        val category: TextView = view.findViewById(R.id.itemCategory)
        val date: TextView = view.findViewById(R.id.itemDate)
        val type: TextView = view.findViewById(R.id.itemType)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val t = transactions[position]
        holder.title.text = t.title
        holder.amount.text = "Rs. %.2f".format(t.amount)
        holder.category.text = t.category
        holder.date.text = t.date
        holder.type.text = t.type

        val colorRes = if (t.type == "Income") android.R.color.holo_green_dark
        else android.R.color.holo_red_dark
        holder.amount.setTextColor(ContextCompat.getColor(holder.itemView.context, colorRes))

        // Button click listeners
        holder.btnEdit.setOnClickListener {
            onEdit(holder.adapterPosition)
        }

        holder.btnDelete.setOnClickListener {
            onDelete(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = transactions.size
}
