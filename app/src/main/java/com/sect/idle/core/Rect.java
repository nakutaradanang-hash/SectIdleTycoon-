package com.sect.idle.core;

public final class Rect {
    public float x, y, w, h;

    public Rect(float x, float y, float w, float h) { set(x, y, w, h); }
    public Rect() { set(0f, 0f, 0f, 0f); }

    public final void set(float x, float y, float w, float h) {
        this.x = Float.isNaN(x) ? 0f : x;
        this.y = Float.isNaN(y) ? 0f : y;
        this.w = Math.max(0f, Float.isNaN(w) ? 0f : w);
        this.h = Math.max(0f, Float.isNaN(h) ? 0f : h);
    }

    public final void set(Rect r) {
        if (r != null) set(r.x, r.y, r.w, r.h);
    }

    public final boolean contains(float px, float py) {
        if (Float.isNaN(px) || Float.isNaN(py)) return false;
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }

    public final boolean contains(Vector2 v) {
        return v != null && contains(v.x, v.y);
    }

    public final boolean intersects(Rect r) {
        if (r == null || w <= 0f || h <= 0f || r.w <= 0f || r.h <= 0f) return false;
        return x < r.x + r.w && x + w > r.x && y < r.y + r.h && y + h > r.y;
    }

    public final float cx() { return x + w * 0.5f; }
    public final float cy() { return y + h * 0.5f; }
    public final float left() { return x; }
    public final float right() { return x + w; }
    public final float top() { return y; }
    public final float bottom() { return y + h; }

    public final void clamp(Vector2 v) {
        if (v == null || !isValid()) return;
        v.x = Math.max(x, Math.min(right(), v.x));
        v.y = Math.max(y, Math.min(bottom(), v.y));
    }

    public final boolean isValid() {
        return w > 0f && h > 0f && !Float.isNaN(x) && !Float.isNaN(y);
    }

    public final void expand(float amount) {
        x -= amount; y -= amount; w += amount * 2f; h += amount * 2f;
        if (w < 0f) w = 0f; if (h < 0f) h = 0f;
    }

    public final float area() { return w * h; }

    @Override
    public final String toString() {
        return "[" + x + "," + y + "," + w + "," + h + "]";
    }
}
