package com.kiaranhurley.mediatracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
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
import com.kiaranhurley.mediatracker.database.entities.CustomList
import com.kiaranhurley.mediatracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomListScreen(
    user: User,
    onListClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomListViewModel = hiltViewModel()
) {
    val customListState by viewModel.customListState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedList by remember { mutableStateOf<CustomList?>(null) }

    // Load custom lists when screen appears
    LaunchedEffect(user.userId) {
        viewModel.loadCustomLists(user.userId)
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
                    text = "My Lists",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppBarText
                )
            },
            actions = {
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create List",
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
        when (val currentState = customListState) {
            is CustomListState.Loading -> {
                LoadingContent()
            }
            is CustomListState.Success -> {
                if (currentState.customLists.isEmpty()) {
                    EmptyListsContent(onCreateList = { showCreateDialog = true })
                } else {
                    CustomListsContent(
                        customLists = currentState.customLists,
                        onListClick = onListClick,
                        onEditList = { list ->
                            selectedList = list
                            showEditDialog = true
                        },
                        onDeleteList = { list ->
                            viewModel.deleteCustomList(list.listId)
                        }
                    )
                }
            }
            is CustomListState.Error -> {
                ErrorContent(
                    message = currentState.message,
                    onRetry = { viewModel.loadCustomLists(user.userId) }
                )
            }
            is CustomListState.Idle -> {
                LoadingContent()
            }
            is CustomListState.Detail -> {
                // Detail state is handled in CustomListDetailScreen
                LoadingContent()
            }
        }
    }

    // Create List Dialog
    if (showCreateDialog) {
        CreateListDialog(
            onCreateList = { name, description ->
                viewModel.createCustomList(user.userId, name, description)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    // Edit List Dialog
    if (showEditDialog && selectedList != null) {
        EditListDialog(
            customList = selectedList!!,
            onUpdateList = { name, description ->
                viewModel.updateCustomList(selectedList!!.listId, name, description)
                showEditDialog = false
                selectedList = null
            },
            onDismiss = { 
                showEditDialog = false
                selectedList = null
            }
        )
    }
}

@Composable
private fun CustomListsContent(
    customLists: List<CustomList>,
    onListClick: (Int) -> Unit,
    onEditList: (CustomList) -> Unit,
    onDeleteList: (CustomList) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = customLists,
            key = { it.listId }
        ) { customList ->
            CustomListCard(
                customList = customList,
                onClick = { onListClick(customList.listId) },
                onEditClick = { onEditList(customList) },
                onDeleteClick = { onDeleteList(customList) }
            )
        }
    }
}

@Composable
private fun CustomListCard(
    customList: CustomList,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "List",
                        tint = BrandOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = customList.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CardTitleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit List",
                            tint = CardSubtitleText
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete List",
                            tint = StatusError
                        )
                    }
                }
            }
            
            if (!customList.description.isNullOrBlank()) {
                Text(
                    text = customList.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CardBodyText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Text(
                text = "Created ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(customList.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = CardSubtitleText
            )
        }
    }
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

@Composable
private fun EditListDialog(
    customList: CustomList,
    onUpdateList: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var listName by remember { mutableStateOf(customList.name) }
    var listDescription by remember { mutableStateOf(customList.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit List",
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
                onClick = { onUpdateList(listName, listDescription) },
                enabled = listName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Update")
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
private fun EmptyListsContent(onCreateList: () -> Unit) {
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
                imageVector = Icons.Default.List,
                contentDescription = "Empty lists",
                tint = CardSubtitleText,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = "No Custom Lists Yet",
                style = MaterialTheme.typography.headlineSmall,
                color = CardTitleText,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Create custom lists to organize your favorite movies and games by genre, mood, or any criteria you like!",
                style = MaterialTheme.typography.bodyLarge,
                color = CardSubtitleText,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Button(
                onClick = onCreateList,
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Your First List")
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
            CircularProgressIndicator(color = BrandOrange)
            Text(
                text = "Loading your lists...",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Error loading lists",
                style = MaterialTheme.typography.titleLarge,
                color = StatusError,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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