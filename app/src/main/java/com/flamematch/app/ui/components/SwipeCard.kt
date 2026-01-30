package com.flamematch.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.flamematch.app.data.model.User
import com.flamematch.app.ui.theme.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun SwipeCard(
    user: User,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSuperLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isDragging) offsetX else 0f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "offsetX"
    )
    
    val animatedOffsetY by animateFloatAsState(
        targetValue = if (isDragging) offsetY else 0f,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "offsetY"
    )
    
    val rotation = (animatedOffsetX / 20).coerceIn(-15f, 15f)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = animatedOffsetX
                    translationY = animatedOffsetY
                    rotationZ = rotation
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            when {
                                offsetX > 150 -> onSwipeRight()
                                offsetX < -150 -> onSwipeLeft()
                                offsetY < -150 -> onSuperLike()
                            }
                            offsetX = 0f
                            offsetY = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            offsetX = 0f
                            offsetY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    )
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Profile Photo
                AsyncImage(
                    model = user.profilePhoto.ifEmpty { "https://via.placeholder.com/400x600?text=${user.name}" },
                    contentDescription = user.name,
                    modifier = Modifier.fillMaxSize(),
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
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.8f)
                                )
                            )
                        )
                )
                
                // Like/Nope indicators
                if (offsetX > 50) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(32.dp)
                            .rotate(-15f)
                    ) {
                        Text(
                            "LIKE",
                            color = Color.Green,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (offsetX < -50) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(32.dp)
                            .rotate(15f)
                    ) {
                        Text(
                            "NOPE",
                            color = FlameRed,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (offsetY < -50) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 100.dp)
                    ) {
                        Text(
                            "SUPER LIKE",
                            color = Color.Cyan,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // User info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${user.name}, ${user.age}",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color.Cyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    if (user.city.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = GrayText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${user.city}, ${user.country}",
                                color = GrayText,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    if (user.bio.isNotEmpty()) {
                        Text(
                            user.bio,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp),
                            maxLines = 3
                        )
                    }
                }
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pass button
            FloatingActionButton(
                onClick = onSwipeLeft,
                containerColor = DarkCard,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Pass",
                    tint = FlameRed,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Super Like button
            FloatingActionButton(
                onClick = onSuperLike,
                containerColor = DarkCard,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Super Like",
                    tint = Color.Cyan,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Like button
            FloatingActionButton(
                onClick = onSwipeRight,
                containerColor = FlameRed,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
