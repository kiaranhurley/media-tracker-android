package com.kiaranhurley.mediatracker.ui

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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.database.entities.WatchList
import com.kiaranhurley.mediatracker.database.entities.CustomList
import com.kiaranhurley.mediatracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import com.kiaranhurley.mediatracker.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onLogout: () -> Unit,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToCustomLists: (() -> Unit)? = null,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.state.collectAsStateWithLifecycle()
    
    // Load profile data when screen appears
    LaunchedEffect(user.userId) {
        viewModel.loadProfile(user.userId)
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
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppBarText
                )
            },
            // Removed actions/logout icon
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppBarBackground,
                titleContentColor = AppBarText
            )
        )

        // Content based on state
        when (val currentState = profileState) {
            is ProfileState.Loading -> {
                LoadingContent()
            }
            is ProfileState.Success -> {
                ProfileContent(
                    user = currentState.user ?: user,
                    reviews = currentState.reviews,
                    ratings = currentState.ratings,
                    watchList = currentState.watchList,
                    customLists = currentState.customLists,
                    onNavigateToWatchlist = onNavigateToWatchlist,
                    onNavigateToCustomLists = onNavigateToCustomLists,
                    onLogout = onLogout,
                    navController = navController,
                    viewModel = viewModel
                )
            }
            is ProfileState.Error -> {
                ErrorContent(
                    message = currentState.message,
                    onRetry = { viewModel.loadProfile(user.userId) }
                )
            }
            is ProfileState.Idle -> {
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
                text = "Loading profile...",
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
                    text = "Couldn't load profile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CardBodyText
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
private fun ProfileContent(
    user: User,
    reviews: List<ReviewWithContent>,
    ratings: List<RatingWithContent>,
    watchList: List<WatchListWithContent>,
    customLists: List<CustomList>,
    onNavigateToWatchlist: () -> Unit,
    onNavigateToCustomLists: (() -> Unit)? = null,
    onLogout: () -> Unit,
    navController: NavController,
    viewModel: ProfileViewModel
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Profile Header
        item {
            ProfileHeader(user = user)
        }
        
        // Menu Buttons Section
        item {
            Text(
                text = "Your Content",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = HeadingSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Watchlist Button
                MenuButton(
                    title = "My Watchlist",
                    subtitle = "${watchList.size} items",
                    icon = Icons.Default.Bookmark,
                    color = BrandBlue,
                    posterUrls = watchList.take(3).mapNotNull { it.posterUrl },
                    onClick = onNavigateToWatchlist
                )
                
                // Reviews Button  
                MenuButton(
                    title = "My Reviews",
                    subtitle = "${reviews.size} reviews",
                    icon = Icons.Default.RateReview,
                    color = BrandOrange,
                    posterUrls = reviews.take(3).mapNotNull { 
                        // Extract poster URLs from review content
                        null // We'll implement this later when we have content details
                    },
                    onClick = { 
                        println("DEBUG: Reviews button clicked - navigating to reviews screen")
                        navController.navigate(Screen.Reviews.route)
                    }
                )
                
                // Ratings Button
                MenuButton(
                    title = "My Ratings",
                    subtitle = "${ratings.size} ratings",
                    icon = Icons.Default.Star,
                    color = StatusWarning,
                    posterUrls = ratings.take(3).mapNotNull { 
                        // Extract poster URLs from rating content
                        null // We'll implement this later when we have content details
                    },
                    onClick = { 
                        println("DEBUG: Ratings button clicked - navigating to ratings screen")
                        navController.navigate(Screen.Ratings.route)
                    }
                )
                
                // Settings Button
                MenuButton(
                    title = "Settings",
                    subtitle = "Account & app settings",
                    icon = Icons.Default.Settings,
                    color = CardSubtitleText,
                    posterUrls = emptyList(),
                    onClick = { 
                        println("DEBUG: Settings button clicked - navigating to settings screen")
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileHeader(user: User) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Picture Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.displayName.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // User Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CardTitleText
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    ),
                    color = CardTitleText
                )
                
                if (user.bio != null) {
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CardBodyText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            // Member Since
            Text(
                text = "Member since ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(user.createdAt)}",
                style = MaterialTheme.typography.labelMedium,
                color = CardSubtitleText
            )
        }
    }
}

@Composable
private fun MenuButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    posterUrls: List<String>,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Icon and text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CardTitleText
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = CardSubtitleText
                    )
                }
            }
            
            // Right side: Overlapping poster previews
            if (posterUrls.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
                ) {
                    posterUrls.take(3).forEachIndexed { index, url ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Content poster",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            } else {
                // Arrow icon when no posters
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = CardSubtitleText,
                    modifier = Modifier.size(20.dp)
                )
            }
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
            .height(90.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
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
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CardTitleText
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = CardSubtitleText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
private fun CustomListCard(customList: CustomList, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    text = customList.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CardTitleText
                )
                if (customList.description != null) {
                    Text(
                        text = customList.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = CardBodyText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (customList.isPrivate) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Private",
                        modifier = Modifier.size(16.dp),
                        tint = CardSubtitleText
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View list",
                    tint = CardSubtitleText
                )
            }
        }
    }
}

@Composable
private fun ProfileWatchlistCard(item: WatchListWithContent) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Poster
            if (item.posterUrl != null) {
                AsyncImage(
                    model = item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .width(60.dp)
                        .height(90.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.watchList.itemType == "FILM") Icons.Default.Movie else Icons.Default.SportsEsports,
                        contentDescription = "Content type",
                        tint = CardSubtitleText,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CardTitleText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (item.watchList.itemType == "FILM") "Movie" else "Game",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.watchList.itemType == "FILM") BrandOrange else BrandBlue
                )
                Text(
                    text = "Added ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(item.watchList.addedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CardSubtitleText
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    onNavigateToWatchlist: () -> Unit,
    onNavigateToCustomLists: (() -> Unit)? = null,
    onLogout: () -> Unit,
    onClearAllGames: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CardTitleText
            )
            
            // Watchlist Button
            Button(
                onClick = {
                    println("DEBUG: ProfileScreen - Watchlist button clicked")
                    onNavigateToWatchlist()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Bookmark,
                        contentDescription = "Watchlist",
                        modifier = Modifier.size(20.dp)
                    )
                    Text("View Watchlist")
                }
            }
            
            // Custom Lists Button
            onNavigateToCustomLists?.let { onNavigate ->
                Button(
                    onClick = {
                        println("DEBUG: ProfileScreen - Custom Lists button clicked")
                        onNavigate()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.List,
                            contentDescription = "Custom Lists",
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Custom Lists")
                    }
                }
            }
            
            // Logout Button
            Button(
                onClick = {
                    println("DEBUG: ProfileScreen - Logout button clicked")
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout",
                        modifier = Modifier.size(20.dp)
                    )
                    Text("Logout")
                }
            }
            
            // Developer Option: Clear All Games
            Button(
                onClick = onClearAllGames,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = StatusError)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Clear Content",
                        modifier = Modifier.size(20.dp)
                    )
                    Text("Clear All Content (Developer)")
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = CardTitleText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = CardBodyText
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Navigate",
            tint = TextTertiary
        )
    }
} 

@Composable
private fun RatingChip(rating: Float?) {
    if (rating == null) return
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when {
            rating >= 4.0f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            rating >= 3.0f -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
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
                    rating >= 4.0f -> MaterialTheme.colorScheme.primary
                    rating >= 3.0f -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
            )
            Text(
                text = "${rating}/5",
                style = MaterialTheme.typography.bodySmall,
                color = CardBodyText
            )
        }
    }
} 