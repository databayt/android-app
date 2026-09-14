package org.hogwarts.android.feature.announcements.ui

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.feature.announcements.data.repository.DetailResult
import org.hogwarts.android.feature.announcements.domain.model.AnnouncementsPage
import org.hogwarts.android.feature.announcements.testing.FakeAnnouncementsRepository
import org.hogwarts.android.feature.announcements.testing.announcement
import org.hogwarts.android.feature.announcements.testing.tenant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnnouncementsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeAnnouncementsRepository

    private val first = AnnouncementsPage((1..20).map { announcement("ann-$it") }, total = 23, page = 1, perPage = 20)
    private val second = AnnouncementsPage((21..23).map { announcement("ann-$it") }, total = 23, page = 2, perPage = 20)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeAnnouncementsRepository().apply { pages = mutableMapOf(1 to first, 2 to second) }
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads the first page and pages in the rest with load more`() = runTest(dispatcher) {
        val vm = AnnouncementsViewModel(repository, tenant(UserRole.STUDENT))
        advanceUntilIdle()
        assertEquals(20, vm.uiState.value.items.size)
        assertTrue(vm.uiState.value.hasMore)

        vm.loadMore()
        advanceUntilIdle()
        assertEquals(23, vm.uiState.value.items.size)
        assertFalse(vm.uiState.value.hasMore)
    }

    @Test
    fun `readers get no authoring tabs, writers do, admins also get settings`() = runTest(dispatcher) {
        assertFalse(AnnouncementsViewModel(repository, tenant(UserRole.STUDENT)).uiState.value.canWrite)
        assertFalse(AnnouncementsViewModel(repository, tenant(UserRole.GUARDIAN)).uiState.value.canWrite)
        assertFalse(AnnouncementsViewModel(repository, tenant(UserRole.ACCOUNTANT)).uiState.value.canWrite)
        val teacher = AnnouncementsViewModel(repository, tenant(UserRole.TEACHER)).uiState.value
        assertTrue(teacher.canWrite)
        assertFalse(teacher.isAdmin)
        val admin = AnnouncementsViewModel(repository, tenant(UserRole.ADMIN)).uiState.value
        assertTrue(admin.canWrite && admin.isAdmin)
        advanceUntilIdle()
    }

    @Test
    fun `search is debounced and asks the server for the title`() = runTest(dispatcher) {
        val vm = AnnouncementsViewModel(repository, tenant(UserRole.TEACHER))
        advanceUntilIdle()
        vm.onQueryChange("n")
        vm.onQueryChange("notice ann-2")
        advanceTimeBy(AnnouncementsViewModel.SEARCH_DEBOUNCE_MS / 2)
        assertEquals(listOf(1 to ""), repository.requests)
        advanceUntilIdle()
        assertEquals(1 to "notice ann-2", repository.requests.last())
        assertEquals(listOf("ann-2", "ann-20"), vm.uiState.value.items.map { it.id })
    }

    @Test
    fun `offline shows the saved first page, and nothing saved shows the empty state with retry`() = runTest(dispatcher) {
        repository.failList = true
        repository.cached = first
        val saved = AnnouncementsViewModel(repository, tenant(UserRole.STUDENT))
        advanceUntilIdle()
        assertTrue(saved.uiState.value.isOffline)
        assertEquals(20, saved.uiState.value.items.size)
        assertFalse(saved.uiState.value.hasMore)

        repository.cached = null
        val empty = AnnouncementsViewModel(repository, tenant(UserRole.STUDENT))
        advanceUntilIdle()
        assertTrue(empty.uiState.value.failed)
        assertTrue(empty.uiState.value.items.isEmpty())

        repository.failList = false
        empty.retry()
        advanceUntilIdle()
        assertFalse(empty.uiState.value.failed)
        assertEquals(20, empty.uiState.value.items.size)
    }

    @Test
    fun `detail reads, marks offline copies, and shows not found`() = runTest(dispatcher) {
        val item = announcement("ann-1")
        repository.details["ann-1"] = DetailResult.Fresh(item)
        repository.details["ann-2"] = DetailResult.Cached(announcement("ann-2"))
        repository.details["ann-3"] = DetailResult.Failed("offline")

        fun vm(id: String) = AnnouncementDetailViewModel(SavedStateHandle(mapOf("announcementId" to id)), repository)

        val fresh = vm("ann-1"); val cached = vm("ann-2"); val gone = vm("ann-staff"); val failed = vm("ann-3")
        advanceUntilIdle()
        assertEquals(AnnouncementDetailUiState.Ready(item), fresh.uiState.value)
        assertTrue((cached.uiState.value as AnnouncementDetailUiState.Ready).isOffline)
        assertEquals(AnnouncementDetailUiState.NotFound, gone.uiState.value)
        assertTrue(failed.uiState.value is AnnouncementDetailUiState.Failed)
    }
}
