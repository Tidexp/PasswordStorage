package com.example.passwordstorage.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.passwordstorage.data.repository.AdminRepository
import com.example.passwordstorage.data.repository.AuthRepository
import com.example.passwordstorage.data.repository.VaultRepository
import com.example.passwordstorage.ui.screens.admin.AdminDashboardScreen
import com.example.passwordstorage.ui.screens.auth.LoginScreen
import com.example.passwordstorage.ui.screens.auth.RegisterScreen
import com.example.passwordstorage.ui.screens.generator.PasswordGeneratorScreen
import com.example.passwordstorage.ui.screens.settings.SettingsScreen
import com.example.passwordstorage.ui.screens.vault.VaultDetailScreen
import com.example.passwordstorage.ui.screens.vault.VaultListScreen

object NavRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val VAULT_LIST = "vault_list"
    const val VAULT_DETAIL = "vault_detail/{itemId}"
    const val PASSWORD_GENERATOR = "password_generator"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val SETTINGS = "settings"

    fun vaultDetailRoute(itemId: Long = 0L) = "vault_detail/$itemId"
}

@Composable
fun PasswordVaultAppNav(
    activity: FragmentActivity,
    authRepository: AuthRepository,
    vaultRepository: VaultRepository,
    adminRepository: AdminRepository
) {
    val navController = rememberNavController()
    val currentUser by authRepository.currentUser.collectAsState()

    val startDestination = if (currentUser != null) NavRoutes.VAULT_LIST else NavRoutes.LOGIN

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoutes.LOGIN) {
                LoginScreen(
                    activity = activity,
                    authRepository = authRepository,
                    onLoginSuccess = {
                        navController.navigate(NavRoutes.VAULT_LIST) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(NavRoutes.REGISTER)
                    }
                )
            }

            composable(NavRoutes.REGISTER) {
                RegisterScreen(
                    authRepository = authRepository,
                    onRegisterSuccess = {
                        navController.navigate(NavRoutes.VAULT_LIST) {
                            popUpTo(NavRoutes.REGISTER) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable(NavRoutes.VAULT_LIST) {
                val user = currentUser
                if (user == null) {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0)
                    }
                } else {
                    VaultListScreen(
                        currentUser = user,
                        vaultRepository = vaultRepository,
                        onAddNewItem = {
                            navController.navigate(NavRoutes.vaultDetailRoute(0L))
                        },
                        onEditItem = { item ->
                            navController.navigate(NavRoutes.vaultDetailRoute(item.id))
                        },
                        onOpenGenerator = {
                            navController.navigate(NavRoutes.PASSWORD_GENERATOR)
                        },
                        onOpenAdminDashboard = {
                            navController.navigate(NavRoutes.ADMIN_DASHBOARD)
                        },
                        onOpenSettings = {
                            navController.navigate(NavRoutes.SETTINGS)
                        }
                    )
                }
            }

            composable(
                route = NavRoutes.VAULT_DETAIL,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
                val user = currentUser
                if (user != null) {
                    VaultDetailScreen(
                        itemId = itemId,
                        userEmail = user.email,
                        vaultRepository = vaultRepository,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(NavRoutes.PASSWORD_GENERATOR) {
                PasswordGeneratorScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.ADMIN_DASHBOARD) {
                val user = currentUser
                if (user != null && user.role.name == "ADMIN") {
                    AdminDashboardScreen(
                        currentAdminEmail = user.email,
                        adminRepository = adminRepository,
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    navController.popBackStack()
                }
            }

            composable(NavRoutes.SETTINGS) {
                val user = currentUser
                if (user != null) {
                    SettingsScreen(
                        user = user,
                        authRepository = authRepository,
                        onBack = { navController.popBackStack() },
                        onLogout = {
                            navController.navigate(NavRoutes.LOGIN) {
                                popUpTo(0)
                            }
                        }
                    )
                }
            }
        }
    }
}
