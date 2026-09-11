package com.sect.idle.hub.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun SectOverviewTab(
    state: SectHubState,
    viewModel: SectHubViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Grand 3D Xianxia Sect Panorama Hero Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sect_overview_header"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TwilightSurface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(JadeCyan, CelestialGold, SpiritPurple)))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_grand_sect_hub),
                            contentDescription = "Grand 3D Sect Hub",
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
                        Surface(
                            modifier = Modifier
                                .padding(12.dp)
                                .align(Alignment.TopEnd),
                            shape = RoundedCornerShape(10.dp),
                            color = TwilightElevated.copy(alpha = 0.85f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CelestialGold)
                        ) {
                            Text(
                                text = "⚔️ ${state.sectPower} CP",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = CelestialGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(JadeDark.copy(alpha = 0.35f))
                                    .border(2.dp, CelestialGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏛️", fontSize = 22.sp)
                            }
                            Column {
                                Text(state.sectName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Text("Rank ${state.sectRank} Immortal Sect · ${state.sectRealmName}", fontSize = 12.sp, color = JadeCyan)
                            }
                        }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sect Realm Ascension Progress
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sect Destiny Exp: ${state.sectRealmExp}/${state.sectRealmMaxExp}", fontSize = 11.sp, color = TextMuted)
                            val pct = (state.sectRealmExp * 100f / state.sectRealmMaxExp.coerceAtLeast(1)).toInt().coerceIn(0, 100)
                            Text("$pct%", fontSize = 11.sp, color = JadeCyan, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (state.sectRealmExp.toFloat() / state.sectRealmMaxExp.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = JadeCyan,
                            trackColor = TwilightElevated
                        )
                    }
                }
            }
        }
    }

        // Treasury Matrix
        item {
            Text("Sect Treasury & Heavenly Resources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResourceCard("🪙 Spirit Stones", "${state.spiritStones}", CelestialGold, Modifier.weight(1f))
                ResourceCard("💎 Spirit Jade", "${state.jade}", JadeCyan, Modifier.weight(1f))
                ResourceCard("💊 Spirit Pills", "${state.spiritPills}", LotusPink, Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ResourceCard("🌱 Spirit Herbs", "${state.spiritHerbs}", SuccessGreen, Modifier.weight(1f))
                ResourceCard("⛏️ Spirit Ores", "${state.spiritOres}", CelestialAmber, Modifier.weight(1f))
                ResourceCard("✨ Dao Essence", "${state.essence}", SpiritPurple, Modifier.weight(1f))
            }
        }

        // Sect Hub Quick Navigation Portals
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text("Hub Navigation Portals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NavigationPortalCard(
                    title = "Disciple List",
                    subtitle = "${state.disciples.size} Cultivators",
                    icon = "👥",
                    color = JadeCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(SectHubTab.DISCIPLES) }
                )
                NavigationPortalCard(
                    title = "Cultivation",
                    subtitle = "Array Tier ${state.cultivationChamber.arrayLevel}",
                    icon = "🧘",
                    color = CelestialGold,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(SectHubTab.CULTIVATION) }
                )
                NavigationPortalCard(
                    title = "World Map",
                    subtitle = "${state.worldMap.regions.count { it.isUnlocked }} Territories",
                    icon = "🗺️",
                    color = SpiritPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(SectHubTab.WORLD_MAP) }
                )
            }
        }

        // Sect Economy & Dragon Vein
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text("Sect Economy & Dragon Vein Prosperity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TwilightSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Daily Gross Income:", fontSize = 12.sp, color = TextMuted)
                        Text("+${state.economy.dailyIncomeSS} Stones / day", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Daily Disciple Wages:", fontSize = 12.sp, color = TextMuted)
                        Text("-${state.economy.dailyExpenseSS} Stones / day", fontSize = 12.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = TwilightBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Net Daily Profit:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = (if (state.economy.netDailySS >= 0) "+" else "") + "${state.economy.netDailySS} Stones / day",
                            fontSize = 13.sp,
                            color = if (state.economy.netDailySS >= 0) SuccessGreen else DangerRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResourceCard(title: String, amount: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TwilightElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, color = TextMuted, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(amount, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun NavigationPortalCard(
    title: String,
    subtitle: String,
    icon: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TwilightSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CloudMistWhite, maxLines = 1)
            Text(subtitle, fontSize = 10.sp, color = TextMuted, maxLines = 1)
        }
    }
}
