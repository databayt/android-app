package org.hogwarts.android.feature.profile.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileValidatorTest {

    @Test
    fun `display name is required`() {
        assertEquals("displayName_required", ProfileValidator.validateDisplayName(""))
        assertEquals("displayName_required", ProfileValidator.validateDisplayName("   "))
    }

    @Test
    fun `display name rejects values longer than 100 chars`() {
        val too_long = "a".repeat(101)
        assertEquals("displayName_too_long", ProfileValidator.validateDisplayName(too_long))
    }

    @Test
    fun `display name accepts normal values`() {
        assertNull(ProfileValidator.validateDisplayName("Jane Doe"))
    }

    @Test
    fun `bio caps at 500`() {
        assertNull(ProfileValidator.validateBio(null))
        assertNull(ProfileValidator.validateBio(""))
        assertNull(ProfileValidator.validateBio("a".repeat(500)))
        assertEquals("bio_too_long", ProfileValidator.validateBio("a".repeat(501)))
    }

    @Test
    fun `url validation accepts http and https only`() {
        assertNull(ProfileValidator.validateUrl(null))
        assertNull(ProfileValidator.validateUrl(""))
        assertNull(ProfileValidator.validateUrl("https://example.com"))
        assertEquals("url_invalid", ProfileValidator.validateUrl("not a url"))
        assertEquals("url_invalid", ProfileValidator.validateUrl("ftp://x.com"))
    }

    @Test
    fun `language validation accepts ar en or null`() {
        assertNull(ProfileValidator.validateLanguage(null))
        assertNull(ProfileValidator.validateLanguage("ar"))
        assertNull(ProfileValidator.validateLanguage("en"))
        assertEquals("language_invalid", ProfileValidator.validateLanguage("fr"))
    }

    @Test
    fun `validateProfileUpdate aggregates field errors`() {
        val errors = ProfileValidator.validateProfileUpdate(
            ProfileUpdateForm(
                displayName = "",
                bio = "x".repeat(600),
                website = "not a url",
                github = "https://github.com/jane"
            )
        )
        assertEquals("displayName_required", errors["displayName"])
        assertEquals("bio_too_long", errors["bio"])
        assertEquals("url_invalid", errors["website"])
        assertTrue(!errors.containsKey("github"))
    }

    @Test
    fun `validateProfileUpdate returns empty map for valid form`() {
        val errors = ProfileValidator.validateProfileUpdate(
            ProfileUpdateForm(displayName = "Jane Doe")
        )
        assertTrue(errors.isEmpty())
    }
}
