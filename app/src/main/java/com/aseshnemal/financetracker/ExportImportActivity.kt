package com.aseshnemal.financetracker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aseshnemal.financetracker.model.Transaction
import com.aseshnemal.financetracker.viewmodel.TransactionViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.*
import java.nio.charset.StandardCharsets

class ExportImportActivity : AppCompatActivity() {

    private lateinit var btnExport: Button
    private lateinit var btnImport: Button
    private val viewModel: TransactionViewModel by viewModels()
    private val gson = Gson()

    private val EXPORT_REQUEST_CODE = 1001
    private val IMPORT_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_import)

        btnExport = findViewById(R.id.btnExport)
        btnImport = findViewById(R.id.btnImport)

        btnExport.setOnClickListener { exportData() }
        btnImport.setOnClickListener { importData() }

        setupBottomNavigation()
    }

    private fun exportData() {
        lifecycleScope.launch {
            val transactions = viewModel.allTransactions.first()
            if (transactions.isEmpty()) {
                Toast.makeText(this@ExportImportActivity, "No data to export", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val transactionsJson = gson.toJson(transactions)
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/txt"
                putExtra(Intent.EXTRA_TITLE, "transactions_backup.txt")
            }
            startActivityForResult(intent, EXPORT_REQUEST_CODE)
        }
    }

    private fun importData() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/txt"
        }
        startActivityForResult(intent, IMPORT_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) return

        val uri = data.data ?: return

        when (requestCode) {
            EXPORT_REQUEST_CODE -> {
                lifecycleScope.launch {
                    val transactions = viewModel.allTransactions.first()
                    val transactionsJson = gson.toJson(transactions)
                    try {
                        contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(transactionsJson.toByteArray(StandardCharsets.UTF_8))
                            Toast.makeText(this@ExportImportActivity, "Data exported successfully", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@ExportImportActivity, "Failed to export data", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            IMPORT_REQUEST_CODE -> {
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        val importedJson = reader.readText()

                        val type = object : TypeToken<List<Transaction>>() {}.type
                        val importedList: List<Transaction> = gson.fromJson(importedJson, type)

                        lifecycleScope.launch {
                            importedList.forEach { transaction ->
                                viewModel.insert(transaction)
                            }
                            Toast.makeText(this@ExportImportActivity, "Data imported successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@ExportImportActivity, "Failed to import data", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
