package org.hogwarts.android.feature.timetable.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import org.hogwarts.android.core.designsystem.apple.AppleInsetGroupedList
import org.hogwarts.android.core.designsystem.apple.AppleListRow
import org.hogwarts.android.core.designsystem.apple.AppleListSection
import org.hogwarts.android.core.designsystem.apple.AppleSpacing
import org.hogwarts.android.core.designsystem.apple.HogwartsIcons
import org.hogwarts.android.core.designsystem.atom.EmptyState
import org.hogwarts.android.feature.timetable.R
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    onNavigateBack: () -> Unit,
    viewModel: TimetableViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.timetable_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(HogwartsIcons.Back, contentDescription = stringResource(R.string.timetable_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading && uiState.entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val dayEntries = viewModel.getEntriesForSelectedDay()

            AppleInsetGroupedList(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                state = listState
            ) {
                // Day selector chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppleSpacing.Compact),
                        horizontalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
                    ) {
                        val days = listOf(
                            DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
                            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY
                        )
                        days.forEach { day ->
                            FilterChip(
                                selected = uiState.selectedDay == day,
                                onClick = { viewModel.selectDay(day) },
                                label = {
                                    Text(
                                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (dayEntries.isEmpty()) {
                    item {
                        EmptyState(
                            icon = HogwartsIcons.Schedule,
                            title = stringResource(R.string.timetable_no_classes),
                            subtitle = stringResource(R.string.timetable_no_classes_subtitle)
                        )
                    }
                } else {
                    item {
                        AppleListSection(
                            header = uiState.selectedDay.getDisplayName(TextStyle.FULL, Locale.getDefault())
                        ) {
                            dayEntries.forEachIndexed { index, entry ->
                                AppleListRow(
                                    showDivider = index < dayEntries.size - 1,
                                    leadingContent = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = entry.startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = entry.endTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(AppleSpacing.Tiny)
                                    ) {
                                        Text(
                                            text = entry.subjectName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(R.string.timetable_teacher_room, entry.teacherName, entry.roomNumber),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.error != null) {
                    item {
                        Text(
                            text = stringResource(R.string.timetable_showing_cached, uiState.error!!),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(AppleSpacing.Standard)
                        )
                    }
                }
            }
        }
    }
}
