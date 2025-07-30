package com.kiaranhurley.mediatracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RateReview
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
fun ReviewsScreen(
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
                    text = "My Reviews",
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
                if (currentState.reviews.isEmpty()) {
                    EmptyReviewsContent()
                } else {
                    ReviewsList(
                        reviews = currentState.reviews,
                        onDeleteReview = { review ->
                            viewModel.deleteReview(review.review.reviewId)
                        },
                        onEditReview = { review ->
                            // Navigate to edit screen for the specific review
                            // For now, we'll just show a toast or a simple dialog
                            println("Editing review: ${review.review.reviewId}")
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
private fun ReviewsList(
    reviews: List<ReviewWithContent>,
    onDeleteReview: (ReviewWithContent) -> Unit,
    onEditReview: (ReviewWithContent) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(reviews) { review ->
            ReviewCard(
                review = review,
                onDelete = { onDeleteReview(review) },
                onEdit = { onEditReview(review) }
            )
        }
    }
}

@Composable
private fun ReviewCard(
    review: ReviewWithContent,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with title, rating, and menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.contentTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CardTitleText,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Rating stars
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = StatusWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${review.review.rating}/5",
                        style = MaterialTheme.typography.bodySmall,
                        color = CardSubtitleText
                    )
                }
                
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
                            text = { Text("Edit Review") },
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
                            text = { Text("Delete Review") },
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
            
            // Review content
            Text(
                text = review.review.content,
                style = MaterialTheme.typography.bodyMedium,
                color = CardBodyText
            )
            
            // Date and type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(review.review.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = CardSubtitleText
                )
                
                Surface(
                    color = if (review.review.itemType == "FILM") BrandBlue else BrandOrange,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (review.review.itemType == "FILM") "MOVIE" else "GAME",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyReviewsContent() {
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
                imageVector = Icons.Default.RateReview,
                contentDescription = "No reviews",
                tint = CardSubtitleText,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = "No reviews yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = HeadingSecondary
            )
            
            Text(
                text = "Your reviews will appear here when you start rating content",
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
                text = "Error loading reviews",
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