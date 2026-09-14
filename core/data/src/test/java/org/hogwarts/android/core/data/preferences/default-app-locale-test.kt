package org.hogwarts.android.core.data.preferences

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultAppLocaleTest {

    private class Fake(var locales: String? = null, var defaulted: Boolean = false) {
        var storedLanguage: String? = null
        var setCalls = 0
        val subject = DefaultAppLocale(
            appLocalesEmpty = { locales == null },
            setAppLocale = { locales = it; setCalls++ },
            alreadyDefaulted = { defaulted },
            markDefaulted = { defaulted = true; storedLanguage = it },
        )
    }

    @Test
    fun `first launch becomes Arabic and is remembered`() = runTest {
        val fake = Fake()
        assertTrue(fake.subject.apply())
        assertEquals("ar", fake.locales)
        assertEquals("ar", fake.storedLanguage)
        assertTrue(fake.defaulted)
    }

    @Test
    fun `runs once - a later reset to the system language stays`() = runTest {
        val fake = Fake()
        fake.subject.apply()
        fake.locales = null // the user picked "System default" in Android settings
        assertFalse(fake.subject.apply())
        assertNull(fake.locales)
        assertEquals(1, fake.setCalls)
    }

    @Test
    fun `an existing language choice is never overridden`() = runTest {
        val fake = Fake(locales = "en")
        assertFalse(fake.subject.apply())
        assertEquals("en", fake.locales)
        assertFalse(fake.defaulted)
    }
}
