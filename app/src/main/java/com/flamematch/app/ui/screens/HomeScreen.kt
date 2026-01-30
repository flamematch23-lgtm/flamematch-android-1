package com.flamematch.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.flamematch.app.ui.components.BottomNavBar
import com.flamematch.app.ui.components.MatchPopup
import com.flamematch.app.ui.theme.*
import com.flamematch.app.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToChat: (String) -> Unit,
    onLogout: () -> Unit
) {
    var currentRoute by remember { mutableStateOf("discover") }
    
    val showMatchPopup by viewModel.showMatchPopup.collectAsState()
    
    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.loadDiscoverUsers()
        viewModel.loadMatches()
        viewModel.loadLikesReceived()
        viewModel.updateLocation()
    }
    
    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentRoute) {
                "discover" -> DiscoverContent(viewModel)
                "likes" -> LikesContent(viewModel, onNavigateToChat)
                "matches" -> MatchesContent(viewModel, onNavigateToChat)
                "profile" -> ProfileContent(viewModel, onLogout)
            }
        }
    }
    
    // Match popup
    showMatchPopup?.let { matchedUser ->
        MatchPopup(
            matchedUser = matchedUser,
            onDismiss = { viewModel.dismissMatchPopup() },
            onSendMessage = {
                viewModel.dismissMatchPopup()
                // Navigate to chat with matched user
            }
        )
    }
}
