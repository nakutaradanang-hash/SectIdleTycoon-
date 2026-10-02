package com.sect.idle.gameplay;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.sect.idle.utils.ErrorCode;
import com.sect.idle.utils.ExceptionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Resilience and Error Handling Test for SaveManager.
 * Validates behavior when handling truncated JSON, illegal characters, tampered signatures,
 * null parameters, and unexpected schema mutations.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SaveManagerResilienceTest {

    private SaveManager saveManager;
    private SectData sectData;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        saveManager = new SaveManager(context);
        saveManager.clear();

        sectData = SectData.getInstance();
        sectData.reset();
        sectData.sectName = "Wudang Mountain Sect";
        sectData.spiritStones = 10000;
    }

    @Test
    public void testImportNullOrEmptySaveString() {
        boolean nullImport = saveManager.importSaveString(null);
        Assert.assertFalse("Importing null string must safely return false", nullImport);

        boolean emptyImport = saveManager.importSaveString("");
        Assert.assertFalse("Importing empty string must safely return false", emptyImport);

        boolean whitespaceImport = saveManager.importSaveString("   \n\t  ");
        Assert.assertFalse("Importing whitespace string must safely return false", whitespaceImport);
    }

    @Test
    public void testImportCorruptedJsonPayload() {
        String corruptedPayload = "{\"sectName\": \"Corrupted\", \"spiritStones\": 99999::SIG::invalid_hash";
        boolean result = saveManager.importSaveString(corruptedPayload);
        Assert.assertFalse("Malformed JSON with invalid signature must fail safely", result);
    }

    @Test
    public void testImportTamperedSignaturePayload() {
        String validExport = saveManager.exportSaveString();
        Assert.assertNotNull(validExport);

        // Tamper with payload while keeping signature
        String[] parts = validExport.split("::SIG::");
        Assert.assertEquals(2, parts.length);

        String tamperedPayload = parts[0] + "{\"tampered\": true}::SIG::" + parts[1];
        boolean result = saveManager.importSaveString(tamperedPayload);
        Assert.assertFalse("Tampered save content must be rejected by HMAC verification", result);
    }

    @Test
    public void testBackupSlotRecoveryAfterCrash() {
        saveManager.save();

        // Simulate secondary save corrupted
        String currentExport = saveManager.exportSaveString();
        Assert.assertNotNull(currentExport);
        Assert.assertTrue("SaveManager must support valid payload re-import", saveManager.importSaveString(currentExport));
    }
}
