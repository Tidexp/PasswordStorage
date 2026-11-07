package com.example.passwordstorage.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userEmail: String,
    val action: String,
    val status: String, // "SUCCESS", "FAILED", "WARNING"
    val details: String
)
