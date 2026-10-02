package com.sect.idle.hub.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.sect.idle.gameplay.AchievementMilestone
import com.sect.idle.gameplay.MilestoneCategory
import com.sect.idle.hub.*

@Composable
fun AchievementsDialog(
    milestones: List<AchievementMilestone>,
    onClaimMilestone: (String) -> Unit,
    onClaimAll: () -> Unit,
    onDismissRequest: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(MilestoneCategory.ALL) }

    val filteredMilestones = remember(milestones, selectedCategory) {
        if (selectedCategory == MilestoneCategory.ALL) {
            milestones
        } else {
            milestones.filter { it.category == selectedCategory }
        }
    }

    val claimableCount = remember(milestones) {
        milestones.count { it.isCompleted && !it.isClaimed }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("achievements_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = TwilightSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Brush.verticalGradient(listOf(CelestialGold, JadeCyan)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CelestialGold.copy(alpha = 0.2f))
                                .border(1.5.dp, CelestialGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 20.sp)
                        }
                        Column {
                            Text("Heavenly Milestones & Trophies", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CloudMistWhite)
                            Text("${milestones.count { it.isClaimed }}/${milestones.size} Completed · $claimableCount Ready to Claim", fontSize = 11.sp, color = CelestialGold)
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_achievements_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips & Claim All Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(MilestoneCategory.values()) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text("${cat.iconEmoji} ${cat.label}", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JadeDark.copy(alpha = 0.5f),
                                    selectedLabelColor = JadeCyan,
                                    containerColor = TwilightElevated
                                )
                            )
                        }
                    }

                    if (claimableCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onClaimAll,
                            colors = ButtonDefaults.buttonColors(containerColor = CelestialGold),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("claim_all_milestones_btn")
                        ) {
                            Text("Claim All ($claimableCount)", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Milestone list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredMilestones, key = { it.id }) { milestone ->
                        MilestoneCard(
                            milestone = milestone,
                            onClaim = { onClaimMilestone(milestone.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneCard(
    milestone: AchievementMilestone,
    onClaim: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("milestone_card_${milestone.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (milestone.isClaimed) TwilightSurface.copy(alpha = 0.6f) else TwilightElevated
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (milestone.isCompleted && !milestone.isClaimed) CelestialGold else TwilightBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (milestone.isClaimed) SuccessGreen.copy(alpha = 0.2f) else TwilightSurface)
                            .border(1.dp, if (milestone.isClaimed) SuccessGreen else CelestialGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(milestone.iconEmoji, fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = milestone.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (milestone.isClaimed) TextMuted else CloudMistWhite
                        )
                        Text(
                            text = milestone.description,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Status or Claim Button
                when {
                    milestone.isClaimed -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Claimed", tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                Text("Claimed", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    milestone.isCompleted -> {
                        Button(
                            onClick = onClaim,
                            colors = ButtonDefaults.buttonColors(containerColor = CelestialGold),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("claim_btn_${milestone.id}")
                        ) {
                            Text("Claim Reward", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    else -> {
                        Text(
                            text = "${milestone.currentValue}/${milestone.targetValue}",
                            fontSize = 11.sp,
                            color = JadeCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { milestone.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (milestone.isClaimed) SuccessGreen else if (milestone.isCompleted) CelestialGold else JadeCyan,
                trackColor = VoidBlack
            )

            // Rewards display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rewards:", fontSize = 10.sp, color = TextMuted)
                if (milestone.rewardStones > 0) {
                    RewardTag("🪙 +${milestone.rewardStones}", CelestialGold)
                }
                if (milestone.rewardJade > 0) {
                    RewardTag("💎 +${milestone.rewardJade}", JadeCyan)
                }
                if (milestone.rewardPills > 0) {
                    RewardTag("💊 +${milestone.rewardPills}", LotusPink)
                }
                milestone.rewardItemName?.let { item ->
                    RewardTag("🎁 $item", SpiritPurple)
                }
            }
        }
    }
}

@Composable
private fun RewardTag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
