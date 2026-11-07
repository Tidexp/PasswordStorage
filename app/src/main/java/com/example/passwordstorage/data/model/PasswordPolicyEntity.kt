package com.example.passwordstorage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_policy")
data class PasswordPolicyEntity(
    @PrimaryKey
    val id: Int = 1,
    val minLength: Int = 8,
    val requireUppercase: Boolean = true,
    val requireLowercase: Boolean = true,
    val requireNumbers: Boolean = true,
    val requireSymbols: Boolean = true
)
