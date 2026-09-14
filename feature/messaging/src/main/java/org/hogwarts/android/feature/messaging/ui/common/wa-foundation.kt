package org.hogwarts.android.feature.messaging.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.hogwarts.android.core.designsystem.theme.LocalWhatsAppColors
import org.hogwarts.android.core.designsystem.theme.WhatsAppColors
import org.hogwarts.android.feature.messaging.R
import org.hogwarts.android.feature.messaging.ui.format.avatarColorFor

/** The `--wa-*` tokens in scope. */
internal val wa: WhatsAppColors
    @Composable @ReadOnlyComposable get() = LocalWhatsAppColors.current

/**
 * A Tailwind text class as a style: `text-[size] leading-[line] font-… tracking-[…]`,
 * merged over the theme's text style so it keeps the brand font of the locale.
 */
@Composable
@ReadOnlyComposable
internal fun waType(
    size: Float,
    line: Float = size,
    weight: FontWeight = FontWeight.Normal,
    tracking: Float = 0f,
    italic: Boolean = false,
): TextStyle = LocalTextStyle.current.merge(
    TextStyle(
        fontSize = size.sp,
        lineHeight = line.sp,
        fontWeight = weight,
        // Arabic is cursive: Android letter spacing breaks the joins (and wraps
        // "رسائل" mid-word), so the web's tracking only applies to LTR text.
        letterSpacing = if (tracking == 0f || LocalLayoutDirection.current == LayoutDirection.Rtl) TextUnit.Unspecified else tracking.sp,
        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
    )
)

/** A web `WaIcon`: a monochrome glyph tinted like the CSS mask draws it. */
@Composable
internal fun WaIcon(id: Int, size: Dp, tint: Color, modifier: Modifier = Modifier, description: String? = null) {
    Icon(painterResource(id), contentDescription = description, tint = tint, modifier = modifier.size(size))
}

private fun vector(name: String, w: Float, h: Float, block: ImageVector.Builder.() -> Unit) =
    ImageVector.Builder(name, w.dp, h.dp, w, h).apply(block).build()

private fun ImageVector.Builder.fillPath(d: String, evenOdd: Boolean = false) = addPath(
    pathData = PathParser().parsePathString(d).toNodes(),
    fill = SolidColor(Color.Black),
    pathFillType = if (evenOdd) PathFillType.EvenOdd else PathFillType.NonZero,
)

private fun ImageVector.Builder.strokePath(d: String, width: Float) = addPath(
    pathData = PathParser().parsePathString(d).toNodes(),
    stroke = SolidColor(Color.Black),
    strokeLineWidth = width,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
)

/** `PersonGlyph` in `messaging/avatar.tsx`. */
internal val PersonGlyph: ImageVector = vector("person", 24f, 24f) {
    fillPath("M12,3.8a4.2,4.2 0 1,0 0,8.4a4.2,4.2 0 1,0 0,-8.4Z")
    fillPath("M12 13.6c-4.1 0-7.4 2.4-7.4 5.4v1h14.8v-1c0-3-3.3-5.4-7.4-5.4Z")
}

/** `SelectChatsGlyph` in `ios-chat-list.tsx`. */
internal val SelectChatsGlyph: ImageVector = vector("select-chats", 24f, 24f) {
    strokePath("M12,4.25a7.75,7.75 0 1,0 0,15.5a7.75,7.75 0 1,0 0,-15.5Z", 1.5f)
    strokePath("m8.6 12.2 2.3 2.3 4.5-5", 1.5f)
}

/** `ReadAllGlyph` in `ios-chat-list.tsx`. */
internal val ReadAllGlyph: ImageVector = vector("read-all", 24f, 24f) {
    strokePath("M6.2 4.25h11.6c1.1 0 1.95.85 1.95 1.95v8.6c0 1.1-.85 1.95-1.95 1.95h-6.3l-3.7 3v-3H6.2c-1.1 0-1.95-.85-1.95-1.95V6.2c0-1.1.85-1.95 1.95-1.95Z", 1.5f)
    strokePath("m8.8 10.6 2.2 2.2 4.2-4.6", 1.5f)
}

/** `SingleTick` in `bubble-timestamp.tsx`. */
internal val SingleTickGlyph: ImageVector = vector("tick", 13f, 10f) {
    strokePath("M1.2 5.4 4.6 8.7 11.8 1.3", 1.5f)
}

/** `ClockGlyph` in `bubble-timestamp.tsx`. */
internal val ClockGlyph: ImageVector = vector("clock", 12f, 12f) {
    strokePath("M6,1a5,5 0 1,0 0,10a5,5 0 1,0 0,-10Z", 1.1f)
    strokePath("M6 3.2V6l1.9 1.2", 1.1f)
}

/** The inline lock of `encryption-notice.tsx`. */
internal val NoticeLockGlyph: ImageVector = vector("lock", 10f, 12f) {
    fillPath("M5 0a3 3 0 0 0-3 3v1.2h1.6V3a1.4 1.4 0 0 1 2.8 0v1.2H8V3a3 3 0 0 0-3-3Z")
    fillPath("M1.4 5.2h7.2c.44 0 .8.36.8.8v5.2c0 .44-.36.8-.8.8H1.4a.8.8 0 0 1-.8-.8V6c0-.44.36-.8.8-.8Z")
}

/** The settings row chevron and the header back chevron (`polyline 2 2 10 10 2 18`). */
internal val ChevronEndGlyph: ImageVector = vector("chevron", 12f, 20f) {
    strokePath("M2 2 10 10 2 18", 2.5f)
}

/** The jump-to-newest arrow in `messages-view.tsx`. */
internal val ChevronDownGlyph: ImageVector = vector("chevron-down", 20f, 20f) {
    strokePath("M4 7.5 10 13.5 16 7.5", 2f)
}

/** The dismiss cross of `ios-notice-card.tsx`. */
internal val CrossGlyph: ImageVector = vector("cross", 20f, 20f) {
    strokePath("M4 4l12 12M16 4L4 16", 1.8f)
}

/** `BellGlyph` in `ios-notice-card.tsx`. */
internal val BellGlyph: ImageVector = vector("bell", 32f, 32f) {
    strokePath("M15 5.5a7.5 7.5 0 0 0-7 7.5v4.2l-1.8 3.4a1 1 0 0 0 .9 1.5h16.4", 2.1f)
    strokePath("M22.5 13.2v4l1.8 3.4a1 1 0 0 1-.9 1.5", 2.1f)
    strokePath("M12.6 25.2a3.6 3.6 0 0 0 6.8 0", 2.1f)
    fillPath("M24,4.5a4,4 0 1,0 0,8a4,4 0 1,0 0,-8Z")
}

/** The red pin of `location-bubble.tsx`. */
internal val MapPinGlyph: ImageVector = vector("pin", 24f, 24f) {
    fillPath("M12 2a7 7 0 00-7 7c0 5 7 13 7 13s7-8 7-13a7 7 0 00-7-7zm0 9.5a2.5 2.5 0 110-5 2.5 2.5 0 010 5z")
}

/** The play triangle of `voice-note-bubble.tsx`. */
internal val PlayGlyph: ImageVector = vector("play", 24f, 24f) {
    fillPath("M5 3l14 9-14 9V3z")
}

enum class AvatarSkin { Person, Group, Product }

/**
 * The 56dp disc every list row leads with (`ios-chat-row.tsx`,
 * `ios-list-row.tsx`): a photo when there is one, else a glyph on either the
 * hashed person palette ([colorKey]) or a fixed skin.
 */
@Composable
internal fun WaAvatar(
    url: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    colorKey: String? = null,
    group: Boolean = false,
    skin: AvatarSkin = AvatarSkin.Person,
    iconRes: Int? = null,
    glyphSize: Dp = 30.dp,
    borderWidth: Dp = 0.33.dp,
) {
    val (bg, fg) = when {
        colorKey != null -> avatarColorFor(colorKey).let { it.background to it.glyph }
        skin == AvatarSkin.Group -> wa.surfaceAvatarGroup to wa.textAvatarGroup
        skin == AvatarSkin.Product -> wa.surfaceProduct to wa.textInvert
        else -> wa.surfaceAvatarPerson to wa.textAvatarPerson
    }
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .border(borderWidth, wa.borderAvatar, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            url != null -> AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
            iconRes != null -> WaIcon(iconRes, glyphSize, fg)
            group -> WaIcon(R.drawable.ic_wa_group_16, glyphSize, fg)
            else -> Image(
                imageVector = PersonGlyph,
                contentDescription = null,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(fg),
                modifier = Modifier.size(glyphSize),
            )
        }
    }
}
