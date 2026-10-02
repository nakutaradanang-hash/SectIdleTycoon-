package com.sect.idle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.sect.idle.apm.ApmManager
import com.sect.idle.apm.StartupMetricsTracker
import com.sect.idle.gameplay.SectLifecycleManager
import com.sect.idle.hub.SectHubTab
import com.sect.idle.hub.SectHubTheme
import com.sect.idle.hub.SectHubViewModel
import com.sect.idle.hub.ui.SectHubScreen
import com.sect.idle.ui.GameActivity
import com.sect.idle.utils.CrashHandler
import com.sect.idle.utils.LocalizationManager

/**
 * MainActivity - Modern Jetpack Compose Material 3 Main Activity for Sect Idle Cultivation.
 * Hosts the Material 3 dashboard, tick-based time simulation, Room-backed disciple management,
 * and multi-tab Xianxia realm navigation.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: SectHubViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply user selected language locale
        LocalizationManager.getInstance(this).applyLocale(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        StartupMetricsTracker.get().recordSplashCreated()

        // Install Centralized UncaughtExceptionHandler & Real-Time APM System
        CrashHandler.init(applicationContext).install(applicationContext)
        ApmManager.init(applicationContext)

        // Initialize Sect state & repository in background
        SectLifecycleManager.getInstance().loadOrInitializeSect(applicationContext)

        // Allow deep-linking or intent extra tab selection
        val initialTabName = intent.getStringExtra(EXTRA_INITIAL_TAB)
        if (initialTabName != null) {
            try {
                val tab = SectHubTab.valueOf(initialTabName)
                viewModel.selectTab(tab)
            } catch (ignored: Exception) {}
        }

        StartupMetricsTracker.get().recordSplashReady()

        setContent {
            SectHubTheme {
                SectHubScreen(
                    viewModel = viewModel,
                    onNavigateBackToGame = {
                        // Launch full-screen Canvas 2D/3D GameActivity if requested
                        try {
                            val intent = Intent(this@MainActivity, GameActivity::class.java)
                            startActivity(intent)
                        } catch (ignored: Exception) {}
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncFromSectData()
    }

    companion object {
        const val EXTRA_INITIAL_TAB = "extra_initial_tab"
    }
}
