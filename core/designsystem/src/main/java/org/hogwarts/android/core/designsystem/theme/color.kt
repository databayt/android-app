package org.hogwarts.android.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Hogwarts Android color palette.
 *
 * IMPORTANT: Always use semantic tokens (MaterialTheme.colorScheme) in composables.
 * Never use these color values directly in UI code.
 *
 * Usage:
 * ```kotlin
 * // ❌ WRONG
 * Surface(color = HogwartsPrimary)
 *
 * // ✅ CORRECT
 * Surface(color = MaterialTheme.colorScheme.primary)
 * ```
 */

// Primary - iOS 26 Blue
val HogwartsPrimary = Color(0xFF0062D9)
val HogwartsPrimaryLight = Color(0xFF409CFF)
val HogwartsPrimaryDark = Color(0xFF004DB3)
val HogwartsOnPrimary = Color(0xFFFFFFFF)

// Secondary - Gold/Amber
val HogwartsSecondary = Color(0xFFD97706)
val HogwartsSecondaryLight = Color(0xFFF59E0B)
val HogwartsSecondaryDark = Color(0xFFB45309)
val HogwartsOnSecondary = Color(0xFFFFFFFF)

// Tertiary - Emerald
val HogwartsTertiary = Color(0xFF059669)
val HogwartsTertiaryLight = Color(0xFF10B981)
val HogwartsTertiaryDark = Color(0xFF047857)
val HogwartsOnTertiary = Color(0xFFFFFFFF)

// Error
val HogwartsError = Color(0xFFDC2626)
val HogwartsErrorLight = Color(0xFFFEE2E2)
val HogwartsOnError = Color(0xFFFFFFFF)
val HogwartsOnErrorContainer = Color(0xFF7F1D1D)

// Surface - Light mode (iOS 26)
val HogwartsBackground = Color(0xFFF2F2F7)
val HogwartsSurface = Color(0xFFFFFFFF)
val HogwartsSurfaceVariant = Color(0xFFE5E5EA)
val HogwartsOnBackground = Color(0xFF000000)
val HogwartsOnSurface = Color(0xFF1C1C1E)
val HogwartsOnSurfaceVariant = Color(0xFF8E8E93)

// Surface - Dark mode (iOS 26)
val HogwartsBackgroundDark = Color(0xFF000000)
val HogwartsSurfaceDark = Color(0xFF1C1C1E)
val HogwartsSurfaceVariantDark = Color(0xFF2C2C2E)
val HogwartsOnBackgroundDark = Color(0xFFFFFFFF)
val HogwartsOnSurfaceDark = Color(0xFFE5E5EA)
val HogwartsOnSurfaceVariantDark = Color(0xFF8E8E93)

// Outline (iOS separators)
val HogwartsOutline = Color(0xFFC6C6C8)
val HogwartsOutlineDark = Color(0xFF38383A)

// Status colors (for semantic usage)
val StatusSuccess = Color(0xFF10B981)
val StatusWarning = Color(0xFFF59E0B)
val StatusError = Color(0xFFEF4444)
val StatusInfo = Color(0xFF3B82F6)

// Form message colors (matching web app design)
// Error: destructive with 15% opacity background
val FormErrorBackground = Color(0x26DC2626)  // 15% opacity red
val FormErrorForeground = Color(0xFFDC2626)  // Solid red
// Success: emerald with 15% opacity background
val FormSuccessBackground = Color(0x2610B981)  // 15% opacity emerald
val FormSuccessForeground = Color(0xFF10B981)  // Solid emerald

// Muted colors (iOS systemGray)
val MutedForeground = Color(0xFF8E8E93)       // Light mode
val MutedForegroundDark = Color(0xFF8E8E93)   // Dark mode
val MutedBackground = Color(0xFFE5E5EA)       // Light mode (systemGray5)
val MutedBackgroundDark = Color(0xFF2C2C2E)   // Dark mode

// Divider colors (iOS opaqueSeparator)
val DividerColor = Color(0xFFC6C6C8)          // Light mode
val DividerColorDark = Color(0xFF38383A)      // Dark mode

// Attendance status colors
val AttendancePresent = Color(0xFF10B981)
val AttendanceAbsent = Color(0xFFEF4444)
val AttendanceLate = Color(0xFFF59E0B)
val AttendanceExcused = Color(0xFF6366F1)

// Grade colors
val GradeExcellent = Color(0xFF10B981)  // A
val GradeGood = Color(0xFF3B82F6)       // B
val GradeAverage = Color(0xFFF59E0B)    // C
val GradeBelowAverage = Color(0xFFF97316) // D
val GradeFail = Color(0xFFEF4444)       // F

// Apple gray scale (matching iOS systemGray1-6)
val AppleGray1 = Color(0xFF8E8E93)
val AppleGray2 = Color(0xFFAEAEB2)
val AppleGray3 = Color(0xFFC7C7CC)
val AppleGray4 = Color(0xFFD1D1D6)
val AppleGray5 = Color(0xFFE5E5EA)
val AppleGray6 = Color(0xFFF2F2F7)

// Apple semantic colors
val AppleBlue = Color(0xFF0088FF) // Aligned with Figma iOS 26 accents/blue
val AppleGreen = Color(0xFF34C759)
val AppleOrange = Color(0xFFFF9500)
val AppleRed = Color(0xFFFF3B30)
val ApplePurple = Color(0xFFAF52DE)
val AppleTeal = Color(0xFF5AC8FA)
val AppleIndigo = Color(0xFF5856D6)
val ApplePink = Color(0xFFFF2D55)
val AppleYellow = Color(0xFFFFCC00)

// Glass material background colors
val GlassLightBackground = Color(0xE6FFFFFF)  // 90% white
val GlassDarkBackground = Color(0x40FFFFFF)   // 25% white
