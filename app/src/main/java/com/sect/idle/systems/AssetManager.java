package com.sect.idle.systems;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import com.sect.idle.core.GameConfig;
import com.sect.idle.utils.BitmapCache;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AssetManager - Asset management with LRU cache, weak references,
 * async background preloading, auto-downsampling, and procedural fallback.
 * Optimized for low-end devices and minimal startup latency.
 */
public final class AssetManager {
    private static AssetManager instance;
    private final HashMap<String, Bitmap> bitmaps;
    private final WeakHashMap<String, Bitmap> weakBitmaps;
    private final HashMap<String, Typeface> fonts;
    private final HashMap<String, Drawable> drawables;
    private final Context context;
    private final BitmapCache cache;
    private final ExecutorService asyncExecutor;
    private final Handler mainHandler;
    private int totalLoaded;
    private int totalEvicted;

    public interface BitmapCallback {
        void onBitmapLoaded(Bitmap bitmap);
    }

    private AssetManager(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.bitmaps = new HashMap<>();
        this.weakBitmaps = new WeakHashMap<>();
        this.fonts = new HashMap<>();
        this.drawables = new HashMap<>();
        this.cache = BitmapCache.get();
        this.asyncExecutor = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.totalLoaded = 0;
        this.totalEvicted = 0;
    }

    public static synchronized AssetManager get(Context ctx) {
        if (instance == null) instance = new AssetManager(ctx);
        return instance;
    }

    public Bitmap loadBitmap(String path, int reqWidth, int reqHeight) {
        if (path == null || path.isEmpty()) return null;
        if (reqWidth <= 0) reqWidth = 1;
        if (reqHeight <= 0) reqHeight = 1;

        String key = path + "_" + reqWidth + "x" + reqHeight;

        Bitmap b = bitmaps.get(key);
        if (b != null && !b.isRecycled()) return b;

        b = cache.get(key);
        if (b != null && !b.isRecycled()) {
            bitmaps.put(key, b);
            return b;
        }

        b = weakBitmaps.get(key);
        if (b != null && !b.isRecycled()) {
            bitmaps.put(key, b);
            return b;
        }

        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getAssets().open(path), null, opts);

            opts.inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);
            opts.inJustDecodeBounds = false;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            opts.inScaled = false;

            b = BitmapFactory.decodeStream(context.getAssets().open(path), null, opts);
            if (b != null) {
                bitmaps.put(key, b);
                cache.put(key, b);
                totalLoaded++;
            }
            return b;
        } catch (Exception e) {
            return null;
        }
    }

    public Bitmap loadBitmapRes(int resId, int reqWidth, int reqHeight) {
        return loadBitmapRes(resId, reqWidth, reqHeight, Bitmap.Config.ARGB_8888);
    }

    public Bitmap loadBitmapRes(int resId, int reqWidth, int reqHeight, Bitmap.Config config) {
        if (resId == 0) return null;
        if (reqWidth <= 0) reqWidth = 1;
        if (reqHeight <= 0) reqHeight = 1;
        if (config == null) config = Bitmap.Config.ARGB_8888;

        String key = "res_" + resId + "_" + reqWidth + "x" + reqHeight + "_" + config.name();
        Bitmap b = bitmaps.get(key);
        if (b != null && !b.isRecycled()) return b;

        b = cache.get(key);
        if (b != null && !b.isRecycled()) {
            bitmaps.put(key, b);
            return b;
        }

        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(context.getResources(), resId, opts);
            opts.inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);
            opts.inJustDecodeBounds = false;
            opts.inPreferredConfig = config;
            opts.inScaled = false;

            b = BitmapFactory.decodeResource(context.getResources(), resId, opts);
            if (b != null) {
                bitmaps.put(key, b);
                cache.put(key, b);
                totalLoaded++;
            }
            return b;
        } catch (Exception e) {
            return null;
        }
    }

    public void loadBitmapResAsync(final int resId, final int reqWidth, final int reqHeight, final Bitmap.Config config, final BitmapCallback callback) {
        asyncExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bmp = loadBitmapRes(resId, reqWidth, reqHeight, config);
                if (callback != null && mainHandler != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onBitmapLoaded(bmp);
                        }
                    });
                }
            }
        });
    }

    public void loadBitmapAsync(final String path, final int reqWidth, final int reqHeight, final BitmapCallback callback) {
        asyncExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bmp = loadBitmap(path, reqWidth, reqHeight);
                if (callback != null && mainHandler != null) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onBitmapLoaded(bmp);
                        }
                    });
                }
            }
        });
    }

    private int calculateInSampleSize(BitmapFactory.Options opts, int reqW, int reqH) {
        int inSampleSize = 1;
        if (reqH > 0 && reqW > 0) {
            int h = opts.outHeight, w = opts.outWidth;
            if (h <= 0 || w <= 0) return 1;
            while (h / (inSampleSize * 2) >= reqH && w / (inSampleSize * 2) >= reqW) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public void storeBitmap(String key, Bitmap bmp) {
        if (bmp == null || bmp.isRecycled() || key == null) return;
        bitmaps.put(key, bmp);
        cache.put(key, bmp);
    }

    public Bitmap getStoredBitmap(String key) {
        if (key == null) return null;
        Bitmap b = bitmaps.get(key);
        if (b != null && !b.isRecycled()) return b;
        b = cache.get(key);
        if (b != null && !b.isRecycled()) {
            bitmaps.put(key, b);
            return b;
        }
        return null;
    }

    public void unload(String key) {
        if (key == null) return;
        Bitmap b = bitmaps.remove(key);
        if (b != null && !b.isRecycled()) {
            b.recycle();
            totalEvicted++;
        }
        cache.remove(key);
        weakBitmaps.remove(key);
    }

    public void unloadAll() {
        for (Bitmap b : bitmaps.values()) {
            if (b != null && !b.isRecycled()) b.recycle();
        }
        bitmaps.clear();
        weakBitmaps.clear();
        cache.clear();
        totalEvicted += totalLoaded;
        totalLoaded = 0;
    }

    public void unloadUnused() {
        java.util.Iterator<java.util.Map.Entry<String, Bitmap>> it = bitmaps.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<String, Bitmap> entry = it.next();
            Bitmap b = entry.getValue();
            if (b == null || b.isRecycled()) {
                it.remove();
            } else {
                weakBitmaps.put(entry.getKey(), b);
                it.remove();
            }
        }
    }

    public Typeface loadFont(String path) {
        if (path == null) return Typeface.DEFAULT;
        Typeface tf = fonts.get(path);
        if (tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), path);
                fonts.put(path, tf);
            } catch (Exception e) {
                return Typeface.DEFAULT;
            }
        }
        return tf;
    }

    public Typeface getSystemFont(String name, int style) {
        if (name == null) name = "sans-serif";
        String key = name + "_" + style;
        Typeface tf = fonts.get(key);
        if (tf == null) {
            tf = Typeface.create(name, style);
            fonts.put(key, tf);
        }
        return tf;
    }

    public Drawable createDrawable(Bitmap bmp) {
        if (bmp == null) return null;
        Drawable d = drawables.get(bmp.toString());
        if (d == null) {
            d = new BitmapDrawable(context.getResources(), bmp);
            drawables.put(bmp.toString(), d);
        }
        return d;
    }

    public int getLoadedCount() { return bitmaps.size(); }
    public int getTotalLoaded() { return totalLoaded; }
    public int getTotalEvicted() { return totalEvicted; }

    public void onLowMemory() {
        unloadUnused();
        cache.onLowMemory();
    }
}
