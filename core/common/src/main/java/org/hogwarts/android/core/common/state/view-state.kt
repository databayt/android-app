package org.hogwarts.android.core.common.state

/**
 * Unified view state for all UI screens.
 *
 * Replaces per-feature sealed interfaces with a single generic state machine.
 * Matches iOS app's ViewState<T> pattern from swift-app/hogwarts/shared/ui/view-state.swift.
 *
 * States:
 * - Idle: Initial state before any action
 * - Loading: Fetching data (no previous data available)
 * - Loaded: Data successfully loaded
 * - Empty: Data loaded but result set is empty
 * - Error: An error occurred (no cached data available)
 * - Offline: Device is offline but cached data may be available
 */
sealed interface ViewState<out T> {

    /** Initial state before any data fetch. */
    data object Idle : ViewState<Nothing>

    /** Loading state - fetching data with no prior cache. */
    data object Loading : ViewState<Nothing>

    /** Data successfully loaded. */
    data class Loaded<T>(val data: T) : ViewState<T>

    /** Data fetched but empty result set. */
    data object Empty : ViewState<Nothing>

    /** Error state with message. */
    data class Error(val message: String) : ViewState<Nothing>

    /** Offline state, may contain cached data. */
    data class Offline<T>(val cachedData: T? = null) : ViewState<T>
}

/** Check if state is Loading. */
val <T> ViewState<T>.isLoading: Boolean
    get() = this is ViewState.Loading

/** Check if state is Loaded. */
val <T> ViewState<T>.isLoaded: Boolean
    get() = this is ViewState.Loaded

/** Check if state is Error. */
val <T> ViewState<T>.isError: Boolean
    get() = this is ViewState.Error

/** Check if state is Offline. */
val <T> ViewState<T>.isOffline: Boolean
    get() = this is ViewState.Offline

/** Check if state is Empty. */
val <T> ViewState<T>.isEmpty: Boolean
    get() = this is ViewState.Empty

/** Get data if Loaded, cached data if Offline, or null otherwise. */
val <T> ViewState<T>.dataOrNull: T?
    get() = when (this) {
        is ViewState.Loaded -> data
        is ViewState.Offline -> cachedData
        else -> null
    }

/** Get error message if Error state, or null otherwise. */
val <T> ViewState<T>.errorOrNull: String?
    get() = (this as? ViewState.Error)?.message

/** Map loaded data to a new type. */
fun <T, R> ViewState<T>.map(transform: (T) -> R): ViewState<R> = when (this) {
    is ViewState.Idle -> ViewState.Idle
    is ViewState.Loading -> ViewState.Loading
    is ViewState.Loaded -> ViewState.Loaded(transform(data))
    is ViewState.Empty -> ViewState.Empty
    is ViewState.Error -> ViewState.Error(message)
    is ViewState.Offline -> ViewState.Offline(cachedData?.let(transform))
}

/** Execute action when state is Loaded. */
inline fun <T> ViewState<T>.onLoaded(action: (T) -> Unit): ViewState<T> {
    if (this is ViewState.Loaded) action(data)
    return this
}

/** Execute action when state is Error. */
inline fun <T> ViewState<T>.onError(action: (String) -> Unit): ViewState<T> {
    if (this is ViewState.Error) action(message)
    return this
}
