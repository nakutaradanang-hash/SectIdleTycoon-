package com.sect.idle.gameplay;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * SaveManagerTest - Unit tests for save export, import, anti-tamper signature checking,
 * corrupt payload recovery, and local state persistence.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SaveManagerTest {

    private SaveManager saveManager;
    private SectData sectData;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        saveManager = new SaveManager(context);
        saveManager.clear();

        sectData = SectData.getInstance();
        sectData.reset();
        sectData.sectName = "Immortal Sword Sect";
        sectData.spiritStones = 25000;
        sectData.spiritHerbs = 5000;
        sectData.spiritOres = 3000;
        sectData.sectRealm = 3;
        sectData.sectExp = 45000;
    }

    @Test
    public void testSaveAndLoadCycle() {
        saveManager.save();

        sectData.spiritStones = 0;
        sectData.sectRealm = 0;
        sectData.sectName = "Reset Sect";

        boolean loaded = saveManager.load();
        Assert.assertTrue(loaded);
        Assert.assertEquals("Immortal Sword Sect", sectData.sectName);
        Assert.assertEquals(25000L, sectData.spiritStones);
        Assert.assertEquals(5000L, sectData.spiritHerbs);
        Assert.assertEquals(3, sectData.sectRealm);
    }

    @Test
    public void testExportAndImportSaveString() {
        String exported = saveManager.exportSaveString();
        Assert.assertNotNull(exported);
        Assert.assertTrue(exported.contains("::SIG::"));

        sectData.reset();
        Assert.assertEquals(1000L, sectData.spiritStones);

        boolean imported = saveManager.importSaveString(exported);
        Assert.assertTrue(imported);
        Assert.assertEquals("Immortal Sword Sect", sectData.sectName);
        Assert.assertEquals(25000L, sectData.spiritStones);
        Assert.assertEquals(3, sectData.sectRealm);
    }

    @Test
    public void testImportTamperedSaveStringRejection() {
        String exported = saveManager.exportSaveString();
        // Modify the payload without updating the signature
        String tampered = exported.replace("25000", "999999999");

        boolean imported = saveManager.importSaveString(tampered);
        Assert.assertFalse(imported);
    }

    @Test
    public void testImportMaliciousSaveStringRejection() {
        Assert.assertFalse(saveManager.importSaveString(null));
        Assert.assertFalse(saveManager.importSaveString(""));
        Assert.assertFalse(saveManager.importSaveString("<script>alert('hack')</script>::SIG::1234"));
        Assert.assertFalse(saveManager.importSaveString("DROP TABLE users;::SIG::1234"));
        Assert.assertFalse(saveManager.importSaveString("InvalidFormatPayloadNoSig"));
    }
}
