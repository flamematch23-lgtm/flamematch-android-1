package com.flamematch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flamematch.app.ui.theme.FlameRed
import com.flamematch.app.ui.theme.FlameOrange
import com.flamematch.app.ui.theme.DarkBackground
import com.flamematch.app.ui.theme.GoldPremium
import com.flamematch.app.ui.theme.PlatinumPremium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onNavigateBack: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium Plans", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Upgrade to Premium",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Unlock all features and find your perfect match faster!",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
            
            // Gold Plan
            item {
                PlanCard(
                    planName = "Gold",
                    price = "€14.99/month",
                    color = GoldPremium,
                    features = listOf(
                        "Unlimited Likes",
                        "See who likes you",
                        "5 Super Likes per day",
                        "1 Boost per month",
                        "Rewind last swipe"
                    ),
                    isSelected = selectedPlan == "gold",
                    onSelect = { selectedPlan = "gold" }
                )
            }
            
            // Platinum Plan
            item {
                PlanCard(
                    planName = "Platinum",
                    price = "€24.99/month",
                    color = PlatinumPremium,
                    features = listOf(
                        "All Gold features",
                        "Unlimited Super Likes",
                        "5 Boosts per month",
                        "Priority likes",
                        "Message before matching",
                        "Advanced filters"
                    ),
                    isSelected = selectedPlan == "platinum",
                    onSelect = { selectedPlan = "platinum" }
                )
            }
            
            // Subscribe Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { /* PayPal integration */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedPlan == "platinum") PlatinumPremium else GoldPremium
                    ),
                    shape = RoundedCornerShape(28.dp),
                    enabled = selectedPlan != null
                ) {
                    Text(
                        text = if (selectedPlan != null) "Subscribe with PayPal" else "Select a Plan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PlanCard(
    planName: String,
    price: String,
    color: Color,
    features: List<String>,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF1E1E1E)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, color) else null,
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = planName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                
                Text(
                    text = price,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓",
                        color = color,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = feature,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
