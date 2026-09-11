package com.sect.idle.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * SectSceneViewModelTest - Unit tests for SectSceneViewModel state management,
 * scene mode transitions, audio triggers, camera focus updates, and telemetry monitoring.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SectSceneViewModelTest {

    private lateinit var application: Application
    private lateinit var viewModel: SectSceneViewModel

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = SectSceneViewModel(application)
    }

    @Test
    fun testInitialState() {
        val state = viewModel.state.value
        assertNotNull(state)
        assertEquals(SectSceneMode.REALM_OVERVIEW, state.currentMode)
        assertEquals(WeatherAtmosphere.CLEAR_QI_AURA, state.atmosphere)
        assertEquals(2000f, state.cameraFocusX)
        assertEquals(2000f, state.cameraFocusY)
        assertEquals(1.0f, state.cameraZoom)
        assertNull(state.selectedBuildingIndex)
        assertNull(state.selectedDiscipleId)
    }

    @Test
    fun testSelectBuilding() {
        viewModel.selectBuilding(2)
        assertEquals(2, viewModel.state.value.selectedBuildingIndex)
        assertNull(viewModel.state.value.selectedDiscipleId)

        viewModel.selectBuilding(null)
        assertNull(viewModel.state.value.selectedBuildingIndex)
    }

    @Test
    fun testSelectDisciple() {
        viewModel.selectDisciple("D1001", 0)
        assertEquals("D1001", viewModel.state.value.selectedDiscipleId)
        assertEquals(0, viewModel.state.value.selectedDiscipleIndex)
        assertNull(viewModel.state.value.selectedBuildingIndex)
    }

    @Test
    fun testCameraFocusUpdate() {
        viewModel.setCameraFocus(500f, 600f, 1.5f)
        assertEquals(500f, viewModel.state.value.cameraFocusX)
        assertEquals(600f, viewModel.state.value.cameraFocusY)
        assertEquals(1.5f, viewModel.state.value.cameraZoom)
    }

    @Test
    fun testDayNightCycleUpdate() {
        viewModel.updateDayNight(14.0f)
        assertEquals(14.0f, viewModel.state.value.timeOfDayHour)
        assertEquals(false, viewModel.state.value.isNightTime)

        viewModel.updateDayNight(22.0f)
        assertEquals(22.0f, viewModel.state.value.timeOfDayHour)
        assertEquals(true, viewModel.state.value.isNightTime)
    }

    @Test
    fun testAtmosphereAndQualitySettings() {
        viewModel.setAtmosphere(WeatherAtmosphere.PURPLE_SPIRIT_STORM)
        assertEquals(WeatherAtmosphere.PURPLE_SPIRIT_STORM, viewModel.state.value.atmosphere)

        viewModel.setRenderQuality(RenderQualityTier.MEDIUM_3D)
        assertEquals(RenderQualityTier.MEDIUM_3D, viewModel.state.value.renderQuality)
    }

    @Test
    fun testTelemetryUpdates() {
        viewModel.updateTelemetry(58, 120, 45.5f)
        assertEquals(58, viewModel.state.value.currentFps)
        assertEquals(120, viewModel.state.value.activeParticles)
        assertEquals(45.5f, viewModel.state.value.heapMemoryMb)
    }
}
