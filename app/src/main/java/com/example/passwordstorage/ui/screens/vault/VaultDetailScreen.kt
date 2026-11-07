package com.example.passwordstorage.ui.screens.vault

import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passwordstorage.data.model.VaultCategory
import com.example.passwordstorage.data.repository.VaultRepository
import com.example.passwordstorage.data.security.ClipboardHelper
import com.example.passwordstorage.data.security.PasswordGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultDetailScreen(
    itemId: Long,
    userEmail: String,
    vaultRepository: VaultRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var category by remember { mutableStateOf(VaultCategory.PASSWORD) }
    var title by remember { mutableStateOf("") }
    var websiteUrl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(itemId != 0L) }

    LaunchedEffect(itemId) {
        if (itemId != 0L) {
            val item = vaultRepository.getDecryptedItemById(itemId, userEmail)
            if (item != null) {
                category = item.category
                title = item.title
                websiteUrl = item.websiteUrl
                username = item.username
                password = item.password
                cardNumber = item.cardNumber
                cardHolder = item.cardHolder
                expiryDate = item.expiryDate
                cvv = item.cvv
                note = item.note
                isFavorite = item.isFavorite
            }
            isLoading = false
        }
    }

    val passwordStrength = remember(password) {
        if (password.isNotEmpty()) PasswordGenerator.calculateStrength(password) else null
    }

    fun handleSave() {
        if (title.isBlank()) {
            Toast.makeText(context, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            vaultRepository.saveVaultItem(
                userEmail = userEmail,
                id = itemId,
                category = category,
                title = title,
                websiteUrl = websiteUrl,
                username = username,
                password = password,
                cardNumber = cardNumber,
                cardHolder = cardHolder,
                expiryDate = expiryDate,
                cvv = cvv,
                note = note,
                isFavorite = isFavorite
            )
            Toast.makeText(context, "Đã mã hóa AES-256 & lưu thành công", Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == 0L) "Thêm Mục Bảo Mật Mới" else "Chi Tiết & Chỉnh Sửa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.Yellow else Color.Gray
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Category Selector
                Text(text = "Loại dữ liệu bảo mật", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = category == VaultCategory.PASSWORD,
                        onClick = { category = VaultCategory.PASSWORD },
                        label = { Text("Mật khẩu") }
                    )
                    FilterChip(
                        selected = category == VaultCategory.BANK_CARD,
                        onClick = { category = VaultCategory.BANK_CARD },
                        label = { Text("Thẻ ngân hàng") }
                    )
                    FilterChip(
                        selected = category == VaultCategory.SECURE_NOTE,
                        onClick = { category = VaultCategory.SECURE_NOTE },
                        label = { Text("Ghi chú") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề (VD: Facebook, Momo, Mã PIN)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (category) {
                    VaultCategory.PASSWORD -> {
                        OutlinedTextField(
                            value = websiteUrl,
                            onValueChange = { websiteUrl = it },
                            label = { Text("Địa chỉ Website / App") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Tài khoản / Email đăng nhập") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mật khẩu") },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle Visibility"
                                        )
                                    }
                                    IconButton(onClick = {
                                        password = PasswordGenerator.generate(length = 16)
                                        isPasswordVisible = true
                                    }) {
                                        Icon(imageVector = Icons.Default.AutoMode, contentDescription = "Generate")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        passwordStrength?.let { strength ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Độ mạnh mật khẩu: ", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    text = strength.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(strength.colorHex)
                                )
                            }
                        }
                    }

                    VaultCategory.BANK_CARD -> {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { cardNumber = it },
                            label = { Text("Số thẻ ngân hàng") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = cardHolder,
                            onValueChange = { cardHolder = it },
                            label = { Text("Tên chủ thẻ") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { expiryDate = it },
                                label = { Text("Hạn dùng (MM/YY)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { cvv = it },
                                label = { Text("Mã CVV") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    VaultCategory.SECURE_NOTE -> {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Nội dung ghi chú bảo mật") },
                            minLines = 5,
                            maxLines = 10,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Dữ liệu được mã hóa AES-256 qua Android Keystore System trên bộ nhớ thiết bị.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { handleSave() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Lưu Mã Hóa Dữ Liệu")
                }
            }
        }
    }
}
