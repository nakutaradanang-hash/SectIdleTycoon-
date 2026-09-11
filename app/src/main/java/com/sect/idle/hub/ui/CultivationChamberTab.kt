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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sect.idle.R
import com.sect.idle.hub.*

@Composable
fun CultivationChamberTab(
    state: SectHubState,
    viewModel: SectHubViewModel,
    modifier: Modifier = Modifier
) {
    var showSeatDiscipleDialogForSlot by remember { mutableStateOf<Int?>(null) }
    var selectedBreakthroughDiscipleId by remember { mutableStateOf<String?>(null) }

    val readyDisciples = remember(state.disciples) {
        state.disciples.filter { it.isReadyForBreakthrough }
    }

    // Auto-select first ready disciple if available
    LaunchedEffect(readyDisciples) {
        if (selectedBreakthroughDiscipleId == null && readyDisciples.isNotEmpty()) {
            selectedBreakthroughDiscipleId = readyDisciples.first().id
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Grand 3D Qi Gathering & Alchemy Crucible Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("qi_array_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TwilightSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(JadeCyan, SpiritPurple, CelestialGold)))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_alchemy_chamber),
                            contentDescription = "3D Sacred Alchemy & Qi Chamber",
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
                                            TwilightSurface.copy(alpha = 0.6f),
                                            TwilightSurface
                                        )
                                    )
                                )
                        )
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
                                        .background(JadeDark.copy(alpha = 0.4f))
                                        .border(2.dp, JadeCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⚡", fontSize = 20.sp)
                                }
                                Column {
                                    Text("Nine Dragons Qi Array", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("Array Tier ${state.cultivationChamber.arrayLevel} · Qi Density +${state.cultivationChamber.qiDensityBonusPercent}%", fontSize = 12.sp, color = JadeCyan)
                                }
                            }

                            Button(
                                onClick = { viewModel.upgradeCultivationArray() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JadeCyan, contentColor = Color.Black),
                                modifier = Modifier.testTag("upgrade_array_button")
                            ) {
                                Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upgrade", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Upgrade Cost: 🪙 ${state.cultivationChamber.arrayUpgradeCost} Stones", fontSize = 11.sp, color = TextMuted)
                            Text("Meditation Multiplier: x${1f + state.cultivationChamber.qiDensityBonusPercent / 100f}f", fontSize = 11.sp, color = CelestialAmber, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Quick Ambient Qi Harvester
        item {
            FilledTonalButton(
                onClick = { viewModel.gatherAmbientQi() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("gather_qi_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = TwilightElevated, contentColor = CloudMistWhite)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CelestialGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Channel Ambient Qi (+${25 + state.cultivationChamber.arrayLevel * 10} Spirit Stones)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Section: Meditation Cushion Seats
        item {
            Text("🧘 Meditation Cushion Seats", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Assign disciples to meditate on sacred spirit cushions to rapidly absorb cultivation Qi.", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }

        items(state.cultivationChamber.meditationSlots, key = { it.slotIndex }) { slot ->
            MeditationSlotCard(
                slot = slot,
                onSeat = { showSeatDiscipleDialogForSlot = slot.slotIndex },
                onVacate = { viewModel.vacateMeditationSlot(slot.slotIndex) },
                onUnlock = { viewModel.unlockMeditationSlot(slot.slotIndex) }
            )
        }

        // Section: Heavenly Breakthrough Crucible
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("⚡ Heavenly Breakthrough Crucible", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Assist disciples at the bottleneck of their cultivation realm to overcome tribulations and ascend.", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }

        item {
            val candidateDisciples = remember(state.disciples) {
                state.disciples.sortedByDescending { it.realmExp.toFloat() / it.realmMaxExp.coerceAtLeast(1) }
            }
            val activeDisciple = candidateDisciples.firstOrNull { it.id == selectedBreakthroughDiscipleId }
                ?: candidateDisciples.firstOrNull()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("breakthrough_crucible_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = TwilightSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (candidateDisciples.isEmpty()) {
                        Text("No disciples in sect.", color = TextMuted)
                    } else {
                        // Candidate Picker Row
                        Text("Select Cultivator for Ascension:", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            candidateDisciples.take(4).forEach { disc ->
                                val isSel = disc.id == activeDisciple?.id
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedBreakthroughDiscipleId = disc.id },
                                    label = {
                                        Text(
                                            "${disc.name} (${(disc.realmExp * 100f / disc.realmMaxExp.coerceAtLeast(1)).toInt()}%)",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (disc.isReadyForBreakthrough) CelestialGold else JadeCyan,
                                        selectedLabelColor = Color.Black,
                                        containerColor = TwilightElevated,
                                        labelColor = CloudMistWhite
                                    )
                                )
                            }
                        }

                        activeDisciple?.let { disc ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = TwilightElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (disc.isReadyForBreakthrough) CelestialGold else TwilightBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(disc.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text("Current Realm: ${disc.realmName}", fontSize = 12.sp, color = JadeCyan)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (disc.isReadyForBreakthrough) CelestialGold else TwilightSurface
                                        ) {
                                            Text(
                                                text = if (disc.isReadyForBreakthrough) "⚡ BOTTLE-NECK REACHED" else "Cultivating (${disc.realmExp}/${disc.realmMaxExp})",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                color = if (disc.isReadyForBreakthrough) Color.Black else TextMuted,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val progress = (disc.realmExp.toFloat() / disc.realmMaxExp.coerceAtLeast(1)).coerceIn(0f, 1f)
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = if (disc.isReadyForBreakthrough) CelestialGold else JadeCyan,
                                        trackColor = TwilightSurface
                                    )
                                }
                            }

                            // Spirit Pill Booster Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(TwilightElevated)
                                    .clickable { viewModel.togglePillBoost() }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("💊", fontSize = 18.sp)
                                    Column {
                                        Text("Infuse Spirit Pill (+20% Success Chance)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("Available in treasury: 💊 ${state.spiritPills} Pills", fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                                Switch(
                                    checked = state.cultivationChamber.pillBoostActive && state.spiritPills > 0,
                                    onCheckedChange = { viewModel.togglePillBoost() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CelestialGold,
                                        checkedTrackColor = JadeDark
                                    )
                                )
                            }

                            // Success Probability Matrix
                            val baseChance = state.cultivationChamber.baseBreakthroughChance
                            val pillBonus = if (state.cultivationChamber.pillBoostActive && state.spiritPills > 0) state.cultivationChamber.pillBonusChance else 0
                            val totalChance = (baseChance + pillBonus).coerceIn(0, 100)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Estimated Ascension Rate:", fontSize = 12.sp, color = TextMuted)
                                Text("$totalChance% Success Probability", fontSize = 13.sp, color = if (totalChance >= 80) SuccessGreen else CelestialAmber, fontWeight = FontWeight.Bold)
                            }

                            // Big Action Button
                            val expRatio = (disc.realmExp.toFloat() / disc.realmMaxExp.coerceAtLeast(1)).coerceIn(0f, 1f)
                            Button(
                                onClick = { viewModel.attemptChamberBreakthrough(disc.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("breakthrough_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (disc.isReadyForBreakthrough) CelestialGold else TwilightElevated,
                                    contentColor = if (disc.isReadyForBreakthrough) Color.Black else TextMuted
                                ),
                                enabled = disc.isReadyForBreakthrough
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (disc.isReadyForBreakthrough) "⚡ INVOKE HEAVENLY TRIBULATION ASCENSION" else "Accumulate More Qi (${(expRatio * 100).toInt()}%)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            // Log readout
                            if (state.cultivationChamber.lastBreakthroughLog.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = TwilightElevated
                                ) {
                                    Text(
                                        text = state.cultivationChamber.lastBreakthroughLog,
                                        modifier = Modifier.padding(10.dp),
                                        fontSize = 11.sp,
                                        color = if (state.cultivationChamber.lastBreakthroughLog.contains("TRIUMPH")) SuccessGreen else CelestialAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to pick disciple for empty meditation slot
    showSeatDiscipleDialogForSlot?.let { slotIndex ->
        AlertDialog(
            onDismissRequest = { showSeatDiscipleDialogForSlot = null },
            title = { Text("Select Disciple to Meditate", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.disciples.forEach { disciple ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.seatDiscipleInMeditation(slotIndex, disciple.id)
                                    showSeatDiscipleDialogForSlot = null
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = TwilightElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(disciple.name, fontWeight = FontWeight.Bold)
                                    Text(disciple.realmName, fontSize = 11.sp, color = JadeCyan)
                                }
                                Text("Current: ${disciple.taskName}", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSeatDiscipleDialogForSlot = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = TwilightSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun MeditationSlotCard(
    slot: MeditationSlot,
    onSeat: () -> Unit,
    onVacate: () -> Unit,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TwilightSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (slot.assignedDiscipleId != null) JadeCyan else TwilightBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (slot.isUnlocked) TwilightElevated else VoidBlack)
                        .border(1.dp, if (slot.isUnlocked) JadeCyan else TwilightBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (!slot.isUnlocked) "🔒" else if (slot.assignedDiscipleId != null) "🧘" else "#${slot.slotIndex + 1}",
                        fontSize = 14.sp
                    )
                }

                Column {
                    Text(
                        text = if (!slot.isUnlocked) "Locked Cushion #${slot.slotIndex + 1}" else slot.assignedDiscipleName ?: "Empty Cushion #${slot.slotIndex + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (slot.assignedDiscipleId != null) JadeCyan else CloudMistWhite
                    )
                    Text(
                        text = if (!slot.isUnlocked) "Cost: 🪙 ${slot.unlockCost} Stones" else if (slot.assignedDiscipleId != null) "Realm: ${slot.assignedDiscipleRealm} · Rate: x${slot.qiGainMultiplier} Qi" else "Tap 'Seat Cultivator' to begin meditation",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            if (!slot.isUnlocked) {
                Button(
                    onClick = onUnlock,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CelestialGold, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Unlock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else if (slot.assignedDiscipleId != null) {
                OutlinedButton(
                    onClick = onVacate,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TextMuted),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Vacate", fontSize = 11.sp)
                }
            } else {
                FilledTonalButton(
                    onClick = onSeat,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = JadeCyan, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Seat Cultivator", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
