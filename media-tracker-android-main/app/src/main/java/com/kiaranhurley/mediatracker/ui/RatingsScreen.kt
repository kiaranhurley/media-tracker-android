package com.kiaranhurley.mediatracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingsScreen(
    user: User,
    onBackClick: () -> Unit,
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
                    colors = listOf(PrimaryGray, SurfaceGray)
                )
            )
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "My Ratings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppBarText
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AppBarText
                    )
                }
            },
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
                if (currentState.ratings.isEmpty()) {
                    EmptyRatingsContent()
                } else {
                    RatingsList(
                        ratings = currentState.ratings,
                        onDeleteRating = { rating ->
                            viewModel.deleteRating(rating.rating.ratingId)
                        },
                        onEditRating = { rating ->
                            // Navigate to edit screen for the specific rating
                            // For now, we'll just show a toast or a simple dialog
                            // In a real app, you'd navigate to EditRatingScreen(rating)
                            // This is a placeholder for now.
                            println("Editing rating: ${rating.rating.ratingId}")
                        }
                    )
                }
            }
            is ProfileState.Error -> {
                ErrorContent(
                    message = currentState.message,
                    onRetry = { viewModel.loadProfile(user.userId) }
                )
            }
            else -> {
                LoadingContent()
            }
        }
    }
}

@Composable
private fun RatingsList(
    ratings: List<RatingWithContent>,
    onDeleteRating: (RatingWithContent) -> Unit,
    onEditRating: (RatingWithContent) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ratings) { rating ->
            RatingCard(
                rating = rating,
                onDelete = { onDeleteRating(rating) },
                onEdit = { onEditRating(rating) }
            )
        }
    }
}

@Composable
private fun RatingCard(
    rating: RatingWithContent,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Content info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rating.contentTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CardTitleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Menu button
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = CardSubtitleText
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Rating") },
                                onClick = {
                                    onEdit()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Rating") },
                                onClick = {
                                    onDelete()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = StatusError
                                    )
                                }
                            )
                        }
                    }
                }
                
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(rating.rating.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = CardSubtitleText
                )
                
                Surface(
                    color = if (rating.rating.itemType == "FILM") BrandBlue else BrandOrange,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (rating.rating.itemType == "FILM") "MOVIE" else "GAME",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            // Rating display
            Surface(
                color = when {
                    rating.rating.rating >= 4.0f -> StatusSuccess.copy(alpha = 0.2f)
                    rating.rating.rating >= 3.0f -> StatusWarning.copy(alpha = 0.2f)
                    else -> StatusError.copy(alpha = 0.2f)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = when {
                            rating.rating.rating >= 4.0f -> StatusSuccess
                            rating.rating.rating >= 3.0f -> StatusWarning
                            else -> StatusError
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${rating.rating.rating}/5",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CardTitleText
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRatingsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "No ratings",
                tint = CardSubtitleText,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = "No ratings yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = HeadingSecondary
            )
            
            Text(
                text = "Your ratings will appear here when you start rating content",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Error loading ratings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = HeadingSecondary
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
} 