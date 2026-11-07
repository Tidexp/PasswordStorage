package com.example.passwordstorage.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.passwordstorage.data.repository.AuthRepository
import com.example.passwordstorage.data.repository.AuthResult
import com.example.passwordstorage.data.security.BiometricPromptManager
import com.example.passwordstorage.data.security.BiometricResult
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    activity: FragmentActivity,
    authRepository: AuthRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("user@vault.com") }
    var password by remember { mutableStateOf("User@123456") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val biometricManager = remember { BiometricPromptManager(activity) }
    val isBiometricAvailable = remember { biometricManager.checkBiometricAvailability() }

    fun performLogin(targetEmail: String, targetPass: String) {
        isLoading = true
        errorMessage = null
        scope.launch {
            when (val result = authRepository.login(targetEmail, targetPass)) {
                is AuthResult.Success -> {
                    isLoading = false
                    onLoginSuccess()
                }
                is AuthResult.Error -> {
                    isLoading = false
                    errorMessage = result.message
                }
            }
        }
    }

    fun triggerBiometricAuth() {
        biometricManager.showBiometricPrompt(
            title = "Mở khóa Password Vault",
            subtitle = "Sử dụng Vân tay hoặc Khuôn mặt để xác thực"
        ) { result ->
            when (result) {
                is BiometricResult.AuthenticationSuccess -> {
                    scope.launch {
                        when (val bioRes = authRepository.loginWithBiometric(email)) {
                            is AuthResult.Success -> onLoginSuccess()
                            is AuthResult.Error -> errorMessage = bioRes.message
                        }
                    }
                }
                is BiometricResult.AuthenticationFailed -> {
                    Toast.makeText(context, "Xác thực sinh trắc học thất bại", Toast.LENGTH_SHORT).show()
                }
                is BiometricResult.AuthenticationError -> {
                    Toast.makeText(context, result.error, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(context, "Sinh trắc học không khả dụng", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PASSWORD VAULT",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Lưu trữ mã hóa AES-256 & Sinh trắc học",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu / PIN Master") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { performLogin(email, password) },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đăng nhập An Toàn")
                        }
                    }

                    if (isBiometricAvailable) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { triggerBiometricAuth() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mở khóa bằng Biometric")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Demo Quick Logins
            Text(text = "Đăng nhập nhanh Demo:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        email = "user@vault.com"
                        password = "User@123456"
                        performLogin("user@vault.com", "User@123456")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("User Demo", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        email = "admin@vault.com"
                        password = "Admin@123456"
                        performLogin("admin@vault.com", "Admin@123456")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Admin Demo", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text("Chưa có tài khoản? Đăng ký ngay")
            }
        }
    }
}
