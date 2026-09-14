package org.hogwarts.android.feature.settings.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val prefs = FakePreferences()
    private val appliedLocales = mutableListOf<String>()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = SettingsViewModel(prefs) { appliedLocales += it }

    @Test
    fun `opens on appearance, like the web`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(SettingsTab.Appearance, vm.uiState.value.tab)
    }

    @Test
    fun `reflects and writes the saved theme mode`() = runTest(dispatcher) {
        prefs.mode.value = "dark"
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(ThemeMode.Dark, vm.uiState.value.themeMode)

        vm.setThemeMode(ThemeMode.Light)
        advanceUntilIdle()
        assertEquals("light", prefs.mode.value)
        assertEquals(ThemeMode.Light, vm.uiState.value.themeMode)
    }

    @Test
    fun `an unknown stored mode reads as system`() = runTest(dispatcher) {
        prefs.mode.value = "sepia"
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(ThemeMode.System, vm.uiState.value.themeMode)
    }

    @Test
    fun `language applies the per-app locale and remembers it`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.setLanguage("en")
        advanceUntilIdle()
        assertEquals(listOf("en"), appliedLocales)
        assertEquals("en", prefs.language)
    }

    @Test
    fun `tabs switch in place`() = runTest(dispatcher) {
        val vm = viewModel()
        vm.selectTab(SettingsTab.Language)
        assertEquals(SettingsTab.Language, vm.uiState.value.tab)
    }

    private class FakePreferences : SettingsPreferences {
        val mode = MutableStateFlow("system")
        var language: String? = null
        override fun themeMode() = mode
        override suspend fun setThemeMode(mode: String) { this.mode.value = mode }
        override suspend fun setLanguage(language: String) { this.language = language }
    }
}
