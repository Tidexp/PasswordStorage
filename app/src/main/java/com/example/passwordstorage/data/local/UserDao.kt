package com.example.passwordstorage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.passwordstorage.data.model.UserEntity
import com.example.passwordstorage.data.model.UserRole
import com.example.passwordstorage.data.model.UserStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET status = :status WHERE email = :email")
    suspend fun updateUserStatus(email: String, status: UserStatus)

    @Query("UPDATE users SET role = :role WHERE email = :email")
    suspend fun updateUserRole(email: String, role: UserRole)

    @Query("UPDATE users SET lastLoginAt = :timestamp WHERE email = :email")
    suspend fun updateLastLogin(email: String, timestamp: Long)
}
