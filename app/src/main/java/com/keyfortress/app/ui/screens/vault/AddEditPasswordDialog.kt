package com.keyfortress.app.ui.screens.vault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.keyfortress.app.core.generator.PasswordGenerator
import com.keyfortress.app.core.generator.PasswordGeneratorConfig
import com.keyfortress.app.core.generator.PasswordStrengthEvaluator
import com.keyfortress.app.data.repository.DecryptedPasswordItem
import com.keyfortress.app.ui.components.CustomTextField
import com.keyfortress.app.ui.components.StrengthIndicator
import com.keyfortress.app.ui.viewmodel.VaultViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditPasswordDialog(
    initialPassword: String = "",
    existingItem: DecryptedPasswordItem? = null,
    vaultViewModel: VaultViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(existingItem?.title ?: "") }
    var username by remember { mutableStateOf(existingItem?.username ?: "") }
    var password by remember { mutableStateOf(existingItem?.plaintextPassword ?: initialPassword) }
    var websiteUrl by remember { mutableStateOf(existingItem?.websiteUrl ?: "") }
    var category by remember { mutableStateOf(existingItem?.category ?: "Personal") }
    var notes by remember { mutableStateOf(existingItem?.notes ?: "") }
    var expiryDays by remember { mutableStateOf(existingItem?.expiryDays ?: 0) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val strengthResult = remember(password) { PasswordStrengthEvaluator.evaluate(password) }
    val scrollState = rememberScrollState()

    val availableCategories = listOf("Personal", "Work", "Finance", "Social", "Streaming", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                Text(
                    text = if (existingItem == null) "Save to Vault" else "Edit Entry",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Title / Service Name (e.g. Google, GitHub)",
                    placeholder = "e.g. ProtonMail"
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Username or Email",
                    placeholder = "e.g. user@example.com"
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility"
                                )
                            }
                            IconButton(onClick = {
                                password = PasswordGenerator.generate(PasswordGeneratorConfig(length = 18))
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Quick Generate")
                            }
                        }
                    }
                )

                if (password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    StrengthIndicator(strengthResult = strengthResult)
                }

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = websiteUrl,
                    onValueChange = { websiteUrl = it },
                    label = "Website / App (optional)",
                    placeholder = "https://..."
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCategories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (optional)",
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Rotation Period",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0 to "None", 30 to "30d", 90 to "90d", 180 to "180d").forEach { (days, label) ->
                        FilterChip(
                            selected = expiryDays == days,
                            onClick = { expiryDays = days },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && password.isNotBlank()) {
                                vaultViewModel.savePassword(
                                    id = existingItem?.id ?: 0L,
                                    title = title.trim(),
                                    username = username.trim(),
                                    plainPassword = password,
                                    websiteUrl = websiteUrl.trim(),
                                    category = category,
                                    notes = notes.trim(),
                                    isFavorite = existingItem?.isFavorite ?: false,
                                    expiryDays = expiryDays
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank() && password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
