package com.sect.idle.hub.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sect.idle.gameplay.EncounterChoice
import com.sect.idle.gameplay.SectEncounter
import com.sect.idle.hub.*

@Composable
fun EncounterModal(
    encounter: SectEncounter,
    state: SectHubState,
    onSelectChoice: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .testTag("encounter_modal_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = TwilightSurface),
            border = androidx.compose.foundation.BorderStroke(2.dp, Brush.verticalGradient(listOf(LotusPink, CelestialGold)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with lore and emoji
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(LotusPink.copy(alpha = 0.25f))
                                .border(1.5.dp, LotusPink, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(encounter.iconEmoji, fontSize = 24.sp)
                        }
                        Column {
                            Text(
                                text = "⚡ Sect Event: ${encounter.title}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CelestialGold
                            )
                            Text(
                                text = "${encounter.chineseTitle} · Strategic Decision Required",
                                fontSize = 12.sp,
                                color = JadeCyan,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_encounter_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // Lore Narrative
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = TwilightElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
                ) {
                    Text(
                        text = encounter.description,
                        modifier = Modifier.padding(14.dp),
                        fontSize = 13.sp,
                        color = CloudMistWhite,
                        lineHeight = 18.sp
                    )
                }

                Text(
                    text = "Grandmaster's Decree (Select Course of Action):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CelestialGold
                )

                // Choices list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    encounter.choices.forEach { choice ->
                        val canAffordStones = state.spiritStones >= choice.requiresSpiritStones
                        val canAffordHerbs = state.spiritHerbs >= choice.requiresSpiritHerbs
                        val canAffordOres = state.spiritOres >= choice.requiresSpiritOres
                        val canAfford = canAffordStones && canAffordHerbs && canAffordOres

                        EncounterChoiceCard(
                            choice = choice,
                            canAfford = canAfford,
                            onClick = {
                                if (canAfford) {
                                    onSelectChoice(choice.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EncounterChoiceCard(
    choice: EncounterChoice,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = canAfford) { onClick() }
            .testTag("encounter_choice_${choice.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (canAfford) TwilightElevated else VoidBlack.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (canAfford) JadeCyan.copy(alpha = 0.6f) else DangerRed.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = choice.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (canAfford) CloudMistWhite else TextMuted
                )
                if (!canAfford) {
                    Text(
                        text = "Insufficient Resources",
                        fontSize = 10.sp,
                        color = DangerRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = choice.description,
                fontSize = 11.sp,
                color = TextMuted
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cost: ${choice.costDescription}",
                    fontSize = 11.sp,
                    color = if (canAfford) DangerRed else TextMuted,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Reward: ${choice.rewardDescription}",
                    fontSize = 11.sp,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
