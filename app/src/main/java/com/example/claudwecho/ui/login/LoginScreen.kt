package com.example.claudwecho.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.*
import coil.compose.rememberAsyncImagePainter
import org.koin.androidx.compose.koinViewModel

import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import android.util.Base64

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val qrCode by viewModel.qrCodeBase64.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState == LoginState.LOGGED_IN) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            LoginState.IDLE -> {
                ScalingLazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text(
                            text = "ClaudWecho",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    item {
                        Button(
                            onClick = { viewModel.loadQrCode() },
                            modifier = Modifier.fillMaxWidth(0.8f).padding(bottom = 8.dp)
                        ) {
                            Text("QR Code Login")
                        }
                    }
                    item {
                        Button(
                            onClick = { viewModel.setPhoneInputState() },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text("Phone Login")
                        }
                    }
                }
            }
            LoginState.LOADING_QR, LoginState.LOGGING_IN -> {
                Text("Loading...", color = MaterialTheme.colorScheme.primary)
            }
            LoginState.QR_READY -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Scan with Netease App", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                    qrCode?.let { base64Url ->
                        val bitmap = remember(base64Url) {
                            try {
                                val base64Str = if (base64Url.contains(",")) base64Url.split(",")[1] else base64Url
                                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }
                        
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )
                        } else {
                            Text("Failed to decode QR", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            LoginState.PHONE_INPUT -> {
                var phone by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

                ScalingLazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text("Phone Login", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    item {
                        androidx.compose.foundation.text.BasicTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(Color.DarkGray, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            decorationBox = { innerTextField ->
                                if (phone.isEmpty()) Text("Phone Number", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                innerTextField()
                            }
                        )
                    }
                    item {
                        androidx.compose.foundation.text.BasicTextField(
                            value = password,
                            onValueChange = { password = it },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .padding(top = 8.dp)
                                .background(Color.DarkGray, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            decorationBox = { innerTextField ->
                                if (password.isEmpty()) Text("Password", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                innerTextField()
                            }
                        )
                    }
                    item {
                        Button(
                            onClick = { viewModel.loginWithPhone(phone, password) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Login")
                        }
                    }
                    item {
                        Button(
                            onClick = { viewModel.resetState() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Back")
                        }
                    }
                }
            }
            LoginState.ERROR -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = error ?: "Unknown error", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.resetState() }) {
                        Text("Retry")
                    }
                }
            }
            LoginState.LOGGED_IN -> {
                Text("Success!")
            }
        }
    }
}
