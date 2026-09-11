package com.sect.idle.systems;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * CompressionUtilTest - Unit tests for GZIP/Deflate compression and Base64 string roundtrips.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class CompressionUtilTest {

    @Test
    public void testByteCompressionRoundtrip() {
        String original = "Cloud Mist Immortal Sect - Daoist Heavenly Cultivation Protocol v7.0";
        byte[] rawBytes = original.getBytes();

        byte[] compressed = CompressionUtil.compress(rawBytes);
        Assert.assertNotNull(compressed);

        byte[] decompressed = CompressionUtil.decompress(compressed);
        Assert.assertNotNull(decompressed);
        Assert.assertEquals(original, new String(decompressed));
    }

    @Test
    public void testBase64StringCompressionRoundtrip() {
        String jsonPayload = "{\"sectName\":\"Cloud Mist Sect\",\"spiritStones\":999999,\"disciples\":[{\"name\":\"Lin Feng\",\"realm\":5}]}";

        String compressedB64 = CompressionUtil.compressToBase64(jsonPayload);
        Assert.assertNotNull(compressedB64);
        Assert.assertFalse(compressedB64.isEmpty());

        String decompressed = CompressionUtil.decompressFromBase64(compressedB64);
        Assert.assertEquals(jsonPayload, decompressed);
    }

    @Test
    public void testNullAndEmptySafety() {
        Assert.assertNull(CompressionUtil.compress(null));
        Assert.assertNull(CompressionUtil.decompress(null));
        Assert.assertEquals("", CompressionUtil.compressToBase64(null));
        Assert.assertEquals("", CompressionUtil.decompressFromBase64(null));
        Assert.assertEquals("", CompressionUtil.decompressFromBase64(""));
    }
}
