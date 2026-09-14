package org.hogwarts.android.feature.notifications.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.feature.notifications.data.repository.PageResult
import org.hogwarts.android.feature.notifications.domain.model.NotificationChannel
import org.hogwarts.android.feature.notifications.domain.model.NotificationKind
import org.hogwarts.android.feature.notifications.testing.FakeNotificationsRepository
import org.hogwarts.android.feature.notifications.testing.notification
import org.hogwarts.android.feature.notifications.ui.preferences.PreferencesViewModel
import org.hogwarts.android.feature.notifications.ui.preferences.SaveOutcome
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelsTest {

    private val dispatcher = StandardTestDispatcher()
    private val repo = FakeNotificationsRepository().apply {
        rows = listOf(notification("n-1", url = "/announcements"), notification("n-2", read = true), notification("n-3"))
    }

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `the unread route starts on the unread tab and asks only for unread`() = runTest(dispatcher) {
        val vm = NotificationsViewModel(repo)
        vm.start(NotificationsTab.Unread)
        advanceUntilIdle()
        assertEquals(listOf("page:true:1"), repo.calls)
        assertEquals(2, vm.uiState.value.items.size)
        assertEquals(2, vm.uiState.value.unreadCount)
    }

    @Test
    fun `opening a card marks it read and hands back its path`() = runTest(dispatcher) {
        val vm = NotificationsViewModel(repo)
        vm.start(NotificationsTab.All)
        advanceUntilIdle()
        val target = vm.open(vm.uiState.value.items.first())
        advanceUntilIdle()
        assertEquals(NotificationTarget.Path("/announcements"), target)
        assertTrue(vm.uiState.value.items.first().isRead)
        assertTrue("read:n-1" in repo.calls)
    }

    @Test
    fun `a read card opens without another read call`() = runTest(dispatcher) {
        val vm = NotificationsViewModel(repo)
        vm.start(NotificationsTab.All)
        advanceUntilIdle()
        vm.open(vm.uiState.value.items[1])
        advanceUntilIdle()
        assertFalse(repo.calls.any { it.startsWith("read:") })
    }

    @Test
    fun `dismissing deletes and reloads the page`() = runTest(dispatcher) {
        val vm = NotificationsViewModel(repo)
        vm.start(NotificationsTab.All)
        advanceUntilIdle()
        vm.delete(vm.uiState.value.items[2])
        advanceUntilIdle()
        assertEquals(listOf("n-1", "n-2"), vm.uiState.value.items.map { it.id })
        assertTrue(vm.uiState.value.deleting.isEmpty())
        assertEquals(2, repo.calls.count { it.startsWith("page:") })
    }

    @Test
    fun `mark all read clears the badge`() = runTest(dispatcher) {
        val vm = NotificationsViewModel(repo)
        vm.start(NotificationsTab.All)
        advanceUntilIdle()
        vm.markAllRead()
        advanceUntilIdle()
        assertEquals(0, vm.uiState.value.unreadCount)
        assertTrue(vm.uiState.value.items.all { it.isRead })
    }

    @Test
    fun `a failed first load shows the failure, a cached page shows offline`() = runTest(dispatcher) {
        repo.result = { _, _ -> PageResult.Failed("offline") }
        val vm = NotificationsViewModel(repo)
        vm.start(NotificationsTab.All)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.failed)

        repo.result = { _, page -> PageResult.Cached(org.hogwarts.android.feature.notifications.domain.model.NotificationPage(repo.rows, 3, 2, page, 20)) }
        vm.retry()
        advanceUntilIdle()
        assertFalse(vm.uiState.value.failed)
        assertTrue(vm.uiState.value.isOffline)
    }

    @Test
    fun `preferences toggle, reset and save every switch`() = runTest(dispatcher) {
        val vm = PreferencesViewModel(repo)
        advanceUntilIdle()
        vm.toggle(NotificationKind.FeeDue, NotificationChannel.Email, true)
        assertTrue(vm.uiState.value.matrix!!.isOn(NotificationKind.FeeDue, NotificationChannel.Email))
        vm.reset()
        assertFalse(vm.uiState.value.matrix!!.isOn(NotificationKind.FeeDue, NotificationChannel.Email))

        vm.toggle(NotificationKind.FeeDue, NotificationChannel.WhatsApp, true)
        vm.save()
        advanceUntilIdle()
        assertEquals(SaveOutcome.Saved, vm.uiState.value.outcome)
        assertTrue(repo.saved!!.isOn(NotificationKind.FeeDue, NotificationChannel.WhatsApp))

        repo.failSave = true
        vm.save()
        advanceUntilIdle()
        assertEquals(SaveOutcome.Failed, vm.uiState.value.outcome)
    }
}
