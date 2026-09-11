package com.sect.idle.utils;

import android.graphics.Bitmap;
import android.util.LruCache;
import com.sect.idle.core.GameConfig;

/**
 * BitmapCache v2.0 - LRU cache for bitmaps with memory pressure handling.
 */
public final class BitmapCache {
    private static BitmapCache instance;
    private final LruCache<String, Bitmap> cache;
    private int hitCount;
    private int missCount;
    private int evictCount;
    
    private BitmapCache() {
        int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        if (GameConfig.deviceTier == GameConfig.TIER_LOW) cacheSize = maxMemory / 16;
        
        cache = new LruCache<String, Bitmap>(Math.max(1024, cacheSize)) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
            
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                if (evicted && oldValue != null && !oldValue.isRecycled()) {
                    oldValue.recycle();
                    evictCount++;
                }
            }
        };
    }
    
    public static synchronized BitmapCache get() {
        if (instance == null) instance = new BitmapCache();
        return instance;
    }
    
    public Bitmap get(String key) {
        if (key == null) return null;
        Bitmap bmp = cache.get(key);
        if (bmp != null && !bmp.isRecycled()) {
            hitCount++;
            return bmp;
        }
        missCount++;
        return null;
    }
    
    public void put(String key, Bitmap bitmap) {
        if (key == null || bitmap == null || bitmap.isRecycled()) return;
        cache.put(key, bitmap);
    }
    
    public Bitmap remove(String key) {
        if (key == null) return null;
        return cache.remove(key);
    }
    
    public boolean contains(String key) {
        if (key == null) return false;
        Bitmap bmp = cache.get(key);
        return bmp != null && !bmp.isRecycled();
    }
    
    public void clear() {
        cache.evictAll();
    }
    
    public void trimToSize(int maxSizeKB) {
        cache.trimToSize(maxSizeKB);
    }
    
    public void onLowMemory() {
        cache.evictAll();
    }
    
    public String getStats() {
        int total = hitCount + missCount;
        float hitRate = total > 0 ? (hitCount * 100f / total) : 0;
        return "Cache: " + cache.size() + "/" + cache.maxSize() + "KB Hits:" + String.format(java.util.Locale.US, "%.1f", hitRate) + "% Evicted:" + evictCount;
    }
    
    public void resetStats() {
        hitCount = 0; missCount = 0; evictCount = 0;
    }
}
