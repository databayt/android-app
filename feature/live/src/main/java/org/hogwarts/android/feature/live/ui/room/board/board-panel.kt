package org.hogwarts.android.feature.live.ui.room.board

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hogwarts.android.feature.live.R

/** Who the host can hand the pen to. */
data class BoardPerson(val identity: String, val name: String)

private val SURROUND = Color(0xFF171717)
private val CHIP = Color.White.copy(alpha = 0.12f)

/** Pen, highlighter or eraser — what a finger on the page does. */
private enum class BoardMode { Pen, Highlighter, Eraser }

/** One undoable step of this person's own. */
private sealed interface Step {
    data class Added(val stroke: Stroke) : Step
    data class Removed(val strokes: List<Stroke>) : Step
}

/**
 * The class board, native: a 16:9 page that keeps its shape on every screen, and — for whoever
 * may draw (the host, or a student the host handed the pen) — a toolbar of pen, highlighter,
 * eraser, undo, eight inks and three widths. The host also clears the page and chooses who may
 * draw. Upright phones put the toolbar under the page; a phone on its side puts it at the start.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun BoardPanel(
    board: BoardState,
    isHost: Boolean,
    canDraw: Boolean,
    localIdentity: String,
    people: List<BoardPerson>,
    onStroke: (Stroke) -> Unit,
    onDraft: (DraftStroke) -> Unit,
    onRemove: (List<String>) -> Unit,
    onClear: () -> Unit,
    onGrant: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(BoardMode.Pen) }
    var color by remember { mutableStateOf(BOARD_COLORS[0]) }
    var width by remember { mutableStateOf(BOARD_WIDTHS[0]) }
    val undo = remember { mutableStateListOf<Step>() }
    var granting by remember { mutableStateOf(false) }

    fun record(step: Step) {
        undo.add(step)
        while (undo.size > 50) undo.removeAt(0)
    }
    val strokeDone: (Stroke) -> Unit = { s ->
        onStroke(s)
        record(Step.Added(s))
    }
    val erased: (List<Stroke>) -> Unit = { gone ->
        onRemove(gone.map { it.id })
        record(Step.Removed(gone))
    }
    val onUndo = {
        val step = undo.removeLastOrNull()
        when (step) {
            is Step.Added -> onRemove(listOf(step.stroke.id))
            is Step.Removed -> step.strokes.forEach(onStroke)
            null -> Unit
        }
    }

    val description = stringResource(R.string.live_board_title)
    val canvas: @Composable (Modifier) -> Unit = { m ->
        BoardCanvas(
            strokes = board.strokes,
            drafts = board.drafts.values.map { it.draft },
            canDraw = canDraw,
            eraser = mode == BoardMode.Eraser,
            tool = if (mode == BoardMode.Highlighter) BoardTool.Highlighter else BoardTool.Pen,
            color = color,
            width = width,
            isHost = isHost,
            localIdentity = localIdentity,
            description = description,
            onStroke = strokeDone,
            onDraft = onDraft,
            onErase = erased,
            modifier = m,
        )
    }
    val toolbar: @Composable (Modifier) -> Unit = { m ->
        FlowRow(
            m, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Chip(stringResource(R.string.live_board_pen), mode == BoardMode.Pen) { mode = BoardMode.Pen }
            Chip(stringResource(R.string.live_board_highlighter), mode == BoardMode.Highlighter) { mode = BoardMode.Highlighter }
            Chip(stringResource(R.string.live_board_eraser), mode == BoardMode.Eraser) { mode = BoardMode.Eraser }
            Chip(stringResource(R.string.live_board_undo), false, enabled = undo.isNotEmpty(), onClick = onUndo)
            if (isHost) {
                Chip(stringResource(R.string.live_board_clear), false) {
                    onClear()
                    undo.clear()
                }
                Chip(stringResource(R.string.live_board_grant), board.grant != null) { granting = true }
            }
            val colorLabel = stringResource(R.string.live_board_color)
            for (c in BOARD_COLORS) Swatch(c, selected = c == color, label = colorLabel) {
                color = c
                if (mode == BoardMode.Eraser) mode = BoardMode.Pen
            }
            val widthLabels = listOf(R.string.live_board_thin, R.string.live_board_medium, R.string.live_board_thick)
            BOARD_WIDTHS.forEachIndexed { i, wd ->
                WidthDot(i, selected = wd == width, label = stringResource(widthLabels[i])) { width = wd }
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxSize().background(SURROUND)) {
        val portrait = maxHeight > maxWidth
        if (portrait) {
            // Upright: the page across the width, the toolbar right under it where a thumb reaches.
            Column(
                Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                canvas(Modifier.fillMaxWidth().aspectRatio(16f / 9f))
                if (canDraw) toolbar(Modifier.fillMaxWidth().padding(top = 12.dp, start = 4.dp, end = 4.dp))
            }
        } else {
            Row(
                Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                    .padding(top = 56.dp, bottom = 80.dp, start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (canDraw) {
                    Column(Modifier.width(168.dp).fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) {
                        toolbar(Modifier.fillMaxWidth())
                    }
                }
                canvas(Modifier.weight(1f).fillMaxHeight())
            }
        }
    }

    if (granting) {
        GrantDialog(
            current = board.grant, people = people,
            onPick = { onGrant(it); granting = false },
            onDismiss = { granting = false },
        )
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) Color.Black else Color.White.copy(alpha = if (enabled) 1f else 0.4f),
        fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1,
        modifier = Modifier.clip(CircleShape).background(if (selected) Color.White else CHIP)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    )
}

@Composable
private fun Swatch(hex: String, selected: Boolean, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(36.dp).clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "$label $hex" },
        Alignment.Center,
    ) {
        Box(
            Modifier.size(24.dp).clip(CircleShape).background(inkColor(hex))
                .border(BorderStroke(if (selected) 3.dp else 1.dp, Color.White.copy(alpha = if (selected) 1f else 0.35f)), CircleShape),
        )
    }
}

@Composable
private fun WidthDot(index: Int, selected: Boolean, label: String, onClick: () -> Unit) {
    val dot = listOf(6.dp, 10.dp, 16.dp)[index]
    Box(
        Modifier.size(36.dp).clip(CircleShape).background(if (selected) Color.White.copy(alpha = 0.28f) else CHIP)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        Alignment.Center,
    ) {
        Box(Modifier.size(dot).clip(CircleShape).background(Color.White))
    }
}

/** The host's pick: everyone, no one, or one student from the room. */
@Composable
private fun GrantDialog(current: String?, people: List<BoardPerson>, onPick: (String?) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.live_board_grant)) },
        text = {
            LazyColumn(Modifier.fillMaxWidth()) {
                item { GrantRow(stringResource(R.string.live_board_grant_everyone), current == GRANT_EVERYONE) { onPick(GRANT_EVERYONE) } }
                item { GrantRow(stringResource(R.string.live_board_grant_nobody), current == null) { onPick(null) } }
                items(people, key = { it.identity }) { p -> GrantRow(p.name, current == p.identity) { onPick(p.identity) } }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.live_room_close)) } },
    )
}

@Composable
private fun GrantRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.RadioButton, onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.weight(1f),
        )
        if (selected) Text("✓", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
