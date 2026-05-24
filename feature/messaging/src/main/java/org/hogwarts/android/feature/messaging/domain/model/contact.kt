package org.hogwarts.android.feature.messaging.domain.model

import java.time.Instant

data class Contact(
    val id: String,
    val firstName: String,
    val lastName: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null,
    val role: String,
    val category: ContactCategory,
    val contextLabel: String? = null,
    val hasWhatsApp: Boolean = false,
    val conversationId: String? = null,
    val lastMessage: String? = null,
    val lastMessageAt: Instant? = null,
    val lastMessageContentType: String? = null,
    val lastMessageHasAttachments: Boolean = false,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isTyping: Boolean = false,
)

enum class ContactCategory(val key: String) {
    TEACHERS("teachers"),
    STUDENTS("students"),
    PARENTS("parents"),
    STAFF("staff"),
    ADMIN("admin"),
    ACCOUNTANTS("accountants"),
    MY_STUDENTS("my_students"),
    MY_TEACHERS("my_teachers"),
    CLASSMATES("classmates"),
    MY_CHILDREN_TEACHERS("my_children_teachers");

    companion object {
        fun fromKey(key: String): ContactCategory? =
            entries.find { it.key == key }
    }
}

data class ContactGroup(
    val category: ContactCategory,
    val contacts: List<Contact>,
)

sealed class SidebarFilter {
    data object All : SidebarFilter()
    data object Unread : SidebarFilter()
    data object Favourites : SidebarFilter()
    data class Category(val category: ContactCategory) : SidebarFilter()

    val key: String
        get() = when (this) {
            All -> "all"
            Unread -> "unread"
            Favourites -> "favourites"
            is Category -> category.key
        }
}
