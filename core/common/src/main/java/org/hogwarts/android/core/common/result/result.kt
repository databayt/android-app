package org.hogwarts.android.core.common.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * A generic class that holds a value or an error status.
 * Used for wrapping API responses and database operations.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}

/**
 * Extension to check if Result is successful
 */
val <T> Result<T>.isSuccess: Boolean
    get() = this is Result.Success

/**
 * Extension to check if Result is an error
 */
val <T> Result<T>.isError: Boolean
    get() = this is Result.Error

/**
 * Extension to check if Result is loading
 */
val <T> Result<T>.isLoading: Boolean
    get() = this is Result.Loading

/**
 * Get data or null if error/loading
 */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

/**
 * Get data or throw exception
 */
fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Error -> throw exception
    is Result.Loading -> throw IllegalStateException("Result is still loading")
}

/**
 * Get data or default value
 */
fun <T> Result<T>.getOrDefault(default: T): T = when (this) {
    is Result.Success -> data
    else -> default
}

/**
 * Map success data
 */
fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    is Result.Loading -> this
}

/**
 * Execute action on success
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        action(data)
    }
    return this
}

/**
 * Execute action on error
 */
inline fun <T> Result<T>.onError(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) {
        action(exception)
    }
    return this
}

/**
 * Convert Flow to Flow<Result<T>> with loading and error handling
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
    .map<T, Result<T>> { Result.Success(it) }
    .onStart { emit(Result.Loading) }
    .catch { emit(Result.Error(it)) }

/**
 * Execute a suspending block and wrap result
 */
suspend fun <T> runCatching(block: suspend () -> T): Result<T> = try {
    Result.Success(block())
} catch (e: Exception) {
    Result.Error(e)
}
