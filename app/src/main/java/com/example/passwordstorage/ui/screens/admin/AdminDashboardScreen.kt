package com.example.passwordstorage.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passwordstorage.data.model.AuditLogEntity
import com.example.passwordstorage.data.model.PasswordPolicyEntity
import com.example.passwordstorage.data.model.UserEntity
import com.example.passwordstorage.data.model.UserRole
import com.example.passwordstorage.data.model.UserStatus
import com.example.passwordstorage.data.repository.AdminRepository
import com.example.passwordstorage.ui.screens.generator.GeneratorOptionRow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    currentAdminEmail: String,
    adminRepository: AdminRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Audit Logs, 1: Users, 2: Password Policy

    val auditLogs by adminRepository.getAllAuditLogs().collectAsState(initial = emptyList())
    val users by adminRepository.getAllUsers().collectAsState(initial = emptyList())
    val currentPolicy by adminRepository.getPasswordPolicy().collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bảng Quản Trị Bảo Mật Admin", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Admin: $currentAdminEmail", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
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
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Audit Logs (${auditLogs.size})", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Quản lý User", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Chính sách Mật khẩu", fontSize = 12.sp) }
                )
            }

            when (selectedTab) {
                0 -> AuditLogsTab(
                    auditLogs = auditLogs,
                    onClearLogs = {
                        scope.launch {
                            adminRepository.clearAuditLogs(currentAdminEmail)
                            Toast.makeText(context, "Đã dọn dẹp nhật ký", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                1 -> UserManagementTab(
                    currentAdminEmail = currentAdminEmail,
                    users = users,
                    onToggleLock = { targetUser ->
                        scope.launch {
                            adminRepository.toggleUserLock(currentAdminEmail, targetUser.email, targetUser.status)
                            Toast.makeText(context, "Đã cập nhật trạng thái khóa", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRevokeAccess = { targetUser ->
                        scope.launch {
                            adminRepository.revokeUserAccess(currentAdminEmail, targetUser.email)
                            Toast.makeText(context, "Đã thu hồi quyền & hủy dữ liệu của ${targetUser.email}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onToggleRole = { targetUser ->
                        scope.launch {
                            adminRepository.toggleUserRole(currentAdminEmail, targetUser.email, targetUser.role)
                            Toast.makeText(context, "Đã thay đổi vai trò", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                2 -> PasswordPolicyTab(
                    currentPolicy = currentPolicy ?: PasswordPolicyEntity(),
                    onSavePolicy = { newPolicy ->
                        scope.launch {
                            adminRepository.updatePasswordPolicy(currentAdminEmail, newPolicy)
                            Toast.makeText(context, "Đã áp dụng chính sách mới!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AuditLogsTab(
    auditLogs: List<AuditLogEntity>,
    onClearLogs: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Nhật ký kiểm tra an toàn (Audit Logs)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            IconButton(onClick = onClearLogs) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear logs", tint = Color.Gray)
            }
        }

        if (auditLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Chưa có nhật ký ghi nhận", color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(auditLogs, key = { it.id }) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.action,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = when (log.status) {
                                        "SUCCESS" -> MaterialTheme.colorScheme.tertiary
                                        "FAILED" -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "User: ${log.userEmail}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = log.details, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserManagementTab(
    currentAdminEmail: String,
    users: List<UserEntity>,
    onToggleLock: (UserEntity) -> Unit,
    onRevokeAccess: (UserEntity) -> Unit,
    onToggleRole: (UserEntity) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(users, key = { it.email }) { user ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = user.email, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "Vai trò: ${user.role} | Trạng thái: ${user.status}",
                                fontSize = 12.sp,
                                color = if (user.status == UserStatus.LOCKED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                            )
                        }

                        if (user.email != currentAdminEmail) {
                            Row {
                                IconButton(onClick = { onToggleLock(user) }) {
                                    Icon(
                                        imageVector = if (user.status == UserStatus.LOCKED) Icons.Default.LockOpen else Icons.Default.Lock,
                                        contentDescription = "Lock/Unlock",
                                        tint = if (user.status == UserStatus.LOCKED) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                    )
                                }
                                IconButton(onClick = { onRevokeAccess(user) }) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = "Remote Revoke Access",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    if (user.email != currentAdminEmail) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onToggleRole(user) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Chuyển thành ${if (user.role == UserRole.USER) "ADMIN" else "USER"}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordPolicyTab(
    currentPolicy: PasswordPolicyEntity,
    onSavePolicy: (PasswordPolicyEntity) -> Unit
) {
    var minLength by remember(currentPolicy) { mutableFloatStateOf(currentPolicy.minLength.toFloat()) }
    var requireUpper by remember(currentPolicy) { mutableStateOf(currentPolicy.requireUppercase) }
    var requireLower by remember(currentPolicy) { mutableStateOf(currentPolicy.requireLowercase) }
    var requireNumbers by remember(currentPolicy) { mutableStateOf(currentPolicy.requireNumbers) }
    var requireSymbols by remember(currentPolicy) { mutableStateOf(currentPolicy.requireSymbols) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Bắt buộc chính sách mật khẩu (Password Policy)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = "Áp dụng cho tất cả tài khoản khi đăng ký hoặc tạo mật khẩu mới", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Độ dài tối thiểu: ${minLength.toInt()} ký tự", fontWeight = FontWeight.SemiBold)
        Slider(
            value = minLength,
            onValueChange = { minLength = it },
            valueRange = 6f..20f,
            steps = 14
        )

        Spacer(modifier = Modifier.height(12.dp))

        GeneratorOptionRow(label = "Bắt buộc chữ in hoa (A-Z)", checked = requireUpper, onCheckedChange = { requireUpper = it })
        GeneratorOptionRow(label = "Bắt buộc chữ in thường (a-z)", checked = requireLower, onCheckedChange = { requireLower = it })
        GeneratorOptionRow(label = "Bắt buộc chữ số (0-9)", checked = requireNumbers, onCheckedChange = { requireNumbers = it })
        GeneratorOptionRow(label = "Bắt buộc ký tự đặc biệt (!@#$)", checked = requireSymbols, onCheckedChange = { requireSymbols = it })

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onSavePolicy(
                    PasswordPolicyEntity(
                        id = 1,
                        minLength = minLength.toInt(),
                        requireUppercase = requireUpper,
                        requireLowercase = requireLower,
                        requireNumbers = requireNumbers,
                        requireSymbols = requireSymbols
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lưu & Thực Thể Chính Sách")
        }
    }
}
