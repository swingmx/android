package com.android.swingmusic.auth.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUtilsTest {

    // normalizeUrl
    @Test
    fun `normalizeUrl returns null for null input`() {
        assertNull(AuthUtils.normalizeUrl(null))
    }

    @Test
    fun `normalizeUrl returns null for blank input`() {
        assertNull(AuthUtils.normalizeUrl("   \t  \n "))
    }

    @Test
    fun `normalizeUrl trims whitespace`() {
        assertEquals("https://example.com", AuthUtils.normalizeUrl("  example.com  "))
    }

    @Test
    fun `normalizeUrl prepends https when scheme is missing`() {
        assertEquals("https://example.com", AuthUtils.normalizeUrl("example.com"))
        assertEquals("https://sub.domain.com", AuthUtils.normalizeUrl("sub.domain.com"))
    }

    @Test
    fun `normalizeUrl leaves http and https unchanged`() {
        assertEquals("http://example.com", AuthUtils.normalizeUrl("http://example.com"))
        assertEquals("https://example.com", AuthUtils.normalizeUrl("https://example.com"))
    }

    @Test
    fun `normalizeUrl leaves other schemes unchanged`() {
        assertEquals("ftp://example.com", AuthUtils.normalizeUrl("ftp://example.com"))
        assertEquals("custom+scheme://host/path", AuthUtils.normalizeUrl("custom+scheme://host/path"))
    }

    // validInputUrl
    @Test
    fun `validInputUrl accepts http, https, and ftp`() {
        assertTrue(AuthUtils.validInputUrl("http://example.com"))
        assertTrue(AuthUtils.validInputUrl("https://example.com"))
        assertTrue(AuthUtils.validInputUrl("ftp://example.com"))
    }

    @Test
    fun `validInputUrl rejects missing scheme`() {
        assertFalse(AuthUtils.validInputUrl("example.com"))
        assertFalse(AuthUtils.validInputUrl("www.example.com"))
    }

    @Test
    fun `validInputUrl rejects blank or null`() {
        assertFalse(AuthUtils.validInputUrl(null))
        assertFalse(AuthUtils.validInputUrl("   "))
    }

    @Test
    fun `validInputUrl accepts typical paths and query`() {
        assertTrue(AuthUtils.validInputUrl("https://example.com/path?query=1#frag"))
    }

    @Test
    fun `validInputUrl rejects obvious invalid urls`() {
        assertFalse(AuthUtils.validInputUrl("https:///example.com"))
        assertFalse(AuthUtils.validInputUrl("https://"))
        assertFalse(AuthUtils.validInputUrl("not a url"))
    }
}
