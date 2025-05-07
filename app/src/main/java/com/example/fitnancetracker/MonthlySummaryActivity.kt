package com.example.fitnancetracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnancetracker.adapter.CategorySummaryAdapter
import com.example.fitnancetracker.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class MonthlySummaryActivity : AppCompatActivity() {

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrevMonth: Button
    private lateinit var btnNextMonth: Button
    private lateinit var rvCategorySummary: RecyclerView

    private var currentMonth: Int = 0
    private var currentYear: Int = 0

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_summary)

        setupBottomNavigation()

        sharedPrefs = getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)

        tvMonthYear = findViewById(R.id.tvMonthYear)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        rvCategorySummary = findViewById(R.id.rvCategorySummary)

        val calendar = Calendar.getInstance()
        currentMonth = calendar.get(Calendar.MONTH)
        currentYear = calendar.get(Calendar.YEAR)

        rvCategorySummary.layoutManager = LinearLayoutManager(this)

        btnPrevMonth.setOnClickListener {
            changeMonth(-1)
        }

        btnNextMonth.setOnClickListener {
            changeMonth(1)
        }

        updateSummary()
    }

    private fun changeMonth(offset: Int) {
        currentMonth += offset
        if (currentMonth < 0) {
            currentMonth = 11
            currentYear -= 1
        } else if (currentMonth > 11) {
            currentMonth = 0
            currentYear += 1
        }
        updateSummary()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_dashboard

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

    private fun updateSummary() {
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.YEAR, currentYear)
        tvMonthYear.text = monthYearFormat.format(calendar.time)

        val transactions = getTransactionsForMonth(currentMonth, currentYear)
        val categoryMap = transactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount.toDouble() } }

        val summaryList = categoryMap.map { (category, total) ->
            CategorySummary(category, total.toFloat())
        }

        rvCategorySummary.adapter = CategorySummaryAdapter(summaryList)
    }

    private fun getTransactionsForMonth(month: Int, year: Int): List<Transaction> {
        val transactionsJson = sharedPrefs.getString("transactions", null) ?: return emptyList()
        val type = object : TypeToken<List<Transaction>>() {}.type
        val allTransactions: List<Transaction> = Gson().fromJson(transactionsJson, type)

        return allTransactions.filter { transaction ->
            val date = dateFormat.parse(transaction.date)
            val cal = Calendar.getInstance()
            cal.time = date
            cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
        }
    }
}

data class CategorySummary(val category: String, val totalAmount: Float)
