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
import androidx.compose.material.icons.rounded.Warning

@Composable
fun ErrorState(
    error: String?,
    onRetry: () -> Unit,
    onSecondaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null
) {
    Box(modifier = Modifier.fillMaxSize()) {
        ScalingLazyColumn(
            autoCentering = null,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp, top = 32.dp)
        ) {
            item {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = "Error",
                    tint = Color(0xFFCC785C),
                    modifier = Modifier.size(32.dp).padding(bottom = 4.dp)
                )
            }
            item {
                Text(
                    text = error ?: "未知错误",
                    color = Color(0xFFCC785C),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
                )
            }
            item {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC785C), contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = "重试",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                )
            }
            if (onSecondaryAction != null && secondaryActionLabel != null) {
                item {
                    Button(
                        onClick = onSecondaryAction,
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = secondaryActionLabel,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
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
                androidx.wear.compose.material3.CircularProgressIndicator(modifier = Modifier.fillMaxSize())
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
                ErrorState(error = error, onRetry = { viewModel.loadQrCode() })
            }
            LoginState.LOGGED_IN -> {
                Text("登录成功！")
            }
            else -> {}
        }
    }
}




