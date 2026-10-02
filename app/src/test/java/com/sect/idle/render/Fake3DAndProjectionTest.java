package com.sect.idle.render;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import com.sect.idle.systems.CameraSystem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class Fake3DAndProjectionTest {

    private Fake3D fake3D;
    private Projection3D projection3D;
    private CameraSystem cameraSystem;

    @Before
    public void setUp() {
        fake3D = new Fake3D();
        projection3D = new Projection3D();
        cameraSystem = new CameraSystem(800, 600);
    }

    @Test
    public void testFake3DNullSafety() {
        // Must not crash when canvas or camera is null
        fake3D.renderShadow(null, cameraSystem, 100f, 100f, 50f, 50f, 10f);
        fake3D.renderShadow(new Canvas(), null, 100f, 100f, 50f, 50f, 10f);
        fake3D.renderExtrudedRect(null, cameraSystem, 0f, 0f, 10f, 10f, 10f, 0xFF000000, 0xFFFFFFFF);
        fake3D.renderIsoBlock(null, cameraSystem, 0f, 0f, 10f, 10f, 0, 0, 0);
        fake3D.renderReflection(null, cameraSystem, 0f, 0f, 10f, 10f, 0, 0.5f);
        fake3D.renderSpecular(null, cameraSystem, 0f, 0f, 10f, 0, 0.5f);
    }

    @Test
    public void testFake3DRenderingExecution() {
        Canvas canvas = new Canvas();
        fake3D.renderShadow(canvas, cameraSystem, 200f, 300f, 40f, 30f, 15f);
        fake3D.renderExtrudedRect(canvas, cameraSystem, 100f, 100f, 60f, 40f, 20f, 0xFF555555, 0xFF222222);
        fake3D.renderIsoBlock(canvas, cameraSystem, 150f, 150f, 32f, 24f, 0xFF777777, 0xFF444444, 0xFF222222);
        fake3D.renderReflection(canvas, cameraSystem, 150f, 200f, 40f, 40f, 0x88FFFFFF, 0.3f);
        fake3D.renderSpecular(canvas, cameraSystem, 150f, 150f, 25f, 0xFFFFFFFF, 0.8f);
    }

    @Test
    public void testProjection3DCalculations() {
        projection3D.setViewport(1080, 1920);
        projection3D.yaw = 15f;
        projection3D.pitch = 45f;
        projection3D.updateTrig();

        PointF outScreen = new PointF();
        float scale = projection3D.project(0f, 0f, 0f, outScreen);

        Assert.assertTrue("Scale must be positive", scale > 0f);
        Assert.assertTrue("Screen X must be bounded", outScreen.x > 0f);
        Assert.assertTrue("Screen Y must be bounded", outScreen.y > 0f);

        // Near vs Far depth scaling test
        PointF nearScreen = new PointF();
        PointF farScreen = new PointF();
        float nearScale = projection3D.project(0f, -100f, 0f, nearScreen);
        float farScale = projection3D.project(0f, 200f, 0f, farScreen);

        Assert.assertTrue("Near scale should exceed far scale", nearScale >= farScale);
    }

    @Test
    public void testProjection3DPolygonAndShadow() {
        Path path = new Path();
        float[][] quad = new float[][] {
            {-50f, -50f, 0f},
            {50f, -50f, 0f},
            {50f, 50f, 0f},
            {-50f, 50f, 0f}
        };

        projection3D.projectPolygon(quad, path);
        Assert.assertFalse("Path should not be empty after projecting quad", path.isEmpty());

        // Null checks
        projection3D.projectPolygon(null, path);
        projection3D.projectPolygon(new float[1][3], path);

        Canvas canvas = new Canvas();
        android.graphics.Paint paint = new android.graphics.Paint();
        projection3D.render3DShadow(canvas, paint, 0f, 0f, 50f, 30f);
        projection3D.render3DShadow(null, paint, 0f, 0f, 50f, 30f);
        projection3D.render3DShadow(canvas, null, 0f, 0f, 50f, 30f);
    }
}
