package com.sect.idle.hub.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sect.idle.hub.*
import com.sect.idle.hub.router.HashRouter
import com.sect.idle.hub.router.LocalHashRouter
import com.sect.idle.security.EncryptionManager
import com.sect.idle.security.MfaManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SectHubViewModel,
    modifier: Modifier = Modifier
) {
    val router = LocalHashRouter.current
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("IdleSect_UserSettings", Context.MODE_PRIVATE) }

    // Audio & Haptics States
    var bgmVolume by remember { mutableFloatStateOf(prefs.getFloat("pref_bgm_volume", 0.75f)) }
    var sfxVolume by remember { mutableFloatStateOf(prefs.getFloat("pref_sfx_volume", 0.85f)) }
    var ambientSoundEnabled by remember { mutableStateOf(prefs.getBoolean("pref_ambient_enabled", true)) }
    var hapticsEnabled by remember { mutableStateOf(prefs.getBoolean("pref_haptics_enabled", true)) }

    // Notification Toggles
    var notifyBreakthrough by remember { mutableStateOf(prefs.getBoolean("pref_notify_breakthrough", true)) }
    var notifyFullStorage by remember { mutableStateOf(prefs.getBoolean("pref_notify_storage", true)) }
    var notifyInvasion by remember { mutableStateOf(prefs.getBoolean("pref_notify_invasion", true)) }
    var notifyDailyFortune by remember { mutableStateOf(prefs.getBoolean("pref_notify_fortune", true)) }

    // Theme & Visual Preferences
    var selectedThemeIndex by remember { mutableIntStateOf(prefs.getInt("pref_theme_index", 0)) }
    val themeOptions = listOf("Celestial Jade", "Imperial Gold", "Spirit Lotus", "Void Midnight")
    var highFpsMode by remember { mutableStateOf(prefs.getBoolean("pref_high_fps", true)) }
    var particleQuality by remember { mutableIntStateOf(prefs.getInt("pref_particle_quality", 2)) } // 0: Eco, 1: Normal, 2: Ultra

    // Gameplay & Math Preferences
    var autoMeditation by remember { mutableStateOf(prefs.getBoolean("pref_auto_meditation", true)) }
    var useSpiritualUnits by remember { mutableStateOf(prefs.getBoolean("pref_spiritual_units", true)) }
    var simulationSpeed by remember { mutableFloatStateOf(prefs.getFloat("pref_sim_speed", 1.0f)) }

    // Dialogs
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showResetConfirmation by remember { mutableStateOf(false) }

    if (showSecurityDialog) {
        SecuritySettingsDialog(
            onDismissRequest = { showSecurityDialog = false },
            sectName = viewModel.uiState.value.sectName
        )
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reincarnate & Reset Sect Data?", fontWeight = FontWeight.Bold) },
            text = { Text("This will reset your local cultivation data and start anew with your current Heavenly Karma.", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        prefs.edit().clear().apply()
                        showResetConfirmation = false
                        Toast.makeText(context, "Sect Karma Reset. Reincarnating...", Toast.LENGTH_SHORT).show()
                        router.navigate("#/dashboard")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Reincarnate", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancel", color = CloudMistWhite)
                }
            },
            containerColor = TwilightSurface
        )
    }

    // Determine active section from HashRouter route (e.g. #/settings/audio, #/settings/themes)
    val activeSection = when {
        router.isActive("#/settings/audio") -> 0
        router.isActive("#/settings/notifications") -> 1
        router.isActive("#/settings/themes") -> 2
        router.isActive("#/settings/gameplay") -> 3
        else -> 0 // Default overview / audio
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_scaffold"),
        containerColor = VoidBlack,
        topBar = {
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
                                .background(CelestialGold.copy(alpha = 0.2f))
                                .border(1.dp, CelestialGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚙️", fontSize = 14.sp)
                        }
                        Column {
                            Text("Sect Preferences & Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = CloudMistWhite)
                            Text("Route: ${router.currentRoute}", fontSize = 10.sp, color = JadeCyan)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (!router.pop()) {
                                router.navigate("#/dashboard")
                            }
                        },
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CloudMistWhite)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSecurityDialog = true },
                        modifier = Modifier.testTag("open_security_btn")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = "Security Vault", tint = CelestialGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TwilightSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // HashRouter Section Navigation Tabs
            TabRow(
                selectedTabIndex = activeSection,
                containerColor = TwilightSurface,
                contentColor = JadeCyan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeSection == 0,
                    onClick = { router.replace("#/settings/audio") },
                    text = { Text("🔊 Audio", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_settings_audio")
                )
                Tab(
                    selected = activeSection == 1,
                    onClick = { router.replace("#/settings/notifications") },
                    text = { Text("🔔 Alerts", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_settings_notifications")
                )
                Tab(
                    selected = activeSection == 2,
                    onClick = { router.replace("#/settings/themes") },
                    text = { Text("🎨 Themes", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_settings_themes")
                )
                Tab(
                    selected = activeSection == 3,
                    onClick = { router.replace("#/settings/gameplay") },
                    text = { Text("⚡ Gameplay", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_settings_gameplay")
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                when (activeSection) {
                    0 -> {
                        // Audio & Haptic Sensory Settings
                        item {
                            SettingsCategoryCard(title = "🎵 Music & Soundscapes", icon = "🎶") {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    SliderPreference(
                                        title = "Background Music (BGM)",
                                        value = bgmVolume,
                                        onValueChange = {
                                            bgmVolume = it
                                            prefs.edit().putFloat("pref_bgm_volume", it).apply()
                                        }
                                    )
                                    SliderPreference(
                                        title = "Sound Effects (SFX)",
                                        value = sfxVolume,
                                        onValueChange = {
                                            sfxVolume = it
                                            prefs.edit().putFloat("pref_sfx_volume", it).apply()
                                        }
                                    )
                                    TogglePreference(
                                        title = "Ambient Immortal Wind & Qi Chimes",
                                        subtitle = "Play dynamic environmental soundscapes",
                                        checked = ambientSoundEnabled,
                                        onCheckedChange = {
                                            ambientSoundEnabled = it
                                            prefs.edit().putBoolean("pref_ambient_enabled", it).apply()
                                        }
                                    )
                                    TogglePreference(
                                        title = "Haptic Vibration Feedback",
                                        subtitle = "Tactile sensations on breakthrough & combat",
                                        checked = hapticsEnabled,
                                        onCheckedChange = {
                                            hapticsEnabled = it
                                            prefs.edit().putBoolean("pref_haptics_enabled", it).apply()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // Notifications & Alarms
                        item {
                            SettingsCategoryCard(title = "🔔 Cultivation Alarms & Alerts", icon = "📣") {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    TogglePreference(
                                        title = "Breakthrough Ready Alert",
                                        subtitle = "Notify when a disciple achieves peak Qi",
                                        checked = notifyBreakthrough,
                                        onCheckedChange = {
                                            notifyBreakthrough = it
                                            prefs.edit().putBoolean("pref_notify_breakthrough", it).apply()
                                        }
                                    )
                                    TogglePreference(
                                        title = "Treasury & Resource Full Alert",
                                        subtitle = "Alert when spirit stone storehouse reaches 100%",
                                        checked = notifyFullStorage,
                                        onCheckedChange = {
                                            notifyFullStorage = it
                                            prefs.edit().putBoolean("pref_notify_storage", it).apply()
                                        }
                                    )
                                    TogglePreference(
                                        title = "Demonic Invasion Warning",
                                        subtitle = "Immediate notification when sect perimeter is challenged",
                                        checked = notifyInvasion,
                                        onCheckedChange = {
                                            notifyInvasion = it
                                            prefs.edit().putBoolean("pref_notify_invasion", it).apply()
                                        }
                                    )
                                    TogglePreference(
                                        title = "Daily Dao Fortune Reminder",
                                        subtitle = "Remind to claim daily heavenly destiny blessings",
                                        checked = notifyDailyFortune,
                                        onCheckedChange = {
                                            notifyDailyFortune = it
                                            prefs.edit().putBoolean("pref_notify_fortune", it).apply()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // Visuals & Themes
                        item {
                            SettingsCategoryCard(title = "🎨 Xianxia Visual Themes & Rendering", icon = "🌌") {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text("Sect Interface Theme Palette:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CloudMistWhite)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        themeOptions.forEachIndexed { index, name ->
                                            val isSelected = selectedThemeIndex == index
                                            OutlinedButton(
                                                onClick = {
                                                    selectedThemeIndex = index
                                                    prefs.edit().putInt("pref_theme_index", index).apply()
                                                    Toast.makeText(context, "$name Theme Applied!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = if (isSelected) JadeDark.copy(alpha = 0.4f) else Color.Transparent
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.5.dp,
                                                    if (isSelected) JadeCyan else TwilightBorder
                                                )
                                            ) {
                                                Text(name.split(" ")[0], fontSize = 11.sp, color = if (isSelected) JadeCyan else CloudMistWhite, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                            }
                                        }
                                    }

                                    TogglePreference(
                                        title = "High Refresh Rate (60 FPS Mode)",
                                        subtitle = "Smooth particle dynamics and render animations",
                                        checked = highFpsMode,
                                        onCheckedChange = {
                                            highFpsMode = it
                                            prefs.edit().putBoolean("pref_high_fps", it).apply()
                                        }
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Spiritual Particle Quality", fontSize = 13.sp, color = CloudMistWhite, fontWeight = FontWeight.Medium)
                                            Text(
                                                text = when (particleQuality) { 0 -> "Eco (Low battery usage)"; 1 -> "Balanced"; else -> "Ultra (Full VFX)" },
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf("Eco", "Bal", "Ultra").forEachIndexed { qIndex, qLabel ->
                                                val isQSelected = particleQuality == qIndex
                                                FilterChip(
                                                    selected = isQSelected,
                                                    onClick = {
                                                        particleQuality = qIndex
                                                        prefs.edit().putInt("pref_particle_quality", qIndex).apply()
                                                    },
                                                    label = { Text(qLabel, fontSize = 10.sp) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // Gameplay & Numerical Progression Tuning
                        item {
                            SettingsCategoryCard(title = "⚡ Mathematical Engine & Progression", icon = "🧮") {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    TogglePreference(
                                        title = "Auto-Meditation Chamber Dispatch",
                                        subtitle = "Automatically seat idle disciples in meditation array",
                                        checked = autoMeditation,
                                        onCheckedChange = {
                                            autoMeditation = it
                                            prefs.edit().putBoolean("pref_auto_meditation", it).apply()
                                        }
                                    )

                                    TogglePreference(
                                        title = "Xianxia Large Number Formatting",
                                        subtitle = "Display resources as '12.5M Spirit Qi' instead of raw numbers",
                                        checked = useSpiritualUnits,
                                        onCheckedChange = {
                                            useSpiritualUnits = it
                                            prefs.edit().putBoolean("pref_spiritual_units", it).apply()
                                        }
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Simulation Game Loop Speed", fontSize = 13.sp, color = CloudMistWhite, fontWeight = FontWeight.Medium)
                                            Text("${simulationSpeed}x Speed Multiplier", fontSize = 11.sp, color = CelestialGold, fontWeight = FontWeight.Bold)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            listOf(1.0f, 1.5f, 2.0f).forEach { speed ->
                                                val isSpeedSelected = simulationSpeed == speed
                                                FilterChip(
                                                    selected = isSpeedSelected,
                                                    onClick = {
                                                        simulationSpeed = speed
                                                        prefs.edit().putFloat("pref_sim_speed", speed).apply()
                                                    },
                                                    label = { Text("${speed}x", fontSize = 10.sp) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Danger Zone
                        item {
                            SettingsCategoryCard(title = "⚠️ Sect Vault & Reincarnation", icon = "🛡️") {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { showSecurityDialog = true },
                                        modifier = Modifier.fillMaxWidth().testTag("open_security_dialog_from_settings"),
                                        colors = ButtonDefaults.buttonColors(containerColor = JadeDark),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, JadeCyan)
                                    ) {
                                        Text("Open Heavenly Security & MFA Shield", fontSize = 12.sp, color = CloudMistWhite)
                                    }

                                    OutlinedButton(
                                        onClick = { showResetConfirmation = true },
                                        modifier = Modifier.fillMaxWidth().testTag("reincarnate_reset_button"),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed)
                                    ) {
                                        Text("Reincarnate & Reset Local Progress", fontSize = 12.sp, color = DangerRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    icon: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TwilightSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, TwilightBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(icon, fontSize = 16.sp)
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CelestialGold)
            }
            HorizontalDivider(color = TwilightBorder)
            content()
        }
    }
}

@Composable
private fun TogglePreference(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontSize = 13.sp, color = CloudMistWhite, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = JadeCyan,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = TwilightElevated
            )
        )
    }
}

@Composable
private fun SliderPreference(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 13.sp, color = CloudMistWhite, fontWeight = FontWeight.Medium)
            Text("${(value * 100).toInt()}%", fontSize = 12.sp, color = JadeCyan, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = JadeCyan,
                activeTrackColor = JadeCyan,
                inactiveTrackColor = TwilightElevated
            )
        )
    }
}
