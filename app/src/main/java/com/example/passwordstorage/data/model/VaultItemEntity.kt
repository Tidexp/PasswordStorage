package com.example.passwordstorage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VaultCategory {
    PASSWORD,
    BANK_CARD,
    SECURE_NOTE
}

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String,
    val category: VaultCategory,
    val title: String,
    val websiteUrl: String = "",
    val encryptedUsername: String = "",
    val encryptedPassword: String = "",
    val encryptedCardNumber: String = "",
    val encryptedCardHolder: String = "",
    val encryptedExpiryDate: String = "",
    val encryptedCvv: String = "",
    val encryptedNote: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
