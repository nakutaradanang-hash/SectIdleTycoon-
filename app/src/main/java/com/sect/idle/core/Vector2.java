package com.sect.idle.core;

/**
 * Vector2 - 2D Vector representation with utility operations.
 */
public final class Vector2 {
    public float x, y;
    private static final float EPSILON = 0.0001f;

    public Vector2(float x, float y) { set(x, y); }
    public Vector2() { this(0f, 0f); }

    public final void set(float x, float y) {
        this.x = Float.isNaN(x) ? 0f : x;
        this.y = Float.isNaN(y) ? 0f : y;
    }

    public final void set(Vector2 v) {
        if (v != null) { this.x = v.x; this.y = v.y; }
    }

    public final void add(float x, float y) {
        this.x = safe(this.x + x);
        this.y = safe(this.y + y);
    }

    public final void add(Vector2 v) {
        if (v != null) { add(v.x, v.y); }
    }

    public final void sub(float x, float y) {
        this.x = safe(this.x - x);
        this.y = safe(this.y - y);
    }

    public final void sub(Vector2 v) {
        if (v != null) { sub(v.x, v.y); }
    }

    public final void mul(float s) {
        if (Float.isNaN(s) || Float.isInfinite(s)) return;
        this.x = safe(this.x * s);
        this.y = safe(this.y * s);
    }

    public final void div(float s) {
        if (Float.isNaN(s) || Math.abs(s) < EPSILON) return;
        this.x = safe(this.x / s);
        this.y = safe(this.y / s);
    }

    public final float dist(float tx, float ty) {
        if (Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(tx) || Float.isNaN(ty)) return 0f;
        float dx = x - tx, dy = y - ty;
        return (float) Math.hypot(dx, dy);
    }

    public final float dist(Vector2 v) {
        return v == null ? 0f : dist(v.x, v.y);
    }

    public final float distSq(float tx, float ty) {
        float dx = x - tx, dy = y - ty;
        return dx * dx + dy * dy;
    }

    public final float distSq(Vector2 v) {
        return v == null ? 0f : distSq(v.x, v.y);
    }

    public final void lerp(float tx, float ty, float a) {
        a = clamp01(a);
        x = safe(x + (tx - x) * a);
        y = safe(y + (ty - y) * a);
    }

    public final void lerp(Vector2 v, float a) {
        if (v != null) lerp(v.x, v.y, a);
    }

    public final void clamp(Rect r) {
        if (r == null || !r.isValid()) return;
        x = Math.max(r.left(), Math.min(r.right(), x));
        y = Math.max(r.top(), Math.min(r.bottom(), y));
    }

    public final float length() {
        return (float) Math.hypot(x, y);
    }

    public final float lengthSq() {
        return x * x + y * y;
    }

    public final void normalize() {
        float len = length();
        if (len > EPSILON) { div(len); }
        else { x = 0f; y = 0f; }
    }

    public final void copyFrom(Vector2 v) {
        if (v != null) set(v.x, v.y);
    }

    public final void zero() { x = 0f; y = 0f; }

    public final boolean isZero() { return Math.abs(x) < EPSILON && Math.abs(y) < EPSILON; }

    @Override
    public final String toString() {
        return x + "," + y;
    }

    public static Vector2 parse(String s) {
        if (s == null || s.length() == 0) return new Vector2();
        int comma = s.indexOf(',');
        if (comma < 0) return new Vector2();
        try {
            float px = Float.parseFloat(s.substring(0, comma).trim());
            float py = Float.parseFloat(s.substring(comma + 1).trim());
            return new Vector2(px, py);
        } catch (Exception e) {
            return new Vector2();
        }
    }

    private static float safe(float v) {
        return Float.isNaN(v) || Float.isInfinite(v) ? 0f : v;
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }
}
