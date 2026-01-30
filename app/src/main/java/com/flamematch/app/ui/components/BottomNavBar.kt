package com.flamematch.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.flamematch.app.ui.theme.*

data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        NavItem("discover", "Discover", Icons.Filled.LocalFireDepartment, Icons.Outlined.LocalFireDepartment),
        NavItem("likes", "Likes", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
        NavItem("matches", "Matches", Icons.Filled.Chat, Icons.Outlined.Chat),
        NavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )
    
    NavigationBar(
        containerColor = DarkSurface,
        contentColor = LightText,
        modifier = Modifier.height(80.dp)
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(28.dp)
                    )
                },
                label = {
                    Text(item.label)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FlameRed,
                    selectedTextColor = FlameRed,
                    unselectedIconColor = GrayText,
                    unselectedTextColor = GrayText,
                    indicatorColor = DarkCard
                )
            )
        }
    }
}
