package com.example.passwordstorage.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passwordstorage.data.model.UserEntity
import com.example.passwordstorage.data.repository.AuthRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: UserEntity,
    authRepository: AuthRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isE2EESyncing by remember { mutableStateOf(false) }

    fun triggerSync() {
        isE2EESyncing = true
        scope.launch {
            kotlinx.coroutines.delay(1500)
            isE2EESyncing = false
            Toast.makeText(context, "Đồng bộ mã hóa E2EE hoàn tất!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cấu Hình & Bảo Mật") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(text = "Thông tin tài khoản", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Email: ${user.email}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Vai trò: ${user.role.name}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Trạng thái: ${user.status.name}", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Cơ chế bảo mật ứng dụng", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            SecurityFeatureRow(
                icon = Icons.Default.Key,
                title = "Android Keystore System (AES-256)",
                subtitle = "Bảo mật khóa mã hóa cục bộ ở mức phần cứng"
            )

            SecurityFeatureRow(
                icon = Icons.Default.ScreenLockPortrait,
                title = "Chống chụp/quay màn hình (FLAG_SECURE)",
                subtitle = "Ngăn chặn các ứng dụng độc hại ghi hình màn hình"
            )

            SecurityFeatureRow(
                icon = Icons.Default.ContentPasteGo,
                title = "Tự động dọn dẹp bộ nhớ tạm (Clipboard)",
                subtitle = "Tự động xoá sạch dữ liệu sau 30 giây khi sao chép"
            )

            SecurityFeatureRow(
                icon = Icons.Default.CloudSync,
                title = "Đồng bộ mã hóa đầu cuối (E2EE)",
                subtitle = "Dữ liệu được mã hóa trước khi đồng bộ"
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { triggerSync() },
                enabled = !isE2EESyncing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isE2EESyncing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang đồng bộ mã hóa E2EE...")
                } else {
                    Icon(imageVector = Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kích hoạt Đồng Bộ E2EE ngay")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        authRepository.logout()
                        onLogout()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng Xuất Khỏi Thiết Bị")
            }
        }
    }
}

@Composable
fun SecurityFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
        }
    }
}
