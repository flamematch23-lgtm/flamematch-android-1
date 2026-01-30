package com.flamematch.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.flamematch.app.data.model.PremiumPlans
import com.flamematch.app.ui.theme.*
import com.flamematch.app.viewmodel.MainViewModel

@Composable
fun ProfileContent(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadPhoto(it) }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile header
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = currentUser?.profilePhoto?.ifEmpty { "https://via.placeholder.com/200" },
                contentDescription = "Profile",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, FlameRed, CircleShape),
                contentScale = ContentScale.Crop
            )
            
            // Edit photo button
            IconButton(
                onClick = { photoLauncher.launch("image/*") },
                modifier = Modifier
                    .size(36.dp)
                    .background(FlameRed, CircleShape)
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    "Change photo",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Name and verification
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${currentUser?.name ?: "User"}, ${currentUser?.age ?: 18}",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            if (currentUser?.isVerified == true) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Verified,
                    "Verified",
                    tint = Color.Cyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Location
        if (currentUser?.city?.isNotEmpty() == true) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    "Location",
                    tint = GrayText,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "${currentUser?.city}, ${currentUser?.country}",
                    color = GrayText,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Premium status / upgrade card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (currentUser?.isPremium == true) 
                    (if (currentUser?.premiumType == "platinum") PlatinumPremium else GoldPremium).copy(alpha = 0.2f)
                else DarkCard
            ),
            shape = RoundedCornerShape(16.dp),
            onClick = { showPremiumDialog = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    "Premium",
                    tint = if (currentUser?.isPremium == true) 
                        (if (currentUser?.premiumType == "platinum") PlatinumPremium else GoldPremium)
                    else GrayText,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (currentUser?.isPremium == true) 
                            "${currentUser?.premiumType?.replaceFirstChar { it.uppercase() }} Member"
                        else "Get FlameMatch Gold",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        if (currentUser?.isPremium == true)
                            "Enjoy your premium benefits!"
                        else "Unlimited likes, see who likes you & more",
                        color = GrayText,
                        fontSize = 12.sp
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    "View",
                    tint = GrayText
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Photos section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "My Photos",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { photoLauncher.launch("image/*") }) {
                Icon(Icons.Default.Add, "Add", tint = FlameRed)
                Text("Add", color = FlameRed)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(currentUser?.photos ?: emptyList()) { photo ->
                AsyncImage(
                    model = photo,
                    contentDescription = "Photo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Add photo placeholder
            item {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .clickable { photoLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        "Add photo",
                        tint = GrayText,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Bio section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "About Me",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, "Edit", tint = FlameRed)
                    }
                }
                Text(
                    currentUser?.bio?.ifEmpty { "Tell others about yourself..." } ?: "Tell others about yourself...",
                    color = if (currentUser?.bio?.isEmpty() != false) GrayText else Color.White,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Interests section
        if (currentUser?.interests?.isNotEmpty() == true) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Interests",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentUser?.interests?.forEach { interest ->
                            Surface(
                                color = FlameRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    interest,
                                    color = FlameRed,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Settings buttons
        SettingsButton(
            icon = Icons.Default.Tune,
            title = "Discovery Settings",
            subtitle = "Age, distance, gender preferences"
        ) { /* TODO */ }
        
        SettingsButton(
            icon = Icons.Default.Shield,
            title = "Safety & Privacy",
            subtitle = "Block list, hidden profiles"
        ) { /* TODO */ }
        
        SettingsButton(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            subtitle = "Push, email preferences"
        ) { /* TODO */ }
        
        SettingsButton(
            icon = Icons.Default.Help,
            title = "Help & Support",
            subtitle = "FAQ, contact us"
        ) { /* TODO */ }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Logout button
        OutlinedButton(
            onClick = {
                viewModel.signOut()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = FlameRed)
        ) {
            Icon(Icons.Default.Logout, "Logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
    
    // Premium dialog
    if (showPremiumDialog) {
        PremiumDialog(
            currentPlan = currentUser?.premiumType,
            onDismiss = { showPremiumDialog = false },
            onSelectPlan = { plan ->
                // TODO: Implement PayPal subscription
                showPremiumDialog = false
            }
        )
    }
}

@Composable
fun SettingsButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = DarkCard,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, title, tint = GrayText)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Medium)
                Text(subtitle, color = GrayText, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, "Open", tint = GrayText)
        }
    }
}

@Composable
fun PremiumDialog(
    currentPlan: String?,
    onDismiss: () -> Unit,
    onSelectPlan: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("FlameMatch Premium", color = Color.White)
        },
        text = {
            Column {
                // Gold Plan
                PlanCard(
                    name = "Gold",
                    price = "€14.99/month",
                    features = PremiumPlans.GOLD.features,
                    color = GoldPremium,
                    isSelected = currentPlan == "gold",
                    onClick = { onSelectPlan("gold") }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Platinum Plan
                PlanCard(
                    name = "Platinum",
                    price = "€24.99/month",
                    features = PremiumPlans.PLATINUM.features,
                    color = PlatinumPremium,
                    isSelected = currentPlan == "platinum",
                    onClick = { onSelectPlan("platinum") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = FlameRed)
            }
        }
    )
}

@Composable
fun PlanCard(
    name: String,
    price: String,
    features: List<String>,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) color else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(price, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            features.take(3).forEach { feature ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Icon(Icons.Default.Check, feature, tint = Color.Green, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(feature, color = GrayText, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Simple implementation - in production use accompanist or compose foundation FlowRow
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}
