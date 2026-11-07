package com.example.passwordstorage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    USER,
    ADMIN
}

enum class UserStatus {
    ACTIVE,
    LOCKED
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val status: UserStatus = UserStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
