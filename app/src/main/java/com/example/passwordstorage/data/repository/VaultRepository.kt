package com.example.passwordstorage.data.repository

import com.example.passwordstorage.data.local.AuditLogDao
import com.example.passwordstorage.data.local.VaultDao
import com.example.passwordstorage.data.model.AuditLogEntity
import com.example.passwordstorage.data.model.VaultCategory
import com.example.passwordstorage.data.model.VaultItemEntity
import com.example.passwordstorage.data.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class VaultItemDecrypted(
    val id: Long,
    val userEmail: String,
    val category: VaultCategory,
    val title: String,
    val websiteUrl: String,
    val username: String,
    val password: String,
    val cardNumber: String,
    val cardHolder: String,
    val expiryDate: String,
    val cvv: String,
    val note: String,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

class VaultRepository(
    private val vaultDao: VaultDao,
    private val auditLogDao: AuditLogDao,
    private val cryptoManager: CryptoManager
) {

    fun getDecryptedItems(userEmail: String): Flow<List<VaultItemDecrypted>> {
        return vaultDao.getVaultItems(userEmail).map { list ->
            list.map { decryptItem(it) }
        }
    }

    fun getDecryptedItemsByCategory(userEmail: String, category: VaultCategory): Flow<List<VaultItemDecrypted>> {
        return vaultDao.getVaultItemsByCategory(userEmail, category).map { list ->
            list.map { decryptItem(it) }
        }
    }

    suspend fun getDecryptedItemById(id: Long, userEmail: String): VaultItemDecrypted? {
        val entity = vaultDao.getVaultItemById(id) ?: return null
        auditLogDao.insertLog(
            AuditLogEntity(
                userEmail = userEmail,
                action = "VAULT_READ_ITEM",
                status = "SUCCESS",
                details = "Giải mã & đọc dữ liệu nhạy cảm: '${entity.title}' (Category: ${entity.category})."
            )
        )
        return decryptItem(entity)
    }

    suspend fun saveVaultItem(
        userEmail: String,
        id: Long = 0,
        category: VaultCategory,
        title: String,
        websiteUrl: String = "",
        username: String = "",
        password: String = "",
        cardNumber: String = "",
        cardHolder: String = "",
        expiryDate: String = "",
        cvv: String = "",
        note: String = "",
        isFavorite: Boolean = false
    ): Long {
        val entity = VaultItemEntity(
            id = id,
            userEmail = userEmail,
            category = category,
            title = title,
            websiteUrl = websiteUrl,
            encryptedUsername = cryptoManager.encrypt(username),
            encryptedPassword = cryptoManager.encrypt(password),
            encryptedCardNumber = cryptoManager.encrypt(cardNumber),
            encryptedCardHolder = cryptoManager.encrypt(cardHolder),
            encryptedExpiryDate = cryptoManager.encrypt(expiryDate),
            encryptedCvv = cryptoManager.encrypt(cvv),
            encryptedNote = cryptoManager.encrypt(note),
            isFavorite = isFavorite,
            updatedAt = System.currentTimeMillis()
        )

        val insertedId = if (id == 0L) {
            val newId = vaultDao.insertVaultItem(entity)
            auditLogDao.insertLog(
                AuditLogEntity(
                    userEmail = userEmail,
                    action = "VAULT_CREATE_ITEM",
                    status = "SUCCESS",
                    details = "Mã hóa AES-256 & lưu mục mới: '$title' (${category.name})."
                )
            )
            newId
        } else {
            vaultDao.updateVaultItem(entity)
            auditLogDao.insertLog(
                AuditLogEntity(
                    userEmail = userEmail,
                    action = "VAULT_UPDATE_ITEM",
                    status = "SUCCESS",
                    details = "Mã hóa AES-256 & cập nhật mục: '$title' (${category.name})."
                )
            )
            id
        }
        return insertedId
    }

    suspend fun deleteVaultItem(item: VaultItemDecrypted) {
        val entity = vaultDao.getVaultItemById(item.id)
        if (entity != null) {
            vaultDao.deleteVaultItem(entity)
            auditLogDao.insertLog(
                AuditLogEntity(
                    userEmail = item.userEmail,
                    action = "VAULT_DELETE_ITEM",
                    status = "SUCCESS",
                    details = "Xóa mục vault: '${item.title}'."
                )
            )
        }
    }

    suspend fun logCopyAction(userEmail: String, itemTitle: String, fieldName: String) {
        auditLogDao.insertLog(
            AuditLogEntity(
                userEmail = userEmail,
                action = "VAULT_COPY_CLIPBOARD",
                status = "SUCCESS",
                details = "Sao chép $fieldName của '$itemTitle' vào bộ nhớ tạm. Hẹn giờ tự xóa sau 30 giây."
            )
        )
    }

    private fun decryptItem(entity: VaultItemEntity): VaultItemDecrypted {
        return VaultItemDecrypted(
            id = entity.id,
            userEmail = entity.userEmail,
            category = entity.category,
            title = entity.title,
            websiteUrl = entity.websiteUrl,
            username = cryptoManager.decrypt(entity.encryptedUsername),
            password = cryptoManager.decrypt(entity.encryptedPassword),
            cardNumber = cryptoManager.decrypt(entity.encryptedCardNumber),
            cardHolder = cryptoManager.decrypt(entity.encryptedCardHolder),
            expiryDate = cryptoManager.decrypt(entity.encryptedExpiryDate),
            cvv = cryptoManager.decrypt(entity.encryptedCvv),
            note = cryptoManager.decrypt(entity.encryptedNote),
            isFavorite = entity.isFavorite,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
