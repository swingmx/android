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
    fun `normalizeUrl trims whitespace without scheme`() {
        assertEquals("https://example.com", AuthUtils.normalizeUrl("  example.com  "))
    }

    @Test
    fun `normalizeUrl trims whitespace with scheme`() {
        assertEquals("https://example.com", AuthUtils.normalizeUrl("   https://example.com   "))
    }

    @Test
    fun `normalizeUrl prepends https when scheme is missing`() {
        assertEquals("https://example.com", AuthUtils.normalizeUrl("example.com"))
        assertEquals("https://sub.domain.com", AuthUtils.normalizeUrl("sub.domain.com"))
    }

    @Test
    fun `normalizeUrl handles ports and IPs when missing scheme`() {
        assertEquals("https://example.com:8080", AuthUtils.normalizeUrl("example.com:8080"))
        assertEquals("https://192.168.1.1", AuthUtils.normalizeUrl("192.168.1.1"))
        assertEquals("https://192.168.1.1:8443", AuthUtils.normalizeUrl("192.168.1.1:8443"))
        assertEquals("https://localhost:3000", AuthUtils.normalizeUrl("localhost:3000"))
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
    fun `validInputUrl accepts domains with ports`() {
        assertTrue(AuthUtils.validInputUrl("https://example.com:8080"))
        assertTrue(AuthUtils.validInputUrl("http://sub.example.co.uk:3000/path"))
    }

    @Test
    fun `validInputUrl accepts localhost and IPv4 with optional ports`() {
        assertTrue(AuthUtils.validInputUrl("http://localhost"))
        assertTrue(AuthUtils.validInputUrl("http://localhost:3000/health"))
        assertTrue(AuthUtils.validInputUrl("https://192.168.1.1"))
        assertTrue(AuthUtils.validInputUrl("https://192.168.1.1:8443/api"))
    }

    @Test
    fun `validInputUrl rejects malformed IPv4`() {
        assertFalse(AuthUtils.validInputUrl("https://999.1.1.1"))
        assertFalse(AuthUtils.validInputUrl("http://256.256.256.256"))
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
    fun `validInputUrl rejects obvious invalid urls and custom schemes`() {
        assertFalse(AuthUtils.validInputUrl("https:///example.com"))
        assertFalse(AuthUtils.validInputUrl("https://"))
        assertFalse(AuthUtils.validInputUrl("not a url"))
        assertFalse(AuthUtils.validInputUrl("custom+scheme://host/path"))
    }
}
