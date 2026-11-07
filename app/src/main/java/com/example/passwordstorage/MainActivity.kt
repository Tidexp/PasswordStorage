package com.example.passwordstorage

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.example.passwordstorage.ui.navigation.PasswordVaultAppNav
import com.example.passwordstorage.ui.theme.PasswordStorageTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Enforce FLAG_SECURE to prevent screenshot and screen recording across the entire app
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val app = application as PasswordStorageApp

        setContent {
            PasswordStorageTheme {
                PasswordVaultAppNav(
                    activity = this,
                    authRepository = app.authRepository,
                    vaultRepository = app.vaultRepository,
                    adminRepository = app.adminRepository
                )
            }
        }
    }
}
