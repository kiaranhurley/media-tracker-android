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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kiaranhurley.mediatracker.database.entities.User
import com.kiaranhurley.mediatracker.database.entities.CustomList
import com.kiaranhurley.mediatracker.database.entities.ListItem
import com.kiaranhurley.mediatracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomListDetailScreen(
    listId: Int,
    user: User,
    viewModel: CustomListViewModel = hiltViewModel()
) {
    val state by viewModel.customListState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<ListItem?>(null) }

    LaunchedEffect(listId) {
        viewModel.loadList(listId)
    }

    when (state) {
        is CustomListState.Loading, is CustomListState.Idle -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is CustomListState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text((state as CustomListState.Error).message, color = MaterialTheme.colorScheme.error)
            }
        }
        is CustomListState.Detail -> {
            val detail = state as CustomListState.Detail
            Column(
                modifier = Modifier
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
                            text = detail.customList.name,
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

                // List Items
                if (detail.items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No items in this list.", color = CardSubtitleText)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(detail.items) { item ->
                            CustomListItemCard(
                                item = item,
                                onEdit = { showEditDialog = item },
                                onDelete = { viewModel.removeItemFromList(item) }
                            )
                        }
                    }
                }

                // Add Item Button
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = BrandOrange,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item")
                    }
                }
            }

            // Add Item Dialog
            if (showAddDialog) {
                AddEditListItemDialog(
                    onDismiss = { showAddDialog = false },
                    onSubmit = { idString, type ->
                        val id = idString.toIntOrNull() ?: 0
                        viewModel.addItemToList(ListItem(listId = listId, itemId = id, itemType = type))
                        showAddDialog = false
                        viewModel.loadList(listId)
                    }
                )
            }
            // Edit Item Dialog
            showEditDialog?.let { item ->
                AddEditListItemDialog(
                    initialTitle = item.itemId.toString(),
                    initialType = item.itemType,
                    onDismiss = { showEditDialog = null },
                    onSubmit = { idString, type ->
                        val id = idString.toIntOrNull() ?: 0
                        viewModel.removeItemFromList(item)
                        viewModel.addItemToList(item.copy(itemId = id, itemType = type))
                        showEditDialog = null
                        viewModel.loadList(listId)
                    }
                )
            }
        }
        else -> {}
    }
}

@Composable
private fun CustomListItemCard(
    item: ListItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${item.itemType}: ${item.itemId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CardTitleText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun AddEditListItemDialog(
    initialTitle: String = "",
    initialType: String = "FILM",
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var type by remember { mutableStateOf(initialType) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add/Edit Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Item ID") },
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Type:")
                    Spacer(modifier = Modifier.width(8.dp))
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = {},
                        modifier = Modifier.width(120.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Movie") },
                            onClick = { type = "FILM" }
                        )
                        DropdownMenuItem(
                            text = { Text("Game") },
                            onClick = { type = "GAME" }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(title, type) },
                enabled = title.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
} 