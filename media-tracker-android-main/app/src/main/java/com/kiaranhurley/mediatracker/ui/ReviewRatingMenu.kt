package com.kiaranhurley.mediatracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kiaranhurley.mediatracker.ui.theme.*

@Composable
fun ReviewRatingMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePrivacy: () -> Unit,
    isPrivate: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = CardSubtitleText,
                modifier = Modifier.size(16.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(200.dp)
        ) {
            DropdownMenuItem(
                onClick = {
                    onEdit()
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = CardBodyText
                    )
                },
                text = {
                    Text(
                        text = "Edit",
                        color = CardBodyText
                    )
                }
            )
            
            DropdownMenuItem(
                onClick = {
                    onTogglePrivacy()
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        if (isPrivate) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = CardBodyText
                    )
                },
                text = {
                    Text(
                        text = if (isPrivate) "Make Public" else "Make Private",
                        color = CardBodyText
                    )
                }
            )
            
            HorizontalDivider(color = CardSubtitleText)
            
            DropdownMenuItem(
                onClick = {
                    onDelete()
                    expanded = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = StatusError
                    )
                },
                text = {
                    Text(
                        text = "Delete",
                        color = StatusError
                    )
                }
            )
        }
    }
} 