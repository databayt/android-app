package org.hogwarts.android.core.designsystem.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/**
 * The app's current locale, read so that composition recomposes when the
 * per-app language changes. `Locale.getDefault()` would keep the old value.
 */
@Composable
@ReadOnlyComposable
fun currentLocale(): Locale = LocalConfiguration.current.locales[0]
