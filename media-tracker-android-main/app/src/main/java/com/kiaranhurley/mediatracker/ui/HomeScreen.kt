package com.kiaranhurley.mediatracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.database.entities.Film
import com.kiaranhurley.mediatracker.database.entities.Game
import com.kiaranhurley.mediatracker.database.entities.Rating
import com.kiaranhurley.mediatracker.database.entities.Review
import com.kiaranhurley.mediatracker.ui.theme.*
import com.kiaranhurley.mediatracker.ui.ReviewWithContent
import com.kiaranhurley.mediatracker.ui.RatingWithContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    onLogout: () -> Unit,
    onNavigateToWatchlist: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    
    // Load home data when screen appears
    LaunchedEffect(user.userId) {
        viewModel.loadHome(user.userId)
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PrimaryGray,
                        SurfaceGray
                    )
                )
            )
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Welcome back",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppBarSubtext
                    )
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppBarText
                    )
                }
            },
            actions = {
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        tint = AppBarText
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppBarBackground,
                titleContentColor = AppBarText,
                actionIconContentColor = AppBarText
            )
        )
        
        // Content based on state
        val currentState = homeState
        when (currentState) {
            is HomeState.Loading -> {
                LoadingContent()
            }
            is HomeState.Success -> {
                SuccessContent(
                    filmCount = currentState.filmCount,
                    gameCount = currentState.gameCount,
                    ratingCount = currentState.ratingCount,
                    reviewCount = currentState.reviewCount,
                    recentReviews = currentState.recentReviews,
                    recentRatings = currentState.recentRatings,
                    viewModel = viewModel,
                    user = user
                )
            }
            is HomeState.Error -> {
                ErrorContent(
                    message = currentState.message,
                    onRetry = { viewModel.loadHome(user.userId) }
                )
            }
            is HomeState.Idle -> {
                // Show loading while waiting for data
                LoadingContent()
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Loading your media library...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Couldn't load data",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = HeadingSecondary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(
    filmCount: Int,
    gameCount: Int,
    ratingCount: Int,
    reviewCount: Int,
    recentReviews: List<ReviewWithContent>,
    recentRatings: List<RatingWithContent>,
    viewModel: HomeViewModel,
    user: User
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Statistics Cards
        item {
            Text(
                text = "Your Collection",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = HeadingPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                item {
                    StatCard(
                        title = "Movies",
                        count = filmCount,
                        icon = Icons.Default.Movie,
                        color = BrandOrange
                    )
                }
                item {
                    StatCard(
                        title = "Games", 
                        count = gameCount,
                        icon = Icons.Default.SportsEsports,
                        color = BrandBlue
                    )
                }
                item {
                    StatCard(
                        title = "Ratings",
                        count = ratingCount,
                        icon = Icons.Default.Star,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                item {
                    StatCard(
                        title = "Reviews",
                        count = reviewCount,
                        icon = Icons.Default.RateReview,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
        
        // Recent Reviews Section
        if (recentReviews.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Reviews",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = HeadingSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            
            items(recentReviews.take(3)) { reviewWithContent ->
                ReviewCard(
                    reviewWithContent = reviewWithContent,
                    onEdit = { 
                        // TODO: Navigate to edit review screen or show dialog
                        // For now, we'll just reload the data
                        viewModel.loadHome(user.userId)
                    },
                    onDelete = { viewModel.deleteReview(reviewWithContent.review.reviewId) },
                    onTogglePrivacy = { viewModel.toggleReviewPrivacy(reviewWithContent.review.reviewId) }
                )
            }
        }
        
        // Recent Ratings Section  
        if (recentRatings.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Ratings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = HeadingSecondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
            
            items(recentRatings.take(5)) { ratingWithContent ->
                RatingCard(
                    ratingWithContent = ratingWithContent,
                    onEdit = { 
                        // TODO: Navigate to edit rating screen or show dialog
                        // For now, we'll just reload the data
                        viewModel.loadHome(user.userId)
                    },
                    onDelete = { viewModel.deleteRating(ratingWithContent.rating.ratingId) },
                    onTogglePrivacy = { viewModel.toggleRatingPrivacy(ratingWithContent.rating.ratingId) }
                )
            }
        }
        
        // Empty state if no activity
        if (recentReviews.isEmpty() && recentRatings.isEmpty()) {
            item {
                EmptyActivityCard()
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    ElevatedCard(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = CardTitleText
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = CardSubtitleText
            )
        }
    }
}

@Composable
private fun ReviewCard(
    reviewWithContent: ReviewWithContent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePrivacy: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reviewWithContent.contentTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = CardTitleText
                    )
                    Text(
                        text = if (reviewWithContent.contentType == "FILM") "Movie" else "Game",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (reviewWithContent.contentType == "FILM") BrandOrange else BrandBlue
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RatingChip(rating = reviewWithContent.review.rating)
                    ReviewRatingMenu(
                        onEdit = onEdit,
                        onDelete = onDelete,
                        onTogglePrivacy = onTogglePrivacy,
                        isPrivate = reviewWithContent.review.isPrivate
                    )
                }
            }
            Text(
                text = reviewWithContent.review.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = CardBodyText
            )
        }
    }
}

@Composable
private fun RatingCard(
    ratingWithContent: RatingWithContent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePrivacy: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ratingWithContent.contentTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = CardTitleText
                )
                Text(
                    text = if (ratingWithContent.contentType == "FILM") "Movie" else "Game",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ratingWithContent.contentType == "FILM") BrandOrange else BrandBlue
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RatingChip(rating = ratingWithContent.rating.rating)
                ReviewRatingMenu(
                    onEdit = onEdit,
                    onDelete = onDelete,
                    onTogglePrivacy = onTogglePrivacy,
                    isPrivate = ratingWithContent.rating.isPrivate
                )
            }
        }
    }
}

@Composable
private fun RatingChip(rating: Float) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when {
            rating >= 8.0f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            rating >= 6.0f -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                modifier = Modifier.size(14.dp),
                tint = when {
                    rating >= 8.0f -> MaterialTheme.colorScheme.primary
                    rating >= 6.0f -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
            )
            Text(
                text = String.format("%.1f", rating),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    rating >= 8.0f -> MaterialTheme.colorScheme.primary
                    rating >= 6.0f -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
private fun EmptyActivityCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = "No activity",
                modifier = Modifier.size(48.dp),
                tint = CardSubtitleText
            )
            Text(
                text = "Start Your Journey",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = CardTitleText
            )
            Text(
                text = "Browse movies and games, then rate and review them to see your activity here.",
                style = MaterialTheme.typography.bodyMedium,
                color = CardBodyText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
} 