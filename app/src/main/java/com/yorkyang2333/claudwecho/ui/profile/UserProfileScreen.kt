package com.yorkyang2333.claudwecho.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Check
@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    onNavigateToLogin: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val vipInfo by viewModel.vipInfo.collectAsState()
    var showLogoutConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
            onNavigateToLogin()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ScalingLazyColumn(
            autoCentering = null,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 32.dp, start = 8.dp, end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
            
            item {
                if (userProfile?.avatarUrl != null) {
                    AsyncImage(
                        model = userProfile?.avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userProfile?.nickname ?: "User",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                val isVip = (userProfile?.vipType ?: 0) > 0 || (vipInfo?.redVipLevel ?: 0) > 0
                val vipLevelText = if (vipInfo == null) {
                    "VIP 状态"
                } else if (vipInfo?.redVipLevel != null && vipInfo?.redVipLevel!! > 0) {
                    "黑胶 VIP Lv.${vipInfo?.redVipLevel}"
                } else if (isVip) {
                    "黑胶 VIP"
                } else {
                    "普通用户"
                }
                
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = { 
                        Text(
                            text = vipLevelText,
                            style = MaterialTheme.typography.titleMedium
                        ) 
                    },
                    secondaryLabel = {
                        val expireTime = vipInfo?.musicPackage?.expireTime
                        if (vipInfo == null) {
                            Text(text = "加载中...")
                        } else if (expireTime != null && expireTime > 0) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd 到期", Locale.getDefault())
                            Text(text = sdf.format(Date(expireTime)))
                        } else {
                            if (isVip) {
                                Text(text = "永久有效 / 未知")
                            } else {
                                Text(text = "开通 VIP 畅听全库")
                            }
                        }
                    },
                    icon = { Icon(Icons.Rounded.WorkspacePremium, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
            
            item {
                Button(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    label = { 
                        Text(
                            text = "退出登录",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    icon = { Icon(Icons.AutoMirrored.Rounded.ExitToApp, null, tint = MaterialTheme.colorScheme.error) }
                )
            }
        }
        com.yorkyang2333.claudwecho.ui.components.PinnedHeader(title = "个人中心")
    }

    if (showLogoutConfirm) {
        Dialog(
            onDismissRequest = { showLogoutConfirm = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "确认退出登录？",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { showLogoutConfirm = false },
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Rounded.Close, null)
                    }
                    Button(
                        onClick = {
                            viewModel.logout()
                            showLogoutConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Rounded.Check, null)
                    }
                }
            }
        }
    }
}
