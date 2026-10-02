package com.sect.idle.hub.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sect.idle.hub.*
import com.sect.idle.hub.router.HashRouter
import com.sect.idle.hub.router.LocalHashRouter
import com.sect.idle.ui.SectScene
import com.sect.idle.ui.SectSceneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectHubScreen(
    viewModel: SectHubViewModel,
    onNavigateBackToGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sectSceneViewModel: SectSceneViewModel = viewModel()
    val router = remember { HashRouter("#/dashboard") }

    // Synchronize ViewModel tab selection with HashRouter route
    LaunchedEffect(state.currentTab) {
        val targetRoute = when (state.currentTab) {
            SectHubTab.DASHBOARD -> "#/dashboard"
            SectHubTab.SECT_REALM -> "#/realm"
            SectHubTab.DISCIPLES -> "#/disciples"
            SectHubTab.CULTIVATION -> "#/cultivation"
            SectHubTab.WORLD_MAP -> "#/world_map"
            SectHubTab.SETTINGS -> "#/settings"
        }
        if (router.getBasePath() != targetRoute) {
            router.replace(targetRoute)
        }
    }

    // Listen for state notifications
    LaunchedEffect(state.activeNotification) {
        state.activeNotification?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.dismissNotification()
        }
    }

    CompositionLocalProvider(LocalHashRouter provides router) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("sect_hub_scaffold"),
            containerColor = VoidBlack,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        containerColor = TwilightElevated,
                        contentColor = CloudMistWhite,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(data.visuals.message, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            },
            topBar = {
                if (state.currentTab != SectHubTab.SECT_REALM && state.currentTab != SectHubTab.SETTINGS) {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(JadeCyan.copy(alpha = 0.2f))
                                        .border(1.dp, JadeCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(state.currentTab.iconLabel, fontSize = 14.sp)
                                }
                                Column {
                                    Text(
                                        text = state.currentTab.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = CloudMistWhite
                                    )
                                    Text(
                                        text = "${state.sectName} · ${state.sectRealmName}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontSize = 11.sp,
                                        color = JadeCyan
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onNavigateBackToGame,
                                modifier = Modifier.testTag("back_to_game_button")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Return to Sect Valley",
                                    tint = CloudMistWhite
                                )
                            }
                        },
                        actions = {
                            // Treasury quick indicators
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TwilightElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🪙 ${state.spiritStones}", fontSize = 11.sp, color = CelestialGold, fontWeight = FontWeight.Bold)
                                    Text("🌿 ${state.spiritHerbs}", fontSize = 11.sp, color = JadeCyan, fontWeight = FontWeight.Bold)
                                    Text("💊 ${state.spiritPills}", fontSize = 11.sp, color = LotusPink, fontWeight = FontWeight.Bold)
                                }
                            }
                            IconButton(
                                onClick = { viewModel.selectTab(SectHubTab.SETTINGS) },
                                modifier = Modifier.testTag("topbar_settings_btn")
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = CloudMistWhite)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = TwilightSurface,
                            titleContentColor = CloudMistWhite
                        )
                    )
                }
            },
            bottomBar = {
                if (state.currentTab != SectHubTab.SECT_REALM && state.currentTab != SectHubTab.SETTINGS) {
                    NavigationBar(
                        containerColor = TwilightSurface,
                        contentColor = CloudMistWhite,
                        tonalElevation = 8.dp,
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sect_hub_bottom_nav")
                    ) {
                        val tabs = listOf(
                            SectHubTab.DASHBOARD to (Icons.Default.Dashboard to Icons.Outlined.Dashboard),
                            SectHubTab.SECT_REALM to (Icons.Default.AutoAwesome to Icons.Outlined.AutoAwesome),
                            SectHubTab.DISCIPLES to (Icons.Default.People to Icons.Outlined.PeopleOutline),
                            SectHubTab.CULTIVATION to (Icons.Default.SelfImprovement to Icons.Outlined.SelfImprovement),
                            SectHubTab.WORLD_MAP to (Icons.Default.Public to Icons.Outlined.Public),
                            SectHubTab.SETTINGS to (Icons.Default.Settings to Icons.Outlined.Settings)
                        )

                        tabs.forEach { (tab, icons) ->
                            val isSelected = state.currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) icons.first else icons.second,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = JadeCyan,
                                    indicatorColor = JadeCyan,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = state.currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (state.currentTab == SectHubTab.SECT_REALM) PaddingValues(0.dp) else innerPadding),
                label = "TabContentAnimation"
            ) { targetTab ->
                when (targetTab) {
                    SectHubTab.DASHBOARD -> SectDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTab = { viewModel.selectTab(it) }
                    )
                    SectHubTab.SECT_REALM -> SectScene(
                        viewModel = sectSceneViewModel,
                        onNavigateToHub = {
                            viewModel.selectTab(SectHubTab.DASHBOARD)
                        },
                        onNavigateToBattle = {
                            viewModel.selectTab(SectHubTab.WORLD_MAP)
                        },
                        onNavigateToWar = {
                            viewModel.selectTab(SectHubTab.WORLD_MAP)
                        },
                        onNavigateToTournament = {
                            viewModel.selectTab(SectHubTab.CULTIVATION)
                        },
                        onNavigateToRecruit = {
                            viewModel.selectTab(SectHubTab.DISCIPLES)
                        },
                        onNavigateToMenu = {
                            viewModel.selectTab(SectHubTab.DASHBOARD)
                        }
                    )
                    SectHubTab.DISCIPLES -> DiscipleListTab(state = state, viewModel = viewModel)
                    SectHubTab.CULTIVATION -> CultivationChamberTab(state = state, viewModel = viewModel)
                    SectHubTab.WORLD_MAP -> WorldMapTab(state = state, viewModel = viewModel)
                    SectHubTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
