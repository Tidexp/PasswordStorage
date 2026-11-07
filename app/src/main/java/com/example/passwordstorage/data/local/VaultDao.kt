package com.example.passwordstorage.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.passwordstorage.data.model.VaultCategory
import com.example.passwordstorage.data.model.VaultItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_items WHERE userEmail = :userEmail ORDER BY isFavorite DESC, updatedAt DESC")
    fun getVaultItems(userEmail: String): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE userEmail = :userEmail AND category = :category ORDER BY isFavorite DESC, updatedAt DESC")
    fun getVaultItemsByCategory(userEmail: String, category: VaultCategory): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE id = :id LIMIT 1")
    suspend fun getVaultItemById(id: Long): VaultItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaultItem(item: VaultItemEntity): Long

    @Update
    suspend fun updateVaultItem(item: VaultItemEntity)

    @Delete
    suspend fun deleteVaultItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE userEmail = :userEmail")
    suspend fun deleteAllUserItems(userEmail: String)
}
