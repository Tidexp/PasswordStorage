package com.example.passwordstorage.ui.screens.vault

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.passwordstorage.data.model.UserRole
import com.example.passwordstorage.data.model.VaultCategory
import com.example.passwordstorage.data.repository.VaultItemDecrypted
import com.example.passwordstorage.data.repository.VaultRepository
import com.example.passwordstorage.data.security.ClipboardHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultListScreen(
    currentUser: UserEntity,
    vaultRepository: VaultRepository,
    onAddNewItem: () -> Unit,
    onEditItem: (VaultItemDecrypted) -> Unit,
    onOpenGenerator: () -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Password, 2: Card, 3: Note
    var searchQuery by remember { mutableStateOf("") }

    val vaultItems by remember(currentUser.email, selectedTab) {
        when (selectedTab) {
            1 -> vaultRepository.getDecryptedItemsByCategory(currentUser.email, VaultCategory.PASSWORD)
            2 -> vaultRepository.getDecryptedItemsByCategory(currentUser.email, VaultCategory.BANK_CARD)
            3 -> vaultRepository.getDecryptedItemsByCategory(currentUser.email, VaultCategory.SECURE_NOTE)
            else -> vaultRepository.getDecryptedItems(currentUser.email)
        }
    }.collectAsState(initial = emptyList())

    val filteredItems = remember(vaultItems, searchQuery) {
        if (searchQuery.isBlank()) vaultItems
        else vaultItems.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.websiteUrl.contains(searchQuery, ignoreCase = true) ||
                    it.username.contains(searchQuery, ignoreCase = true)
        }
    }

    fun copySensitiveText(label: String, text: String, itemTitle: String) {
        ClipboardHelper.copyToClipboard(context, label, text, autoClearSeconds = 30) {
            Toast.makeText(context, "🧹 Bộ nhớ tạm đã được tự động dọn dẹp (30s)", Toast.LENGTH_SHORT).show()
        }
        scope.launch {
            vaultRepository.logCopyAction(currentUser.email, itemTitle, label)
            snackbarHostState.showSnackbar(
                message = "Đã sao chép $label! Bộ nhớ tạm sẽ tự xóa sau 30s.",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Ví Bảo Mật Vault", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = currentUser.email + if (currentUser.role == UserRole.ADMIN) " (ADMIN)" else "",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenGenerator) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = "Password Generator")
                    }
                    if (currentUser.role == UserRole.ADMIN) {
                        IconButton(onClick = onOpenAdminDashboard) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewItem,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm mới")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm kiếm mật khẩu, thẻ, ghi chú...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Filter Tabs
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Tất cả (${vaultItems.size})") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Mật khẩu") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Thẻ ngân hàng") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Ghi chú") })
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Chưa có dữ liệu bảo mật nào", color = Color.Gray, fontSize = 14.sp)
                        Text(text = "Nhấn nút + bên dưới để thêm mới", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        VaultItemCard(
                            item = item,
                            onClick = { onEditItem(item) },
                            onCopyPassword = {
                                val textToCopy = when (item.category) {
                                    VaultCategory.PASSWORD -> item.password
                                    VaultCategory.BANK_CARD -> item.cardNumber
                                    VaultCategory.SECURE_NOTE -> item.note
                                }
                                copySensitiveText(
                                    label = when (item.category) {
                                        VaultCategory.PASSWORD -> "Mật khẩu"
                                        VaultCategory.BANK_CARD -> "Số thẻ"
                                        VaultCategory.SECURE_NOTE -> "Ghi chú"
                                    },
                                    text = textToCopy,
                                    itemTitle = item.title
                                )
                            },
                            onDelete = {
                                scope.launch {
                                    vaultRepository.deleteVaultItem(item)
                                    snackbarHostState.showSnackbar("Đã xóa ${item.title}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VaultItemCard(
    item: VaultItemDecrypted,
    onClick: () -> Unit,
    onCopyPassword: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (icon, tintColor) = when (item.category) {
                VaultCategory.PASSWORD -> Icons.Default.VpnKey to MaterialTheme.colorScheme.primary
                VaultCategory.BANK_CARD -> Icons.Default.CreditCard to MaterialTheme.colorScheme.tertiary
                VaultCategory.SECURE_NOTE -> Icons.Default.Description to MaterialTheme.colorScheme.secondary
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                when (item.category) {
                    VaultCategory.PASSWORD -> {
                        Text(
                            text = if (item.username.isNotEmpty()) item.username else item.websiteUrl,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "••••••••••••",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    VaultCategory.BANK_CARD -> {
                        Text(
                            text = if (item.cardNumber.length >= 4) "•••• " + item.cardNumber.takeLast(4) else "•••• ••••",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    VaultCategory.SECURE_NOTE -> {
                        Text(
                            text = "Ghi chú mã hóa AES-256",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            IconButton(onClick = onCopyPassword) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Password",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}
