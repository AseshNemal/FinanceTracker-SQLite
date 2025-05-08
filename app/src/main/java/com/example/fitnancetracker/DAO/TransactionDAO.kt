package com.example.fitnancetracker.DAO

import android.content.Context
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query

import androidx.room.Upsert
import com.example.fitnancetracker.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDAO {
    @Upsert
    suspend fun upsertTransaction(context: Transaction)
    @Delete
    suspend fun deleteTransaction(context: Transaction)

    @Query("SELECT * FROM `transaction` ORDER BY DATE")
    fun getTransactionOrderedByDate(): Flow<List<Transaction>>



}