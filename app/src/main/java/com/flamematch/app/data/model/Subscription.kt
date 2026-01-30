package com.flamematch.app.data.model

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val price: Double,
    val currency: String = "EUR",
    val period: String = "month",
    val features: List<String>
)

object PremiumPlans {
    val GOLD = SubscriptionPlan(
        id = "gold",
        name = "Gold",
        price = 14.99,
        features = listOf(
            "Unlimited Likes",
            "See Who Likes You",
            "5 Super Likes per day",
            "1 Boost per month",
            "Rewind last swipe",
            "No ads"
        )
    )
    
    val PLATINUM = SubscriptionPlan(
        id = "platinum",
        name = "Platinum",
        price = 24.99,
        features = listOf(
            "All Gold features",
            "Unlimited Super Likes",
            "3 Boosts per month",
            "Priority Likes",
            "Message before matching",
            "See who's online",
            "Advanced filters",
            "Video Date feature"
        )
    )
}
