package com.example.fitnancetracker.data

import com.example.fitnancetracker.model.Budget
import com.example.fitnancetracker.model.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun insert(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun update(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    fun getTransactionsByType(type: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByType(type)
    }

    fun getTransactionsForMonth(monthYear: String): Flow<List<Transaction>> {
        return transactionDao.getTransactionsForMonth(monthYear)
    }

    // Budget operations
    fun getBudget(): Flow<Budget?> {
        return transactionDao.getBudget()
    }

    suspend fun insertBudget(budget: Budget) {
        transactionDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        transactionDao.updateBudget(budget)
    }
} 