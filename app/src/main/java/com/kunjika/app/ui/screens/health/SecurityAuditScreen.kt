package com.kunjika.app.ui.screens.health

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontFamily
import com.kunjika.app.ui.components.GlossyCard
import com.kunjika.app.ui.components.glossyTopShine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kunjika.app.data.repository.DecryptedPasswordItem
import com.kunjika.app.ui.screens.vault.AddEditPasswordDialog
import com.kunjika.app.ui.theme.StrengthFair
import com.kunjika.app.ui.theme.StrengthStrong
import com.kunjika.app.ui.theme.StrengthVeryStrong
import com.kunjika.app.ui.theme.StrengthVeryWeak
import com.kunjika.app.ui.components.glossyBorder
import com.kunjika.app.ui.theme.StrengthWeak
import com.kunjika.app.ui.viewmodel.VaultViewModel

@Composable
fun SecurityAuditScreen(vaultViewModel: VaultViewModel, onViewAuditLog: () -> Unit) {
    val auditSummary by vaultViewModel.auditSummary.collectAsState()
    val isChainValid by vaultViewModel.isChainValid.collectAsState()

    val scrollState = rememberScrollState()

    var selectedItemForEdit by remember { mutableStateOf<DecryptedPasswordItem?>(null) }
    var selectedAuditFilter by remember { mutableStateOf<String?>(null) }

    val scoreColor = when {
        auditSummary.securityScore >= 80 -> StrengthVeryStrong
        auditSummary.securityScore >= 60 -> StrengthFair
        else -> StrengthVeryWeak
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Overall Security Score Card
        GlossyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Vault Security Health",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "100% Offline Cryptographic Analysis",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { auditSummary.securityScore / 100f },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 10.dp,
                        color = scoreColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${auditSummary.securityScore}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            text = "Score",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (auditSummary.totalPasswords == 0) {
                        "No passwords in vault yet. Add passwords to analyze health."
                    } else if (auditSummary.securityScore >= 80) {
                        "Excellent! Your vault is well-protected with strong, unique credentials."
                    } else if (auditSummary.securityScore >= 60) {
                        "Good, but some passwords need attention to enhance security."
                    } else {
                        "Warning: You have weak or reused passwords that need updating."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Blockchain Integrity Card
        GlossyCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            onClick = onViewAuditLog
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isChainValid) Icons.Default.VerifiedUser else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isChainValid) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Blockchain Integrity",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isChainValid) "Audit ledger verified" else "Tampering detected!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2FA Protection Coverage Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "2FA Protection Coverage",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${auditSummary.totpCount} of ${auditSummary.totalPasswords} accounts protected with 2FA",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val totpCoverageRatio = if (auditSummary.totalPasswords > 0)
                    auditSummary.totpCount / auditSummary.totalPasswords.toFloat()
                else 0f

                LinearProgressIndicator(
                    progress = { totpCoverageRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Metrics Grid (Interactive Filters)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AuditMetricCard(
                title = "Weak",
                count = auditSummary.weakPasswords.size,
                icon = Icons.Default.LockOpen,
                tint = StrengthVeryWeak,
                isSelected = selectedAuditFilter == "WEAK",
                onClick = {
                    selectedAuditFilter = if (selectedAuditFilter == "WEAK") null else "WEAK"
                },
                modifier = Modifier.weight(1f)
            )
            AuditMetricCard(
                title = "Reused",
                count = auditSummary.reusedPasswords.size,
                icon = Icons.Default.Repeat,
                tint = StrengthWeak,
                isSelected = selectedAuditFilter == "REUSED",
                onClick = {
                    selectedAuditFilter = if (selectedAuditFilter == "REUSED") null else "REUSED"
                },
                modifier = Modifier.weight(1f)
            )
            AuditMetricCard(
                title = "Old",
                count = auditSummary.oldPasswords.size,
                icon = Icons.Default.History,
                tint = StrengthFair,
                isSelected = selectedAuditFilter == "OLD",
                onClick = {
                    selectedAuditFilter = if (selectedAuditFilter == "OLD") null else "OLD"
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expired Metric Bar (if any expired)
        if (auditSummary.expiredPasswords.isNotEmpty()) {
            AuditMetricCard(
                title = "Expired Rotation Period",
                count = auditSummary.expiredPasswords.size,
                icon = Icons.Default.Warning,
                tint = MaterialTheme.colorScheme.error,
                isSelected = selectedAuditFilter == "EXPIRED",
                onClick = {
                    selectedAuditFilter = if (selectedAuditFilter == "EXPIRED") null else "EXPIRED"
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Filtered or Full Audit Section Lists
        val showAll = selectedAuditFilter == null

        // Expired Passwords List
        if ((showAll || selectedAuditFilter == "EXPIRED") && auditSummary.expiredPasswords.isNotEmpty()) {
            AuditSectionList(
                title = "Expired Passwords (${auditSummary.expiredPasswords.size})",
                subtitle = "These passwords have exceeded their set rotation period",
                items = auditSummary.expiredPasswords,
                icon = Icons.Default.Warning,
                badgeColor = MaterialTheme.colorScheme.error,
                onItemClick = { selectedItemForEdit = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Weak Passwords List
        if ((showAll || selectedAuditFilter == "WEAK") && auditSummary.weakPasswords.isNotEmpty()) {
            AuditSectionList(
                title = "Weak Passwords (${auditSummary.weakPasswords.size})",
                subtitle = "These passwords have low entropy and can be easily cracked",
                items = auditSummary.weakPasswords,
                icon = Icons.Default.Warning,
                badgeColor = StrengthVeryWeak,
                onItemClick = { selectedItemForEdit = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Reused Passwords List
        if ((showAll || selectedAuditFilter == "REUSED") && auditSummary.reusedPasswords.isNotEmpty()) {
            AuditSectionList(
                title = "Reused Passwords (${auditSummary.reusedPasswords.size})",
                subtitle = "Reusing passwords across services creates a critical security risk",
                items = auditSummary.reusedPasswords,
                icon = Icons.Default.Repeat,
                badgeColor = StrengthWeak,
                onItemClick = { selectedItemForEdit = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Old Passwords List
        if ((showAll || selectedAuditFilter == "OLD") && auditSummary.oldPasswords.isNotEmpty()) {
            AuditSectionList(
                title = "Old Passwords (${auditSummary.oldPasswords.size})",
                subtitle = "Consider rotating passwords that haven't been updated in over 90 days",
                items = auditSummary.oldPasswords,
                icon = Icons.Default.History,
                badgeColor = StrengthFair,
                onItemClick = { selectedItemForEdit = it }
            )
        }

        Spacer(modifier = Modifier.height(60.dp))
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
private fun AuditMetricCard(
    title: String,
    count: Int,
    icon: ImageVector,
    tint: Color,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) tint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(1.5.dp, tint) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$count", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AuditSectionList(
    title: String,
    subtitle: String,
    items: List<DecryptedPasswordItem>,
    icon: ImageVector,
    badgeColor: Color,
    onItemClick: (DecryptedPasswordItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    Card(
                        onClick = { onItemClick(item) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = item.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                if (item.username.isNotEmpty()) {
                                    Text(text = item.username, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(text = "Update", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
