package com.example.fitnancetracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnancetracker.data.AppDatabase
import com.example.fitnancetracker.data.TransactionRepository
import com.example.fitnancetracker.model.Budget
import com.example.fitnancetracker.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository
    val allTransactions: Flow<List<Transaction>>

    init {
        val transactionDao = AppDatabase.getDatabase(application).transactionDao()
        repository = TransactionRepository(transactionDao)
        allTransactions = repository.allTransactions
    }

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    fun getTransactionsByType(type: String): Flow<List<Transaction>> {
        return repository.getTransactionsByType(type)
    }

    fun getTransactionsForMonth(monthYear: String): Flow<List<Transaction>> {
        return repository.getTransactionsForMonth(monthYear)
    }

    // Budget operations
    fun getMonthlyBudget(): Flow<Float> {
        return repository.getBudget().map { it?.amount ?: 0f }
    }

    fun setMonthlyBudget(amount: Float) = viewModelScope.launch {
        repository.insertBudget(Budget(amount = amount))
    }
} 