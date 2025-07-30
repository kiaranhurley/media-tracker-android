package com.kiaranhurley.mediatracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.database.entities.Film
import com.kiaranhurley.mediatracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmCatalogScreen(
    user: User,
    onFilmClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FilmCatalogViewModel = hiltViewModel()
) {
    val filmState by viewModel.state.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
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
        // Top App Bar with Search
        TopAppBar(
            title = {
                Text(
                    text = "Films",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppBarText
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppBarBackground,
                titleContentColor = AppBarText
            )
        )
        
        // Search Bar
        Surface(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    if (it.isBlank()) {
                        viewModel.loadAllFilms()
                    } else {
                        viewModel.searchFilms(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(
                        "Search movies...",
                        color = InputPlaceholderColor
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                viewModel.loadAllFilms()
                                keyboardController?.hide()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                    }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedTextColor = InputTextColor,
                    unfocusedTextColor = InputTextColor,
                    cursorColor = InputTextColor
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
        
        // Content based on state
        val currentState = filmState
        when (currentState) {
            is FilmCatalogState.Loading -> {
                LoadingContent()
            }
            is FilmCatalogState.Success -> {
                if (currentState.films.isEmpty()) {
                    EmptyContent(searchQuery.isNotEmpty())
                } else {
                    FilmList(
                        films = currentState.films,
                        onFilmClick = onFilmClick
                    )
                }
            }
            is FilmCatalogState.Error -> {
                ErrorContent(
                    message = currentState.message,
                    onRetry = { viewModel.loadAllFilms() }
                )
            }
            is FilmCatalogState.Idle -> {
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
                text = "Loading movies...",
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
                    text = "Couldn't load movies",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = HeadingSecondary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CardBodyOnLight
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
private fun EmptyContent(isSearching: Boolean) {
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
                imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.Movie,
                contentDescription = if (isSearching) "No results" else "No movies",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = if (isSearching) "No movies found" else "No movies available",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = HeadingSecondary
            )
            Text(
                text = if (isSearching) 
                    "Try a different search term" 
                else 
                    "Movies will appear here when available",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun FilmList(
    films: List<Film>,
    onFilmClick: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(films) { film ->
            FilmCard(
                film = film,
                onClick = { 
                    // Use TMDB ID if available, otherwise use local film ID
                    val idToPass = if (film.filmId == 0) film.tmdbId else film.filmId
                    onFilmClick(idToPass)
                }
            )
        }
    }
}

@Composable
private fun FilmCard(
    film: Film,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Movie Poster
            AsyncImage(
                model = if (film.posterPath != null) 
                    "https://image.tmdb.org/t/p/w342${film.posterPath}" 
                else null,
                contentDescription = "${film.title} poster",
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                contentScale = ContentScale.Crop
            )
            
            // Movie Details
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = film.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = CardTitleText
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Release Year
                    Text(
                        text = SimpleDateFormat("yyyy", Locale.getDefault()).format(film.releaseDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CardSubtitleText
                    )
                    
                    // Rating
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            modifier = Modifier.size(16.dp),
                            tint = BrandOrange
                        )
                        Text(
                            text = String.format("%.1f", film.voteAverage),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = BrandOrange
                        )
                    }
                    
                    // Runtime
                    if (film.runtime != null) {
                        Text(
                            text = "${film.runtime}min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CardSubtitleText
                        )
                    }
                }
                
                // Overview
                if (film.overview != null) {
                    Text(
                        text = film.overview,
                        style = MaterialTheme.typography.bodySmall,
                        color = CardBodyText,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Genres
                if (film.genres != null) {
                    Text(
                        text = film.genres,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
} 