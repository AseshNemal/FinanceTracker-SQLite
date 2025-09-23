package com.aseshnemal.financetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aseshnemal.financetracker.adapter.TransactionAdapter
import com.aseshnemal.financetracker.model.Transaction
import com.aseshnemal.financetracker.viewmodel.TransactionViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ViewTransactionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private val viewModel: TransactionViewModel by viewModels()
    private var transactionList: MutableList<Transaction> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_transactions)

        setupBottomNavigation()

        recyclerView = findViewById(R.id.transactionRecyclerView)
        adapter = TransactionAdapter(
            transactionList,
            onDelete = { position -> deleteTransaction(position) },
            onEdit = { position -> editTransaction(position) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        observeTransactions()
    }

    private fun observeTransactions() {
        lifecycleScope.launch {
            viewModel.allTransactions.collectLatest { transactions ->
                transactionList.clear()
                transactionList.addAll(transactions)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun deleteTransaction(position: Int) {
        val transaction = transactionList[position]
        viewModel.delete(transaction)
        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
    }

    private fun editTransaction(position: Int) {
        val transaction = transactionList[position]
        val intent = Intent(this, EditTransactionActivity::class.java)
        intent.putExtra("transaction_id", transaction.id)
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, MonthlySummaryActivity::class.java))
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, NotificationsActivity::class.java))
                    true
                }
                R.id.nav_Settings -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
