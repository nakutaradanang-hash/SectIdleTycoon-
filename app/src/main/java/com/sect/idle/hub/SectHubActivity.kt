package com.sect.idle.hub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.sect.idle.hub.ui.SectHubScreen

class SectHubActivity : ComponentActivity() {

    private val viewModel: SectHubViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialTabName = intent.getStringExtra(EXTRA_INITIAL_TAB)
        if (initialTabName != null) {
            try {
                val tab = SectHubTab.valueOf(initialTabName)
                viewModel.selectTab(tab)
            } catch (ignored: Exception) {}
        }

        setContent {
            SectHubTheme {
                SectHubScreen(
                    viewModel = viewModel,
                    onNavigateBackToGame = {
                        finish()
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
                        } else {
                            @Suppress("DEPRECATION")
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
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
