package com.example.passwordstorage

import android.app.Application
import com.example.passwordstorage.data.local.AppDatabase
import com.example.passwordstorage.data.repository.AdminRepository
import com.example.passwordstorage.data.repository.AuthRepository
import com.example.passwordstorage.data.repository.VaultRepository
import com.example.passwordstorage.data.security.CryptoManager

class PasswordStorageApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var cryptoManager: CryptoManager
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var vaultRepository: VaultRepository
        private set

    lateinit var adminRepository: AdminRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        cryptoManager = CryptoManager()

        authRepository = AuthRepository(
            userDao = database.userDao(),
            auditLogDao = database.auditLogDao(),
            passwordPolicyDao = database.passwordPolicyDao()
        )

        vaultRepository = VaultRepository(
            vaultDao = database.vaultDao(),
            auditLogDao = database.auditLogDao(),
            cryptoManager = cryptoManager
        )

        adminRepository = AdminRepository(
            userDao = database.userDao(),
            auditLogDao = database.auditLogDao(),
            passwordPolicyDao = database.passwordPolicyDao(),
            vaultDao = database.vaultDao()
        )
    }
}
