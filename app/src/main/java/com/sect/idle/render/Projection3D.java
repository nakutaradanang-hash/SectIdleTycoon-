package com.sect.idle.render;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Projection3D - Ultra-fast pseudo-3D and full 3D perspective projection pipeline for 2D Android Canvas.
 * Computes 3D world space (X, Y, Z) to 2D screen space projection with pitch, yaw, elevation, and FOV depth scaling.
 *
 * 100% Zero-GC in runtime render loops using pre-allocated matrix and vector registers.
 */
public final class Projection3D {

    // Camera parameters
    public float camX = 0f;
    public float camY = -350f;
    public float camZ = 520f;
    public float targetX = 0f;
    public float targetY = 0f;
    public float targetZ = 0f;

    public float fov = 480f;
    public float pitch = 38f; // Degrees downward tilt
    public float yaw = 0f;    // Degrees rotation around Z/Y axis
    public float roll = 0f;
    public float zoom = 1.0f;

    // Viewport
    public float screenCenterX = 360f;
    public float screenCenterY = 640f;

    // Pre-calculated trigonometric caches
    private float cosYaw = 1f, sinYaw = 0f;
    private float cosPitch = 1f, sinPitch = 0f;

    // Pre-allocated scratch registers for zero GC
    private final float[] point3D = new float[3];
    private final PointF screenPoint = new PointF();
    private final Path groundPath = new Path();
    private final RectF shadowRect = new RectF();

    public Projection3D() {
        updateTrig();
    }

    public void setViewport(int width, int height) {
        this.screenCenterX = width * 0.5f;
        this.screenCenterY = height * 0.52f;
    }

    public void updateTrig() {
        double radYaw = Math.toRadians(yaw);
        double radPitch = Math.toRadians(pitch);
        cosYaw = (float) Math.cos(radYaw);
        sinYaw = (float) Math.sin(radYaw);
        cosPitch = (float) Math.cos(radPitch);
        sinPitch = (float) Math.sin(radPitch);
    }

    /**
     * Projects 3D world coordinates (x, y, z) to 2D screen coordinates with depth scale.
     * World coordinates: X = Left/Right (- to +), Y = Forward/Depth (- to +), Z = Elevation (Up +)
     *
     * @param wx World X
     * @param wy World Y (Depth in arena)
     * @param wz World Z (Height above ground)
     * @param outScreen Target PointF to write (x, y)
     * @return Depth scale factor (1.0 = near, < 1.0 = far, used for scaling sprites/shadows)
     */
    public float project(float wx, float wy, float wz, PointF outScreen) {
        // Translate relative to camera target
        float dx = wx - (targetX + camX);
        float dy = wy - (targetY + camY);
        float dz = wz - (targetZ + camZ);

        // Apply Yaw (Y-axis / Z-axis rotation)
        float rx = dx * cosYaw - dy * sinYaw;
        float ry = dx * sinYaw + dy * cosYaw;
        float rz = dz;

        // Apply Pitch (Tilt around X-axis)
        float py = ry * cosPitch - rz * sinPitch;
        float pz = ry * sinPitch + rz * cosPitch;

        // Perspective depth division
        float depth = py + fov;
        if (depth <= 10f) depth = 10f; // Prevent division by zero / behind camera

        float scale = (fov / depth) * zoom;

        float screenX = screenCenterX + rx * scale;
        float screenY = screenCenterY - pz * scale; // Invert Z so +Z goes up on screen

        if (outScreen != null) {
            outScreen.x = screenX;
            outScreen.y = screenY;
        }

        return scale;
    }

    /**
     * Projects a 3D ground polygon (such as the Bagua formation stage or tile grid).
     */
    public void projectPolygon(float[][] worldPoints, Path outPath) {
        if (worldPoints == null || worldPoints.length < 3 || outPath == null) return;
        outPath.reset();

        for (int i = 0; i < worldPoints.length; i++) {
            float scale = project(worldPoints[i][0], worldPoints[i][1], worldPoints[i][2], screenPoint);
            if (i == 0) {
                outPath.moveTo(screenPoint.x, screenPoint.y);
            } else {
                outPath.lineTo(screenPoint.x, screenPoint.y);
            }
        }
        outPath.close();
    }

    /**
     * Renders a perspective 3D shadow on the ground plane (Z = 0) under an airborne or grounded unit.
     */
    public void render3DShadow(Canvas canvas, Paint shadowPaint, float wx, float wy, float wz, float radius) {
        if (canvas == null || shadowPaint == null) return;
        float scale = project(wx, wy, 0f, screenPoint);
        if (scale <= 0f) return;

        float shadowW = radius * 2f * scale;
        float shadowH = radius * 0.9f * scale; // Elliptical foreshortening on ground plane

        // Height attenuation: higher airborne units cast softer, larger shadows
        float heightFactor = Math.max(0.15f, 1.0f - (wz / 350f));
        int originalAlpha = shadowPaint.getAlpha();
        shadowPaint.setAlpha((int) (originalAlpha * heightFactor));

        shadowRect.set(screenPoint.x - shadowW * 0.5f, screenPoint.y - shadowH * 0.5f,
                screenPoint.x + shadowW * 0.5f, screenPoint.y + shadowH * 0.5f);
        canvas.drawOval(shadowRect, shadowPaint);

        shadowPaint.setAlpha(originalAlpha);
    }
}
