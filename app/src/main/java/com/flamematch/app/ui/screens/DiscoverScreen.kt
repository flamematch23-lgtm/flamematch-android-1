package com.flamematch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flamematch.app.ui.components.SwipeCard
import com.flamematch.app.ui.theme.*
import com.flamematch.app.viewmodel.MainViewModel

@Composable
fun DiscoverContent(
    viewModel: MainViewModel
) {
    val discoverUsers by viewModel.discoverUsers.collectAsState()
    val currentIndex by viewModel.currentDiscoverIndex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Favorite,
                    "Logo",
                    tint = FlameRed,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "FlameMatch",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Premium status
            if (currentUser?.isPremium == true) {
                Surface(
                    color = if (currentUser?.premiumType == "platinum") PlatinumPremium else GoldPremium,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        currentUser?.premiumType?.uppercase() ?: "PREMIUM",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
        
        // Main content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = FlameRed)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Finding people near you...",
                            color = GrayText
                        )
                    }
                }
            }
            
            discoverUsers.isEmpty() || currentIndex >= discoverUsers.size -> {
                // No more users
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            "No users",
                            tint = GrayText,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No more profiles",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Come back later or expand your preferences",
                            color = GrayText,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.loadDiscoverUsers() },
                            colors = ButtonDefaults.buttonColors(containerColor = FlameRed)
                        ) {
                            Text("Refresh")
                        }
                    }
                }
            }
            
            else -> {
                // Show swipe card
                val user = discoverUsers[currentIndex]
                
                SwipeCard(
                    user = user,
                    onSwipeLeft = { viewModel.passCurrentUser() },
                    onSwipeRight = { viewModel.likeCurrentUser() },
                    onSuperLike = { viewModel.likeCurrentUser(isSuperLike = true) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp)
                )
            }
        }
    }
}
