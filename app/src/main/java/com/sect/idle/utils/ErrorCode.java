package com.sect.idle.utils;

/**
 * Standardized Error Codes for Sect Idle Error Handling, APM Telemetry & Diagnostics.
 *
 * Categorized by subsystem:
 * - SYS: System, Threading, Garbage Collection, Heap & Fatal Errors (1000-1999)
 * - SAVE: Persistence, Serialization, Checksum & File I/O (2000-2999)
 * - COMBAT: Combat AI, Formation, Calculations & Game Loop (3000-3999)
 * - SECT: Sect Management, Disciples, Economy & Resources (4000-4999)
 * - AUDIO: PCM Mixer, Synthesizer & Sound FX (5000-5999)
 * - SEC: Security, Anti-Tamper, Cryptography & Validation (6000-6999)
 * - UI: SurfaceView, Canvas Rendering, Bitmaps & Dialogs (7000-7999)
 *
 * 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public enum ErrorCode {

    // --- SYSTEM & RUNTIME (1000-1999) ---
    SYS_UNKNOWN(1000, "Unknown system error", "SYS", ExceptionManager.LEVEL_ERROR, "Inspect stack trace and telemetry."),
    SYS_NPE_GUARD(1001, "Null pointer intercepted by defensive guard", "SYS", ExceptionManager.LEVEL_WARN, "Used safe fallback value or default instance."),
    SYS_OOM_DISTRESS(1002, "OutOfMemoryError or critical heap pressure", "SYS", ExceptionManager.LEVEL_CRITICAL, "Purge bitmap caches and drop particle fidelity."),
    SYS_GC_FREEZE(1003, "Garbage collection execution freeze detected", "SYS", ExceptionManager.LEVEL_WARN, "Enforce zero-allocation object pooling in render loops."),
    SYS_FATAL_CRASH(1004, "Uncaught fatal exception intercepted", "SYS", ExceptionManager.LEVEL_FATAL, "Persisted emergency crash dump and executed graceful shutdown."),
    SYS_THREAD_INTERRUPT(1005, "Background worker thread interrupted", "SYS", ExceptionManager.LEVEL_WARN, "Worker gracefully exited or reset interrupt flag."),
    SYS_RESOURCE_LEAK(1006, "Unclosed file descriptor or un-recycled bitmap", "SYS", ExceptionManager.LEVEL_WARN, "Enforce deterministic close and recycle callbacks."),

    // --- PERSISTENCE & DATA (2000-2999) ---
    SAVE_CORRUPTION(2001, "Save data corrupted or invalid payload structure", "SAVE", ExceptionManager.LEVEL_ERROR, "Attempt rollback to backup save slot."),
    SAVE_CHECKSUM_MISMATCH(2002, "Cryptographic checksum mismatch on save payload", "SAVE", ExceptionManager.LEVEL_ERROR, "Verify anti-tamper signature or restore backup."),
    SAVE_IO_ERROR(2003, "File I/O failure during save or load", "SAVE", ExceptionManager.LEVEL_ERROR, "Retry write with atomic temporary file swap."),
    SAVE_ENCODING_ERROR(2004, "Failed to encode/decode save state", "SAVE", ExceptionManager.LEVEL_ERROR, "Fallback to default sect template."),

    // --- COMBAT & GAME ENGINE (3000-3999) ---
    COMBAT_AI_INVALID_STATE(3001, "Cultivator combat AI transitioned to invalid state", "COMBAT", ExceptionManager.LEVEL_WARN, "Reset unit to idle/ready state."),
    COMBAT_RENDER_OVERFLOW(3002, "Exceeded maximum active VFX particles or flying swords", "COMBAT", ExceptionManager.LEVEL_INFO, "Recycle oldest particle via MemoryPool."),
    COMBAT_CALCULATION_NAN(3003, "Combat calculation yielded NaN or Infinity", "COMBAT", ExceptionManager.LEVEL_WARN, "Clamped attribute value to safe bound."),
    COMBAT_TARGET_NOT_FOUND(3004, "Target combatant is null or already dead", "COMBAT", ExceptionManager.LEVEL_INFO, "Re-evaluated target selection tree."),

    // --- SECT & ECONOMY (4000-4999) ---
    SECT_RESOURCE_UNDERFLOW(4001, "Attempted resource deduction exceeds current balance", "SECT", ExceptionManager.LEVEL_WARN, "Clamped balance to 0 and rejected transaction."),
    SECT_DISCIPLE_CAP_REACHED(4002, "Disciple recruitment exceeded sect housing capacity", "SECT", ExceptionManager.LEVEL_INFO, "Prompt user to upgrade sect dwellings."),
    SECT_BUILDING_UPGRADE_INVALID(4003, "Building upgrade prereqs not satisfied", "SECT", ExceptionManager.LEVEL_INFO, "Rejected invalid upgrade attempt."),

    // --- AUDIO & SYNTHESIZER (5000-5999) ---
    AUDIO_PCM_BUFFER_OVERRUN(5001, "PCM audio mixer buffer overflow or underrun", "AUDIO", ExceptionManager.LEVEL_WARN, "Flushed audio track and reset mixer clock."),
    AUDIO_SYNTH_UNDERFLOW(5002, "Procedural audio synthesis buffer allocation error", "AUDIO", ExceptionManager.LEVEL_WARN, "Muted sound voice and restored pool."),
    AUDIO_RESOURCE_DISPOSED(5003, "Attempted playback on disposed audio track", "AUDIO", ExceptionManager.LEVEL_INFO, "Re-initialized SectPcmMixer."),

    // --- SECURITY & VALIDATION (6000-6999) ---
    SEC_TAMPER_DETECTED(6001, "Memory or save file tampering detected", "SEC", ExceptionManager.LEVEL_WARN, "Quarantined modified fields and logged security event."),
    SEC_INVALID_SIGNATURE(6002, "HMAC-SHA256 signature mismatch", "SEC", ExceptionManager.LEVEL_ERROR, "Rejected untrusted game payload."),
    SEC_INPUT_MALFORMED(6003, "User input contained invalid or malicious characters", "SEC", ExceptionManager.LEVEL_INFO, "Sanitized input string via DataValidator."),
    SEC_RATE_LIMIT_EXCEEDED(6004, "Action frequency exceeded safe threshold", "SEC", ExceptionManager.LEVEL_INFO, "Throttled rapid user interactions."),

    // --- UI & RENDERING (7000-7999) ---
    UI_BAD_TOKEN_GUARD(7001, "Activity finished before dialog dismissed", "UI", ExceptionManager.LEVEL_WARN, "Safely intercepted BadTokenException."),
    UI_SURFACE_DESTROYED(7002, "Canvas render attempt on destroyed SurfaceHolder", "UI", ExceptionManager.LEVEL_INFO, "Yielded frame render loop."),
    UI_BITMAP_RECYCLED_ACCESS(7003, "Attempted draw of recycled bitmap", "UI", ExceptionManager.LEVEL_WARN, "Re-baked texture procedurally."),
    UI_EVENT_QUEUE_OVERFLOW(7004, "UI touch event queue capacity exceeded", "UI", ExceptionManager.LEVEL_INFO, "Dropped dropped stale touch events.");

    private final int code;
    private final String description;
    private final String subsystem;
    private final int defaultSeverity;
    private final String recoverySuggestion;

    ErrorCode(int code, String description, String subsystem, int defaultSeverity, String recoverySuggestion) {
        this.code = code;
        this.description = description;
        this.subsystem = subsystem;
        this.defaultSeverity = defaultSeverity;
        this.recoverySuggestion = recoverySuggestion;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getSubsystem() {
        return subsystem;
    }

    public int getDefaultSeverity() {
        return defaultSeverity;
    }

    public String getRecoverySuggestion() {
        return recoverySuggestion;
    }

    /**
     * Resolves ErrorCode by integer code, or returns SYS_UNKNOWN if not found.
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode ec : values()) {
            if (ec.code == code) {
                return ec;
            }
        }
        return SYS_UNKNOWN;
    }

    @Override
    public String toString() {
        return "[" + subsystem + "-" + code + "] " + name() + ": " + description;
    }
}
