package com.kunjika.app.ui.screens.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kunjika.app.data.local.blockchain.BlockEntity
import com.kunjika.app.ui.components.GlossyCard
import com.kunjika.app.ui.components.glossyBorder
import com.kunjika.app.ui.components.glossyTopShine
import com.kunjika.app.ui.viewmodel.VaultViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(vaultViewModel: VaultViewModel, onBack: () -> Unit) {
    val ledger by vaultViewModel.blockchainLedger.collectAsState()
    val isChainValid by vaultViewModel.isChainValid.collectAsState()
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blockchain Audit Ledger", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { vaultViewModel.verifyIntegrity() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Verify")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Integrity Status Banner
            GlossyCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isChainValid) Icons.Default.VerifiedUser else Icons.Default.GppBad,
                        contentDescription = null,
                        tint = if (isChainValid) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (isChainValid) "Chain Integrity Verified" else "Tampering Detected!",
                            fontWeight = FontWeight.Bold,
                            color = if (isChainValid) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (isChainValid) "All blocks are cryptographically linked and signed by this device." 
                                   else "The ledger has been modified externally or a block signature is invalid.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = "Ledger Height: ${ledger.size} Blocks",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ledger.reversed()) { block ->
                    BlockItem(block, dateFormat)
                }
            }
        }
    }
}

@Composable
fun BlockItem(block: BlockEntity, dateFormat: SimpleDateFormat) {
    GlossyCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                color = when (block.action) {
                                    "CREATE" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    "UPDATE" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    "DELETE" -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    "PIN_CREATE" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    "PIN_CHANGE" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    "VAULT_IMPORT" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                    "VAULT_EXPORT" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    "BIOMETRIC_TOGGLE" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                    "RECOVERY_KIT_GENERATE" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    "EXPORT_BLE" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .glossyBorder(shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (block.action) {
                                "CREATE" -> Icons.Default.Add
                                "UPDATE" -> Icons.Default.Edit
                                "DELETE" -> Icons.Default.Delete
                                "PIN_CREATE" -> Icons.Default.VpnKey
                                "PIN_CHANGE" -> Icons.Default.Key
                                "VAULT_IMPORT" -> Icons.Default.Download
                                "VAULT_EXPORT" -> Icons.Default.Upload
                                "BIOMETRIC_TOGGLE" -> Icons.Default.Fingerprint
                                "RECOVERY_KIT_GENERATE" -> Icons.Default.Description
                                "EXPORT_BLE" -> Icons.Default.Share
                                else -> Icons.Default.Shield
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = when (block.action) {
                                "CREATE" -> MaterialTheme.colorScheme.tertiary
                                "UPDATE" -> MaterialTheme.colorScheme.primary
                                "DELETE" -> MaterialTheme.colorScheme.error
                                "PIN_CREATE" -> MaterialTheme.colorScheme.tertiary
                                "PIN_CHANGE" -> MaterialTheme.colorScheme.secondary
                                "VAULT_IMPORT" -> MaterialTheme.colorScheme.tertiary
                                "VAULT_EXPORT" -> MaterialTheme.colorScheme.primary
                                "BIOMETRIC_TOGGLE" -> MaterialTheme.colorScheme.secondary
                                "RECOVERY_KIT_GENERATE" -> MaterialTheme.colorScheme.primary
                                "EXPORT_BLE" -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Block #${block.id}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    text = block.action,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow("Timestamp", dateFormat.format(Date(block.timestamp)))
            DetailRow("Content Hash", block.contentHash)
            DetailRow("Prev Hash", block.previousHash)
            DetailRow("Signature", block.signature)
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
