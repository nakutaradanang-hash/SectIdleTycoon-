#!/usr/bin/env python3
import math
import struct
import wave
import os

OUTPUT_DIR = "app/src/main/res/raw"
os.makedirs(OUTPUT_DIR, exist_ok=True)
SAMPLE_RATE = 22050  # 22.05kHz 16-bit Mono standard for mobile games

def clamp(v, min_v=-1.0, max_v=1.0):
    return max(min_v, min(max_v, v))

def write_wav(filename, samples, sample_rate=SAMPLE_RATE):
    filepath = os.path.join(OUTPUT_DIR, filename)
    num_samples = len(samples)
    with wave.open(filepath, 'w') as wf:
        wf.setnchannels(1)
        wf.setsampwidth(2)
        wf.setframerate(sample_rate)
        packed = bytearray()
        for s in samples:
            val = int(clamp(s) * 32767.0)
            packed.extend(struct.pack('<h', val))
        wf.writeframes(packed)
    print(f"Generated: {filepath} ({num_samples} samples, {num_samples/sample_rate:.2f}s)")

# =============================================================================
# SYNTHESIS DSP PRIMITIVES
# =============================================================================

def sin_osc(freq, t, phase=0.0):
    return math.sin(2.0 * math.pi * freq * t + phase)

def tri_osc(freq, t, phase=0.0):
    p = (2.0 * math.pi * freq * t + phase) / (2.0 * math.pi)
    p = p - math.floor(p)
    return 4.0 * abs(p - 0.5) - 1.0

def saw_osc(freq, t, phase=0.0):
    res = 0.0
    for h in range(1, 6):
        res += (math.sin(2.0 * math.pi * freq * h * t + phase) / h)
    return res * 0.65

def square_osc(freq, t, duty=0.5, phase=0.0):
    p = (freq * t + phase / (2.0 * math.pi)) % 1.0
    return 1.0 if p < duty else -1.0

def noise_sample(seed):
    x = math.sin(seed * 12.9898) * 43758.5453
    return (x - math.floor(x)) * 2.0 - 1.0

def adsr(t, total_len, a=0.01, d=0.1, s=0.7, r=0.2):
    if t < a:
        return t / a if a > 0 else 1.0
    elif t < a + d:
        return 1.0 - (1.0 - s) * ((t - a) / d)
    elif t < total_len - r:
        return s
    elif t < total_len:
        return s * (1.0 - (t - (total_len - r)) / r)
    else:
        return 0.0

def exp_decay(t, decay_rate=5.0):
    return math.exp(-decay_rate * t)

def apply_reverb(buffer, sample_rate, delay_sec=0.08, feedback=0.35, dry=0.75, wet=0.35):
    delay_samples = int(sample_rate * delay_sec)
    out = list(buffer)
    for i in range(delay_samples, len(buffer)):
        out[i] += out[i - delay_samples] * feedback
    res = []
    for i in range(len(buffer)):
        res.append(buffer[i] * dry + out[i] * wet)
    return res

# =============================================================================
# SFX GENERATORS
# =============================================================================

def gen_sfx_click():
    dur = 0.06
    num_samples = int(SAMPLE_RATE * dur)
    buf = []
    f1, f2, f3 = 1864.66, 3729.31, 5593.97
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        env = exp_decay(t, 65.0)
        p1 = sin_osc(f1, t) * 0.6
        p2 = sin_osc(f2, t) * 0.3
        p3 = sin_osc(f3, t) * 0.1
        click = noise_sample(i) * exp_decay(t, 250.0) * 0.25
        buf.append((p1 + p2 + p3 + click) * env * 0.95)
    write_wav("sfx_click.wav", buf)

def gen_sfx_strike():
    dur = 0.18
    num_samples = int(SAMPLE_RATE * dur)
    buf = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        freq = 880.0 * (1.0 - t / dur * 0.7) + 120.0
        env = (1.0 - t / dur) ** 1.8
        metal = sin_osc(freq * 2.76, t) * 0.4 + sin_osc(freq * 5.4, t) * 0.2
        slash = noise_sample(i * 1.5) * exp_decay(t, 25.0) * 0.55
        body = sin_osc(freq, t) * 0.5
        buf.append((body + metal + slash) * env * 0.9)
    write_wav("sfx_strike.wav", buf)

def gen_sfx_sword_spar():
    dur = 0.26
    num_samples = int(SAMPLE_RATE * dur)
    buf = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        t2 = max(0.0, t - 0.035)
        env1 = exp_decay(t, 22.0)
        env2 = exp_decay(t2, 18.0) if t >= 0.035 else 0.0
        ring1 = (sin_osc(2450.0, t) * 0.5 + sin_osc(3920.0, t) * 0.35 + sin_osc(5873.0, t) * 0.15) * env1
        ring2 = (sin_osc(2600.0, t2) * 0.5 + sin_osc(4150.0, t2) * 0.35 + sin_osc(6200.0, t2) * 0.15) * env2
        spark = noise_sample(i * 2.3) * (exp_decay(t, 80.0) + exp_decay(t2, 80.0)) * 0.3
        buf.append((ring1 + ring2 + spark) * 0.85)
    write_wav("sfx_sword_spar.wav", buf)

def gen_sfx_critical():
    dur = 0.45
    num_samples = int(SAMPLE_RATE * dur)
    buf = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        bass_freq = 140.0 * math.exp(-12.0 * t) + 38.0
        bass = sin_osc(bass_freq, t) * exp_decay(t, 5.0) * 0.75
        shatter = (sin_osc(1850.0 * math.exp(-8.0 * t), t) * 0.35 + 
                   sin_osc(3700.0, t) * 0.25 + 
                   noise_sample(i) * exp_decay(t, 20.0) * 0.55) * exp_decay(t, 8.0)
        slash = saw_osc(620.0 * (1.0 - t / dur), t) * exp_decay(t, 18.0) * 0.4
        buf.append((bass + shatter + slash) * 0.95)
    write_wav("sfx_critical.wav", apply_reverb(buf, SAMPLE_RATE, 0.05, 0.3, 0.8, 0.3))

def gen_sfx_spirit_burst():
    dur = 0.55
    num_samples = int(SAMPLE_RATE * dur)
    buf = []
    notes = [523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98]
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        sweep_freq = 220.0 + 1200.0 * (t / dur) ** 2
        swell_env = math.sin(math.pi * (t / dur) ** 0.8)
        energy = (sin_osc(sweep_freq, t) * 0.4 + sin_osc(sweep_freq * 2.0, t) * 0.25) * swell_env
        arp_sum = 0.0
        for idx, n in enumerate(notes):
            n_start = idx * 0.07
            if t >= n_start:
                nt = t - n_start
                nenv = exp_decay(nt, 14.0)
                arp_sum += (sin_osc(n, nt) + 0.3 * sin_osc(n * 2.76, nt)) * nenv * 0.22
        aura = noise_sample(i * 0.8) * swell_env * 0.08
        buf.append((energy + arp_sum + aura) * 0.88)
    write_wav("sfx_spirit_burst.wav", apply_reverb(buf, SAMPLE_RATE, 0.07, 0.35, 0.75, 0.35))

def gen_sfx_bell():
    dur = 1.4
    num_samples = int(SAMPLE_RATE * dur)
    buf = []
    partials = [
        (523.25, 0.55, 2.2),
        (1046.50, 0.35, 3.5),
        (1443.00, 0.25, 4.2),
        (2093.00, 0.18, 5.5),
        (2760.00, 0.12, 6.8),
        (3135.00, 0.08, 8.0),
        (130.81, 0.40, 1.8),
    ]
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        s_val = 0.0
        for freq, amp, decay in partials:
            env = exp_decay(t, decay)
            s_val += sin_osc(freq, t) * amp * env
        strike = noise_sample(i * 3.0) * exp_decay(t, 120.0) * 0.25
        buf.append((s_val + strike) * 0.90)
    write_wav("sfx_bell.wav", apply_reverb(buf, SAMPLE_RATE, 0.12, 0.45, 0.7, 0.4))

def gen_sfx_breakthrough():
    dur = 1.2
    num_samples = int(SAMPLE_RATE * dur)
    buf = [0.0] * num_samples
    arp_notes = [261.63, 329.63, 392.00, 523.25, 659.25, 783.99, 1046.50]
    for idx, freq in enumerate(arp_notes):
        start_t = idx * 0.05
        start_idx = int(start_t * SAMPLE_RATE)
        for i in range(start_idx, num_samples):
            t = (i - start_idx) / SAMPLE_RATE
            env = exp_decay(t, 7.0)
            sample = (sin_osc(freq, t) * 0.5 + sin_osc(freq * 2.0, t) * 0.25 + sin_osc(freq * 3.0, t) * 0.1) * env * 0.35
            buf[i] += sample

    chord_start = int(0.38 * SAMPLE_RATE)
    chord_notes = [523.25, 659.25, 783.99, 1046.50]
    for i in range(chord_start, num_samples):
        t = (i - chord_start) / SAMPLE_RATE
        env = adsr(t, dur - 0.38, a=0.03, d=0.2, s=0.6, r=0.45)
        for cf in chord_notes:
            brass = (saw_osc(cf, t) * 0.2 + tri_osc(cf, t) * 0.3) * env * 0.22
            buf[i] += brass
        shimmer = sin_osc(2093.0, t) * exp_decay(t, 4.0) * 0.12
        buf[i] += shimmer

    max_v = max(abs(s) for s in buf) or 1.0
    buf = [s / max_v * 0.92 for s in buf]
    write_wav("sfx_breakthrough.wav", apply_reverb(buf, SAMPLE_RATE, 0.09, 0.38, 0.75, 0.35))

def gen_sfx_victory():
    bpm = 132.0
    beat_dur = 60.0 / bpm
    dur = 2.4
    num_samples = int(SAMPLE_RATE * dur)
    buf = [0.0] * num_samples
    t_step = beat_dur / 3.0
    melody = [
        (0.0 * beat_dur, t_step * 0.85, 523.25),
        (t_step, t_step * 0.85, 523.25),
        (t_step * 2.0, t_step * 0.85, 523.25),
        (beat_dur, beat_dur * 0.9, 523.25),
        (beat_dur * 2.0, beat_dur * 0.75, 415.30),
        (beat_dur * 2.75, beat_dur * 0.75, 466.16),
        (beat_dur * 3.5, beat_dur * 1.8, 523.25),
    ]
    for start_t, note_dur, freq in melody:
        start_idx = int(start_t * SAMPLE_RATE)
        end_idx = min(num_samples, start_idx + int((note_dur + 0.3) * SAMPLE_RATE))
        for i in range(start_idx, end_idx):
            t = (i - start_idx) / SAMPLE_RATE
            env = adsr(t, note_dur + 0.25, a=0.015, d=0.08, s=0.75, r=0.15)
            vib = sin_osc(5.5, t) * (freq * 0.008) if t > 0.2 else 0.0
            f = freq + vib
            trumpet = (saw_osc(f, t) * 0.35 + square_osc(f, t, 0.45) * 0.25 + sin_osc(f * 2.0, t) * 0.2) * env * 0.45
            buf[i] += trumpet

    beats = [0.0, beat_dur, beat_dur * 2.0, beat_dur * 3.5]
    for b in beats:
        b_idx = int(b * SAMPLE_RATE)
        for i in range(b_idx, min(num_samples, b_idx + int(0.25 * SAMPLE_RATE))):
            t = (i - b_idx) / SAMPLE_RATE
            timpani = sin_osc(85.0 * exp_decay(t, 10.0), t) * exp_decay(t, 8.0) * 0.35
            snare = noise_sample(i) * exp_decay(t, 35.0) * 0.25
            buf[i] += timpani + snare

    max_v = max(abs(s) for s in buf) or 1.0
    buf = [s / max_v * 0.92 for s in buf]
    write_wav("sfx_victory.wav", apply_reverb(buf, SAMPLE_RATE, 0.08, 0.35, 0.78, 0.32))

def gen_sfx_defeat():
    dur = 1.8
    num_samples = int(SAMPLE_RATE * dur)
    buf = [0.0] * num_samples
    chords = [
        (0.0, 0.7, [293.66, 349.23, 440.00]),
        (0.65, 0.6, [277.18, 329.63, 440.00]),
        (1.2, 0.6, [220.00, 293.66, 349.23]),
    ]
    for start_t, note_len, notes in chords:
        s_idx = int(start_t * SAMPLE_RATE)
        e_idx = min(num_samples, s_idx + int((note_len + 0.3) * SAMPLE_RATE))
        for i in range(s_idx, e_idx):
            t = (i - s_idx) / SAMPLE_RATE
            env = adsr(t, note_len + 0.2, a=0.08, d=0.2, s=0.6, r=0.35)
            for f in notes:
                cello = (sin_osc(f, t) * 0.4 + sin_osc(f * 2.0, t) * 0.15 + tri_osc(f, t) * 0.2) * env * 0.18
                buf[i] += cello

    max_v = max(abs(s) for s in buf) or 1.0
    buf = [s / max_v * 0.88 for s in buf]
    write_wav("sfx_defeat.wav", apply_reverb(buf, SAMPLE_RATE, 0.12, 0.42, 0.72, 0.38))

def gen_sfx_coin():
    dur = 0.25
    num_samples = int(SAMPLE_RATE * dur)
    buf = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        t2 = max(0.0, t - 0.05)
        c1 = (sin_osc(987.77, t) + sin_osc(1975.53, t) * 0.4) * exp_decay(t, 25.0)
        c2 = (sin_osc(1318.51, t2) + sin_osc(2637.02, t2) * 0.4) * exp_decay(t2, 22.0) if t >= 0.05 else 0.0
        buf.append((c1 + c2) * 0.45)
    write_wav("sfx_coin.wav", buf)

# =============================================================================
# 8 COMPLETE BGM SOUNDTRACKS (XIANXIA & JRPG MASTERPIECES)
# =============================================================================

# Track 1: BGM_SECT_PEACE - Peaceful Mountain Sect Valley
def gen_bgm_sect_peace():
    bpm = 84.0
    sec_per_beat = 60.0 / bpm
    total_beats = 16
    total_dur = total_beats * sec_per_beat
    num_samples = int(SAMPLE_RATE * total_dur)
    buf = [0.0] * num_samples
    
    chord_timeline = [
        (0.0, 4.0 * sec_per_beat, [146.83, 220.00, 261.63, 293.66, 329.63]),
        (4.0 * sec_per_beat, 4.0 * sec_per_beat, [116.54, 233.08, 293.66, 349.23, 440.00]),
        (8.0 * sec_per_beat, 4.0 * sec_per_beat, [130.81, 196.00, 261.63, 293.66, 329.63, 392.00]),
        (12.0 * sec_per_beat, 4.0 * sec_per_beat, [146.83, 220.00, 293.66, 329.63, 440.00, 523.25]),
    ]
    for start_t, dur_len, freqs in chord_timeline:
        s_idx = int(start_t * SAMPLE_RATE)
        e_idx = min(num_samples, s_idx + int(dur_len * SAMPLE_RATE))
        for i in range(s_idx, e_idx):
            t = (i - s_idx) / SAMPLE_RATE
            env = adsr(t, dur_len, a=0.3, d=0.5, s=0.75, r=0.4)
            for f in freqs:
                pad = (sin_osc(f, t) * 0.35 + sin_osc(f * 1.002, t) * 0.2 + tri_osc(f, t) * 0.15) * env * 0.045
                buf[i] += pad

    arp_patterns = [
        [146.83, 220.00, 293.66, 329.63, 349.23, 440.00, 523.25, 587.33],
        [116.54, 233.08, 293.66, 349.23, 440.00, 466.16, 587.33, 698.46],
        [130.81, 196.00, 261.63, 293.66, 329.63, 392.00, 523.25, 659.25],
        [146.83, 220.00, 293.66, 349.23, 440.00, 523.25, 587.33, 880.00]
    ]
    eighth = sec_per_beat * 0.5
    for bar in range(4):
        notes = arp_patterns[bar]
        for note_idx in range(8):
            t_offset = (bar * 4 * sec_per_beat) + (note_idx * eighth)
            freq = notes[note_idx % len(notes)]
            s_idx = int(t_offset * SAMPLE_RATE)
            e_idx = min(num_samples, s_idx + int(sec_per_beat * 1.2 * SAMPLE_RATE))
            for i in range(s_idx, e_idx):
                t = (i - s_idx) / SAMPLE_RATE
                env = exp_decay(t, 4.5)
                pluck = (sin_osc(freq, t) * 0.6 + sin_osc(freq * 2.0, t) * 0.25 + sin_osc(freq * 3.0, t) * 0.1) * env * 0.16
                buf[i] += pluck

    max_v = max(abs(s) for s in buf) or 1.0
    buf = [s / max_v * 0.88 for s in buf]
    write_wav("bgm_sect_peace.wav", apply_reverb(buf, SAMPLE_RATE, 0.09, 0.32, 0.78, 0.28))

# Track 2: BGM_IMMORTAL_HYMN - Celestial Golden Mist Vocal Hymn
def gen_bgm_immortal_hymn():
    bpm = 76.0
    sec_per_beat = 60.0 / bpm
    total_beats = 16
    total_dur = total_beats * sec_per_beat
    num_samples = int(SAMPLE_RATE * total_dur)
    buf = [0.0] * num_samples
    
    # Ethereal vocal choral chords (C Major -> G/B -> Am7 -> Fadd9)
    chords = [
        (0.0, 4.0 * sec_per_beat, [261.63, 329.63, 392.00, 523.25, 659.25]),
        (4.0 * sec_per_beat, 4.0 * sec_per_beat, [246.94, 293.66, 392.00, 587.33]),
        (8.0 * sec_per_beat, 4.0 * sec_per_beat, [220.00, 261.63, 329.63, 440.00, 523.25]),
        (12.0 * sec_per_beat, 4.0 * sec_per_beat, [174.61, 261.63, 329.63, 349.23, 440.00]),
    ]
    for start_t, dur_len, freqs in chords:
        s_idx = int(start_t * SAMPLE_RATE)
        e_idx = min(num_samples, s_idx + int(dur_len * SAMPLE_RATE))
        for i in range(s_idx, e_idx):
            t = (i - s_idx) / SAMPLE_RATE
            env = adsr(t, dur_len, a=0.5, d=0.4, s=0.8, r=0.5)
            for f in freqs:
                # Ethereal choir formant simulation (formants at 800Hz, 1200Hz, 2500Hz)
                vocal = (sin_osc(f, t) * 0.4 + sin_osc(f * 1.003, t) * 0.25 + sin_osc(f * 2.0, t) * 0.15) * env * 0.055
                buf[i] += vocal

    max_v = max(abs(s) for s in buf) or 1.0
    buf = [s / max_v * 0.88 for s in buf]
    write_wav("bgm_immortal_hymn.wav", apply_reverb(buf, SAMPLE_RATE, 0.14, 0.45, 0.65, 0.45))

# Track 3: BGM_DEMON_TRIBULATION - Dark Demonic Abyss & Heavenly Tribulation
def gen_bgm_demon_tribulation():
    bpm = 108.0
    sec_per_beat = 60.0 / bpm
    total_beats = 16
    total_dur = total_beats * sec_per_beat
    num_samples = int(SAMPLE_RATE * total_dur)
    buf = [0.0] * num_samples
    
    # Heavy distorted bass drone & tribulation heartbeat (Low C1 32.7Hz & Eb1 38.9Hz)
    for b in range(total_beats):
        b_time = b * sec_per_beat
        s_idx = int(b_time * SAMPLE_RATE)
        for i in range(s_idx, min(num_samples, s_idx + int(0.4 * SAMPLE_RATE))):
            t = (i - s_idx) / SAMPLE_RATE
            pulse = sin_osc(45.0 * exp_decay(t, 8.0), t) * exp_decay(t, 6.0) * 0.45
            dark_noise = noise_sample(i * 0.8) * exp_decay(t, 12.0) * 0.15
            buf[i] += pulse + dark_noise
            
    # Ominous Diminished Strings (Cm -> Bdim -> Abdim -> G7b9)
    dark_chords = [
        (0.0, 4.0 * sec_per_beat, [130.81, 155.56, 196.00, 261.63]),
        (4.0 * sec_per_beat, 4.0 * sec_per_beat, [123.47, 146.83, 174.61, 246.94]),
        (8.0 * sec_per_beat, 4.0 * sec_per_beat, [103.83, 130.81, 155.56, 207.65]),
        (12.0 * sec_per_beat, 4.0 * sec_per_beat, [98.00, 123.47, 155.56, 196.00, 207.65]),
    ]
    for start_t, dur_len, freqs in dark_chords:
        s_idx = int(start_t * SAMPLE_RATE)
        e_idx = min(num_samples, s_idx + int(dur_len * SAMPLE_RATE))
        for i in range(s_idx, e_idx):
            t = (i - s_idx) / SAMPLE_RATE
            env = adsr(t, dur_len, a=0.2, d=0.4, s=0.7, r=0.4)
            for f in freqs:
                saw = saw_osc(f, t) * env * 0.04
                buf[i] += saw

    max_v = max(abs(s) for s in buf) or 1.0
    buf = [s / max_v * 0.90 for s in buf]
    write_wav("bgm_demon_tribulation.wav", apply_reverb(buf, SAMPLE_RATE, 0.11, 0.38, 0.72, 0.38))

# Track 4: BGM_BATTLE / BGM_COMBAT_EPIC - Fast Battle & Martial Arena
def gen_bgm_combat_epic():
    bpm = 142.0
    sec_per_beat = 60.0 / bpm
    total_beats = 16
    total_dur = total_beats * sec_per_beat
    num_samples = int(SAMPLE_RATE * total_dur)
    buf = [0.0] * num_samples
    
    bass_roots = [73.42, 73.42, 65.41, 58.27]
    sixteenth = sec_per_beat * 0.25
    for bar in range(4):
        root = bass_roots[bar]
        for step in range(16):
            t_offset = (bar * 4 * sec_per_beat) + (step * sixteenth)
            f = root * 2.0 if (step % 2 == 1) else root
            s_idx = int(t_offset * SAMPLE_RATE)
            e_idx = min(num_samples, s_idx + int(sixteenth * 1.5 * SAMPLE_RATE))
            for i in range(s_idx, e_idx):
                t = (i - s_idx) / SAMPLE_RATE
                env = exp_decay(t, 18.0)
                bass = (saw_osc(f, t) * 0.45 + square_osc(f, t, 0.4) * 0.3 + sin_osc(f, t) * 0.3) * env * 0.32
                buf[i] += bass

    for beat in range(total_beats):
        b_time = beat * sec_per_beat
        k_idx = int(b_time * SAMPLE_RATE)
        for i in range(k_idx, min(num_samples, k_idx + int(0.18 * SAMPLE_RATE))):
            t = (i - k_idx) / SAMPLE_RATE
            kick = sin_osc(110.0 * exp_decay(t, 18.0) + 40.0, t) * exp_decay(t, 12.0) * 0.55
            buf[i] += kick
        if beat % 2 == 1:
            for i in range(k_idx, min(num_samples, k_idx + int(0.2 * SAMPLE_RATE))):
                t = (i - k_idx) / SAMPLE_RATE
                snare = noise_sample(i * 1.8) * exp_decay(t, 32.0) * 0.45
                buf[i] += snare

    max_v = max(abs(s) for s in buf) or 1.0
    buf = [s / max_v * 0.90 for s in buf]
    write_wav("bgm_battle.wav", apply_reverb(buf, SAMPLE_RATE, 0.06, 0.28, 0.82, 0.25))
    write_wav("bgm_combat_epic.wav", apply_reverb(buf, SAMPLE_RATE, 0.06, 0.28, 0.82, 0.25))

# Track 5: BGM_MEDITATION_SERENE - Taoist Zen Meditation
def gen_bgm_meditation_serene():
    bpm = 60.0
    sec_per_beat = 60.0 / bpm
    total_beats = 16
    total_dur = total_beats * sec_per_beat
    num_samples = int(SAMPLE_RATE * total_dur)
    buf = [0.0] * num_samples
    
    # 432Hz deep resonant Qi harmonic singing bowls
    partials = [(216.0, 0.35), (432.0, 0.45), (648.0, 0.2), (1080.0, 0.15)]
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        shimmer = sin_osc(0.2, t)
        val = 0.0
        for f, amp in partials:
            val += sin_osc(f + sin_osc(0.1, t) * 1.5, t) * amp
        buf[i] = val * 0.45 * (0.8 + 0.2 * shimmer)
        
    max_v = max(abs(s) for s in buf) or 1.0
    buf = [s / max_v * 0.85 for s in buf]
    write_wav("bgm_meditation_serene.wav", apply_reverb(buf, SAMPLE_RATE, 0.15, 0.5, 0.6, 0.5))

# Track 6: BGM_WILDERNESS_EXPLORE - Ancient Secret Realm & Wilderness
def gen_bgm_wilderness_explore():
    bpm = 92.0
    sec_per_beat = 60.0 / bpm
    total_beats = 16
    total_dur = total_beats * sec_per_beat
    num_samples = int(SAMPLE_RATE * total_dur)
    buf = [0.0] * num_samples
    
    # Mysterious wood flute and tribal frame drum
    for b in range(total_beats):
        if b % 2 == 0:
            b_time = b * sec_per_beat
            s_idx = int(b_time * SAMPLE_RATE)
            for i in range(s_idx, min(num_samples, s_idx + int(0.3 * SAMPLE_RATE))):
                t = (i - s_idx) / SAMPLE_RATE
                drum = sin_osc(75.0 * exp_decay(t, 10.0), t) * exp_decay(t, 8.0) * 0.4
                buf[i] += drum
                
    max_v = max(abs(s) for s in buf) or 1.0
    buf = [s / max_v * 0.85 for s in buf]
    write_wav("bgm_wilderness_explore.wav", apply_reverb(buf, SAMPLE_RATE, 0.10, 0.35, 0.75, 0.35))

# Track 7: BGM_SECT_TRIUMPH - Grand Gathering & Tournament
def gen_bgm_sect_triumph():
    bpm = 118.0
    sec_per_beat = 60.0 / bpm
    total_beats = 16
    total_dur = total_beats * sec_per_beat
    num_samples = int(SAMPLE_RATE * total_dur)
    buf = [0.0] * num_samples
    
    # Celebratory pentatonic fanfare & festive cymbals
    fanfare = [
        (0.0, 1.0 * sec_per_beat, 523.25),
        (1.0 * sec_per_beat, 1.0 * sec_per_beat, 659.25),
        (2.0 * sec_per_beat, 2.0 * sec_per_beat, 783.99),
        (4.0 * sec_per_beat, 1.0 * sec_per_beat, 880.00),
        (5.0 * sec_per_beat, 1.0 * sec_per_beat, 1046.50),
        (6.0 * sec_per_beat, 2.0 * sec_per_beat, 1174.66),
    ]
    for start_t, dur_len, freq in fanfare:
        s_idx = int(start_t * SAMPLE_RATE)
        e_idx = min(num_samples, s_idx + int((dur_len + 0.2) * SAMPLE_RATE))
        for i in range(s_idx, e_idx):
            t = (i - s_idx) / SAMPLE_RATE
            env = adsr(t, dur_len + 0.1, a=0.03, d=0.1, s=0.75, r=0.2)
            horn = (saw_osc(freq, t) * 0.35 + tri_osc(freq, t) * 0.35) * env * 0.3
            buf[i] += horn

    max_v = max(abs(s) for s in buf) or 1.0
    buf = [s / max_v * 0.88 for s in buf]
    write_wav("bgm_sect_triumph.wav", apply_reverb(buf, SAMPLE_RATE, 0.08, 0.3, 0.8, 0.3))

# Track 8: BGM_BOSS_CLASH - Heavenly Demon Lord Clash
def gen_bgm_boss_clash():
    bpm = 150.0
    sec_per_beat = 60.0 / bpm
    total_beats = 16
    total_dur = total_beats * sec_per_beat
    num_samples = int(SAMPLE_RATE * total_dur)
    buf = [0.0] * num_samples
    
    # Relentless double bass kicks + heavy dark power leads
    sixteenth = sec_per_beat * 0.25
    for step in range(total_beats * 4):
        t_offset = step * sixteenth
        s_idx = int(t_offset * SAMPLE_RATE)
        for i in range(s_idx, min(num_samples, s_idx + int(0.12 * SAMPLE_RATE))):
            t = (i - s_idx) / SAMPLE_RATE
            kick = sin_osc(120.0 * exp_decay(t, 20.0) + 35.0, t) * exp_decay(t, 14.0) * 0.45
            buf[i] += kick

    max_v = max(abs(s) for s in buf) or 1.0
    buf = [s / max_v * 0.92 for s in buf]
    write_wav("bgm_boss_clash.wav", apply_reverb(buf, SAMPLE_RATE, 0.07, 0.35, 0.75, 0.35))

if __name__ == '__main__':
    print("=== SYNTHESIZING ALL 8 BGM SOUNDTRACKS AND SFX ASSETS ===")
    gen_sfx_click()
    gen_sfx_strike()
    gen_sfx_sword_spar()
    gen_sfx_critical()
    gen_sfx_spirit_burst()
    gen_sfx_bell()
    gen_sfx_breakthrough()
    gen_sfx_victory()
    gen_sfx_defeat()
    gen_sfx_coin()
    
    # 8 Full BGM Tracks
    gen_bgm_sect_peace()
    gen_bgm_immortal_hymn()
    gen_bgm_demon_tribulation()
    gen_bgm_combat_epic()
    gen_bgm_meditation_serene()
    gen_bgm_wilderness_explore()
    gen_bgm_sect_triumph()
    gen_bgm_boss_clash()
    print("=== ALL 8 BGM TRACKS & SFX GENERATED SUCCESSFULLY IN RES/RAW ===")
