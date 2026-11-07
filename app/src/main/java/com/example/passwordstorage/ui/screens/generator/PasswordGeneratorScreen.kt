package com.example.passwordstorage.ui.screens.generator

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
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
import com.example.passwordstorage.data.security.ClipboardHelper
import com.example.passwordstorage.data.security.PasswordGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var length by remember { mutableFloatStateOf(16f) }
    var includeUppercase by remember { mutableStateOf(true) }
    var includeLowercase by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }

    var generatedPassword by remember { mutableStateOf("") }

    fun generateNewPassword() {
        generatedPassword = PasswordGenerator.generate(
            length = length.toInt(),
            includeUppercase = includeUppercase,
            includeLowercase = includeLowercase,
            includeNumbers = includeNumbers,
            includeSymbols = includeSymbols
        )
    }

    LaunchedEffect(length, includeUppercase, includeLowercase, includeNumbers, includeSymbols) {
        generateNewPassword()
    }

    val strength = remember(generatedPassword) {
        if (generatedPassword.isNotEmpty()) PasswordGenerator.calculateStrength(generatedPassword) else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo Mật Khẩu Mạnh Ngẫu Nhiên") },
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Password Display Box
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = generatedPassword,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )

                    strength?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(it.colorHex).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Đánh giá: ${it.label}",
                                color = Color(it.colorHex),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(onClick = { generateNewPassword() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate")
                        }

                        Button(
                            onClick = {
                                ClipboardHelper.copyToClipboard(context, "Mật khẩu ngẫu nhiên", generatedPassword, 30)
                                Toast.makeText(context, "Đã sao chép! Tự xóa sau 30s.", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sao chép Mật Khẩu")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Configuration Controls
            Text(
                text = "Độ dài mật khẩu: ${length.toInt()} ký tự",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            Slider(
                value = length,
                onValueChange = { length = it },
                valueRange = 8f..32f,
                steps = 24
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                GeneratorOptionRow(
                    label = "Chữ in hoa (A-Z)",
                    checked = includeUppercase,
                    onCheckedChange = { includeUppercase = it }
                )
                GeneratorOptionRow(
                    label = "Chữ in thường (a-z)",
                    checked = includeLowercase,
                    onCheckedChange = { includeLowercase = it }
                )
                GeneratorOptionRow(
                    label = "Chữ số (0-9)",
                    checked = includeNumbers,
                    onCheckedChange = { includeNumbers = it }
                )
                GeneratorOptionRow(
                    label = "Ký tự đặc biệt (!@#$%)",
                    checked = includeSymbols,
                    onCheckedChange = { includeSymbols = it }
                )
            }
        }
    }
}

@Composable
fun GeneratorOptionRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
