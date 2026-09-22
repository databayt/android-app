package org.hogwarts.android.feature.library.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.hogwarts.android.feature.library.ui.BookCatalogScreen
import org.hogwarts.android.feature.library.ui.AllBooksScreen
import org.hogwarts.android.feature.library.ui.BookDetailScreen
import org.hogwarts.android.feature.library.ui.MyBorrowingsScreen

@Serializable data object LibraryCatalog
@Serializable data class LibraryBookDetail(val bookId: String)
@Serializable data object LibraryMyBorrowings
/** `/library/books` — optionally opened on a search (an author) or a grade level. */
@Serializable data class LibraryBooks(val search: String? = null, val gradeLevel: String? = null)

fun NavGraphBuilder.libraryCatalogScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBook: (String) -> Unit,
    onNavigateToMyBorrowings: () -> Unit,
    onNavigateToAllBooks: () -> Unit = {},
) {
    composable<LibraryCatalog> {
        BookCatalogScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToBook = onNavigateToBook,
            onNavigateToMyBorrowings = onNavigateToMyBorrowings,
            onNavigateToAllBooks = onNavigateToAllBooks,
        )
    }
}

fun NavGraphBuilder.bookDetailScreen(
    onNavigateBack: () -> Unit,
    onOpenBook: (String) -> Unit = {},
    onBrowse: (search: String?, gradeLevel: String?) -> Unit = { _, _ -> },
    onOpenUrl: (String) -> Unit = {},
) {
    composable<LibraryBookDetail> {
        BookDetailScreen(
            onNavigateBack = onNavigateBack,
            onOpenBook = onOpenBook,
            onBrowse = onBrowse,
            onOpenUrl = onOpenUrl,
        )
    }
}

fun NavGraphBuilder.allBooksScreen(onOpenBook: (String) -> Unit) {
    composable<LibraryBooks> {
        AllBooksScreen(onOpenBook = onOpenBook)
    }
}

fun NavGraphBuilder.myBorrowingsScreen(
    onNavigateBack: () -> Unit
) {
    composable<LibraryMyBorrowings> {
        MyBorrowingsScreen(onNavigateBack = onNavigateBack)
    }
}
