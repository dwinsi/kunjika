package com.keyfortress.app.ui.screens.vault

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keyfortress.app.core.generator.PasswordStrengthEvaluator
import com.keyfortress.app.core.security.ClipboardHelper
import com.keyfortress.app.data.repository.DecryptedPasswordItem
import com.keyfortress.app.ui.components.CategoryChip
import com.keyfortress.app.ui.components.CustomTextField
import com.keyfortress.app.ui.theme.StrengthFair
import com.keyfortress.app.ui.theme.StrengthStrong
import com.keyfortress.app.ui.theme.StrengthVeryStrong
import com.keyfortress.app.ui.theme.StrengthVeryWeak
import com.keyfortress.app.ui.theme.StrengthWeak
import com.keyfortress.app.ui.viewmodel.VaultViewModel

@Composable
fun VaultScreen(vaultViewModel: VaultViewModel) {
    val filteredPasswords by vaultViewModel.filteredPasswords.collectAsState()
    val selectedCategory by vaultViewModel.selectedCategory.collectAsState()
    val searchQuery by vaultViewModel.searchQuery.collectAsState()

    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(value = false) }
    var selectedItemForDetail by remember { mutableStateOf<DecryptedPasswordItem?>(null) }
    var selectedItemForEdit by remember { mutableStateOf<DecryptedPasswordItem?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Password")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            CustomTextField(
                value = searchQuery,
                onValueChange = { vaultViewModel.onSearchQueryChanged(it) },
                label = "Search vault",
                placeholder = "Search accounts, sites, emails...",
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { vaultViewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Category Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                vaultViewModel.categories.forEach { category ->
                    CategoryChip(
                        label = category,
                        isSelected = selectedCategory == category,
                        onClick = { vaultViewModel.selectCategory(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password List or Empty State
            if (filteredPasswords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No results found" else "Vault is empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different search keyword" else "Tap '+' below to add your first password",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPasswords, key = { it.id }) { item ->
                        PasswordCardItem(
                            item = item,
                            onClick = { selectedItemForDetail = item },
                            onToggleFavorite = { vaultViewModel.toggleFavorite(item) }
                        ) {
                            ClipboardHelper.copyToClipboard(
                                context = context,
                                label = item.title,
                                text = item.plaintextPassword,
                                isSensitive = true,
                                autoClearSeconds = 30L
                            )
                            Toast.makeText(context, "${item.title} password copied (auto-clears in 30s)", Toast.LENGTH_SHORT).show()
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddEditPasswordDialog(
            vaultViewModel = vaultViewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    selectedItemForDetail?.let { item ->
        PasswordDetailDialog(
            item = item,
            onEdit = {
                selectedItemForDetail = null
                selectedItemForEdit = item
            },
            onDelete = { vaultViewModel.deletePassword(item.id) },
            onToggleFavorite = { vaultViewModel.toggleFavorite(item) },
            onDismiss = { selectedItemForDetail = null }
        )
    }

    selectedItemForEdit?.let { item ->
        AddEditPasswordDialog(
            existingItem = item,
            vaultViewModel = vaultViewModel,
            onDismiss = { selectedItemForEdit = null }
        )
    }
}

@Composable
private fun PasswordCardItem(
    item: DecryptedPasswordItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCopyPassword: () -> Unit
) {
    val strength = remember(item.plaintextPassword) {
        PasswordStrengthEvaluator.evaluate(item.plaintextPassword)
    }

    val strengthColor = when (strength.score) {
        com.keyfortress.app.core.generator.PasswordStrength.VERY_WEAK -> StrengthVeryWeak
        com.keyfortress.app.core.generator.PasswordStrength.WEAK -> StrengthWeak
        com.keyfortress.app.core.generator.PasswordStrength.FAIR -> StrengthFair
        com.keyfortress.app.core.generator.PasswordStrength.STRONG -> StrengthStrong
        com.keyfortress.app.core.generator.PasswordStrength.VERY_STRONG -> StrengthVeryStrong
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Category Icon Badge & Info
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.username.isNotEmpty()) {
                        Text(
                            text = item.username,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(strengthColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onCopyPassword) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Password",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
