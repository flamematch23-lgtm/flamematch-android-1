package com.flamematch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.flamematch.app.data.model.Like
import com.flamematch.app.ui.theme.*
import com.flamematch.app.viewmodel.MainViewModel

@Composable
fun LikesContent(
    viewModel: MainViewModel,
    onNavigateToChat: (String) -> Unit
) {
    val likesReceived by viewModel.likesReceived.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isPremium = currentUser?.isPremium == true
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // Header
        Text(
            "Likes",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            "${likesReceived.size} people like you",
            color = GrayText,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (!isPremium && likesReceived.isNotEmpty()) {
            // Premium upsell
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GoldPremium.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        "Premium",
                        tint = GoldPremium,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "See who likes you",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Upgrade to Gold to reveal all your admirers",
                            color = GrayText,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        
        if (likesReceived.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        "No likes",
                        tint = GrayText,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No likes yet",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Keep swiping to get more matches!",
                        color = GrayText,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // Likes grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(likesReceived) { like ->
                    LikeCard(
                        like = like,
                        isPremium = isPremium,
                        onAccept = { viewModel.acceptLike(like) },
                        onReject = { viewModel.rejectLike(like) }
                    )
                }
            }
        }
    }
}

@Composable
fun LikeCard(
    like: Like,
    isPremium: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.75f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Photo (blurred if not premium)
            AsyncImage(
                model = like.fromUserPhoto.ifEmpty { "https://via.placeholder.com/200" },
                contentDescription = like.fromUserName,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (!isPremium) Modifier.blur(20.dp) else Modifier),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
            
            // Super Like badge
            if (like.isSuperLike) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    color = Color.Cyan,
                    shape = CircleShape
                ) {
                    Icon(
                        Icons.Default.Star,
                        "Super Like",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp)
                    )
                }
            }
            
            // User info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    if (isPremium) "${like.fromUserName}, ${like.fromUserAge}" else "???",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            // Action buttons (only for premium)
            if (isPremium) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = onReject,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "Pass", tint = FlameRed)
                    }
                    
                    IconButton(
                        onClick = onAccept,
                        modifier = Modifier
                            .size(36.dp)
                            .background(FlameRed, CircleShape)
                    ) {
                        Icon(Icons.Default.Favorite, "Like", tint = Color.White)
                    }
                }
            }
        }
    }
}
