package com.android.swingmusic

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/** Guards the bundled What's New notes so a version bump without notes fails the build. */
class ChangelogAssetTest {

    private val releases: List<JsonObject> by lazy {
        val file = File("src/main/assets/changelog.json")
        assertTrue("changelog.json is missing", file.exists())
        JsonParser.parseString(file.readText()).asJsonObject
            .getAsJsonArray("releases")
            .map { it.asJsonObject }
    }

    @Test
    fun `changelog is not empty`() {
        assertTrue(releases.isNotEmpty())
    }

    @Test
    fun `running build has release notes`() {
        // Gradle falls back to 2 / 1.0.0 when no -PversionCode is passed; only a real build is checked.
        assumeTrue("Run with -PversionCode and -PversionName", BuildConfig.VERSION_CODE != 2)

        val entry = releases.firstOrNull { it["versionCode"].asInt == BuildConfig.VERSION_CODE }
        assertTrue("No changelog entry for versionCode ${BuildConfig.VERSION_CODE}", entry != null)
        assertEquals(BuildConfig.VERSION_NAME, entry!!["versionName"].asString)
    }

    @Test
    fun `version codes are unique and newest first`() {
        val codes = releases.map { it["versionCode"].asInt }
        assertEquals(codes.distinct(), codes)
        assertEquals(codes.sortedDescending(), codes)
    }

    @Test
    fun `every release is well formed`() {
        releases.forEach { release ->
            val version = release["versionName"].asString
            assertTrue("$version: versionName must look like x.y.z", Regex("""\d+\.\d+\.\d+""").matches(version))
            assertTrue("$version: date must be yyyy-MM-dd", Regex("""\d{4}-\d{2}-\d{2}""").matches(release["date"].asString))
            assertTrue("$version: channel", release["channel"].asString in setOf("stable", "beta"))

            val changes = release.getAsJsonArray("changes").map { it.asJsonObject }
            assertFalse("$version has no changes", changes.isEmpty())
            changes.forEach { change ->
                assertTrue("$version: type", change["type"].asString in setOf("new", "tweak", "fixed"))
                assertTrue("$version: empty change text", change["text"].asString.isNotBlank())
                change["link"]?.asJsonObject?.let { link ->
                    assertTrue("$version: link url", link["url"].asString.startsWith("https://"))
                }
            }

            val highlightFlags = changes.map { it["highlight"]?.asBoolean == true }
            assertEquals("$version: highlights must come first", highlightFlags.sortedDescending(), highlightFlags)
        }
    }
}
