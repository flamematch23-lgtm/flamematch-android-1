package com.flamematch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.flamematch.app.data.model.Match
import com.flamematch.app.ui.theme.*
import com.flamematch.app.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MatchesContent(
    viewModel: MainViewModel,
    onNavigateToChat: (String) -> Unit
) {
    val matches by viewModel.matches.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    
    // Separate new matches (no messages) from conversations
    val newMatches = matches.filter { it.lastMessage == null }
    val conversations = matches.filter { it.lastMessage != null }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Text(
            "Messages",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        if (matches.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Chat,
                        "No matches",
                        tint = GrayText,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No matches yet",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Start swiping to find your match!",
                        color = GrayText,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn {
                // New Matches section
                if (newMatches.isNotEmpty()) {
                    item {
                        Text(
                            "New Matches",
                            color = GrayText,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(newMatches) { match ->
                                NewMatchItem(
                                    match = match,
                                    currentUserId = currentUserId,
                                    onClick = { onNavigateToChat(match.id) }
                                )
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // Conversations section
                if (conversations.isNotEmpty()) {
                    item {
                        Text(
                            "Messages",
                            color = GrayText,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    items(conversations) { match ->
                        ConversationItem(
                            match = match,
                            currentUserId = currentUserId,
                            onClick = { onNavigateToChat(match.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewMatchItem(
    match: Match,
    currentUserId: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = match.getOtherUserPhoto(currentUserId).ifEmpty { "https://via.placeholder.com/100" },
                contentDescription = match.getOtherUserName(currentUserId),
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            // Online indicator
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Green, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            match.getOtherUserName(currentUserId).split(" ").firstOrNull() ?: "",
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@Composable
fun ConversationItem(
    match: Match,
    currentUserId: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile photo
            AsyncImage(
                model = match.getOtherUserPhoto(currentUserId).ifEmpty { "https://via.placeholder.com/100" },
                contentDescription = match.getOtherUserName(currentUserId),
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Name and message
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    match.getOtherUserName(currentUserId),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                Text(
                    match.lastMessage ?: "",
                    color = GrayText,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Time indicator
            Icon(
                Icons.Default.ChevronRight,
                "Open",
                tint = GrayText
            )
        }
    }
    
    HorizontalDivider(
        color = DarkCard,
        modifier = Modifier.padding(start = 88.dp)
    )
}
