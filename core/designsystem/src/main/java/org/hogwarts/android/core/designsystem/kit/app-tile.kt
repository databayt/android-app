package org.hogwarts.android.core.designsystem.kit

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.core.designsystem.R
import org.hogwarts.android.core.designsystem.theme.BrandColors
import org.hogwarts.android.core.designsystem.theme.HogwartsShapes
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/** Artwork in `public/tiles/` on the web, shipped here as drawables. */
enum class TileArt(@DrawableRes val res: Int) {
    Announcements(R.drawable.tile_announcements),
    Assignments(R.drawable.tile_assignments),
    Attendance(R.drawable.tile_attendance),
    Events(R.drawable.tile_events),
    Exams(R.drawable.tile_exams),
    Grades(R.drawable.tile_grades),
    Home(R.drawable.tile_home),
    Library(R.drawable.tile_library),
    Message(R.drawable.tile_message),
    Notifications(R.drawable.tile_notifications),
    Profile(R.drawable.tile_profile),
    Schedule(R.drawable.tile_schedule),
    Setting(R.drawable.tile_setting),
    Stream(R.drawable.tile_stream),
    Students(R.drawable.tile_students),
    Subject(R.drawable.tile_subject),
    Wallet(R.drawable.tile_wallet),
}

/**
 * Grounds for destinations without a picture: iOS system colours lit ~12% at
 * the top, literal hexes because they are artwork and must not invert.
 */
enum class TileTint(val top: Color, val bottom: Color) {
    Blue(Color(0xFF3E9BFF), Color(0xFF0A6CF0)),
    Green(Color(0xFF5DDB78), Color(0xFF27B34A)),
    Indigo(Color(0xFF7C7AF2), Color(0xFF4B48D1)),
    Orange(Color(0xFFFFB340), Color(0xFFF58A00)),
    Pink(Color(0xFFFF6482), Color(0xFFF0284F)),
    Purple(Color(0xFFC97AF0), Color(0xFF9A45D1)),
    Red(Color(0xFFFF6B61), Color(0xFFEA3025)),
    Teal(Color(0xFF5AC8E0), Color(0xFF1E9BBA)),
    Yellow(Color(0xFFFFD84D), Color(0xFFF5B800)),
    Gray(Color(0xFFA5A5AB), Color(0xFF77777D)),
    Mint(Color(0xFF5FE0C8), Color(0xFF14B89A)),
    Brown(Color(0xFFC29A72), Color(0xFF94704E)),
}

@Immutable
data class AppTileItem(
    val key: String,
    val label: String,
    val onClick: () -> Unit,
    /** Picture; wins over [icon]. */
    val art: TileArt? = null,
    /** Glyph drawn on a tinted ground when there is no picture. */
    val icon: ImageVector? = null,
    val tint: TileTint = TileTint.Blue,
    /** Pending items behind the door — a red count. */
    val badge: Int = 0,
)

/** The icon face alone — `TileFace` on the web. Clipped at 29.2%. */
@Composable
fun TileFace(
    modifier: Modifier = Modifier,
    art: TileArt? = null,
    icon: ImageVector? = null,
    tint: TileTint = TileTint.Blue,
) {
    val base = modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .shadow(elevation = 3.dp, shape = HogwartsShapes.Tile, clip = false)
        .clip(HogwartsShapes.Tile)
    if (art != null) {
        Image(
            painter = painterResource(art.res),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = base,
        )
    } else {
        Box(
            modifier = base.background(Brush.verticalGradient(listOf(tint.top, tint.bottom))),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.fillMaxSize(0.5f))
            }
        }
    }
}

/** One destination as a home-screen icon: face, then a label that may wrap to two lines. */
@Composable
fun AppTile(
    item: AppTileItem,
    modifier: Modifier = Modifier,
    face: (@Composable () -> Unit)? = null,
    /** Section doors cap the icon at 64dp; the dashboard lets it fill its cell. */
    maxFaceSize: Dp? = 64.dp,
    /** Section doors may wrap to two lines; the dashboard truncates to one. */
    labelLines: Int = 2,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = item.onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box((if (maxFaceSize != null) Modifier.widthIn(max = maxFaceSize) else Modifier).scale(if (pressed) 0.95f else 1f)) {
            face?.invoke() ?: TileFace(art = item.art, icon = item.icon, tint = item.tint)
            if (item.badge > 0) {
                CountBadge(
                    count = item.badge,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp),
                )
            }
        }
        Text(
            text = item.label,
            style = type.tileLabel,
            color = colors.foreground,
            textAlign = TextAlign.Center,
            maxLines = labelLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Doors as rows of four icons — `AppTileGrid`: 16dp column gap, 20dp row gap. */
@Composable
fun AppTileGrid(
    items: List<AppTileItem>,
    modifier: Modifier = Modifier,
    columnGap: Dp = 16.dp,
    maxFaceSize: Dp? = 64.dp,
    labelLines: Int = 2,
) {
    if (items.isEmpty()) return
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        items.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(columnGap)) {
                row.forEach { AppTile(it, Modifier.weight(1f), maxFaceSize = maxFaceSize, labelLines = labelLines) }
                repeat(4 - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

/** A date as the Calendar icon: red weekday over a large light day number. */
@Composable
fun DateTile(weekday: String, day: String, modifier: Modifier = Modifier) {
    val colors = HogwartsTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(3.dp, HogwartsShapes.Tile, clip = false)
            .clip(HogwartsShapes.Tile)
            .background(colors.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(weekday, color = BrandColors.WeekdayRed, fontSize = 10.sp, lineHeight = 12.sp, style = HogwartsTheme.type.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
        Text(day, color = colors.foreground, style = HogwartsTheme.type.figureWide.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Light))
    }
}

/** A destructive-red count, capped at 99+. */
@Composable
fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .clip(CircleShape)
            .background(HogwartsTheme.colors.destructive)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (count > 99) "99+" else count.toString(), style = HogwartsTheme.type.badge, color = Color.White)
    }
}
