package com.yorkyang2333.claudwecho.ui.login

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.QrCodeScanner

@Composable
fun LoginOptionsScreen(
    onNavigateToQr: () -> Unit,
    onNavigateToPhone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLazyColumn(
                scalingParams = androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults.scalingParams(
                    edgeScale = 0.3f,
                    minTransitionArea = 0.4f
                ),
                autoCentering = null,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
                item {
                    Button(
                        onClick = onNavigateToQr,
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = "扫码登录",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.QrCodeScanner,
                                contentDescription = "扫码登录",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
                item {
                    Button(
                        onClick = onNavigateToPhone,
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = "手机验证码登录",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = "手机验证码登录",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
            
            com.yorkyang2333.claudwecho.ui.components.PinnedHeader(title = "ClaudWecho")
        }
    }
}

@Composable
fun LoginQrScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val qrCode by viewModel.qrCodeBase64.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadQrCode()
    }

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
            LoginState.LOADING_QR, LoginState.LOGGING_IN, LoginState.IDLE -> {
                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
            }
            LoginState.QR_READY -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("请使用网易云音乐App扫码", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
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
                                contentDescription = "二维码",
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )
                        } else {
                            Text("二维码解析失败", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            LoginState.ERROR -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = error ?: "未知错误", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadQrCode() }) {
                        Text("重试")
                    }
                }
            }
            LoginState.LOGGED_IN -> {
                Text("登录成功！")
            }
            else -> {}
        }
    }
}

@Composable
fun LoginPhoneScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setPhoneInputState()
    }

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
            LoginState.PHONE_INPUT, LoginState.IDLE -> {
                var phone by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }

                ScalingLazyColumn(
                    scalingParams = androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults.scalingParams(
                        edgeScale = 0.3f,
                        minTransitionArea = 0.4f
                    ),
                    autoCentering = null,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text("手机号登录", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
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
                                if (phone.isEmpty()) Text("输入手机号", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
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
                                if (password.isEmpty()) Text("输入密码", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                innerTextField()
                            }
                        )
                    }
                    item {
                        Button(
                            onClick = { viewModel.loginWithPhone(phone, password) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("登录")
                        }
                    }
                }
            }
            LoginState.LOGGING_IN -> {
                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
            }
            LoginState.ERROR -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = error ?: "未知错误", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.setPhoneInputState() }) {
                        Text("重试")
                    }
                }
            }
            LoginState.LOGGED_IN -> {
                Text("登录成功！")
            }
            else -> {}
        }
    }
}
