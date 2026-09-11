package com.sect.idle.hub.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sect.idle.R
import com.sect.idle.hub.*

@Composable
fun WorldMapTab(
    state: SectHubState,
    viewModel: SectHubViewModel,
    modifier: Modifier = Modifier
) {
    var showDispatchDialogForRegion by remember { mutableStateOf<MapRegion?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // 3D Open World Immortal Realm Panorama Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("world_map_banner"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TwilightSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(JadeCyan, SpiritPurple, CelestialGold)))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_xianxia_openworld),
                            contentDescription = "3D Open World Immortal Realm",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            TwilightSurface.copy(alpha = 0.5f),
                                            TwilightSurface
                                        )
                                    )
                                )
                        )
                        Surface(
                            modifier = Modifier
                                .padding(12.dp)
                                .align(Alignment.TopEnd),
                            shape = RoundedCornerShape(8.dp),
                            color = TwilightElevated.copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, JadeCyan)
                        ) {
                            Text(
                                text = "3D OPEN REALM",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = JadeCyan,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(SpiritPurple.copy(alpha = 0.25f))
                                        .border(2.dp, SpiritPurple, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🗺️", fontSize = 18.sp)
                                }
                                Column {
                                    Text("Immortal Domain 3D Open World", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("Dispatch disciples to forage spirit veins, explore ancient grottos, and harvest heavenly treasures.", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val activeCount = state.worldMap.regions.count { it.isExpeditionActive }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active Expeditions: $activeCount/${state.worldMap.maxExpeditions}", fontSize = 12.sp, color = JadeCyan, fontWeight = FontWeight.Bold)
                            Text("Explored Territories: ${state.worldMap.regions.count { it.isUnlocked }}/${state.worldMap.regions.size}", fontSize = 12.sp, color = CelestialAmber)
                        }
                    }
                }
            }
        }

        // Section: Territory Regions
        item {
            Text("Territory Regions & Exploration Grottos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(state.worldMap.regions, key = { it.id }) { region ->
            RegionCard(
                region = region,
                onDispatch = { showDispatchDialogForRegion = region },
                onClaimLoot = { viewModel.claimExpeditionLoot(region.id) }
            )
        }

        // Section: Exploration Chronicle Log
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text("📜 Exploration Chronicles & Discoveries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TwilightSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.worldMap.recentExplorationLogs.isEmpty()) {
                        Text(
                            "No recent expeditions recorded. Dispatch disciples to discover spirit veins, rare herb blooms, and ancient grottos.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    } else {
                        state.worldMap.recentExplorationLogs.forEach { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✦", color = JadeCyan, fontSize = 12.sp)
                                Text(log, fontSize = 12.sp, color = CloudMistWhite)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to pick disciples for expedition
    showDispatchDialogForRegion?.let { region ->
        DispatchExpeditionModal(
            region = region,
            disciples = state.disciples,
            onDismiss = { showDispatchDialogForRegion = null },
            onConfirmDispatch = { selectedIds ->
                viewModel.dispatchExpedition(region.id, selectedIds)
                showDispatchDialogForRegion = null
            }
        )
    }
}

@Composable
fun RegionCard(
    region: MapRegion,
    onDispatch: () -> Unit,
    onClaimLoot: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("region_card_${region.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TwilightSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (region.hasLootToClaim) CelestialGold else if (region.isExpeditionActive) JadeCyan else TwilightBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Emoji, Name, Chinese Name, Danger Level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(region.iconEmoji, fontSize = 24.sp)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(region.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(region.chineseName, fontSize = 11.sp, color = JadeCyan)
                        }
                        Text("Danger: " + "★".repeat(region.dangerLevel) + "☆".repeat(5 - region.dangerLevel), fontSize = 11.sp, color = if (region.dangerLevel >= 4) DangerRed else CelestialAmber)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TwilightElevated
                ) {
                    Text(
                        text = if (!region.isUnlocked) "🔒 Locked" else if (region.hasLootToClaim) "🎁 LOOT READY" else if (region.isExpeditionActive) "⏳ Exploring" else "Idle",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = if (region.hasLootToClaim) CelestialGold else if (region.isExpeditionActive) JadeCyan else TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(region.description, style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(10.dp))

            // Resource Yield Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = TwilightElevated
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Resource Yield:", fontSize = 11.sp, color = TextMuted)
                    Text(region.resourceYieldDescription, fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                }
            }

            // Expedition Active Progress Bar
            if (region.isExpeditionActive) {
                Spacer(modifier = Modifier.height(10.dp))
                val totalTime = region.expeditionTotalTimeSec.coerceAtLeast(1)
                val remaining = region.expeditionTimeRemainingSec
                val progress = 1f - (remaining.toFloat() / totalTime)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Expedition in progress (${region.assignedDiscipleNames.joinToString(", ")})", fontSize = 11.sp, color = JadeCyan)
                        Text("${remaining}s remaining", fontSize = 11.sp, color = CelestialAmber, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = JadeCyan,
                        trackColor = TwilightElevated
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!region.isUnlocked) {
                    Text("Requires higher Sect Realm", fontSize = 11.sp, color = TextMuted)
                } else if (region.hasLootToClaim) {
                    Button(
                        onClick = onClaimLoot,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CelestialGold, contentColor = Color.Black),
                        modifier = Modifier.testTag("claim_loot_button_${region.id}")
                    ) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Claim Expedition Loot", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else if (!region.isExpeditionActive) {
                    FilledTonalButton(
                        onClick = onDispatch,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = JadeCyan, contentColor = Color.Black),
                        modifier = Modifier.testTag("dispatch_button_${region.id}")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispatch Expedition", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DispatchExpeditionModal(
    region: MapRegion,
    disciples: List<DiscipleUiModel>,
    onDismiss: () -> Unit,
    onConfirmDispatch: (List<String>) -> Unit
) {
    val selectedIds = remember { mutableStateListOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Dispatch Expedition Party", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Target: ${region.name} (${region.chineseName})", fontSize = 12.sp, color = JadeCyan)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select up to 3 cultivators for this expedition:", fontSize = 12.sp, color = TextMuted)

                disciples.forEach { disciple ->
                    val isSelected = selectedIds.contains(disciple.id)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    selectedIds.remove(disciple.id)
                                } else if (selectedIds.size < 3) {
                                    selectedIds.add(disciple.id)
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) JadeDark.copy(alpha = 0.4f) else TwilightElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) JadeCyan else TwilightBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(disciple.name, fontWeight = FontWeight.Bold)
                                Text("${disciple.realmName} · ⚔️ ${disciple.combatPower} CP", fontSize = 11.sp, color = JadeCyan)
                            }
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked && selectedIds.size < 3) {
                                        selectedIds.add(disciple.id)
                                    } else if (!checked) {
                                        selectedIds.remove(disciple.id)
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = JadeCyan, checkmarkColor = Color.Black)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmDispatch(selectedIds.toList()) },
                enabled = selectedIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = JadeCyan, contentColor = Color.Black)
            ) {
                Text("Dispatch (${selectedIds.size})", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = TwilightSurface,
        shape = RoundedCornerShape(18.dp)
    )
}
