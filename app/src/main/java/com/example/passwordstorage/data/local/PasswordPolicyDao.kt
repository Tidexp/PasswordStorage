package com.example.passwordstorage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.passwordstorage.data.model.PasswordPolicyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordPolicyDao {
    @Query("SELECT * FROM password_policy WHERE id = 1 LIMIT 1")
    fun getPolicy(): Flow<PasswordPolicyEntity?>

    @Query("SELECT * FROM password_policy WHERE id = 1 LIMIT 1")
    suspend fun getPolicyDirect(): PasswordPolicyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePolicy(policy: PasswordPolicyEntity)
}
