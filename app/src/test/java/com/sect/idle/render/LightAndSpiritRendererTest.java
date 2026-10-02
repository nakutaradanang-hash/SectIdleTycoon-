package com.sect.idle.render;

import android.graphics.Canvas;
import com.sect.idle.models.BattleUnit;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.CameraSystem;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class LightAndSpiritRendererTest {

    private LightSystem lightSystem;
    private SpiritAuraBufferRenderer auraRenderer;
    private CameraSystem cameraSystem;

    @Before
    public void setUp() {
        lightSystem = new LightSystem(1080, 1920);
        auraRenderer = new SpiritAuraBufferRenderer();
        auraRenderer.updateBufferSize(1080, 1920);
        cameraSystem = new CameraSystem(1080, 1920);
    }

    @Test
    public void testLightSystemAddUpdateRemove() {
        Assert.assertEquals(0, lightSystem.getActiveCount());

        int id1 = lightSystem.addLight(100f, 150f, 80f, 0xFFFFCC00, 1.0f, 0);
        int id2 = lightSystem.addLight(300f, 400f, 120f, 0xFF00E5FF, 0.8f, 1);
        int id3 = lightSystem.addLight(500f, 600f, 100f, 0xFFFF1744, 0.9f, 2);

        Assert.assertTrue(id1 >= 0);
        Assert.assertTrue(id2 >= 0);
        Assert.assertTrue(id3 >= 0);
        Assert.assertEquals(3, lightSystem.getActiveCount());

        // Update light
        lightSystem.updateLight(id1, 120f, 160f, 90f, 0xFFFFEE55, 1.2f);
        lightSystem.update(0.016f);

        Canvas canvas = new Canvas();
        lightSystem.render(canvas, cameraSystem);
        lightSystem.renderAmbient(canvas);

        // Remove light
        lightSystem.removeLight(id2);
        Assert.assertEquals(2, lightSystem.getActiveCount());

        lightSystem.clearLights();
        Assert.assertEquals(0, lightSystem.getActiveCount());
    }

    @Test
    public void testSpiritAuraBufferRenderer() {
        Canvas canvas = new Canvas();
        ArrayList<BattleUnit> units = new ArrayList<>();
        Disciple d = new Disciple("Lin Feng");
        BattleUnit u = new BattleUnit(d, 0);
        u.pos.set(200f, 200f);
        u.isAlive = true;
        u.actionBar = 100;
        u.maxActionBar = 100;
        units.add(u);

        // Render spirit auras for units
        auraRenderer.renderSpiritAuras(canvas, units, 1.5f, 1080, 1920);
        auraRenderer.renderSingleUnitAura(canvas, u, u.pos.x, u.pos.y, 1.5f, 0);
        auraRenderer.renderSpiritualBeam(canvas, 100f, 100f, 300f, 300f, 0xFF00E5FF, 0.5f);

        // Null checks
        auraRenderer.renderSpiritAuras(null, units, 0f, 1080, 1920);
        auraRenderer.renderSpiritAuras(canvas, null, 0f, 1080, 1920);

        auraRenderer.setQualityTier(SpiritAuraBufferRenderer.QUALITY_ULTRA);
        Assert.assertEquals(SpiritAuraBufferRenderer.QUALITY_ULTRA, auraRenderer.getQualityTier());
        auraRenderer.releaseBuffer();
    }
}
