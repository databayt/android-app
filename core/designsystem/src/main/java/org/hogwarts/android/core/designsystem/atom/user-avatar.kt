package org.hogwarts.android.core.designsystem.atom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Circular avatar with image or initials fallback.
 *
 * Source: Figma iOS 26 — Activity View contacts (node 15:2559)
 */
@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: Dp = 40.dp
) {
    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = name,
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        val initials = name.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")

        Box(
            modifier = modifier
                .size(size)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Avatar with first/last name labels underneath — used in contact rows.
 * Fixed height 104dp container: 70dp avatar + 5dp gap + name lines at 15dp each with 1dp gap.
 *
 * Source: Figma iOS 26 — Activity View Contact (node 15:2509)
 */
@Composable
fun ContactAvatar(
    name: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    avatarSize: Dp = 70.dp,
    badgeContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .width(78.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            UserAvatar(
                name = name,
                imageUrl = imageUrl,
                size = avatarSize
            )
            if (badgeContent != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 5.dp, y = 0.dp)
                ) {
                    badgeContent()
                }
            }
        }

        Column(
            modifier = Modifier.width(78.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            val parts = name.split(" ", limit = 2)
            parts.forEach { part ->
                Text(
                    text = part,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        letterSpacing = 0.06.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(78.dp)
                )
            }
        }
    }
}

/**
 * Overlapping avatars on a semi-transparent circular background for groups.
 * Primary avatar (37dp) at top-start, secondary avatar (22dp) at bottom-end.
 *
 * Source: Figma iOS 26 — Activity View Group Contact (node 15:2511)
 */
@Composable
fun GroupAvatar(
    names: List<String>,
    modifier: Modifier = Modifier,
    imageUrls: List<String> = emptyList(),
    groupSize: Dp = 70.dp,
    badgeContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.width(78.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(groupSize)
                    .background(
                        color = Color(0x29787880),
                        shape = CircleShape
                    )
            ) {
                UserAvatar(
                    name = names.getOrElse(0) { "" },
                    imageUrl = imageUrls.getOrNull(0),
                    size = 37.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 9.dp, y = 9.dp)
                )
                UserAvatar(
                    name = names.getOrElse(1) { "" },
                    imageUrl = imageUrls.getOrNull(1),
                    size = 22.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-9).dp, y = (-9).dp)
                )
            }
            if (badgeContent != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 5.dp, y = 0.dp)
                ) {
                    badgeContent()
                }
            }
        }

        Column(
            modifier = Modifier.width(78.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = names.joinToString(" and ") { it.split(" ").first() },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    letterSpacing = 0.06.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(78.dp)
            )
            Text(
                text = "${names.size} People",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    letterSpacing = 0.06.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class ContactItem(
    val name: String,
    val imageUrl: String? = null
)

data class GroupContactItem(
    val names: List<String>,
    val imageUrls: List<String> = emptyList()
)

/**
 * Horizontal scrollable row of contacts and groups.
 * Padding: 24dp horizontal, 14dp top, 16dp bottom. Gap: 14dp between items.
 *
 * Source: Figma iOS 26 — Activity View Contacts Row (node 15:2508)
 */
@Composable
fun ContactsRow(
    contacts: List<ContactItem>,
    modifier: Modifier = Modifier,
    groups: List<GroupContactItem> = emptyList()
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = AppleSpacing.Large),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(contacts) { contact ->
            ContactAvatar(
                name = contact.name,
                imageUrl = contact.imageUrl
            )
        }
        items(groups) { group ->
            GroupAvatar(
                names = group.names,
                imageUrls = group.imageUrls
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ContactAvatarPreview() {
    HogwartsTheme {
        Surface {
            ContactAvatar(name = "Herland Antezana")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupAvatarPreview() {
    HogwartsTheme {
        Surface {
            GroupAvatar(names = listOf("Magico Test", "Elena Cruz"))
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun ContactsRowRtlPreview() {
    HogwartsTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ContactsRow(
                contacts = listOf(
                    ContactItem("أحمد محمد"),
                    ContactItem("سارة علي")
                ),
                groups = listOf(
                    GroupContactItem(listOf("ليلى حسن", "نور كريم"))
                )
            )
        }
    }
}
