package org.hogwarts.android.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateDownloadLogicTest {
    private val digest = "936de76e0123456789abcdef0123456789abcdef0123456789abcdef01234567"

    // --- SHA-256 check ---
    @Test fun `matching digest passes`() = assertTrue(sha256Matches(digest, digest))
    @Test fun `different digest fails`() = assertFalse(sha256Matches(digest, digest.reversed()))
    @Test fun `upper case manifest matches lower case file digest`() = assertTrue(sha256Matches(digest, digest.uppercase()))
    @Test fun `lower case manifest matches upper case file digest`() = assertTrue(sha256Matches(digest.uppercase(), digest))
    @Test fun `surrounding whitespace is ignored`() = assertTrue(sha256Matches(digest, "  $digest\n"))
    @Test fun `absent digest is accepted`() = assertTrue(sha256Matches(digest, null))
    @Test fun `blank digest is accepted`() = assertTrue(sha256Matches(digest, "   "))

    // --- resume decision ---
    @Test fun `206 from the requested offset appends`() =
        assertEquals(ResumeAction.Append, resumeAction(1000, 206, "bytes 1000-1999/2000"))
    @Test fun `206 without Content-Range appends`() = assertEquals(ResumeAction.Append, resumeAction(1000, 206, null))
    @Test fun `206 for another offset is rejected`() =
        assertEquals(ResumeAction.Reject, resumeAction(1000, 206, "bytes 0-1999/2000"))
    @Test fun `200 restarts from zero whether or not a range was sent`() {
        assertEquals(ResumeAction.Restart, resumeAction(1000, 200, null))
        assertEquals(ResumeAction.Restart, resumeAction(0, 200, null))
    }
    @Test fun `416 after a ranged request means check what we have`() =
        assertEquals(ResumeAction.CheckExisting, resumeAction(1000, 416, "bytes */1000"))
    @Test fun `416 without a ranged request is an error`() = assertEquals(ResumeAction.Reject, resumeAction(0, 416, null))
    @Test fun `http errors are rejected`() {
        assertEquals(ResumeAction.Reject, resumeAction(0, 404, null))
        assertEquals(ResumeAction.Reject, resumeAction(1000, 500, null))
    }
    @Test fun `content range start parses and tolerates junk`() {
        assertEquals(1000L, contentRangeStart("bytes 1000-1999/2000"))
        assertEquals(0L, contentRangeStart("Bytes 0-9/10"))
        assertNull(contentRangeStart("bytes */2000"))
        assertNull(contentRangeStart(null))
    }

    // --- progress ---
    @Test fun `percent is floored and clamped`() {
        assertEquals(0, percentOf(0, 200))
        assertEquals(45, percentOf(91, 200))
        assertEquals(100, percentOf(200, 200))
        assertEquals(100, percentOf(300, 200))
    }
    @Test fun `unknown size is indeterminate`() {
        assertEquals(-1, percentOf(500, -1))
        assertEquals(-1, percentOf(500, 0))
    }

    // --- file name + cleanup ---
    @Test fun `file name carries version name and code`() = assertEquals("1.3.2-12.apk", apkFileName("1.3.2", 12))
    @Test fun `file name never holds a path separator`() {
        val name = apkFileName("../../etc/passwd", 7)
        assertFalse(name.contains('/'))
        assertEquals("etc_passwd-7.apk", name)
    }
    @Test fun `blank or missing version name still gives a name`() {
        assertEquals("update-12.apk", apkFileName(null, 12))
        assertEquals("update-12.apk", apkFileName("  ", 12))
    }
    @Test fun `same release always resolves to the same file`() =
        assertEquals(apkFileName("1.3.2", 12), apkFileName(" 1.3.2 ", 12))
    @Test fun `cleanup keeps only the file about to be written`() {
        val existing = listOf("1.3.1-11.apk", "1.3.2-12.apk", "alabidae (4).apk", "x.part")
        assertEquals(
            listOf("1.3.1-11.apk", "alabidae (4).apk", "x.part"),
            staleUpdateFiles(existing, keep = "1.3.2-12.apk"),
        )
    }
    @Test fun `cleanup of an empty folder is empty`() = assertEquals(emptyList<String>(), staleUpdateFiles(emptyList(), "a.apk"))

    @Test fun `cleanup keeps the partial of the file being downloaded, so a cut download resumes`() =
        assertEquals(
            listOf("1.3.1-11.apk", "1.3.1-11.apk.part"),
            staleUpdateFiles(
                listOf("1.3.1-11.apk", "1.3.1-11.apk.part", "1.3.2-12.apk", "1.3.2-12.apk.part"),
                keep = "1.3.2-12.apk",
            ),
        )

    @Test fun `a partial is named after its file`() = assertEquals("1.3.2-12.apk.part", partialName("1.3.2-12.apk"))
}
