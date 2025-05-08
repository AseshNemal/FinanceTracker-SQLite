package com.example.fitnancetracker.DAO

import androidx.lifecycle.ViewModel
import com.example.fitnancetracker.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class TransactionViewModel(
    private val dao: TransactionDAO
): ViewModel() {
    private val _sortType =  MutableStateFlow(SortType.ID)
    private val _state = MutableStateFlow(TransactionState())

    fun onEvent(event: TransactionEvent){
        when(event){
            is TransactionEvent.DeleteTransaction -> {
                viewModelScope.launch {
                    dao.deleteTransaction(event.transaction)
                }

            }
            TransactionEvent.HideDialog -> {
                _state.update { it.copy(
                    isAdding
                ) }
            }
            is TransactionEvent.SetAmount -> TODO()
            is TransactionEvent.SetCategory -> TODO()
            is TransactionEvent.SetDate -> TODO()
            is TransactionEvent.SetId -> TODO()
            is TransactionEvent.SetTitle -> TODO()
            is TransactionEvent.SetType -> TODO()
            TransactionEvent.ShowDialog -> TODO()
            is TransactionEvent.SortTransaction -> TODO()
            TransactionEvent.saveTrasction -> TODO()
        }
    }
}