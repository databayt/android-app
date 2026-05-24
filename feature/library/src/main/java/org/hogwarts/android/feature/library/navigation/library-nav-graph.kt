package org.hogwarts.android.feature.library.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.library.ui.BookCatalogScreen
import org.hogwarts.android.feature.library.ui.BookDetailScreen
import org.hogwarts.android.feature.library.ui.MyBorrowingsScreen

@Serializable data object LibraryCatalog
@Serializable data class LibraryBookDetail(val bookId: String)
@Serializable data object LibraryMyBorrowings

fun NavGraphBuilder.libraryCatalogScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBook: (String) -> Unit,
    onNavigateToMyBorrowings: () -> Unit
) {
    composable<LibraryCatalog> {
        BookCatalogScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToBook = onNavigateToBook,
            onNavigateToMyBorrowings = onNavigateToMyBorrowings
        )
    }
}

fun NavGraphBuilder.bookDetailScreen(
    onNavigateBack: () -> Unit
) {
    composable<LibraryBookDetail> {
        BookDetailScreen(onNavigateBack = onNavigateBack)
    }
}

fun NavGraphBuilder.myBorrowingsScreen(
    onNavigateBack: () -> Unit
) {
    composable<LibraryMyBorrowings> {
        MyBorrowingsScreen(onNavigateBack = onNavigateBack)
    }
}
