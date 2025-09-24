package com.aseshnemal.financetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aseshnemal.financetracker.model.Budget
import com.aseshnemal.financetracker.model.Transaction

@Database(entities = [Transaction::class, Budget::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_database"
                )
                    .fallbackToDestructiveMigration(false) // This will recreate tables if schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 