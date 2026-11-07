package com.example.passwordstorage.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.passwordstorage.data.model.AuditLogEntity
import com.example.passwordstorage.data.model.PasswordPolicyEntity
import com.example.passwordstorage.data.model.UserEntity
import com.example.passwordstorage.data.model.UserRole
import com.example.passwordstorage.data.model.UserStatus
import com.example.passwordstorage.data.model.VaultCategory
import com.example.passwordstorage.data.model.VaultItemEntity
import com.example.passwordstorage.data.security.CryptoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        VaultItemEntity::class,
        AuditLogEntity::class,
        PasswordPolicyEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun vaultDao(): VaultDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun passwordPolicyDao(): PasswordPolicyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "password_vault.db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial default data
                CoroutineScope(Dispatchers.IO).launch {
                    val appDb = getInstance(context)
                    val crypto = CryptoManager()

                    // Admin user
                    appDb.userDao().insertUser(
                        UserEntity(
                            email = "admin@vault.com",
                            passwordHash = "Admin@123456",
                            role = UserRole.ADMIN,
                            status = UserStatus.ACTIVE
                        )
                    )

                    // Regular user
                    appDb.userDao().insertUser(
                        UserEntity(
                            email = "user@vault.com",
                            passwordHash = "User@123456",
                            role = UserRole.USER,
                            status = UserStatus.ACTIVE
                        )
                    )

                    // Default Password Policy
                    appDb.passwordPolicyDao().savePolicy(
                        PasswordPolicyEntity(
                            id = 1,
                            minLength = 8,
                            requireUppercase = true,
                            requireLowercase = true,
                            requireNumbers = true,
                            requireSymbols = true
                        )
                    )

                    // Initial sample vault items for demo user
                    appDb.vaultDao().insertVaultItem(
                        VaultItemEntity(
                            userEmail = "user@vault.com",
                            category = VaultCategory.PASSWORD,
                            title = "Google Account",
                            websiteUrl = "https://accounts.google.com",
                            encryptedUsername = crypto.encrypt("myuser@gmail.com"),
                            encryptedPassword = crypto.encrypt("P@ssw0rd2024!Google"),
                            isFavorite = true
                        )
                    )

                    appDb.vaultDao().insertVaultItem(
                        VaultItemEntity(
                            userEmail = "user@vault.com",
                            category = VaultCategory.BANK_CARD,
                            title = "Vietcombank Visa",
                            encryptedCardNumber = crypto.encrypt("4123 4567 8901 2345"),
                            encryptedCardHolder = crypto.encrypt("NGUYEN VAN A"),
                            encryptedExpiryDate = crypto.encrypt("12/28"),
                            encryptedCvv = crypto.encrypt("888"),
                            isFavorite = true
                        )
                    )

                    appDb.vaultDao().insertVaultItem(
                        VaultItemEntity(
                            userEmail = "user@vault.com",
                            category = VaultCategory.SECURE_NOTE,
                            title = "Mã PIN Két Sắt Gia Đình",
                            encryptedNote = crypto.encrypt("Mã pin két sắt phòng ngủ: 998877#"),
                            isFavorite = false
                        )
                    )

                    // Initial Audit Logs
                    appDb.auditLogDao().insertLog(
                        AuditLogEntity(
                            userEmail = "system",
                            action = "SYSTEM_INITIALIZED",
                            status = "SUCCESS",
                            details = "Khởi tạo hệ thống Vault & Keystore thành công với AES-256."
                        )
                    )
                    appDb.auditLogDao().insertLog(
                        AuditLogEntity(
                            userEmail = "admin@vault.com",
                            action = "ADMIN_POLICY_SETUP",
                            status = "SUCCESS",
                            details = "Thiết lập chính sách mật khẩu tối thiểu 8 ký tự."
                        )
                    )
                }
            }
        }
    }
}
