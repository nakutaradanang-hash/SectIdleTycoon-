package com.sect.idle.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sect.idle.R
import com.sect.idle.gameplay.CoroutineGameLoop
import com.sect.idle.gameplay.SectData
import com.sect.idle.models.Disciple
import com.sect.idle.systems.AudioManager
import com.sect.idle.systems.NumberFormatter
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// =============================================================================
// OFFICIAL COLOR PALETTE — "DAO OF HEAVEN" (Optimized for 1GB RAM / Itel A70)
// =============================================================================
val CelestialGold = Color(0xFFD4AF37)   // Emas Abadi
val JadeQi = Color(0xFF00A86B)          // Jade Qi
val NightSkyDark = Color(0xFF1A1A2E)    // Langit Malam Utama (80-90% alpha)
val NightSkyDeep = Color(0xFF0F0F1D)    // Void Deep Midnight
val CloudMist = Color(0xFFF0F0F0)       // Awan & Kabut Spiritual
val SpiritCyan = Color(0xFF00E5FF)      // Blue Qi Crystals & Waterfalls
val ImperialPurple = Color(0xFF9C27B0)  // Royal Dan & Array
val FlameCrimson = Color(0xFFFF5252)    // Combat & Breakthrough Fire
val DaoBorderGold = Color(0xFFFFD54F)   // Trim Dragon 8px
val MutedScroll = Color(0xFFD7CCC8)     // Bamboo scroll text

/**
 * Main Compose Scaffold supporting the navigation structure for the Sect scene,
 * featuring dedicated slots for the top HUD bar, navigation bar, side talismans,
 * modal dialogs, and the main game canvas display.
 */
@Composable
fun SectSceneScaffold(
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    floatingActionSlot: (@Composable () -> Unit)? = null,
    overlaySlot: (@Composable () -> Unit)? = null,
    gameCanvas: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(NightSkyDeep)
            .testTag("sect_scene_scaffold"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = topBar,
        bottomBar = bottomBar,
        containerColor = NightSkyDeep
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main game canvas display slot
            gameCanvas()

            // Optional side/floating action controls (e.g. Dao Talismans)
            floatingActionSlot?.invoke()

            // Optional overlays/modals (e.g. Qi toasts, stats, inventory, dialogs)
            overlaySlot?.invoke()
        }
    }
}

/**
 * SectScene — Master Redesign & Rewrite.
 * Isometric 3D Xianxia Sect World Engine:
 *  - Floating Mountain Peaks on Celestial Cloud Platform
 *  - Interactive Canvas with 128x128px tile rendering and drag scrolling
 *  - Upward waterfalls, glowing Qi crystals, and flying sword trails
 *  - Semi-transparent #1A1A2E 9-slice UI Frame with Dragon Gold trim #D4AF37
 *  - 3 Core Interactive Panels: 1. Stats (Blue HP Qi / Gold MP Qi), 2. Inventory Grid, 3. Bamboo Quest Scroll
 *  - 5-Tab Bottom Navigation Bar: Home, Battle, Sect, Bag, Profile with Gold Qi Glow
 *  - Background Coroutine Simulation Loop telemetry & Speed Multipliers via GameViewModel
 */
@Composable
fun SectScene(
    viewModel: SectSceneViewModel,
    gameViewModel: GameViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier,
    onNavigateToHub: (String?) -> Unit = {},
    onNavigateToBattle: () -> Unit = {},
    onNavigateToWar: () -> Unit = {},
    onNavigateToTournament: () -> Unit = {},
    onNavigateToRecruit: () -> Unit = {},
    onNavigateToMenu: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gameUiState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val sectData = SectData.getInstance()
    val gameLoop = remember { CoroutineGameLoop.get() }
    val loopState by gameLoop.loopState.collectAsStateWithLifecycle()

    // Map drag/pan offsets for smooth isometric exploration
    var mapOffsetX by remember { mutableFloatStateOf(0f) }
    var mapOffsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(gameUiState.qiToastMessage) {
        if (gameUiState.qiToastMessage != null) {
            delay(2200)
            gameViewModel.clearToast()
        }
    }

    SectSceneScaffold(
        modifier = modifier.testTag("sect_scene_screen"),
        topBar = {
            // Top HUD Bar
            XianxiaTopHeader(
                sectData = sectData,
                loopState = loopState,
                onProfileClick = { gameViewModel.showModal(ActiveModalType.STATS) },
                onQuestsClick = { gameViewModel.showModal(ActiveModalType.QUESTS) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 4.dp, start = 8.dp, end = 8.dp, bottom = 4.dp)
            )
        },
        bottomBar = {
            // 5-Tab Bottom Navigation Bar (Home, Battle, Sect, Bag, Profile) with Gold Qi Glow
            XianxiaBottomNavBar(
                selectedTab = gameUiState.currentTab.index,
                onTabSelected = { tabIndex ->
                    gameViewModel.selectTabByIndex(tabIndex)
                    when (tabIndex) {
                        0 -> { /* Stay on Main Home Sect */ }
                        1 -> onNavigateToBattle()
                        2 -> onNavigateToHub("overview")
                        3 -> { /* Handled by GameViewModel modal state */ }
                        4 -> { /* Handled by GameViewModel modal state */ }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            )
        },
        floatingActionSlot = {
            // Right-Side Dao Talismans (Cultivation Speed & Breakthrough Buffs)
            Box(modifier = Modifier.fillMaxSize()) {
                RightSideDaoTalismans(
                    currentSpeed = gameUiState.speedMultiplier,
                    onToggleSpeed = {
                        gameViewModel.toggleSpeedMultiplier()
                    },
                    onTriggerBreakthrough = {
                        viewModel.playBreakthrough()
                        gameViewModel.showModal(ActiveModalType.STATS)
                        gameViewModel.showToast("🧘 Heavenly Qi channeled to Disciple stats!")
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                )
            }
        },
        overlaySlot = {
            Box(modifier = Modifier.fillMaxSize()) {
                // Toast Feedback Overlay
                AnimatedVisibility(
                    visible = gameUiState.qiToastMessage != null,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 60.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = NightSkyDark.copy(alpha = 0.92f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, CelestialGold),
                        shadowElevation = 10.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CelestialGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = gameUiState.qiToastMessage ?: "",
                                color = CelestialGold,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Modal Overlays (Stats / Inventory / Quests / Island Inspection)
                when (gameUiState.activeModal) {
                    ActiveModalType.STATS -> {
                        CharacterStatsPanelModal(
                            sectData = sectData,
                            onDismiss = { gameViewModel.dismissModal() }
                        )
                    }
                    ActiveModalType.INVENTORY -> {
                        InventoryPanelModal(
                            sectData = sectData,
                            onDismiss = { gameViewModel.dismissModal() }
                        )
                    }
                    ActiveModalType.QUESTS -> {
                        BambooQuestPanelModal(
                            sectData = sectData,
                            onDismiss = { gameViewModel.dismissModal() },
                            onClaimQuest = { reward ->
                                sectData?.let { it.spiritStones += reward }
                                viewModel.playCoin()
                                gameViewModel.showToast("📜 Quest completed! +$reward Spirit Stones.")
                            }
                        )
                    }
                    null -> {
                        // Island Node Inspection Modal
                        gameUiState.inspectedIslandId?.let { islandId ->
                            val island = getIslandById(islandId)
                            if (island != null) {
                                IslandInspectionModal(
                                    island = island,
                                    sectData = sectData,
                                    onDismiss = { gameViewModel.inspectIsland(null) },
                                    onUpgrade = {
                                        if (sectData != null && sectData.spiritStones >= island.upgradeCost) {
                                            sectData.spiritStones -= island.upgradeCost
                                            island.level += 1
                                            viewModel.playBell()
                                            gameViewModel.showToast("🏛️ ${island.name} upgraded to Lv.${island.level}!")
                                        } else {
                                            viewModel.playSfx(AudioManager.SFX_FAIL)
                                            gameViewModel.showToast("⚠️ Insufficient Spirit Stones!")
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        gameCanvas = {
            // Main Game Canvas Display (Isometric Realm & Visual Layers with Touch Drag Scrolling)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("sect_scene_game_canvas")
            ) {
                val bgBitmap = ImageBitmap.imageResource(id = R.drawable.bg_sect_floating_islands)

                // 1. Isometric Canvas Grid & Background with touch drag scrolling
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("sect_isometric_grid_canvas")
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                mapOffsetX = (mapOffsetX + dragAmount.x).coerceIn(-300f, 300f)
                                mapOffsetY = (mapOffsetY + dragAmount.y).coerceIn(-300f, 300f)
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    // Draw full background map with pan offset
                    drawImage(
                        image = bgBitmap,
                        dstOffset = IntOffset(
                            x = (mapOffsetX * 0.4f).toInt() - 60,
                            y = (mapOffsetY * 0.4f).toInt() - 40
                        ),
                        dstSize = IntSize(
                            width = (canvasWidth * 1.25f).toInt() + 120,
                            height = (canvasHeight * 1.25f).toInt() + 80
                        )
                    )

                    // Draw isometric grid lines and subtle spiritual terrain tiles
                    val tileW = 128f
                    val tileH = 64f
                    val originX = (canvasWidth / 2f) + mapOffsetX
                    val originY = (canvasHeight * 0.35f) + mapOffsetY

                    // Subtle ethereal isometric diamond grid for sect territorial boundary
                    val gridRange = -3..3
                    for (ix in gridRange) {
                        for (iy in gridRange) {
                            val isoX = originX + (ix - iy) * (tileW / 2f)
                            val isoY = originY + (ix + iy) * (tileH / 2f)

                            val path = Path().apply {
                                moveTo(isoX, isoY - tileH / 2f)
                                lineTo(isoX + tileW / 2f, isoY)
                                lineTo(isoX, isoY + tileH / 2f)
                                lineTo(isoX - tileW / 2f, isoY)
                                close()
                            }

                            // Spiritual grid highlight
                            drawPath(
                                path = path,
                                color = CelestialGold.copy(alpha = 0.08f),
                                style = Stroke(width = 1f)
                            )
                        }
                    }
                }

                // 2. Dynamic VFX: Floating Qi, Upward Waterfalls, and Flying Sword Trails
                IsometricVFXLayer(modifier = Modifier.fillMaxSize())

                // 3. Interactive Floating Island Hotspots (Grand Pagoda, Sword Peak, Herb Valley, Mines)
                IsometricFloatingIslandsLayer(
                    panOffsetX = mapOffsetX,
                    panOffsetY = mapOffsetY,
                    onSelectIsland = { island ->
                        gameViewModel.inspectIsland(island.id)
                        viewModel.selectBuilding(island.id)
                        viewModel.playBell()
                    },
                    onGatherQi = { amount ->
                        if (sectData != null) {
                            sectData.spiritStones += amount
                        }
                        viewModel.playCoin()
                        gameViewModel.showToast("✨ Harvested +$amount Spirit Stones!")
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}

// -----------------------------------------------------------------------------
// ENUMS & MODELS
// -----------------------------------------------------------------------------

enum class ActiveModalType {
    STATS,
    INVENTORY,
    QUESTS
}

data class IslandNode(
    val id: Int,
    val name: String,
    val chineseName: String,
    val description: String,
    var level: Int,
    val upgradeCost: Long,
    val normalizedX: Float,
    val normalizedY: Float,
    val accentColor: Color,
    val yieldText: String
)

val DEFAULT_ISLAND_NODES = listOf(
    IslandNode(
        id = 0,
        name = "Grand Palace Hall",
        chineseName = "凌霄宝殿",
        description = "Ancestral throne where the Sect Master gathers celestial Qi and guides disciples.",
        level = 5,
        upgradeCost = 2500L,
        normalizedX = 0.50f,
        normalizedY = 0.38f,
        accentColor = CelestialGold,
        yieldText = "+150 Qi/sec"
    ),
    IslandNode(
        id = 1,
        name = "Celestial Sword Peak",
        chineseName = "万剑仙峰",
        description = "Floating blade sanctuary for sharpening sword intent and martial combat prowess.",
        level = 3,
        upgradeCost = 1800L,
        normalizedX = 0.18f,
        normalizedY = 0.28f,
        accentColor = SpiritCyan,
        yieldText = "+80 Atk Power"
    ),
    IslandNode(
        id = 2,
        name = "Spirit Herb Valley",
        chineseName = "药王仙谷",
        description = "Mystic soil irrigated by upward waterfalls, cultivating heavenly spirit herbs.",
        level = 4,
        upgradeCost = 1200L,
        normalizedX = 0.82f,
        normalizedY = 0.32f,
        accentColor = JadeQi,
        yieldText = "+45 Herbs/sec"
    ),
    IslandNode(
        id = 3,
        name = "Nine-Turn Alchemy",
        chineseName = "九转丹阁",
        description = "Immortal furnace refining elixir pills for cultivation breakthroughs.",
        level = 2,
        upgradeCost = 3000L,
        normalizedX = 0.22f,
        normalizedY = 0.56f,
        accentColor = ImperialPurple,
        yieldText = "+12 Pills/hr"
    ),
    IslandNode(
        id = 4,
        name = "Spirit Ore Mine",
        chineseName = "玄晶灵脉",
        description = "Abyssal crystal veins glowing with deep earth spiritual ores.",
        level = 3,
        upgradeCost = 1500L,
        normalizedX = 0.78f,
        normalizedY = 0.58f,
        accentColor = SpiritCyan,
        yieldText = "+60 Ores/sec"
    )
)

fun getIslandById(id: Int): IslandNode? {
    return DEFAULT_ISLAND_NODES.firstOrNull { it.id == id }
}

// -----------------------------------------------------------------------------
// 1. ISOMETRIC VFX LAYER (Particles, Upward Waterfalls, Sword Trails)
// -----------------------------------------------------------------------------

@Composable
private fun IsometricVFXLayer(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "VFX")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val tSec = time * 0.05f

        // Upward Qi particles
        for (i in 0 until 16) {
            val px = (w * ((i * 0.063f + (tSec * 0.02f)) % 1f))
            val py = h * (1f - ((i * 0.07f + (tSec * 0.05f)) % 1f))
            val radius = 2.5f + (i % 3) * 1.5f
            val alpha = (sin((tSec + i).toDouble()).toFloat() * 0.35f + 0.55f).coerceIn(0f, 1f)

            drawCircle(
                color = if (i % 2 == 0) SpiritCyan.copy(alpha = alpha) else CelestialGold.copy(alpha = alpha),
                radius = radius,
                center = Offset(px, py)
            )
        }

        // Flying sword trail across celestial sky
        val swordProgress = (tSec * 0.15f) % 1f
        val sx = w * swordProgress
        val sy = h * (0.22f + 0.08f * sin((swordProgress * 6.28f).toDouble()).toFloat())

        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(CelestialGold.copy(alpha = 0f), CelestialGold, SpiritCyan),
                start = Offset(sx - 70f, sy - 15f),
                end = Offset(sx, sy)
            ),
            start = Offset(sx - 70f, sy - 15f),
            end = Offset(sx, sy),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}

// -----------------------------------------------------------------------------
// 2. ISOMETRIC FLOATING ISLANDS LAYER
// -----------------------------------------------------------------------------

@Composable
private fun IsometricFloatingIslandsLayer(
    panOffsetX: Float = 0f,
    panOffsetY: Float = 0f,
    onSelectIsland: (IslandNode) -> Unit,
    onGatherQi: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val islands = remember { DEFAULT_ISLAND_NODES }

    BoxWithConstraints(modifier = modifier) {
        val w = maxWidth
        val h = maxHeight

        islands.forEach { island ->
            val posX = (w.value * island.normalizedX).dp + (panOffsetX * 0.7f).dp
            val posY = (h.value * island.normalizedY).dp + (panOffsetY * 0.7f).dp

            FloatingIslandBadge(
                island = island,
                onClick = { onSelectIsland(island) },
                onQuickHarvest = { onGatherQi(island.level * 25L) },
                modifier = Modifier.offset(x = posX - 55.dp, y = posY - 35.dp)
            )
        }
    }
}

@Composable
private fun FloatingIslandBadge(
    island: IslandNode,
    onClick: () -> Unit,
    onQuickHarvest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .testTag("island_badge_${island.id}")
            .scale(pulseScale)
            .sizeIn(minWidth = 56.dp, minHeight = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = island.accentColor),
                onClick = onClick
            )
            .padding(4.dp)
    ) {
        // Glowing Orb Node
        Surface(
            shape = CircleShape,
            color = NightSkyDark.copy(alpha = 0.88f),
            border = androidx.compose.foundation.BorderStroke(2.dp, island.accentColor),
            shadowElevation = 8.dp,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when (island.id) {
                        0 -> Icons.Default.Home
                        1 -> Icons.Default.SportsKabaddi
                        2 -> Icons.Default.LocalFlorist
                        3 -> Icons.Default.AutoAwesome
                        else -> Icons.Default.Diamond
                    },
                    contentDescription = island.name,
                    tint = island.accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Title Tag
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = NightSkyDark.copy(alpha = 0.90f),
            border = androidx.compose.foundation.BorderStroke(1.dp, DaoBorderGold.copy(alpha = 0.7f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = island.chineseName,
                    color = CelestialGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lv.${island.level} • ${island.yieldText}",
                    color = JadeQi,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 3. TOP ORNATE HEADER HUD (#1A1A2E + Dragon Trim #D4AF37)
// -----------------------------------------------------------------------------

@Composable
private fun XianxiaTopHeader(
    sectData: SectData?,
    loopState: com.sect.idle.gameplay.GameLoopTickState,
    onProfileClick: () -> Unit,
    onQuestsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = NightSkyDark.copy(alpha = 0.85f),
        border = androidx.compose.foundation.BorderStroke(2.dp, CelestialGold),
        shadowElevation = 10.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            // Row 1: Sect Title, Power, and Quick Action Badges
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Sect Name & Master Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onProfileClick)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = JadeQi.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JadeQi),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = CelestialGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = sectData?.sectName ?: "Mount Shu Sect",
                            color = CelestialGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Realm Rank ${sectData?.sectRealm ?: 1} • Disc. ${sectData?.disciples?.size ?: 0}",
                            color = CloudMist.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }

                // Power & Quest Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x5500E5FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SpiritCyan)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = SpiritCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = NumberFormatter.format(loopState.totalSectPower),
                                color = SpiritCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onQuestsClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = "Quests",
                            tint = CelestialGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: 4 Key Currencies (Stones, Herbs, Ores, Pills)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                CurrencyChip(
                    icon = Icons.Default.Diamond,
                    value = NumberFormatter.format(sectData?.spiritStones ?: 0L),
                    label = "Stones",
                    tint = CelestialGold
                )
                CurrencyChip(
                    icon = Icons.Default.LocalFlorist,
                    value = NumberFormatter.format(sectData?.spiritHerbs ?: 0L),
                    label = "Herbs",
                    tint = JadeQi
                )
                CurrencyChip(
                    icon = Icons.Default.Workspaces,
                    value = NumberFormatter.format(sectData?.spiritOres ?: 0L),
                    label = "Ores",
                    tint = SpiritCyan
                )
                CurrencyChip(
                    icon = Icons.Default.AutoAwesome,
                    value = NumberFormatter.format(sectData?.spiritPills ?: 0L),
                    label = "Pills",
                    tint = ImperialPurple
                )
            }
        }
    }
}

@Composable
private fun CurrencyChip(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = NightSkyDark.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.5f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 4. 5-TAB BOTTOM NAVIGATION BAR (#1A1A2E with #D4AF37 Active Glow)
// -----------------------------------------------------------------------------

@Composable
private fun XianxiaBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        NavBarItem("Home", Icons.Default.Home),
        NavBarItem("Battle", Icons.Default.SportsKabaddi),
        NavBarItem("Sect", Icons.Default.Shield),
        NavBarItem("Bag", Icons.Default.Inventory2),
        NavBarItem("Profile", Icons.Default.AccountCircle)
    )

    val glowTransition = rememberInfiniteTransition(label = "navGlow")
    val glowAlpha by glowTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = NightSkyDark.copy(alpha = 0.94f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DaoBorderGold.copy(alpha = 0.85f)),
        shadowElevation = 16.dp,
        modifier = modifier.height(68.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            tabs.forEachIndexed { index, item ->
                val isSelected = selectedTab == index
                val scale = if (isSelected) 1.12f else 1.0f

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            CelestialGold.copy(alpha = 0.22f * glowAlpha),
                                            Color.Transparent
                                        )
                                    )
                                )
                            } else Modifier
                        )
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            // Gold Qi Glow Halo behind icon
                            Canvas(modifier = Modifier.size(32.dp)) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            CelestialGold.copy(alpha = 0.55f * glowAlpha),
                                            CelestialGold.copy(alpha = 0.15f * glowAlpha),
                                            Color.Transparent
                                        )
                                    ),
                                    radius = size.minDimension / 1.5f
                                )
                            }
                        }
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) CelestialGold else CloudMist.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.title,
                        color = if (isSelected) CelestialGold else CloudMist.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                    )

                    if (isSelected) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(CelestialGold)
                        )
                    }
                }
            }
        }
    }
}

private data class NavBarItem(val title: String, val icon: ImageVector)

// -----------------------------------------------------------------------------
// 5. RIGHT SIDE TALISMANS (Speed Toggle & Cultivation Qi Boost)
// -----------------------------------------------------------------------------

@Composable
private fun RightSideDaoTalismans(
    currentSpeed: Float,
    onToggleSpeed: () -> Unit,
    onTriggerBreakthrough: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Speed Multiplier
        Surface(
            shape = CircleShape,
            color = NightSkyDark.copy(alpha = 0.90f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, SpiritCyan),
            modifier = Modifier
                .size(46.dp)
                .clickable(onClick = onToggleSpeed)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = SpiritCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${currentSpeed.toInt()}x",
                        color = SpiritCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Breakthrough Qi Burst
        Surface(
            shape = CircleShape,
            color = NightSkyDark.copy(alpha = 0.90f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CelestialGold),
            modifier = Modifier
                .size(46.dp)
                .clickable(onClick = onTriggerBreakthrough)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Breakthrough",
                    tint = CelestialGold,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 6. THREE CORE INTERACTIVE PANELS (STATS, INVENTORY, QUESTS)
// -----------------------------------------------------------------------------

/**
 * PANEL 1: CHARACTER STATS PANEL (Blue HP Qi Bar, Gold MP Qi Bar, Wisdom/Strength/Luck)
 */
@Composable
private fun CharacterStatsPanelModal(
    sectData: SectData?,
    onDismiss: () -> Unit
) {
    val disciple = sectData?.disciples?.firstOrNull() ?: Disciple("Daoist Master")

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(onClick = onDismiss)
            .padding(20.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NightSkyDark.copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(2.dp, CelestialGold),
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = disciple.name,
                            color = CelestialGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Realm: ${disciple.realmDisplay} (Stage ${disciple.realm + 1})",
                            color = JadeQi,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CelestialGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // HP Bar (Blue Qi)
                QiProgressBar(
                    label = "Health / Body Qi",
                    current = disciple.hp,
                    max = disciple.maxHp,
                    fillColor = SpiritCyan
                )

                Spacer(modifier = Modifier.height(8.dp))

                // MP Bar (Gold Qi)
                QiProgressBar(
                    label = "Spiritual Mana / Dao MP",
                    current = disciple.mp,
                    max = disciple.maxMp,
                    fillColor = CelestialGold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Attributes Matrix
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatBox("Attack", "${disciple.atk}", FlameCrimson)
                    StatBox("Defense", "${disciple.def}", SpiritCyan)
                    StatBox("Wisdom", "${disciple.wis}", ImperialPurple)
                    StatBox("Luck", "${disciple.lck}", CelestialGold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = JadeQi),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Channel Spiritual Qi", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QiProgressBar(
    label: String,
    current: Int,
    max: Int,
    fillColor: Color
) {
    val progress = if (max > 0) (current.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 1f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = label, color = CloudMist, fontSize = 11.sp)
            Text(text = "$current / $max", color = fillColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFF111122))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(fillColor)
            )
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, tint: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0x33000000),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.5f)),
        modifier = Modifier.width(68.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            Text(text = label, color = CloudMist.copy(alpha = 0.7f), fontSize = 10.sp)
            Text(text = value, color = tint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * PANEL 2: INVENTORY PANEL (4x5 Grid Slots with Glowing Borders)
 */
@Composable
private fun InventoryPanelModal(
    sectData: SectData?,
    onDismiss: () -> Unit
) {
    val sampleItems = remember {
        listOf(
            InventorySlotItem("Spirit Stone Cache", "500 High-grade Stones", Icons.Default.Diamond, CelestialGold),
            InventorySlotItem("Nine-Leaf Ginseng", "Rare cultivation herb", Icons.Default.LocalFlorist, JadeQi),
            InventorySlotItem("Heavenly Ore", "Forging material for swords", Icons.Default.Workspaces, SpiritCyan),
            InventorySlotItem("Thunder Talisman", "Deals AoE lightning damage", Icons.Default.AutoAwesome, ImperialPurple),
            InventorySlotItem("Golden Elixir", "Instantly restores 500 Qi", Icons.Default.FlashOn, CelestialGold),
            InventorySlotItem("Immortal Robe", "Body armor +120 Def", Icons.Default.Shield, SpiritCyan)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(onClick = onDismiss)
            .padding(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NightSkyDark.copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(2.dp, CelestialGold),
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Qiankun Storage Bag",
                        color = CelestialGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CelestialGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(280.dp)
                ) {
                    items(sampleItems) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x33000000),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, item.rarityColor),
                            modifier = Modifier
                                .height(85.dp)
                                .clickable {}
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = item.rarityColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.title,
                                    color = CloudMist,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class InventorySlotItem(
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val rarityColor: Color
)

/**
 * PANEL 3: BAMBOO QUEST PANEL (Ancient Scroll Styling #1A1A2E + Gold)
 */
@Composable
private fun BambooQuestPanelModal(
    sectData: SectData?,
    onDismiss: () -> Unit,
    onClaimQuest: (Long) -> Unit
) {
    val quests = remember {
        listOf(
            QuestItem("Tame Spirit Beast", "Subjugate demonic wolves at Mount Shu borders.", 350L, true),
            QuestItem("Refine Qi Pills", "Alchemy Pavilion order for disciples.", 200L, false),
            QuestItem("Mine Azure Crystals", "Excavate 100 Spirit Ores.", 150L, true),
            QuestItem("Meditate at Sword Peak", "Attain breakthrough insight.", 500L, false)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(onClick = onDismiss)
            .padding(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NightSkyDark.copy(alpha = 0.96f),
            border = androidx.compose.foundation.BorderStroke(2.dp, DaoBorderGold),
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📜 Bamboo Sect Bounties",
                        color = CelestialGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CelestialGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(260.dp)
                ) {
                    items(quests) { q ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x33000000),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DaoBorderGold.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = q.title,
                                        color = CelestialGold,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = q.desc,
                                        color = MutedScroll,
                                        fontSize = 11.sp
                                    )
                                }

                                Button(
                                    onClick = { onClaimQuest(q.reward) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (q.isReady) JadeQi else Color.Gray.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (q.isReady) "Claim +${q.reward}" else "Pending",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class QuestItem(
    val title: String,
    val desc: String,
    val reward: Long,
    val isReady: Boolean
)

// -----------------------------------------------------------------------------
// 7. ISLAND INSPECTION & UPGRADE MODAL
// -----------------------------------------------------------------------------

@Composable
private fun IslandInspectionModal(
    island: IslandNode,
    sectData: SectData?,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = NightSkyDark.copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(2.dp, island.accentColor),
        shadowElevation = 24.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = island.chineseName,
                        color = CelestialGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${island.name} (Lv.${island.level})",
                        color = island.accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CelestialGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = island.description,
                color = CloudMist.copy(alpha = 0.85f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0x33000000),
                border = androidx.compose.foundation.BorderStroke(1.dp, island.accentColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text("Current Yield:", color = CloudMist, fontSize = 12.sp)
                    Text(island.yieldText, color = JadeQi, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(containerColor = CelestialGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Upgrade, contentDescription = null, tint = NightSkyDark)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Upgrade Pavilion (${island.upgradeCost} Stones)",
                    color = NightSkyDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
