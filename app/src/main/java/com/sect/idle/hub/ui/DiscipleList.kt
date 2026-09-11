package com.sect.idle.hub.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sect.idle.R
import com.sect.idle.core.GameConfig
import com.sect.idle.hub.*

enum class DiscipleSortBy(val label: String) {
    COMBAT_POWER("Combat Power"),
    REALM("Cultivation Realm"),
    LOYALTY("Loyalty"),
    NAME("Name")
}

/**
 * Material 3 DiscipleList UI Component
 * Displays a list of disciples with their cultivation stats, integrating seamlessly
 * with the centralized SectHubState and SectHubViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscipleList(
    state: SectHubState,
    viewModel: SectHubViewModel,
    modifier: Modifier = Modifier
) {
    var showTaskAssignDialogForDisciple by remember { mutableStateOf<DiscipleUiModel?>(null) }
    var showDetailDialogForDisciple by remember { mutableStateOf<DiscipleUiModel?>(null) }
    var sortBy by remember { mutableStateOf(DiscipleSortBy.COMBAT_POWER) }
    var sortAscending by remember { mutableStateOf(false) }

    val filteredAndSortedDisciples = remember(state.disciples, state.searchQuery, state.taskFilter, sortBy, sortAscending) {
        val filtered = state.disciples.filter { d ->
            val matchesQuery = state.searchQuery.isBlank() ||
                    d.name.contains(state.searchQuery, ignoreCase = true) ||
                    d.realmName.contains(state.searchQuery, ignoreCase = true) ||
                    d.talentName.contains(state.searchQuery, ignoreCase = true) ||
                    d.elementName.contains(state.searchQuery, ignoreCase = true)
            val matchesTask = state.taskFilter == null || d.currentTask == state.taskFilter
            matchesQuery && matchesTask
        }

        when (sortBy) {
            DiscipleSortBy.COMBAT_POWER -> if (sortAscending) filtered.sortedBy { it.combatPower } else filtered.sortedByDescending { it.combatPower }
            DiscipleSortBy.REALM -> if (sortAscending) filtered.sortedBy { it.realm * 1000 + it.realmExp } else filtered.sortedByDescending { it.realm * 1000 + it.realmExp }
            DiscipleSortBy.LOYALTY -> if (sortAscending) filtered.sortedBy { it.loyalty } else filtered.sortedByDescending { it.loyalty }
            DiscipleSortBy.NAME -> if (sortAscending) filtered.sortedBy { it.name } else filtered.sortedByDescending { it.name }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("disciple_list_container")
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar and Recruit Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        "Search disciple by name, realm, talent...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search Disciples",
                        tint = JadeCyan
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.setSearchQuery("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("disciple_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JadeCyan,
                    unfocusedBorderColor = TwilightBorder,
                    focusedContainerColor = TwilightElevated,
                    unfocusedContainerColor = TwilightElevated,
                    focusedTextColor = CloudMistWhite,
                    unfocusedTextColor = CloudMistWhite
                ),
                singleLine = true
            )

            FilledTonalButton(
                onClick = { viewModel.recruitNewDisciple() },
                modifier = Modifier
                    .height(52.dp)
                    .testTag("recruit_disciple_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = CelestialGold,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recruit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Task Filter Chips Row
        val taskOptions: List<Pair<Int?, String>> = listOf(
            null to "All (${state.disciples.size})",
            GameConfig.TASK_CULTIVATION to "🧘 Cultivate (${state.disciples.count { it.currentTask == GameConfig.TASK_CULTIVATION }})",
            GameConfig.TASK_ALCHEMY to "🧪 Alchemy (${state.disciples.count { it.currentTask == GameConfig.TASK_ALCHEMY }})",
            GameConfig.TASK_FARMING to "🌱 Herbs (${state.disciples.count { it.currentTask == GameConfig.TASK_FARMING }})",
            GameConfig.TASK_MINING to "⛏️ Mine (${state.disciples.count { it.currentTask == GameConfig.TASK_MINING }})",
            GameConfig.TASK_GUARD to "🛡️ Guard (${state.disciples.count { it.currentTask == GameConfig.TASK_GUARD }})"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            taskOptions.forEach { (taskCode, label) ->
                val isSelected = state.taskFilter == taskCode
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setTaskFilter(taskCode) },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else CloudMistWhite
                        )
                    },
                    modifier = Modifier.testTag("filter_chip_${taskCode ?: "all"}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JadeCyan,
                        selectedLabelColor = Color.Black,
                        containerColor = TwilightElevated,
                        labelColor = CloudMistWhite
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) JadeCyan else TwilightBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Summary Bar & Sort Controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = TwilightElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Roster: ${filteredAndSortedDisciples.size} / ${state.disciples.size} Disciples",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CloudMistWhite
                    )
                    val avgCp = (state.disciples.map { it.combatPower }.average().takeIf { !it.isNaN() } ?: 0.0).toInt()
                    Text(
                        text = "Avg Combat Power: $avgCp CP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CelestialAmber,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val readyCount = state.disciples.count { it.isReadyForBreakthrough }
                    if (readyCount > 0) {
                        AssistChip(
                            onClick = { viewModel.selectTab(SectHubTab.CULTIVATION) },
                            label = {
                                Text(
                                    "⚡ $readyCount Ascend",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(containerColor = CelestialGold),
                            modifier = Modifier.testTag("breakthrough_ready_chip")
                        )
                    }

                    // Sort Toggle
                    FilledTonalIconButton(
                        onClick = {
                            val nextSort = when (sortBy) {
                                DiscipleSortBy.COMBAT_POWER -> DiscipleSortBy.REALM
                                DiscipleSortBy.REALM -> DiscipleSortBy.LOYALTY
                                DiscipleSortBy.LOYALTY -> DiscipleSortBy.NAME
                                DiscipleSortBy.NAME -> DiscipleSortBy.COMBAT_POWER
                            }
                            sortBy = nextSort
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("sort_disciple_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = TwilightSurface,
                            contentColor = JadeCyan
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort By ${sortBy.label}", modifier = Modifier.size(18.dp))
                    }

                    FilledTonalIconButton(
                        onClick = { sortAscending = !sortAscending },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("sort_order_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = TwilightSurface,
                            contentColor = JadeCyan
                        )
                    ) {
                        Icon(
                            imageVector = if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = if (sortAscending) "Ascending" else "Descending",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Disciples Lazy Column with Material 3 Cards
        if (filteredAndSortedDisciples.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("empty_disciple_list"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        Icons.Outlined.People,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = TextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No disciples found matching filters",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Recruit wandering rogue cultivators or clear filters to view disciples.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.recruitNewDisciple() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JadeCyan, contentColor = Color.Black),
                        modifier = Modifier.testTag("empty_recruit_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recruit Disciple (200 SS)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("disciple_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredAndSortedDisciples, key = { it.id }) { disciple ->
                    DiscipleCardItem(
                        disciple = disciple,
                        onOpenDetail = { showDetailDialogForDisciple = disciple },
                        onAssignTask = { showTaskAssignDialogForDisciple = disciple },
                        onQuickBreakthrough = {
                            viewModel.selectTab(SectHubTab.CULTIVATION)
                        }
                    )
                }
            }
        }
    }

    // Modal dialogs
    showTaskAssignDialogForDisciple?.let { disc ->
        TaskAssignmentModal(
            disciple = disc,
            onDismiss = { showTaskAssignDialogForDisciple = null },
            onSelectTask = { newTask ->
                viewModel.assignDiscipleTask(disc.id, newTask)
                showTaskAssignDialogForDisciple = null
            }
        )
    }

    showDetailDialogForDisciple?.let { disc ->
        DiscipleDetailModal(
            disciple = disc,
            onDismiss = { showDetailDialogForDisciple = null },
            onChangeTask = {
                showTaskAssignDialogForDisciple = disc
                showDetailDialogForDisciple = null
            },
            onBreakthroughChamber = {
                showDetailDialogForDisciple = null
                viewModel.selectTab(SectHubTab.CULTIVATION)
            }
        )
    }
}

/**
 * Material 3 Card representing an individual cultivator disciple
 */
@Composable
fun DiscipleCardItem(
    disciple: DiscipleUiModel,
    onOpenDetail: () -> Unit,
    onAssignTask: () -> Unit,
    onQuickBreakthrough: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elementColor = Color(disciple.elementColor)
    val isBreakthroughReady = disciple.isReadyForBreakthrough

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenDetail() }
            .testTag("disciple_card_${disciple.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBreakthroughReady) TwilightSurface.copy(alpha = 0.95f) else TwilightSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isBreakthroughReady) 1.5.dp else 1.dp,
            color = if (isBreakthroughReady) CelestialGold else TwilightBorder
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isBreakthroughReady) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Avatar, Identity, Realm Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Element Affinity Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(elementColor.copy(alpha = 0.2f))
                            .border(1.5.dp, elementColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = disciple.elementName.take(1),
                            color = elementColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = disciple.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CloudMistWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (disciple.isMale) "♂" else "♀",
                                fontSize = 12.sp,
                                color = if (disciple.isMale) Color(0xFF64B5F6) else Color(0xFFF48FB1),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${disciple.title} · Age ${disciple.age} · ${disciple.elementName} Root",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                // Cultivation Realm Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TwilightElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isBreakthroughReady) CelestialGold else TwilightBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isBreakthroughReady) {
                            Text("⚡", fontSize = 11.sp)
                        }
                        Text(
                            text = disciple.realmName,
                            color = if (isBreakthroughReady) CelestialGold else JadeCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cultivation Progress Gauge
            val progress = (disciple.realmExp.toFloat() / disciple.realmMaxExp.coerceAtLeast(1)).coerceIn(0f, 1f)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cultivation Qi: ${disciple.realmExp} / ${disciple.realmMaxExp} EXP",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = if (isBreakthroughReady) "BREAKTHROUGH READY" else "${(progress * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = if (isBreakthroughReady) CelestialGold else JadeCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isBreakthroughReady) CelestialGold else JadeCyan,
                    trackColor = TwilightElevated
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cultivation Stats Chips & Primary Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Key Cultivation Stat Badges
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TwilightElevated
                    ) {
                        Text(
                            text = "⚔️ ${disciple.combatPower} CP",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            color = CelestialAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TwilightElevated
                    ) {
                        Text(
                            text = "📜 ${disciple.talentName}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            color = LotusPink,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TwilightElevated
                    ) {
                        Text(
                            text = "💚 ${disciple.loyalty}%",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Action Affordances
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isBreakthroughReady) {
                        Button(
                            onClick = onQuickBreakthrough,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CelestialGold,
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("ascend_button_${disciple.id}")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Ascend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onAssignTask,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JadeCyan),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JadeCyan),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("task_button_${disciple.id}")
                    ) {
                        Text(disciple.taskName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskAssignmentModal(
    disciple: DiscipleUiModel,
    onDismiss: () -> Unit,
    onSelectTask: (Int) -> Unit
) {
    val tasks = listOf(
        Triple(GameConfig.TASK_CULTIVATION, "🧘 Cultivating Meditation", "Absorbs ambient Heaven & Earth Qi to increase realm cultivation exp."),
        Triple(GameConfig.TASK_ALCHEMY, "🧪 Alchemy Pavilion", "Refines spirit herbs into potent spiritual pills for breakthroughs."),
        Triple(GameConfig.TASK_FARMING, "🌱 Spirit Herb Garden", "Nurtures spiritual botanical herbs to enrich sect treasury."),
        Triple(GameConfig.TASK_MINING, "⛏️ Spirit Ore Cavern", "Excavates spirit stones and raw ore veins."),
        Triple(GameConfig.TASK_GUARD, "🛡️ Sect Mountain Patrol", "Defends against rogue demonic cultivators and raises sect security.")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Assign Sect Duty", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CloudMistWhite)
                Text("Cultivator: ${disciple.name} (${disciple.realmName})", style = MaterialTheme.typography.bodyMedium, color = JadeCyan)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tasks.forEach { (taskCode, title, desc) ->
                    val isCurrent = disciple.currentTask == taskCode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTask(taskCode) }
                            .testTag("assign_task_option_$taskCode"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrent) JadeDark.copy(alpha = 0.4f) else TwilightElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) JadeCyan else TwilightBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(title, fontWeight = FontWeight.Bold, color = if (isCurrent) JadeCyan else CloudMistWhite)
                                if (isCurrent) {
                                    Text("ACTIVE", fontSize = 10.sp, color = JadeCyan, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(desc, style = MaterialTheme.typography.bodyMedium, fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_task_dialog_button")
            ) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = TwilightSurface,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun DiscipleDetailModal(
    disciple: DiscipleUiModel,
    onDismiss: () -> Unit,
    onChangeTask: () -> Unit,
    onBreakthroughChamber: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(disciple.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CloudMistWhite)
                    Text("${disciple.title} · ${disciple.realmName}", color = JadeCyan, style = MaterialTheme.typography.bodyMedium)
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_disciple_detail_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 3D Donghua Cultivator Portrait
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, JadeCyan)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_cultivator_hero),
                            contentDescription = "3D Cultivator Model",
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
                                            TwilightElevated.copy(alpha = 0.7f),
                                            TwilightElevated
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("✦", color = CelestialGold, fontSize = 14.sp)
                            Text(
                                text = "Immortal Sword Cultivator",
                                color = CelestialGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Talent & Attribute Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = TwilightElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Spiritual Talent:", fontSize = 12.sp, color = TextMuted)
                            Text(disciple.talentName, fontSize = 12.sp, color = LotusPink, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Elemental Root:", fontSize = 12.sp, color = TextMuted)
                            Text(disciple.elementName, fontSize = 12.sp, color = Color(disciple.elementColor), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Combat Prowess:", fontSize = 12.sp, color = TextMuted)
                            Text("${disciple.combatPower} CP", fontSize = 12.sp, color = CelestialAmber, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Loyalty / Energy:", fontSize = 12.sp, color = TextMuted)
                            Text("${disciple.loyalty}% / ${disciple.energy}%", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Attributes Matrix
                Text("Cultivation Attributes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = CloudMistWhite)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AttributeBadge("STR", disciple.str, Modifier.weight(1f))
                    AttributeBadge("AGI", disciple.agi, Modifier.weight(1f))
                    AttributeBadge("INT", disciple.intel, Modifier.weight(1f))
                    AttributeBadge("LCK", disciple.lck, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AttributeBadge("VIT", disciple.vit, Modifier.weight(1f))
                    AttributeBadge("WIS", disciple.wis, Modifier.weight(1f))
                    AttributeBadge("CHA", disciple.cha, Modifier.weight(1f))
                    AttributeBadge("ALCH", disciple.alchemySkill, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions in modal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onChangeTask,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_change_task_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JadeCyan)
                    ) {
                        Text("Duty: ${disciple.taskName}", fontSize = 12.sp, color = JadeCyan)
                    }

                    Button(
                        onClick = onBreakthroughChamber,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_cultivate_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CelestialGold, contentColor = Color.Black)
                    ) {
                        Text("🧘 Cultivate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = TwilightSurface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun AttributeBadge(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = TwilightElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = TextMuted)
            Text("$value", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CloudMistWhite)
        }
    }
}
