package com.example.fitnancetracker


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnancetracker.adapter.TransactionAdapter
import com.example.fitnancetracker.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ViewTransactionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var transactionList: MutableList<Transaction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_transactions)

        setupBottomNavigation()

        sharedPrefs = getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)
        recyclerView = findViewById(R.id.transactionRecyclerView)

        transactionList = loadTransactions()

        adapter = TransactionAdapter(transactionList,
            onDelete = { position -> deleteTransaction(position) },
            onEdit = { position -> editTransaction(position) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadTransactions(): MutableList<Transaction> {
        val json = sharedPrefs.getString("transactions", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            Gson().fromJson(json, type)
        } else mutableListOf()
    }

    private fun deleteTransaction(position: Int) {
        transactionList.removeAt(position)
        saveTransactions()
        adapter.notifyItemRemoved(position)
        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
    }

    private fun editTransaction(position: Int) {
        val transaction = transactionList[position]
        val intent = Intent(this, EditTransactionActivity::class.java)
        intent.putExtra("transaction_index", position)
        intent.putExtra("transaction_data", Gson().toJson(transaction))
        startActivity(intent)
    }

    private fun saveTransactions() {
        val json = Gson().toJson(transactionList)
        sharedPrefs.edit().putString("transactions", json).apply()
    }

    override fun onResume() {
        super.onResume()
        transactionList.clear()
        transactionList.addAll(loadTransactions())
        adapter.notifyDataSetChanged()
    }

    private fun setupBottomNavigation() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home ->  {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_dashboard ->  {
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
