package com.sect.idle.security;

import org.junit.Assert;
import org.junit.Test;

public class InputSecurityValidatorTest {

    @Test
    public void testSqlInjectionDetection() {
        String[] sqliPayloads = new String[]{
                "' OR '1'='1",
                "admin' --",
                "1; DROP TABLE disciples; --",
                "1 UNION SELECT null, username, password FROM users --",
                "1' AND 1=1 --",
                "' OR 1=1 #",
                "1' UNION ALL SELECT 1, 2, 3 --",
                "'; EXEC xp_cmdshell('dir'); --",
                "1' AND (SELECT SLEEP(5)) --",
                "' HAVING 1=1 --"
        };

        for (String payload : sqliPayloads) {
            boolean detected = InputSecurityValidator.isSqlInjection(payload);
            Assert.assertTrue("Failed to detect SQLi: " + payload, detected);
        }
    }

    @Test
    public void testSafeInputDoesNotTriggerSqli() {
        String[] safeInputs = new String[]{
                "Mount Tai Immortal Sect",
                "Cloud Sword Pavilion",
                "Level 5 Heavenly Pill",
                "Xiao Yan",
                "Grand Elder Han Li"
        };

        for (String safe : safeInputs) {
            boolean detected = InputSecurityValidator.isSqlInjection(safe);
            Assert.assertFalse("False positive SQLi for safe input: " + safe, detected);
        }
    }

    @Test
    public void testXssDetection() {
        String[] xssPayloads = new String[]{
                "<script>alert('XSS')</script>",
                "<img src=x onerror=alert(1)>",
                "<svg/onload=alert('pwn')>",
                "<iframe src=\"javascript:alert('XSS')\">",
                "<body onload=alert('test')>",
                "javascript:alert(document.cookie)",
                "<a href=\"javascript:evil()\">click me</a>",
                "<input autofocus onfocus=alert(1)>",
                "data:text/html,<script>alert(1)</script>"
        };

        for (String payload : xssPayloads) {
            boolean detected = InputSecurityValidator.isXss(payload);
            Assert.assertTrue("Failed to detect XSS: " + payload, detected);
        }
    }

    @Test
    public void testHtmlEntityEncoding() {
        String raw = "<div class=\"sect\" onclick='attack()'>Li & Xiao</div>";
        String encoded = InputSecurityValidator.encodeHtmlEntities(raw);

        Assert.assertFalse(encoded.contains("<"));
        Assert.assertFalse(encoded.contains(">"));
        Assert.assertFalse(encoded.contains("\""));
        Assert.assertFalse(encoded.contains("'"));
        Assert.assertTrue(encoded.contains("&lt;"));
        Assert.assertTrue(encoded.contains("&gt;"));
        Assert.assertTrue(encoded.contains("&quot;"));
        Assert.assertTrue(encoded.contains("&#x27;"));
        Assert.assertTrue(encoded.contains("&amp;"));
    }

    @Test
    public void testSanitizeTextRemovesMaliciousTags() {
        String malicious = "<script>steal()</script>Hello <b>Immortal</b> World!<img src=x onerror=alert(1)>";
        String clean = InputSecurityValidator.sanitizeText(malicious, 50);

        Assert.assertFalse(clean.contains("<script"));
        Assert.assertFalse(clean.contains("<img"));
        Assert.assertFalse(clean.contains("onerror="));
        Assert.assertTrue(clean.contains("Hello"));
        Assert.assertTrue(clean.contains("Immortal World!"));
    }

    @Test
    public void testValidateAndSanitizeResult() {
        String dangerous = "' OR 1=1; <script>evil()</script>";
        InputSecurityValidator.ValidationResult result = InputSecurityValidator.validateAndSanitize(null, dangerous, 64, "TestField");

        Assert.assertFalse(result.isValid);
        Assert.assertTrue(result.sqliDetected);
        Assert.assertTrue(result.xssDetected);
        Assert.assertFalse(result.sanitizedText.contains("<script>"));
    }

    @Test
    public void testSectNameValidation() {
        Assert.assertTrue(InputSecurityValidator.isValidSectName("Nine Cauldron Sect"));
        Assert.assertTrue(InputSecurityValidator.isValidSectName("Sect #1 - [Divine]"));
        Assert.assertFalse(InputSecurityValidator.isValidSectName(""));
        Assert.assertFalse(InputSecurityValidator.isValidSectName("A")); // Too short
        Assert.assertFalse(InputSecurityValidator.isValidSectName("<script>alert(1)</script>"));
        Assert.assertFalse(InputSecurityValidator.isValidSectName("Sect' OR '1'='1"));
    }
}
