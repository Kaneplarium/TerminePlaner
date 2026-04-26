package com.terminplaner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.terminplaner.data.preferences.ThemePreferences
import com.terminplaner.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    areaName: String,
    userStatus: Int = ThemePreferences.STATUS_NONE,
    navController: NavController,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val userName by viewModel.userName.collectAsState()
    var showAccountSwitcher by remember { mutableStateOf(false) }

    val isBusiness = userStatus == ThemePreferences.STATUS_BUSINESS
    val statusText = if (isBusiness) "Business" else "Persönlich"
    val statusColor = if (isBusiness) Color(0xFFAF52DE) else Color(0xFFFFD700) // Purple vs Gold

    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Terminplaner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = statusColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                    ) {
                        Text(
                            text = statusText.uppercase(),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = areaName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = navigationIcon,
        actions = {
            actions()
            // Google Style Profile Switcher Icon (Badge removed per user request)
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(40.dp)
                    .clickable { showAccountSwitcher = true },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(if (isBusiness) RoundedCornerShape(8.dp) else CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isBusiness) {
                            Icon(Icons.Default.BusinessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                        } else {
                            Text(
                                text = (userName?.take(1) ?: "U").uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior
    )

    if (showAccountSwitcher) {
        ModalBottomSheet(
            onDismissRequest = { showAccountSwitcher = false },
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            AccountSwitcherContent(
                currentStatus = userStatus,
                userName = userName,
                onStatusChange = {
                    viewModel.setUserStatus(it)
                    showAccountSwitcher = false
                }
            )
        }
    }
}

@Composable
fun AccountSwitcherContent(
    currentStatus: Int,
    userName: String?,
    onStatusChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Konto wechseln",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        val isPersonalActive = currentStatus != ThemePreferences.STATUS_BUSINESS
        
        AccountListItem(
            title = "Persönlicher Planer",
            subtitle = userName ?: "Benutzer",
            icon = Icons.Default.Home,
            isActive = isPersonalActive,
            shape = CircleShape,
            onClick = { onStatusChange(ThemePreferences.STATUS_PERSONAL) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp), thickness = 0.5.dp)

        AccountListItem(
            title = "Business-Profil",
            subtitle = "Arbeitsmodus • Fokus aktiv",
            icon = Icons.Default.BusinessCenter,
            isActive = !isPersonalActive,
            shape = RoundedCornerShape(8.dp),
            onClick = { onStatusChange(ThemePreferences.STATUS_BUSINESS) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AccountListItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    shape: androidx.compose.ui.graphics.Shape,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(shape)
                    .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (isActive) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Aktiv",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
