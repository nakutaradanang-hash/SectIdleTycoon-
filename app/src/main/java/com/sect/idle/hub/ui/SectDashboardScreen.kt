package com.sect.idle.hub.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sect.idle.hub.DiscipleUiModel
import com.sect.idle.hub.SectHubTab
import com.sect.idle.hub.SectHubViewModel
import com.sect.idle.systems.NumberFormatter

/**
 * SectDashboardScreen - Material 3 Dashboard displaying core sect resources,
 * tick-based generation rates, disciple rosters, and quick cultivation actions.
 */
@Composable
fun SectDashboardScreen(
    viewModel: SectHubViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTab: (SectHubTab) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("sect_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sect Prestige & Realm Header
        item {
            SectPrestigeHeaderCard(
                sectName = state.sectName,
                realmName = state.sectRealmName,
                realmExp = state.sectRealmExp,
                realmMaxExp = state.sectRealmMaxExp,
                power = state.sectPower,
                rank = state.sectRank
            )
        }

        // 2. Core Resource Dashboard (Material 3 Cards)
        item {
            Text(
                text = "Core Sect Resources",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        item {
            ResourceGridSection(
                spiritStones = state.spiritStones,
                spiritHerbs = state.spiritHerbs,
                spiritOres = state.spiritOres,
                spiritPills = state.spiritPills,
                jade = state.jade,
                essence = state.essence,
                dailyNetStones = state.economy.netDailySS
            )
        }

        // 3. Tick-based Production & Operational Rates
        item {
            IdleProductionOverviewCard(
                economy = state.economy,
                discipleCount = state.disciples.size,
                onViewDisciples = { onNavigateToTab(SectHubTab.DISCIPLES) }
            )
        }

        // 4. Quick Action Hub
        item {
            QuickActionsCard(
                onGatherQi = { viewModel.gatherAmbientQi() },
                onRecruitDisciple = { viewModel.recruitNewDisciple() },
                onOpenChamber = { onNavigateToTab(SectHubTab.CULTIVATION) },
                onOpenWorldMap = { onNavigateToTab(SectHubTab.WORLD_MAP) }
            )
        }

        // 5. Active Cultivator Roster Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cultivator Disciples (${state.disciples.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                OutlinedButton(
                    onClick = { onNavigateToTab(SectHubTab.DISCIPLES) },
                    modifier = Modifier.testTag("view_all_disciples_button")
                ) {
                    Text("Manage All", fontSize = 12.sp)
                }
            }
        }

        if (state.disciples.isEmpty()) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏯",
                            fontSize = 36.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No disciples recruited yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.recruitNewDisciple() },
                            modifier = Modifier.testTag("recruit_first_disciple_btn")
                        ) {
                            Text("Recruit Junior Disciple (200 Stones)")
                        }
                    }
                }
            }
        } else {
            items(state.disciples.take(5)) { disciple ->
                DiscipleDashboardCard(disciple = disciple)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectPrestigeHeaderCard(
    sectName: String,
    realmName: String,
    realmExp: Int,
    realmMaxExp: Int,
    power: Long,
    rank: Int
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sect_prestige_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sectName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Grand Xianxia Cultivation Sect",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "Rank #$rank",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Sect Realm",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = realmName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Combat Power",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "⚔️ ${NumberFormatter.format(power)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val progress = (realmExp.toFloat() / realmMaxExp.coerceAtLeast(1)).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Dao Ascension Progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$realmExp / $realmMaxExp Exp",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ResourceGridSection(
    spiritStones: Long,
    spiritHerbs: Long,
    spiritOres: Long,
    spiritPills: Long,
    jade: Long,
    essence: Long,
    dailyNetStones: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResourceCard(
                name = "Spirit Stones",
                amount = spiritStones,
                icon = "🪙",
                yieldText = if (dailyNetStones >= 0) "+$dailyNetStones/day" else "$dailyNetStones/day",
                containerColor = Color(0xFF1E293B),
                accentColor = Color(0xFFFFD54F),
                modifier = Modifier.weight(1f)
            )

            ResourceCard(
                name = "Spirit Herbs",
                amount = spiritHerbs,
                icon = "🌿",
                yieldText = "Medicinal Herb Garden",
                containerColor = Color(0xFF143026),
                accentColor = Color(0xFF81C784),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResourceCard(
                name = "Spirit Ores",
                amount = spiritOres,
                icon = "⛏️",
                yieldText = "Smelting & Crafting",
                containerColor = Color(0xFF2C2416),
                accentColor = Color(0xFFFFB74D),
                modifier = Modifier.weight(1f)
            )

            ResourceCard(
                name = "Spirit Pills",
                amount = spiritPills,
                icon = "💊",
                yieldText = "Breakthrough Elixirs",
                containerColor = Color(0xFF2B1D3A),
                accentColor = Color(0xFFBA68C8),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResourceCard(
                name = "Spirit Jade",
                amount = jade,
                icon = "💎",
                yieldText = "Rare Divine Currency",
                containerColor = Color(0xFF132D38),
                accentColor = Color(0xFF4DD0E1),
                modifier = Modifier.weight(1f)
            )

            ResourceCard(
                name = "Celestial Essence",
                amount = essence,
                icon = "✨",
                yieldText = "Array & Forge Fuel",
                containerColor = Color(0xFF331D24),
                accentColor = Color(0xFFFF8A80),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ResourceCard(
    name: String,
    amount: Long,
    icon: String,
    yieldText: String,
    containerColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .testTag("resource_card_${name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(10.dp)
                ) {}
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = NumberFormatter.format(amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )

            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = yieldText,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun IdleProductionOverviewCard(
    economy: com.sect.idle.hub.SectEconomyState,
    discipleCount: Int,
    onViewDisciples: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("idle_production_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙️ Active Sect Operations",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$discipleCount Working",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProductionPill("🌾 Farming", economy.activeFarmers, "+Herbs")
                ProductionPill("⛏️ Mining", economy.activeMiners, "+Ores")
                ProductionPill("⚗️ Alchemy", economy.activeAlchemists, "+Pills")
                ProductionPill("🧘 Meditation", economy.activeCultivators, "+Qi")
            }
        }
    }
}

@Composable
private fun ProductionPill(label: String, count: Int, yield: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "$count Active", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = yield, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp)
    }
}

@Composable
private fun QuickActionsCard(
    onGatherQi: () -> Unit,
    onRecruitDisciple: () -> Unit,
    onOpenChamber: () -> Unit,
    onOpenWorldMap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_actions_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "⚡ Instant Dao Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onGatherQi,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gather_qi_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🧘 Gather Qi", fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = onRecruitDisciple,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("recruit_disciple_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("👥 Recruit", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenChamber,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_chamber_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🪷 Chamber", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenWorldMap,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_map_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🗺️ World Map", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DiscipleDashboardCard(disciple: DiscipleUiModel) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("disciple_card_${disciple.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(disciple.elementColor).copy(alpha = 0.25f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (disciple.isMale) "👨‍🎓" else "👩‍🎓",
                            fontSize = 22.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = disciple.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = disciple.realmName,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Task: ${disciple.taskName} • Power: ⚔️ ${disciple.combatPower}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "⚡ Spirit Qi: ${disciple.realmExp} / ${disciple.realmMaxExp}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = Color(0xFF00B0FF)
                    )
                }
            }

            // Material 3 CircularProgressIndicator tracking cultivation energy towards next rank-up
            CultivationProgressRing(
                currentEnergy = disciple.realmExp,
                maxThreshold = disciple.realmMaxExp,
                size = 50.dp,
                strokeWidth = 4.5.dp,
                primaryColor = Color(disciple.elementColor),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
