package org.hogwarts.android.feature.messaging.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.feature.messaging.FakeDrafts
import org.hogwarts.android.feature.messaging.FakeMessagingRepository
import org.hogwarts.android.feature.messaging.FakeMessagingRepository.Companion.ME
import org.hogwarts.android.feature.messaging.chat
import org.hogwarts.android.feature.messaging.message
import org.hogwarts.android.feature.messaging.ui.chats.ChatFilter
import org.hogwarts.android.feature.messaging.ui.shell.MessagesShellViewModel
import org.hogwarts.android.feature.messaging.ui.shell.MessagesTab
import org.hogwarts.android.feature.messaging.ui.thread.ThreadViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class MessagingViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    // --- Shell ---------------------------------------------------------------

    @Test
    fun `shell polls the list on the web cadence while observed`() = runTest(dispatcher) {
        val repo = FakeMessagingRepository()
        repo.chats.value = listOf(chat("a", "Ali", unread = 2), chat("b", "Class 5", type = "group", unread = 1))
        val vm = MessagesShellViewModel(repo, SavedStateHandle())
        backgroundScope.launch { vm.uiState.collect {} }
        runCurrent()
        assertEquals(1, repo.refreshCount)
        assertTrue(vm.uiState.value.loaded)
        assertEquals(3, vm.uiState.value.totalUnread)
        assertEquals("Minerva", vm.uiState.value.viewer?.username)

        advanceTimeBy(MessagesShellViewModel.LIST_POLL_MS + 1)
        assertEquals(2, repo.refreshCount)
    }

    @Test
    fun `a failed list request is reported, not hidden`() = runTest(dispatcher) {
        val repo = FakeMessagingRepository().apply { refreshOk = false }
        val vm = MessagesShellViewModel(repo, SavedStateHandle())
        backgroundScope.launch { vm.uiState.collect {} }
        runCurrent()
        assertTrue(vm.uiState.value.loadFailed)
    }

    @Test
    fun `read all clears only the unread conversations`() = runTest(dispatcher) {
        val repo = FakeMessagingRepository()
        repo.chats.value = listOf(chat("a", "Ali", unread = 2), chat("b", "Sara"), chat("c", "Class", type = "group", unread = 4))
        val vm = MessagesShellViewModel(repo, SavedStateHandle())
        backgroundScope.launch { vm.uiState.collect {} }
        runCurrent()

        vm.readAll()
        runCurrent()
        assertEquals(listOf("a", "c"), repo.markedRead)
        assertEquals(0, vm.uiState.value.totalUnread)
    }

    @Test
    fun `select chats marks exactly the picked rows and leaves select mode`() = runTest(dispatcher) {
        val repo = FakeMessagingRepository()
        repo.chats.value = listOf(chat("a", "Ali", unread = 2), chat("b", "Sara", unread = 1))
        val vm = MessagesShellViewModel(repo, SavedStateHandle())
        backgroundScope.launch { vm.uiState.collect {} }
        runCurrent()

        vm.startSelecting()
        vm.toggleSelected("b")
        vm.toggleSelected("a")
        vm.toggleSelected("a")
        runCurrent()
        assertEquals(setOf("b"), vm.uiState.value.selected)

        vm.readSelected()
        runCurrent()
        assertEquals(listOf("b"), repo.markedRead)
        assertFalse(vm.uiState.value.selecting)
    }

    @Test
    fun `tab and filter survive in state, the tab across process death`() = runTest(dispatcher) {
        val handle = SavedStateHandle()
        val vm = MessagesShellViewModel(FakeMessagingRepository(), handle)
        backgroundScope.launch { vm.uiState.collect {} }
        vm.selectTab(MessagesTab.Settings)
        vm.setFilter(ChatFilter.Groups)
        runCurrent()
        assertEquals(MessagesTab.Settings, vm.uiState.value.tab)
        assertEquals(ChatFilter.Groups, vm.uiState.value.filter)

        val restored = MessagesShellViewModel(FakeMessagingRepository(), handle)
        assertEquals(MessagesTab.Settings, restored.uiState.value.tab)
    }

    // --- Thread --------------------------------------------------------------

    private fun thread(repo: FakeMessagingRepository, drafts: FakeDrafts = FakeDrafts()) =
        ThreadViewModel(repo, drafts, SavedStateHandle(mapOf("conversationId" to "c1")))

    @Test
    fun `opening a thread fetches the newest page, polls every 5s and marks it read`() = runTest(dispatcher) {
        val repo = FakeMessagingRepository()
        repo.messages.value = mapOf("c1" to listOf(message("m1", "c1", "other", "hi", Instant.parse("2026-09-14T08:00:00Z"))))
        val vm = thread(repo)
        backgroundScope.launch { vm.uiState.collect {} }
        runCurrent()
        assertEquals(1, repo.messageRefreshCount)
        assertEquals(1, vm.uiState.value.messages.size)
        assertEquals(ME, vm.uiState.value.currentUserId)

        advanceTimeBy(ThreadViewModel.READ_DEBOUNCE_MS + 1)
        assertEquals(listOf("c1"), repo.markedRead)

        advanceTimeBy(ThreadViewModel.ACTIVE_POLL_MS)
        assertEquals(2, repo.messageRefreshCount)
    }

    @Test
    fun `the encryption card waits until no older page is left`() = runTest(dispatcher) {
        val repo = FakeMessagingRepository().apply {
            newestCursor = "cur-1"
            olderPages["cur-1"] = listOf(message("m0", "c1", "other", "old", Instant.parse("2026-09-10T08:00:00Z"))) to null
        }
        val vm = thread(repo)
        backgroundScope.launch { vm.uiState.collect {} }
        runCurrent()
        assertTrue(vm.uiState.value.hasMore)

        vm.loadOlder()
        runCurrent()
        assertFalse(vm.uiState.value.hasMore)
        assertEquals("m0", vm.uiState.value.messages.first().id)

        // The next poll still reports a cursor for the newest page; history already paged stays complete.
        advanceTimeBy(ThreadViewModel.ACTIVE_POLL_MS + 1)
        assertFalse(vm.uiState.value.hasMore)
    }

    @Test
    fun `sending clears the draft and hands the text to the queued sender`() = runTest(dispatcher) {
        val repo = FakeMessagingRepository()
        val drafts = FakeDrafts().apply { store.value = mapOf("c1" to "half a thought") }
        val vm = thread(repo, drafts)
        backgroundScope.launch { vm.uiState.collect {} }
        assertEquals("half a thought", vm.initialDraft())

        vm.send("   ")
        vm.send("See you at 9")
        runCurrent()
        assertEquals(listOf("See you at 9"), repo.sent)
        assertEquals("", drafts.store.value["c1"])
        assertEquals("See you at 9", vm.uiState.value.messages.last().content)
    }

    @Test
    fun `a failed bubble is retried, never marked sent by the view model`() = runTest(dispatcher) {
        val repo = FakeMessagingRepository().apply { serverAccepts = false }
        val vm = thread(repo)
        backgroundScope.launch { vm.uiState.collect {} }
        vm.send("offline")
        runCurrent()
        val failed = vm.uiState.value.messages.last()
        assertEquals("failed", failed.status)

        vm.retry(failed.id)
        runCurrent()
        assertEquals(listOf(failed.id), repo.retried)
        assertEquals("failed", vm.uiState.value.messages.last().status)
    }
}
