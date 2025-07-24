package com.kiaranhurley.mediatracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kiaranhurley.mediatracker.R
import com.kiaranhurley.mediatracker.database.entities.Game
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.database.entities.Review
import com.kiaranhurley.mediatracker.database.entities.Rating
import com.kiaranhurley.mediatracker.database.entities.CustomList
import com.kiaranhurley.mediatracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    gameId: Int,
    user: User,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameDetailViewModel = hiltViewModel(),
    customListViewModel: CustomListViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val reviewsState by viewModel.reviewsState.collectAsStateWithLifecycle()
    val customListState by customListViewModel.customListState.collectAsStateWithLifecycle()
    
    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewText by remember { mutableStateOf("") }
    var showAddToListDialog by remember { mutableStateOf(false) }
    var showCreateListDialog by remember { mutableStateOf(false) }

    // Load game details and user's custom lists when screen appears
    LaunchedEffect(gameId) {
        viewModel.loadGameDetails(gameId, user.userId)
        viewModel.loadGameReviews(gameId)
        customListViewModel.loadCustomLists(user.userId)
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
            title = { Text("Game Details", color = AppBarText) },
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
                containerColor = AppBarBackground
            )
        )

        when (val currentState = detailState) {
            is GameDetailState.Loading -> {
                LoadingContent()
            }
            is GameDetailState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        GameHeader(
                            game = currentState.game,
                            userRating = currentState.userRating,
                            onRatingChange = { newRating ->
                                viewModel.rateGame(user.userId, gameId, newRating)
                            },
                            isInWatchlist = currentState.isInWatchlist,
                            onWatchlistToggle = {
                                viewModel.toggleWatchlist(user.userId, gameId, !currentState.isInWatchlist)
                            },
                            onAddToListClick = { showAddToListDialog = true }
                        )
                    }
                    
                    item {
                        GameDetails(game = currentState.game)
                    }
                    
                    item {
                        ReviewSection(
                            userReview = currentState.userReview,
                            onWriteReview = { showReviewDialog = true },
                            onEditReview = { showReviewDialog = true },
                            onDeleteReview = { viewModel.deleteReview(user.userId, gameId) },
                            onTogglePrivacy = { 
                                currentState.userReview?.let { review ->
                                    viewModel.toggleReviewPrivacy(review.reviewId)
                                }
                            }
                        )
                    }
                    
                    item {
                        when (val reviewState = reviewsState) {
                            is GameDetailState.Success -> {
                                if (reviewState.reviews.isNotEmpty()) {
                                    ReviewsList(reviews = reviewState.reviews)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
            is GameDetailState.Error -> {
                ErrorContent(
                    message = currentState.message,
                    onRetry = { viewModel.loadGameDetails(gameId, user.userId) }
                )
            }
            is GameDetailState.Idle -> {
                LoadingContent()
            }
        }
    }

    // Review Dialog
    if (showReviewDialog) {
        val currentDetailState = detailState
        if (currentDetailState is GameDetailState.Success) {
            ReviewDialog(
                reviewText = if (currentDetailState.userReview != null) {
                    currentDetailState.userReview.content
                } else {
                    reviewText
                },
                onReviewTextChange = { reviewText = it },
                rating = currentDetailState.userRating?.rating ?: 0f,
                onRatingChange = { userRating ->
                    viewModel.rateGame(user.userId, gameId, userRating)
                },
                onSubmit = {
                    viewModel.submitReview(user.userId, gameId, reviewText, currentDetailState.userRating?.rating ?: 0f)
                    showReviewDialog = false
                    reviewText = ""
                },
                onDismiss = { 
                    showReviewDialog = false
                    reviewText = ""
                }
            )
        }
    }

    // Add to List Dialog
    if (showAddToListDialog) {
        when (val listState = customListState) {
            is CustomListState.Success -> {
                AddToListDialog(
                    customLists = listState.customLists,
                    onListSelected = { listId ->
                        customListViewModel.addItemToList(
                            com.kiaranhurley.mediatracker.database.entities.ListItem(
                                listId = listId,
                                itemId = gameId,
                                itemType = "GAME"
                            )
                        )
                        showAddToListDialog = false
                    },
                    onCreateNewList = { 
                        showAddToListDialog = false
                        showCreateListDialog = true 
                    },
                    onDismiss = { showAddToListDialog = false }
                )
            }
            else -> {
                // Show loading or error state
                showAddToListDialog = false
            }
        }
    }

    // Create List Dialog
    if (showCreateListDialog) {
        CreateListDialog(
            onCreateList = { name, description ->
                customListViewModel.createCustomList(user.userId, name, description)
                showCreateListDialog = false
            },
            onDismiss = { showCreateListDialog = false }
        )
    }
}

@Composable
private fun GameHeader(
    game: Game,
    userRating: Rating?,
    onRatingChange: (Float) -> Unit,
    isInWatchlist: Boolean,
    onWatchlistToggle: () -> Unit,
    onAddToListClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Game Cover
            AsyncImage(
                model = if (!game.coverUrl.isNullOrBlank()) {
                    game.coverUrl
                } else null,
                contentDescription = "Game cover",
                modifier = Modifier
                    .width(120.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.ic_launcher_background)
            )
            
            // Game Info
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = CardTitleText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (game.firstReleaseDate != null) {
                    Text(
                        text = SimpleDateFormat("yyyy", Locale.getDefault()).format(game.firstReleaseDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CardSubtitleText
                    )
                }
                
                if (!game.platforms.isNullOrBlank()) {
                    Text(
                        text = "Platforms: ${game.platforms}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CardBodyText
                    )
                }
                
                if (game.aggregatedRating != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = BrandOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${String.format("%.1f", (game.aggregatedRating / 20))}/5",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CardBodyText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // User Rating
                RatingBar(
                    rating = userRating?.rating ?: 0f,
                    onRatingChange = onRatingChange,
                    maxStars = 5
                )
                
                // Watchlist Button
                Button(
                    onClick = onWatchlistToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInWatchlist) BrandOrange else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Icon(
                        if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Watchlist"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isInWatchlist) "In Library" else "Add to Library")
                }

                // Add to List Button
                Button(
                    onClick = onAddToListClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.List, contentDescription = "Add to List")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to List")
                }
            }
        }
    }
}

@Composable
private fun GameDetails(game: Game) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CardTitleText
            )
            
            if (!game.summary.isNullOrBlank()) {
                Text(
                    text = game.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CardBodyText,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
                )
            } else {
                Text(
                    text = "No summary available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CardSubtitleText,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            
            if (!game.developer.isNullOrBlank()) {
                DetailItem(label = "Developer", value = game.developer)
            }
            
            if (!game.publisher.isNullOrBlank()) {
                DetailItem(label = "Publisher", value = game.publisher)
            }
            
            if (!game.platforms.isNullOrBlank()) {
                DetailItem(label = "Platforms", value = game.platforms)
            }
            
            if (game.firstReleaseDate != null) {
                val releaseDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    .format(Date(game.firstReleaseDate!! * 1000))
                DetailItem(label = "Release Date", value = releaseDate)
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = CardSubtitleText,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = CardBodyText
        )
    }
}

@Composable
private fun RatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    maxStars: Int = 5
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Your Rating",
            style = MaterialTheme.typography.labelMedium,
            color = CardSubtitleText,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Create stars with half-star support
            (1..maxStars).forEach { starPosition ->
                val fullStarValue = starPosition.toFloat()
                val halfStarValue = starPosition - 0.5f
                
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { 
                            // Determine if clicked on left or right half
                            if (rating == halfStarValue) {
                                onRatingChange(fullStarValue)
                            } else if (rating >= fullStarValue) {
                                // If already full, clicking again makes it half
                                onRatingChange(halfStarValue)
                            } else {
                                // If not rated at this position, set to half star first
                                onRatingChange(halfStarValue)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Background star (always visible)
                    Icon(
                        Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = CardSubtitleText,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    // Determine star fill state
                    when {
                        rating >= fullStarValue -> {
                            // Full star
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        rating >= halfStarValue -> {
                            // Half star
                            HalfFilledStar(
                                tint = BrandBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            
            // Show current rating value
            if (rating > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${rating}/5",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CardBodyText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun HalfFilledStar(
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Background empty star
        Icon(
            Icons.Default.StarBorder,
            contentDescription = null,
            tint = CardSubtitleText,
            modifier = Modifier.fillMaxSize()
        )
        
        // Half-filled star using clipped area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(
                    GenericShape { size, _ ->
                        // Create a path that clips the left half of the star
                        addRect(
                            androidx.compose.ui.geometry.Rect(
                                left = 0f,
                                top = 0f,
                                right = size.width / 2f,
                                bottom = size.height
                            )
                        )
                    }
                )
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ReviewSection(
    userReview: Review?,
    onWriteReview: () -> Unit,
    onEditReview: () -> Unit,
    onDeleteReview: () -> Unit,
    onTogglePrivacy: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Review",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CardTitleText
                )
                
                if (userReview != null) {
                    ReviewRatingMenu(
                        onEdit = onEditReview,
                        onDelete = onDeleteReview,
                        onTogglePrivacy = onTogglePrivacy,
                        isPrivate = userReview.isPrivate
                    )
                }
            }
            
            if (userReview != null) {
                Text(
                    text = userReview.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CardBodyText
                )
                
                Text(
                    text = "Written on ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(userReview.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CardSubtitleText
                )
                
                Button(
                    onClick = onWriteReview,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Review")
                }
            } else {
                Text(
                    text = "Share your thoughts about this game",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CardSubtitleText
                )
                
                Button(
                    onClick = onWriteReview,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Write Review")
                }
            }
        }
    }
}

@Composable
private fun ReviewsList(reviews: List<Review>) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Reviews (${reviews.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CardTitleText
            )
            
            reviews.forEach { review ->
                ReviewItem(review = review)
                if (review != reviews.last()) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(review: Review) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "User Review", // In a real app, this would be the username
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = CardSubtitleText
            )
            
            if (review.rating != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = BrandBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${review.rating}/5",
                        style = MaterialTheme.typography.bodySmall,
                        color = CardBodyText
                    )
                }
            }
        }
        
        Text(
            text = review.content,
            style = MaterialTheme.typography.bodyMedium,
            color = CardBodyText
        )
        
        Text(
            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(review.createdAt),
            style = MaterialTheme.typography.bodySmall,
            color = CardSubtitleText
        )
    }
}

@Composable
private fun ReviewDialog(
    reviewText: String,
    onReviewTextChange: (String) -> Unit,
    rating: Float,
    onRatingChange: (Float) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Write Review",
                color = CardTitleText
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RatingBar(
                    rating = rating,
                    onRatingChange = onRatingChange
                )
                
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = onReviewTextChange,
                    label = { Text("Your review", color = CardSubtitleText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CardBodyText,
                        unfocusedTextColor = CardBodyText,
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = reviewText.isNotBlank() && rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CardSubtitleText)
            }
        }
    )
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
            CircularProgressIndicator(color = BrandOrange)
            Text(
                text = "Loading game details...",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Error loading game",
                style = MaterialTheme.typography.titleMedium,
                color = StatusError
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Retry")
            }
        }
    }
} 

@Composable
private fun AddToListDialog(
    customLists: List<CustomList>,
    onListSelected: (Int) -> Unit,
    onCreateNewList: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add to List",
                color = CardTitleText
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Create new list option
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCreateNewList() },
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Create new list",
                                tint = BrandOrange
                            )
                            Text(
                                text = "Create New List",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = BrandOrange
                            )
                        }
                    }
                }
                
                // Existing lists
                items(customLists) { customList ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onListSelected(customList.listId) },
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = "List",
                                tint = CardSubtitleText
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = customList.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = CardTitleText
                                )
                                if (!customList.description.isNullOrBlank()) {
                                    Text(
                                        text = customList.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CardSubtitleText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CardSubtitleText)
            }
        }
    )
}

@Composable
private fun CreateListDialog(
    onCreateList: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var listName by remember { mutableStateOf("") }
    var listDescription by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create New List",
                color = CardTitleText
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = listName,
                    onValueChange = { listName = it },
                    label = { Text("List Name", color = CardSubtitleText) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CardBodyText,
                        unfocusedTextColor = CardBodyText,
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = listDescription,
                    onValueChange = { listDescription = it },
                    label = { Text("Description (Optional)", color = CardSubtitleText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CardBodyText,
                        unfocusedTextColor = CardBodyText,
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateList(listName, listDescription) },
                enabled = listName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CardSubtitleText)
            }
        }
    )
} 