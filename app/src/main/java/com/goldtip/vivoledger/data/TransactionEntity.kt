package com.goldtip.vivoledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val category: String,
    val amount: Double,
    val note: String,
    val date: LocalDate
)
