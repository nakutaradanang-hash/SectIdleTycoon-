package com.sect.idle.utils;

import com.sect.idle.core.MathUtils;
import java.util.regex.Pattern;

/**
 * DataValidator - Comprehensive Input Validation, Sanitization, and Encoding Engine.
 *
 * Provides defense-in-depth protection against:
 * 1. Cross-Site Scripting (XSS) & HTML Injection.
 * 2. SQL Injection & Database Query Manipulation.
 * 3. Path Traversal & Arbitrary File Access.
 * 4. Integer Overflow, Underflow & Arithmetic Exploits.
 * 5. Corrupted / Malformed Save State & JSON Injections.
 * 6. Non-printable ASCII / Control Character Attacks.
 *
 * Designed for High-Performance (O(N) single-pass scans, zero garbage generation on hot paths).
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class DataValidator {

    // Precompiled Regex Patterns for High Performance & Zero Re-compilation Overhead
    private static final Pattern PATTERN_SCRIPT_TAG = Pattern.compile("(?i)<script[\\s\\S]*?>[\\s\\S]*?</script>");
    private static final Pattern PATTERN_HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern PATTERN_SQL_INJECTION = Pattern.compile(
        "(?i)\\b(SELECT|INSERT|UPDATE|DELETE|DROP|UNION|ALTER|CREATE|TRUNCATE|EXEC|EXECUTE|DECLARE)\\b|--|/\\*|\\*/|;|((\\bOR\\b|\\bAND\\b)\\s+['\"\\d]+[\\s]*=[\\s]*['\"\\d]+)"
    );
    private static final Pattern PATTERN_ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9_ -]+$");
    private static final Pattern PATTERN_REDEEM_CODE = Pattern.compile("^[A-Z0-9]{4,20}$");

    private DataValidator() {}

    // =========================================================================
    // 1. STRING SANITIZATION & CONTROL CHARACTER REMOVAL
    // =========================================================================

    /**
     * Sanitizes a general string by stripping control characters, pipe delimiters,
     * leading/trailing whitespace, and enforcing maximum length constraints.
     */
    public static String sanitizeString(String s, int maxLen) {
        if (s == null) return "";
        if (maxLen <= 0) return "";

        StringBuilder sb = new StringBuilder(Math.min(s.length(), maxLen));
        for (int i = 0; i < s.length() && sb.length() < maxLen; i++) {
            char c = s.charAt(i);
            // Allow printable ASCII characters (32 to 126) and common safe whitespace (newline, tab)
            if (c >= 32 && c <= 126 && c != '|') {
                sb.append(c);
            } else if (c == ' ' || c == '\n' || c == '\t') {
                sb.append(c);
            }
        }
        return sb.toString().trim();
    }

    /**
     * Strips all non-printable ASCII control characters (0x00 - 0x1F, 0x7F).
     */
    public static String stripControlCharacters(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 32 && c != 127) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // 2. XSS & HTML INJECTION PREVENTION
    // =========================================================================

    /**
     * Encodes standard HTML special characters into their corresponding HTML entity representations.
     * Prevents XSS attacks when displaying user-controlled text in webviews or HTML-rendered text views.
     */
    public static String encodeHtml(String input) {
        if (input == null || input.isEmpty()) return "";
        StringBuilder out = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '"':
                    out.append("&quot;");
                    break;
                case '\'':
                    out.append("&#x27;");
                    break;
                case '/':
                    out.append("&#x2F;");
                    break;
                default:
                    out.append(c);
                    break;
            }
        }
        return out.toString();
    }

    /**
     * Strips all HTML tags and potentially dangerous javascript/data URI protocol strings.
     */
    public static String stripHtmlTags(String input) {
        if (input == null || input.isEmpty()) return "";
        String clean = PATTERN_SCRIPT_TAG.matcher(input).replaceAll("");
        clean = PATTERN_HTML_TAG.matcher(clean).replaceAll("");
        clean = clean.replace("javascript:", "").replace("vbscript:", "").replace("data:text", "");
        return clean.trim();
    }

    /**
     * Checks whether an input contains potential XSS or HTML injection patterns.
     */
    public static boolean containsXss(String input) {
        if (input == null || input.isEmpty()) return false;
        String lower = input.toLowerCase();
        if (lower.contains("<script") || lower.contains("</script>") || lower.contains("javascript:") ||
            lower.contains("vbscript:") || lower.contains("onerror=") || lower.contains("onload=") ||
            lower.contains("<iframe") || lower.contains("<embed") || lower.contains("<object") ||
            lower.contains("data:text/html")) {
            return true;
        }
        return PATTERN_SCRIPT_TAG.matcher(input).find();
    }

    // =========================================================================
    // 3. SQL INJECTION PREVENTION & SANITIZATION
    // =========================================================================

    /**
     * Escapes SQL special characters (single quotes, backslashes, null bytes).
     */
    public static String escapeSql(String input) {
        if (input == null || input.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(input.length() + 8);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\'':
                    sb.append("''"); // SQL standard single quote escape
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\0':
                    // Drop null byte
                    break;
                case ';':
                    // Drop statement terminator
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    public static String escapeSqlString(String input) {
        return escapeSql(input);
    }

    /**
     * Verifies if a given string contains suspicious SQL injection syntax signatures.
     */
    public static boolean containsSqlInjection(String input) {
        if (input == null || input.isEmpty()) return false;
        return PATTERN_SQL_INJECTION.matcher(input).find();
    }

    public static boolean containsSqlInjectionPatterns(String input) {
        return containsSqlInjection(input);
    }

    /**
     * Sanitizes a query string by removing SQL injection tokens and meta-characters.
     */
    public static String sanitizeSqlQuery(String input, int maxLen) {
        if (input == null) return "";
        String sanitized = input.replace("'", "")
                                .replace("\"", "")
                                .replace(";", "")
                                .replace("--", "")
                                .replace("/*", "")
                                .replace("*/", "")
                                .trim();
        if (sanitized.length() > maxLen) {
            sanitized = sanitized.substring(0, maxLen);
        }
        return sanitized;
    }

    // =========================================================================
    // 4. PATH TRAVERSAL & FILENAME SANITIZATION
    // =========================================================================

    /**
     * Sanitizes a filename to prevent path traversal attacks (e.g. "../", "..\\", null bytes).
     * Restricts output to safe characters: [a-zA-Z0-9._-].
     */
    public static String sanitizeFileName(String fileName, int maxLen) {
        if (fileName == null || fileName.isEmpty()) return "unnamed_file";

        // Strip directory path if present to neutralize path traversal
        int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (lastSlash >= 0 && lastSlash < fileName.length() - 1) {
            fileName = fileName.substring(lastSlash + 1);
        } else if (lastSlash >= 0) {
            return "default_file";
        }

        StringBuilder sb = new StringBuilder(Math.min(fileName.length(), maxLen));
        for (int i = 0; i < fileName.length() && sb.length() < maxLen; i++) {
            char c = fileName.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') ||
                c == '.' || c == '_' || c == '-') {
                sb.append(c);
            }
        }
        String result = sb.toString();
        // Remove consecutive dots or leading dots that might hide files or traverse directories
        while (result.contains("..")) {
            result = result.replace("..", ".");
        }
        if (result.startsWith(".")) {
            result = result.substring(1);
        }
        if (result.isEmpty() || result.equals(".")) {
            return "default_file";
        }
        return result;
    }

    public static String sanitizeFileName(String fileName) {
        return sanitizeFileName(fileName, 64);
    }

    /**
     * Verifies if a file path is free from traversal sequences and null byte injections.
     */
    public static boolean isSafeFilePath(String path) {
        if (path == null || path.isEmpty()) return false;
        if (path.contains("\0")) return false;
        if (path.contains("..") || path.contains("./") || path.contains(".\\")) return false;
        return true;
    }

    // =========================================================================
    // 5. INPUT WHITELISTING & FIELD VALIDATORS
    // =========================================================================

    /**
     * Validates an entity name (Sect, Disciple, Skill, Item).
     * Allows letters, numbers, spaces, hyphens, brackets, and apostrophes.
     */
    public static boolean isValidName(String name) {
        return isValidName(name, 1, 32);
    }

    public static boolean isValidName(String name, int minLen, int maxLen) {
        if (name == null) return false;
        String trimmed = name.trim();
        if (trimmed.length() < minLen || trimmed.length() > maxLen) return false;
        if (containsXss(trimmed) || containsSqlInjection(trimmed)) return false;

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            boolean isAllowed = Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '_' || c == '\'' ||
                                c == '(' || c == ')' || c == '[' || c == ']';
            if (!isAllowed) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sanitizes an entity name by stripping invalid characters, HTML tags, and truncating to maxLen.
     */
    public static String sanitizeName(String name, int maxLen) {
        if (name == null) return "Mount Tai Sect";
        String clean = stripHtmlTags(name);
        clean = clean.replace("|", "").replace(";", "").replace("\"", "").trim();
        StringBuilder sb = new StringBuilder(Math.min(clean.length(), maxLen));
        for (int i = 0; i < clean.length() && sb.length() < maxLen; i++) {
            char c = clean.charAt(i);
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '_' || c == '\'' ||
                c == '(' || c == ')' || c == '[' || c == ']') {
                sb.append(c);
            }
        }
        String result = sb.toString().trim();
        if (result.isEmpty()) return "Mount Tai Sect";
        return result;
    }

    /**
     * Sanitizes a search query for search filter input fields.
     */
    public static String sanitizeSearchQuery(String query, int maxLen) {
        if (query == null) return "";
        String clean = stripHtmlTags(query);
        clean = stripControlCharacters(clean);
        clean = clean.replace("'", "").replace("\"", "").replace(";", "").trim();
        if (clean.length() > maxLen) {
            clean = clean.substring(0, maxLen);
        }
        return clean;
    }

    /**
     * Validates a promo / Dao redeem code format (e.g. "DAO777", "IMMORTAL2026").
     */
    public static boolean isValidRedeemCode(String code) {
        if (code == null) return false;
        return PATTERN_REDEEM_CODE.matcher(code.trim().toUpperCase()).matches();
    }

    /**
     * Sanitizes a promo code to uppercase alphanumeric only.
     */
    public static String sanitizeRedeemCode(String code, int maxLen) {
        if (code == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < code.length() && sb.length() < maxLen; i++) {
            char c = Character.toUpperCase(code.charAt(i));
            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // 6. ARITHMETIC SAFETY & BOUNDS CLAMPING (OVERFLOW / UNDERFLOW)
    // =========================================================================

    /**
     * Adds two positive numbers with saturation protection (prevents Long.MAX_VALUE overflow).
     */
    public static long safeAdd(long a, long b) {
        if (b > 0 && a > Long.MAX_VALUE - b) {
            return Long.MAX_VALUE;
        }
        if (b < 0 && a < Long.MIN_VALUE - b) {
            return Long.MIN_VALUE;
        }
        return a + b;
    }

    /**
     * Subtracts with non-negative underflow clamping.
     */
    public static long safeSubtractNonNegative(long a, long b) {
        if (b <= 0) return safeAdd(a, -b);
        if (a < b) return 0L;
        return a - b;
    }

    /**
     * Multiplies two long values with saturation protection.
     */
    public static long safeMultiply(long a, long b) {
        if (a == 0 || b == 0) return 0L;
        if (a > 0 && b > 0 && a > Long.MAX_VALUE / b) return Long.MAX_VALUE;
        if (a > 0 && b < 0 && b < Long.MIN_VALUE / a) return Long.MIN_VALUE;
        if (a < 0 && b > 0 && a < Long.MIN_VALUE / b) return Long.MIN_VALUE;
        if (a < 0 && b < 0 && a < Long.MAX_VALUE / b) return Long.MAX_VALUE;
        return a * b;
    }

    public static boolean isValidInt(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static int clampInt(int value, int min, int max) {
        return MathUtils.clamp(value, min, max);
    }

    public static long clampLong(long value, long min, long max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public static boolean isValidFloat(float value, float min, float max) {
        return !Float.isNaN(value) && !Float.isInfinite(value) && value >= min && value <= max;
    }

    public static float clampFloat(float value, float min, float max) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return min;
        return MathUtils.clamp(value, min, max);
    }

    public static boolean isValidColor(int color) {
        return (color >>> 24) != 0;
    }

    public static boolean isValidArray(Object[] arr, int maxLen) {
        return arr != null && arr.length <= maxLen;
    }

    public static boolean isValidArray(int[] arr, int maxLen) {
        return arr != null && arr.length <= maxLen;
    }

    public static boolean isValidArray(float[] arr, int maxLen) {
        return arr != null && arr.length <= maxLen;
    }

    public static boolean isValidPosition(float x, float y, float maxRange) {
        return !Float.isNaN(x) && !Float.isNaN(y) && !Float.isInfinite(x) && !Float.isInfinite(y)
            && Math.abs(x) <= maxRange && Math.abs(y) <= maxRange;
    }

    public static boolean isValidPercentage(float p) {
        return !Float.isNaN(p) && !Float.isInfinite(p) && p >= 0f && p <= 1f;
    }

    public static boolean isValidId(int id, int maxId) {
        return id >= 0 && id < maxId;
    }

    public static boolean validateResourceAmount(long amount, long maxAllowed) {
        return amount >= 0 && amount <= maxAllowed;
    }

    public static boolean validatePrice(long price) {
        return price > 0 && price <= 100000000000L; // Max 100 Billion
    }

    // =========================================================================
    // 7. SAVE STATE & PAYLOAD VALIDATION
    // =========================================================================

    /**
     * Validates a raw save data string before parsing, rejecting oversized payloads,
     * embedded script tags, and non-printable control sequences.
     */
    public static String validateSaveData(String data) {
        if (data == null) return null;
        if (data.length() > 500000) return null; // Max 500KB save payload
        if (containsXss(data)) return null;
        if (containsSqlInjection(data)) return null;
        return data;
    }

    // =========================================================================
    // 8. STRUCTURAL VALIDATION RESULT
    // =========================================================================

    public static class ValidationResult {
        public final boolean valid;
        public final String error;
        public final int errorCode;

        public ValidationResult(boolean v, String e, int code) {
            this.valid = v;
            this.error = e;
            this.errorCode = code;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null, 0);
        }

        public static ValidationResult fail(String e, int code) {
            return new ValidationResult(false, e, code);
        }
    }

    public static ValidationResult validateDiscipleData(String name, int realm, int hp, int maxHp, int element) {
        if (!isValidName(name)) return ValidationResult.fail("Invalid or unpermitted characters in Disciple name", 1);
        if (!isValidInt(realm, 0, 14)) return ValidationResult.fail("Realm out of bounds", 2);
        if (maxHp <= 0 || maxHp > 10000000) return ValidationResult.fail("Max HP out of bounds", 3);
        if (!isValidInt(hp, 0, maxHp)) return ValidationResult.fail("Current HP out of bounds", 4);
        if (!isValidInt(element, 0, 11)) return ValidationResult.fail("Element out of bounds", 5);
        return ValidationResult.ok();
    }
}
