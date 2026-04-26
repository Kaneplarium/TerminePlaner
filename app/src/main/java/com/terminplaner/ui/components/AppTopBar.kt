package com.terminplaner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    areaName: String,
    isPro: Boolean = false,
    navController: NavController,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Terminplaner",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (isPro) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.extraSmall,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = areaName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        navigationIcon = navigationIcon,
        actions = actions
    )
}
