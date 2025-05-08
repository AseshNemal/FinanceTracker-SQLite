package com.example.fitnancetracker.DAO

import com.example.fitnancetracker.model.Transaction

data class TransactionState(
    val transaction: List<Transaction> = emptyList(),
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.00,
    val category: String = "",
    val date: String ="",
    val type: String = "General",
    val sortType: SortType = SortType.ID

    )
