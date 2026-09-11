package com.sect.idle.systems;

import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionUtil {
    private static final int BUFFER_SIZE = 2048;
    private static final ThreadLocal<byte[]> BUFFER = new ThreadLocal<byte[]>(){
        @Override protected byte[] initialValue(){ return new byte[BUFFER_SIZE]; }
    };

    public static byte[] compress(byte[] data) {
        if (data == null || data.length == 0) return data;
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length / 2);
        byte[] buffer = BUFFER.get();
        try {
            while (!deflater.finished()) {
                int count = deflater.deflate(buffer);
                out.write(buffer, 0, count);
            }
        } finally { deflater.end(); }
        return out.toByteArray();
    }

    public static byte[] decompress(byte[] data) {
        if (data == null || data.length == 0) return data;
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 2);
        byte[] buffer = BUFFER.get();
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                out.write(buffer, 0, count);
            }
        } catch (Exception e) { return data; }
        finally { inflater.end(); }
        return out.toByteArray();
    }

    public static String compressToBase64(String text) {
        if (text == null) return "";
        try {
            byte[] compressed = compress(text.getBytes("UTF-8"));
            return Base64.encodeToString(compressed, Base64.NO_WRAP | Base64.URL_SAFE);
        } catch (Exception e) { return text; }
    }

    public static String decompressFromBase64(String b64) {
        if (b64 == null || b64.isEmpty()) return "";
        try {
            byte[] compressed = Base64.decode(b64, Base64.NO_WRAP | Base64.URL_SAFE);
            return new String(decompress(compressed), "UTF-8");
        } catch (Exception e) { return b64; }
    }
}
