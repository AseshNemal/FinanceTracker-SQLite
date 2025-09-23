package com.aseshnemal.financetracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aseshnemal.financetracker.adapter.NotificationAdapter
import com.aseshnemal.financetracker.model.Notification
import com.aseshnemal.financetracker.viewmodel.TransactionViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var cardNoNotifications: CardView
    private lateinit var toolbar: Toolbar
    private val viewModel: TransactionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        setupToolbar()
        setupBottomNavigation()
        initializeViews()
        loadNotifications()
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.rvNotifications)
        cardNoNotifications = findViewById(R.id.cardNoNotifications)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            try {
                val transactions = viewModel.allTransactions.first()
                val budget = viewModel.getMonthlyBudget().first() ?: 0f
                
                val notifications = mutableListOf<Notification>()
                val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)

                // Calculate monthly expenses
                val monthlyExpenses = transactions
                    .filter { transaction ->
                        try {
                            val parts = transaction.date.split("/")
                            if (parts.size == 3) {
                                val transactionMonth = parts[1].toInt()
                                val transactionYear = parts[2].toInt()
                                transactionMonth == currentMonth && transactionYear == currentYear
                            } else false
                        } catch (e: Exception) {
                            Log.e("Notifications", "Error parsing date: ${transaction.date}", e)
                            false
                        }
                    }
                    .filter { it.type == "Expense" }
                    .sumOf { it.amount.toDouble() }

                // Add budget notification if expenses exceed budget
                if (budget > 0 && monthlyExpenses > budget) {
                    val overspent = monthlyExpenses - budget
                    notifications.add(
                        Notification(
                            "Budget Exceeded",
                            "You have exceeded your monthly budget by Rs. %.2f".format(overspent),
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        )
                    )
                }

                // Add category-wise notifications
                val categoryExpenses = transactions
                    .filter { it.type == "Expense" }
                    .groupBy { it.category }
                    .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() } }

                categoryExpenses.forEach { (category, amount) ->
                    if (amount > 10000) { // Threshold for category spending
                        notifications.add(
                            Notification(
                                "High Spending Alert",
                                "You have spent Rs. %.2f on %s".format(amount, category),
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                            )
                        )
                    }
                }

                // Update UI
                if (notifications.isEmpty()) {
                    cardNoNotifications.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    cardNoNotifications.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.adapter = NotificationAdapter(notifications)
                }

            } catch (e: Exception) {
                Log.e("Notifications", "Error loading notifications: ${e.message}", e)
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_notifications

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home ->  {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_dashboard ->  {
                    startActivity(Intent(this, MonthlySummaryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_notifications -> {
                    true
                }
                R.id.nav_Settings -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }
}
