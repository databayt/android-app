package org.hogwarts.android.shell.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.hogwarts.android.R
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.core.designsystem.icon.ToolbarIcons
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * The search — the app's answer to the web's Spotlight palette
 * (`generic-command-menu`), opened from the menu's first control.
 *
 * It offers what the web offers below two characters: every page this role
 * can reach, derived from the same `platformNav` registry the sidebar and the
 * menu read, plus the create actions from `platform-config.ts`, plus what the
 * reader opened recently. Typing filters them the web's way — one normalized
 * haystack of title, description and keywords, matched as a substring, with
 * the Arabic synonyms that let "طلاب" find the students page.
 *
 * What it does not have is the web's other half: at two characters or more
 * the palette also searches students, teachers, classes, invoices and eleven
 * more kinds. That runs through a Next server action against Postgres with
 * per-kind RBAC, and no HTTP endpoint exposes it, so there is nothing for a
 * phone to call. It arrives here when hogwarts grows one.
 */
@Composable
fun SearchScreen(
    role: UserRole?,
    enabledModules: List<String>?,
    recentIds: List<String>,
    onOpen: (SearchItem) -> Unit,
    onBack: () -> Unit,
) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    var query by remember { mutableStateOf("") }

    // Titles come from resources through the context rather than
    // `stringResource`, so the rows can be built in `remember` — and they are
    // rebuilt when the configuration changes, which is when they would differ.
    val context = LocalContext.current
    val everything = remember(context, role, enabledModules) {
        val title: (Int) -> String = { id -> context.getString(id) }
        navSearchItems(role, enabledModules, title) + actionSearchItems(role, title)
    }

    val results = filterByQuery(everything, query)
    val recents = if (query.isBlank()) {
        recentIds.mapNotNull { id -> everything.firstOrNull { it.id == id } }
    } else {
        emptyList()
    }

    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding(),
    ) {
        // `SpotlightInput`: a 48dp row, a muted magnifier, the field, and the
        // way out where the dialog's Escape is.
        Row(
            Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                ToolbarIcons.Search,
                contentDescription = null,
                tint = colors.mutedForeground,
                modifier = Modifier.size(20.dp),
            )
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = type.body.copy(color = colors.foreground),
                cursorBrush = SolidColor(colors.foreground),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.weight(1f).focusRequester(focus),
                decorationBox = { field ->
                    if (query.isEmpty()) {
                        Text(
                            stringResource(R.string.search_placeholder),
                            style = type.body,
                            color = colors.mutedForeground.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    field()
                },
            )
            Text(
                stringResource(R.string.search_cancel),
                style = type.bodyMedium,
                color = colors.mutedForeground,
                modifier = Modifier.clickable(role = Role.Button) {
                    keyboard?.hide()
                    onBack()
                },
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

        if (results.isEmpty() && recents.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    stringResource(R.string.search_no_results),
                    style = type.body,
                    color = colors.mutedForeground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(R.string.search_no_results_hint),
                    style = type.caption,
                    color = colors.mutedForeground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
            }
            return@Column
        }

        LazyColumn(Modifier.fillMaxSize().navigationBarsPadding()) {
            if (recents.isNotEmpty()) {
                item { GroupHeading(stringResource(R.string.search_recent)) }
                items(recents, key = { "recent-${it.id}" }) { item ->
                    SearchRow(item) { keyboard?.hide(); onOpen(item) }
                }
            }
            val pageResults = results.filter { it.kind == SearchItemKind.Page }
            val actionResults = results.filter { it.kind == SearchItemKind.Action }
            if (pageResults.isNotEmpty()) {
                item { GroupHeading(stringResource(R.string.search_category_pages)) }
                items(pageResults, key = { "page-${it.id}" }) { item ->
                    SearchRow(item) { keyboard?.hide(); onOpen(item) }
                }
            }
            if (actionResults.isNotEmpty()) {
                item { GroupHeading(stringResource(R.string.search_category_actions)) }
                items(actionResults, key = { "action-${it.id}" }) { item ->
                    SearchRow(item) { keyboard?.hide(); onOpen(item) }
                }
            }
        }
    }
}

@Composable
private fun GroupHeading(title: String) {
    Text(
        title,
        style = HogwartsTheme.type.caption,
        color = HogwartsTheme.colors.mutedForeground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SearchRow(item: SearchItem, onClick: () -> Unit) {
    val colors = HogwartsTheme.colors
    val type = HogwartsTheme.type
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(item.title, style = type.body, color = colors.foreground, maxLines = 1, overflow = TextOverflow.Ellipsis)
        item.description?.let { description ->
            Text(description, style = type.caption, color = colors.mutedForeground, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
