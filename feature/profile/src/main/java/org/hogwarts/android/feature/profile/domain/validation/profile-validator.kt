package org.hogwarts.android.feature.profile.domain.validation

/**
 * Mirrors web Zod schemas in profile/validation.ts.
 * Returns the first error key or null when valid.
 */
object ProfileValidator {

    private const val MAX_BIO = 500
    private const val MAX_DISPLAY_NAME = 100
    private const val MAX_STATUS_MESSAGE = 100
    private val URL_REGEX = Regex("^https?://[^\\s]+\$")

    fun validateDisplayName(value: String): String? = when {
        value.isBlank() -> "displayName_required"
        value.length > MAX_DISPLAY_NAME -> "displayName_too_long"
        else -> null
    }

    fun validateBio(value: String?): String? = when {
        value == null || value.isEmpty() -> null
        value.length > MAX_BIO -> "bio_too_long"
        else -> null
    }

    fun validateUrl(value: String?): String? = when {
        value.isNullOrBlank() -> null
        !URL_REGEX.matches(value) -> "url_invalid"
        else -> null
    }

    fun validateLanguage(value: String?): String? = when (value) {
        null, "ar", "en" -> null
        else -> "language_invalid"
    }

    fun validateStatusMessage(value: String?): String? = when {
        value == null || value.isEmpty() -> null
        value.length > MAX_STATUS_MESSAGE -> "status_message_too_long"
        else -> null
    }

    /**
     * Full GitHub-profile-style update form validation.
     * Returns map of field -> error key (empty when all valid).
     */
    fun validateProfileUpdate(form: ProfileUpdateForm): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        validateDisplayName(form.displayName)?.let { errors["displayName"] = it }
        validateBio(form.bio)?.let { errors["bio"] = it }
        validateUrl(form.website)?.let { errors["website"] = it }
        validateUrl(form.github)?.let { errors["github"] = it }
        validateUrl(form.twitter)?.let { errors["twitter"] = it }
        validateUrl(form.linkedin)?.let { errors["linkedin"] = it }
        validateStatusMessage(form.statusMessage)?.let { errors["statusMessage"] = it }
        return errors
    }
}

data class ProfileUpdateForm(
    val displayName: String = "",
    val bio: String? = null,
    val website: String? = null,
    val github: String? = null,
    val twitter: String? = null,
    val linkedin: String? = null,
    val pronouns: String? = null,
    val statusEmoji: String? = null,
    val statusMessage: String? = null
)
