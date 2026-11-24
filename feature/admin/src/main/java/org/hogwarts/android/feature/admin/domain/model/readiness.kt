package org.hogwarts.android.feature.admin.domain.model

import androidx.annotation.StringRes
import org.hogwarts.android.feature.admin.R

/** How ready one person is for online classes — mirrors the web's `ReadinessLevel`. */
enum class ReadinessLevel {
    Ok, Warn, Bad;

    companion object {
        fun fromCode(code: String?): ReadinessLevel = when (code) {
            "bad" -> Bad
            "warn" -> Warn
            else -> Ok
        }
    }
}

/**
 * A reason code from the server (hogwarts `readiness/rules.ts`) with its chip
 * label. The server sends codes only; the words live in strings.xml (en + ar).
 */
enum class ReadinessReason(val code: String, @StringRes val labelRes: Int, val level: ReadinessLevel) {
    NeverSignedIn("never_signed_in", R.string.readiness_never_signed_in, ReadinessLevel.Bad),
    PasswordTrouble("password_trouble", R.string.readiness_password_trouble, ReadinessLevel.Bad),
    NoClassRecent("no_class_recent", R.string.readiness_no_class_recent, ReadinessLevel.Bad),
    AndroidWeb("android_web", R.string.readiness_android_web, ReadinessLevel.Warn),
    OldApp("old_app", R.string.readiness_old_app, ReadinessLevel.Warn),
    PushOff("push_off", R.string.readiness_push_off, ReadinessLevel.Warn),
    AllowedWeb("allowed_web", R.string.readiness_allowed_web, ReadinessLevel.Ok),
    OnApp("on_app", R.string.readiness_on_app, ReadinessLevel.Ok);

    companion object {
        /** Null for a code a newer server added — the chip is simply not drawn. */
        fun fromCode(code: String): ReadinessReason? = entries.firstOrNull { it.code == code }
    }
}

data class ReadinessPerson(
    val name: String,
    val level: ReadinessLevel,
    val reasons: List<ReadinessReason>,
    val username: String? = null,
    val canCover: Boolean = false,
)

data class ReadinessSession(
    val start: String,
    val subject: String?,
    val teacher: ReadinessPerson?,
)

data class ClassReadiness(
    val sectionId: String,
    val name: String,
    val supervisor: ReadinessPerson?,
    val sessions: List<ReadinessSession>,
    val students: List<ReadinessPerson>,
) {
    val counts: ReadinessCounts get() = ReadinessCounts.of(students)
}

/** The four numbers on a class card, over its students. */
data class ReadinessCounts(
    val onApp: Int,
    val androidWeb: Int,
    val notAttending: Int,
    val signInTrouble: Int,
) {
    companion object {
        fun of(students: List<ReadinessPerson>): ReadinessCounts {
            fun has(p: ReadinessPerson, vararg r: ReadinessReason) = r.any { it in p.reasons }
            return ReadinessCounts(
                onApp = students.count { has(it, ReadinessReason.OnApp) },
                androidWeb = students.count { has(it, ReadinessReason.AndroidWeb) },
                notAttending = students.count { has(it, ReadinessReason.NeverSignedIn, ReadinessReason.NoClassRecent) },
                signInTrouble = students.count { has(it, ReadinessReason.PasswordTrouble) },
            )
        }
    }
}

data class ReadinessDay(val date: String, val classes: List<ClassReadiness>)
