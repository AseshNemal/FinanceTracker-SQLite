package com.example.fitnancetracker.DAO

import androidx.room.Database
import com.example.fitnancetracker.model.Transaction

@Database(
    entities = [Transaction::class],
    version = 1
)
abstract class TransactionDatabase {
    abstract val dao: TransactionDAO
}