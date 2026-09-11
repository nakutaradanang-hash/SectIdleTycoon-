package com.sect.idle.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DataValidatorTest - Rigorous test suite validating input sanitization,
 * XSS & HTML escaping, SQL injection detection, path traversal neutralization,
 * promo code redemption formatting, safe arithmetic, and boundary clamping.
 */
public class DataValidatorTest {

    @Test
    public void testSanitizeString_Basic() {
        assertEquals("Hello World", DataValidator.sanitizeString("Hello World", 50));
        assertEquals("Test", DataValidator.sanitizeString("  Test  ", 50));
        assertEquals("", DataValidator.sanitizeString(null, 50));
        assertEquals("12345", DataValidator.sanitizeString("1234567890", 5));
    }

    @Test
    public void testSanitizeString_ControlCharacters() {
        String dirty = "Immortal\u0000\u0007\u001F Sect";
        assertEquals("Immortal Sect", DataValidator.sanitizeString(dirty, 50));
    }

    @Test
    public void testStripHtmlTags_Xss() {
        String xss1 = "<script>alert('pwned')</script>Mount Tai";
        assertEquals("Mount Tai", DataValidator.stripHtmlTags(xss1));

        String xss2 = "<img src=x onerror=alert(1)>Sword Sect";
        assertEquals("Sword Sect", DataValidator.stripHtmlTags(xss2));

        String nested = "<b><i>Divine</i> Realm</b>";
        assertEquals("Divine Realm", DataValidator.stripHtmlTags(nested));
    }

    @Test
    public void testEncodeHtml() {
        String input = "<script> & \" '";
        String encoded = DataValidator.encodeHtml(input);
        assertEquals("&lt;script&gt; &amp; &quot; &#x27;", encoded);
    }

    @Test
    public void testContainsSqlInjectionPatterns() {
        assertTrue(DataValidator.containsSqlInjectionPatterns("SELECT * FROM disciples WHERE 1=1"));
        assertTrue(DataValidator.containsSqlInjectionPatterns("admin' OR '1'='1' --"));
        assertTrue(DataValidator.containsSqlInjectionPatterns("DROP TABLE disciples;"));
        assertTrue(DataValidator.containsSqlInjectionPatterns("UNION SELECT password FROM users"));
        assertTrue(DataValidator.containsSqlInjectionPatterns("Mount Tai; DROP DATABASE sect;"));

        // Benign text should not trigger SQL injection detection
        assertFalse(DataValidator.containsSqlInjectionPatterns("Heavenly Sword Sect"));
        assertFalse(DataValidator.containsSqlInjectionPatterns("Mount Tai Peak 1"));
        assertFalse(DataValidator.containsSqlInjectionPatterns("Disciple Zhang San"));
    }

    @Test
    public void testEscapeSqlString() {
        assertEquals("Mount Tai", DataValidator.escapeSqlString("Mount Tai"));
        assertEquals("Mount ''Tai'' Sect", DataValidator.escapeSqlString("Mount 'Tai' Sect"));
        assertEquals("No\"Quote", DataValidator.escapeSqlString("No\"Quote"));
        assertEquals("", DataValidator.escapeSqlString(null));
    }

    @Test
    public void testSanitizeFileName_PathTraversal() {
        assertEquals("save.dat", DataValidator.sanitizeFileName("../../etc/passwd/save.dat"));
        assertEquals("sect_backup.json", DataValidator.sanitizeFileName("..\\..\\sect_backup.json"));
        assertEquals("default_file", DataValidator.sanitizeFileName("...///"));
        assertEquals("valid_save_1.dat", DataValidator.sanitizeFileName("valid_save_1.dat"));
    }

    @Test
    public void testIsValidName() {
        assertTrue(DataValidator.isValidName("Heavenly Sword", 2, 32));
        assertTrue(DataValidator.isValidName("Mount Tai (Main)", 2, 32));
        assertTrue(DataValidator.isValidName("Peak-1", 2, 32));
        assertTrue(DataValidator.isValidName("Grand Elder [Fire]", 2, 32));

        // Invalid names
        assertFalse(DataValidator.isValidName("A", 2, 32)); // too short
        assertFalse(DataValidator.isValidName("<script>", 2, 32)); // dangerous tags
        assertFalse(DataValidator.isValidName("Sect; DROP TABLE;", 2, 32)); // SQL pattern
        assertFalse(DataValidator.isValidName("Very Long Name That Exceeds The Maximum Permitted Thirty Two Characters Limit", 2, 32)); // too long
    }

    @Test
    public void testSanitizeName() {
        assertEquals("Heavenly Sword", DataValidator.sanitizeName("<b>Heavenly Sword</b>", 32));
        assertEquals("Mount Tai Sect", DataValidator.sanitizeName("Mount Tai Sect", 32));
        assertEquals("Mount Tai Sect", DataValidator.sanitizeName(null, 32));
    }

    @Test
    public void testRedeemCodeValidation() {
        assertTrue(DataValidator.isValidRedeemCode("DAO777"));
        assertTrue(DataValidator.isValidRedeemCode("IMMORTAL2026"));
        assertTrue(DataValidator.isValidRedeemCode("SECTFOUNDER"));

        assertFalse(DataValidator.isValidRedeemCode("AB")); // too short
        assertFalse(DataValidator.isValidRedeemCode("DAO 777")); // contains space
        assertFalse(DataValidator.isValidRedeemCode("CODE<SCRIPT>")); // invalid symbols
        assertFalse(DataValidator.isValidRedeemCode(null));

        assertEquals("DAO777", DataValidator.sanitizeRedeemCode(" dao777 ", 20));
        assertEquals("IMMORTAL", DataValidator.sanitizeRedeemCode("im!@#mor$tal", 20));
    }

    @Test
    public void testSafeArithmetic_Add() {
        assertEquals(15L, DataValidator.safeAdd(10L, 5L));
        // Overflow test
        assertEquals(Long.MAX_VALUE, DataValidator.safeAdd(Long.MAX_VALUE - 10, 50L));
        // Underflow test
        assertEquals(Long.MIN_VALUE, DataValidator.safeAdd(Long.MIN_VALUE + 10, -50L));
    }

    @Test
    public void testSafeArithmetic_Multiply() {
        assertEquals(50L, DataValidator.safeMultiply(10L, 5L));
        // Overflow test
        assertEquals(Long.MAX_VALUE, DataValidator.safeMultiply(Long.MAX_VALUE / 2 + 100, 3L));
    }

    @Test
    public void testSafeSubtractNonNegative() {
        assertEquals(50L, DataValidator.safeSubtractNonNegative(100L, 50L));
        assertEquals(0L, DataValidator.safeSubtractNonNegative(50L, 100L));
        assertEquals(0L, DataValidator.safeSubtractNonNegative(-10L, 50L));
    }

    @Test
    public void testClamp() {
        assertEquals(50, DataValidator.clampInt(50, 0, 100));
        assertEquals(0, DataValidator.clampInt(-10, 0, 100));
        assertEquals(100, DataValidator.clampInt(150, 0, 100));

        assertEquals(500L, DataValidator.clampLong(500L, 100L, 1000L));
        assertEquals(100L, DataValidator.clampLong(50L, 100L, 1000L));
        assertEquals(1000L, DataValidator.clampLong(5000L, 100L, 1000L));
    }
}
