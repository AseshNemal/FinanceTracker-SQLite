package com.example.fitnancetracker.DAO

import com.example.fitnancetracker.model.Transaction

sealed interface TransactionEvent {
    object saveTrasction: TransactionEvent
    data class SetId(val id: String):TransactionEvent
    data class SetTitle(val title: String):TransactionEvent
    data class SetAmount(val amount: Float):TransactionEvent
    data class SetCategory(val category: String):TransactionEvent
    data class SetDate(val date: String):TransactionEvent
    data class SetType(val type: String):TransactionEvent

    object ShowDialog: TransactionEvent
    object HideDialog: TransactionEvent
    data class SortTransaction(val sortType: SortType): TransactionEvent
    data class DeleteTransaction(val transaction: Transaction): TransactionEvent


}
