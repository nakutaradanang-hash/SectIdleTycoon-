package com.sect.idle.systems;

import com.sect.idle.core.Rect;
import com.sect.idle.core.Vector2;
import org.junit.Assert;
import org.junit.Test;

/**
 * CameraSystemTest - Unit tests for viewport management, smooth camera follow,
 * zoom clamping, shake trauma, world-to-screen transforms, and visibility bounds.
 */
public class CameraSystemTest {

    private static final float DELTA = 0.05f;

    @Test
    public void testInitialization() {
        CameraSystem cam = new CameraSystem(800, 600);
        Assert.assertEquals(800, cam.viewportW);
        Assert.assertEquals(600, cam.viewportH);
        Assert.assertEquals(1.0f, cam.zoom, DELTA);
        Assert.assertEquals(0f, cam.pos.x, DELTA);
        Assert.assertEquals(0f, cam.pos.y, DELTA);
    }

    @Test
    public void testMoveAndFollow() {
        CameraSystem cam = new CameraSystem(800, 600);
        cam.moveTo(500f, 500f);
        Assert.assertEquals(500f, cam.targetPos.x, DELTA);
        Assert.assertEquals(500f, cam.targetPos.y, DELTA);

        // Update camera position
        for (int i = 0; i < 60; i++) {
            cam.update(0.016f);
        }
        Assert.assertTrue(cam.pos.x > 0f);
        Assert.assertTrue(cam.pos.y > 0f);
    }

    @Test
    public void testZoomControls() {
        CameraSystem cam = new CameraSystem(800, 600);
        cam.setZoom(2.0f);
        Assert.assertEquals(2.0f, cam.targetZoom, DELTA);

        cam.zoomIn(0.5f);
        Assert.assertTrue(cam.targetZoom >= 2.0f);

        cam.zoomOut(1.0f);
        Assert.assertTrue(cam.targetZoom >= 1.0f);
    }

    @Test
    public void testCoordinateTransforms() {
        CameraSystem cam = new CameraSystem(800, 600);
        cam.pos.set(0f, 0f);
        cam.zoom = 1.0f;

        // Center of world (0,0) with camera at (0,0) -> screen center (400, 300)
        float sx = cam.worldToScreenX(0f);
        float sy = cam.worldToScreenY(0f);
        Assert.assertEquals(400f, sx, DELTA);
        Assert.assertEquals(300f, sy, DELTA);

        // Reverse: screen center (400, 300) -> world (0, 0)
        float wx = cam.screenToWorldX(400f);
        float wy = cam.screenToWorldY(300f);
        Assert.assertEquals(0f, wx, DELTA);
        Assert.assertEquals(0f, wy, DELTA);
    }

    @Test
    public void testVisibilityCheck() {
        CameraSystem cam = new CameraSystem(800, 600);
        cam.pos.set(0f, 0f);

        // (0,0) is at screen center -> visible
        Assert.assertTrue(cam.isVisible(0f, 0f, 50f));

        // (5000, 5000) is far outside -> not visible
        Assert.assertFalse(cam.isVisible(5000f, 5000f, 50f));
    }
}
