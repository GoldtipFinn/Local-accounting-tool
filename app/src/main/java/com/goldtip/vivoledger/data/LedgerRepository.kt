package com.goldtip.vivoledger.data

import kotlinx.coroutines.flow.Flow

class LedgerRepository(
    private val dao: LedgerDao
) {
    fun observeTransactions(): Flow<List<TransactionEntity>> = dao.observeTransactions()

    suspend fun transactionCount(): Int = dao.countTransactions()

    suspend fun addTransaction(transaction: TransactionEntity) {
        dao.insert(transaction)
    }

    suspend fun addTransactions(transactions: List<TransactionEntity>) {
        dao.insertAll(transactions)
    }

    suspend fun clearTransactions() {
        dao.clearTransactions()
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        dao.delete(transaction)
    }
}
