package com.sect.idle.apm;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * LeakDetector - Lightweight in-app memory leak watcher.
 * Tracks retained activities, views, bitmaps, or listeners that fail to get garbage collected.
 */
public final class LeakDetector {

    public static final class LeakCandidate {
        public final String key;
        public final String className;
        public final String description;
        public final long watchedTime;
        public int retainedGcPasses;

        public LeakCandidate(String key, String className, String description, long watchedTime) {
            this.key = key;
            this.className = className;
            this.description = description;
            this.watchedTime = watchedTime;
            this.retainedGcPasses = 0;
        }

        public long getRetainedDurationMs() {
            return System.currentTimeMillis() - watchedTime;
        }
    }

    private static final class KeyedWeakReference extends WeakReference<Object> {
        final String key;
        final String className;
        final String description;
        final long watchedTime;
        int retainedGcPasses = 0;

        KeyedWeakReference(String key, Object referent, String description,
                           ReferenceQueue<Object> queue) {
            super(referent, queue);
            this.key = key;
            this.className = referent.getClass().getName();
            this.description = description != null ? description : "";
            this.watchedTime = System.currentTimeMillis();
        }
    }

    private final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
    private final ConcurrentHashMap<String, KeyedWeakReference> watchedReferences = 
            new ConcurrentHashMap<String, KeyedWeakReference>();
    private final AtomicInteger keyGenerator = new AtomicInteger(0);

    public String watch(Object object, String description) {
        if (object == null) return null;
        
        pruneClearedReferences();
        String key = "leak_" + keyGenerator.incrementAndGet() + "_" + object.getClass().getSimpleName();
        KeyedWeakReference ref = new KeyedWeakReference(key, object, description, referenceQueue);
        watchedReferences.put(key, ref);
        return key;
    }

    private void pruneClearedReferences() {
        Object ref;
        while ((ref = referenceQueue.poll()) != null) {
            if (ref instanceof KeyedWeakReference) {
                watchedReferences.remove(((KeyedWeakReference) ref).key);
            }
        }
    }

    public synchronized List<LeakCandidate> checkLeaks() {
        pruneClearedReferences();
        
        long now = System.currentTimeMillis();
        List<LeakCandidate> candidates = new ArrayList<LeakCandidate>();

        for (KeyedWeakReference ref : watchedReferences.values()) {
            if (ref.get() != null) {
                long duration = now - ref.watchedTime;
                // If retained for more than 5 seconds after watch
                if (duration > 5000L) {
                    ref.retainedGcPasses++;
                    LeakCandidate candidate = new LeakCandidate(ref.key, ref.className, ref.description, ref.watchedTime);
                    candidate.retainedGcPasses = ref.retainedGcPasses;
                    candidates.add(candidate);
                }
            }
        }

        return candidates;
    }

    public int getActiveLeakCandidateCount() {
        pruneClearedReferences();
        int count = 0;
        long now = System.currentTimeMillis();
        for (KeyedWeakReference ref : watchedReferences.values()) {
            if (ref.get() != null && (now - ref.watchedTime) > 5000L) {
                count++;
            }
        }
        return count;
    }

    public void clear() {
        watchedReferences.clear();
        pruneClearedReferences();
    }
}
