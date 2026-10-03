package com.ilker.rutin.core;

import java.util.Random;

/** Ses dosyası gerektirmeyen sentezleyici: başarı melodileri, efektler ve odak ambiyansı. */
public final class Synth {
    private Synth() {}

    public static final int RATE = 44100;
    public static final int TICK = 0, DING = 1, BLUP = 2, SUCCESS = 3, COMBO = 4, STREAK = 5, FANFARE = 6, CHEST = 7,
            LEVELUP = 8, WITHER = 9, BADGE = 10, UNDO = 11, N_SOFT = 12, N_PUSH = 13, N_CRIT = 14, N_MORNING = 15, POP = 16, COUNT = 17;

    static double freq(double midi) { return 440.0 * Math.pow(2, (midi - 69) / 12.0); }

    /** Tek bir nota: paket (tını) + zarf. */
    static void note(float[] buf, int startSample, double midi, double durSec, double amp, int pack) {
        double f = freq(midi);
        int n = (int) (durSec * RATE);
        double decay = pack == 1 ? 6 : pack == 2 ? 9 : pack == 3 ? 2.5 : 4.5;
        for (int i = 0; i < n && startSample + i < buf.length; i++) {
            if (startSample + i < 0) continue;
            double t = i / (double) RATE;
            double att = Math.min(1, t / 0.006);
            double env = att * Math.exp(-decay * t);
            double ph = 2 * Math.PI * f * t;
            double v;
            switch (pack) {
                case 1: // 8-bit kare dalga
                    v = (Math.sin(ph) >= 0 ? 0.55 : -0.55) + 0.15 * (Math.sin(2 * ph) >= 0 ? 1 : -1) * 0.3;
                    break;
                case 2: // marimba
                    v = Math.sin(ph) + 0.35 * Math.sin(4 * ph) * Math.exp(-30 * t) + 0.1 * Math.sin(10 * ph) * Math.exp(-60 * t);
                    break;
                case 3: // parlak orkestra (yumuşak testere + vibrato)
                    double vib = 1 + 0.004 * Math.sin(2 * Math.PI * 5.5 * t);
                    v = 0;
                    for (int h = 1; h <= 6; h++) v += Math.sin(ph * h * vib) / (h * 1.2);
                    v *= 0.6;
                    break;
                default: // zil
                    v = Math.sin(ph) + 0.4 * Math.sin(2 * ph) * Math.exp(-3 * t) + 0.18 * Math.sin(3 * ph) * Math.exp(-6 * t)
                            + 0.08 * Math.sin(5.4 * ph) * Math.exp(-12 * t);
            }
            buf[startSample + i] += (float) (v * env * amp);
        }
    }

    static void chord(float[] buf, int start, double dur, double amp, int pack, double... midis) {
        for (double m : midis) note(buf, start, m, dur, amp / Math.sqrt(midis.length), pack);
    }

    static int s(double sec) { return (int) (sec * RATE); }

    public static short[] render(int id, int pack) {
        float[] b;
        switch (id) {
            case TICK: {
                b = new float[s(0.05)];
                for (int i = 0; i < b.length; i++) { double t = i / (double) RATE; b[i] = (float) (Math.sin(2 * Math.PI * 1800 * t) * Math.exp(-120 * t) * 0.5); }
                break;
            }
            case POP: {
                b = new float[s(0.09)];
                for (int i = 0; i < b.length; i++) { double t = i / (double) RATE; double f = 500 + 2500 * t; b[i] = (float) (Math.sin(2 * Math.PI * f * t) * Math.exp(-45 * t) * 0.5); }
                break;
            }
            case DING: b = new float[s(0.6)]; note(b, 0, 88, 0.6, 0.5, pack); break;
            case BLUP: {
                b = new float[s(0.18)];
                double ph = 0;
                for (int i = 0; i < b.length; i++) {
                    double t = i / (double) RATE;
                    double f = 380 + 900 * Math.pow(t / 0.18, 0.6);
                    ph += 2 * Math.PI * f / RATE;
                    b[i] = (float) (Math.sin(ph) * Math.exp(-14 * t) * 0.6 * Math.min(1, t / 0.004));
                }
                break;
            }
            case SUCCESS: {
                b = new float[s(1.2)];
                double[] m = {72, 76, 79, 84};
                for (int i = 0; i < 4; i++) note(b, s(i * 0.1), m[i], i == 3 ? 1.0 : 0.4, 0.42, pack);
                chord(b, s(0.3), 0.8, 0.25, pack, 72, 76, 79);
                break;
            }
            case COMBO: {
                b = new float[s(0.6)];
                double[] m = {76, 79, 83, 88};
                for (int i = 0; i < 4; i++) note(b, s(i * 0.07), m[i], 0.35, 0.4, pack);
                break;
            }
            case STREAK: {
                b = new float[s(1.6)];
                Random r = new Random(3);
                for (int i = 0; i < s(0.5); i++) { double t = i / (double) RATE; b[i] += (float) ((r.nextDouble() * 2 - 1) * 0.18 * Math.sin(Math.PI * t / 0.5)); }
                double[] m = {67, 72, 76, 79};
                for (int i = 0; i < 4; i++) note(b, s(0.25 + i * 0.09), m[i], 0.5, 0.4, pack);
                chord(b, s(0.62), 0.95, 0.45, pack, 60, 67, 72, 76, 84);
                break;
            }
            case FANFARE: {
                b = new float[s(2.0)];
                double[][] seq = {{67, 0, 0.15}, {67, 0.16, 0.12}, {67, 0.3, 0.12}, {72, 0.44, 0.5}, {76, 0.8, 0.2}, {74, 0.98, 0.2}, {79, 1.15, 0.8}};
                for (double[] q : seq) note(b, s(q[1]), q[0], q[2] + 0.2, 0.42, pack == 0 ? 3 : pack);
                chord(b, s(1.15), 0.85, 0.3, pack, 60, 67, 72, 76);
                break;
            }
            case CHEST: {
                b = new float[s(1.6)];
                Random r = new Random(7);
                for (int i = 0; i < 12; i++) note(b, s(i * 0.045), 84 + r.nextInt(14), 0.3, 0.22, 2);
                chord(b, s(0.6), 1.0, 0.5, pack, 72, 79, 84, 88, 91);
                break;
            }
            case LEVELUP: {
                b = new float[s(1.8)];
                double[] m = {72, 76, 79, 84, 88};
                for (int i = 0; i < 5; i++) note(b, s(i * 0.08), m[i], 0.35, 0.38, pack);
                chord(b, s(0.45), 1.3, 0.45, pack, 72, 76, 79, 84, 91);
                break;
            }
            case WITHER: {
                b = new float[s(1.4)];
                double[] m = {67, 64, 60, 55};
                for (int i = 0; i < 4; i++) note(b, s(i * 0.25), m[i], 0.6, 0.3, 2);
                break;
            }
            case BADGE: {
                b = new float[s(1.5)];
                note(b, 0, 79, 0.4, 0.4, pack); note(b, s(0.12), 84, 0.4, 0.4, pack); note(b, s(0.24), 88, 1.1, 0.45, pack);
                Random r = new Random(11);
                for (int i = 0; i < 8; i++) note(b, s(0.35 + i * 0.07), 96 + r.nextInt(8), 0.25, 0.12, 0);
                break;
            }
            case UNDO: {
                b = new float[s(0.4)];
                note(b, 0, 72, 0.2, 0.35, pack); note(b, s(0.1), 67, 0.3, 0.35, pack);
                break;
            }
            case N_SOFT: {
                b = new float[s(1.1)];
                note(b, 0, 84, 0.8, 0.5, 0); note(b, s(0.18), 79, 0.9, 0.5, 0);
                break;
            }
            case N_PUSH: {
                b = new float[s(1.1)];
                note(b, 0, 79, 0.5, 0.5, 0); note(b, s(0.12), 84, 0.5, 0.5, 0); note(b, s(0.24), 88, 0.8, 0.55, 0);
                break;
            }
            case N_CRIT: {
                b = new float[s(1.6)];
                for (int k = 0; k < 2; k++) {
                    int o = s(k * 0.7);
                    note(b, o, 88, 0.2, 0.55, 1); note(b, o + s(0.14), 88, 0.2, 0.55, 1); note(b, o + s(0.28), 91, 0.35, 0.6, 1);
                }
                break;
            }
            case N_MORNING: {
                b = new float[s(1.6)];
                double[] m = {72, 76, 79, 84};
                for (int i = 0; i < 4; i++) note(b, s(i * 0.18), m[i], 1.0, 0.4, 2);
                break;
            }
            default: b = new float[10];
        }
        return toPcm(b);
    }

    static short[] toPcm(float[] b) {
        float peak = 0.0001f;
        for (float v : b) peak = Math.max(peak, Math.abs(v));
        float g = peak > 0.95f ? 0.95f / peak : 1f;
        short[] out = new short[b.length];
        int fade = Math.min(400, b.length / 4);
        for (int i = 0; i < b.length; i++) {
            float v = b[i] * g;
            if (i > b.length - fade) v *= (b.length - i) / (float) fade;
            out[i] = (short) Math.max(-32767, Math.min(32767, v * 32767));
        }
        return out;
    }

    /** WAV dosyası baytları (16-bit mono). */
    public static byte[] wav(short[] pcm) {
        int dataLen = pcm.length * 2;
        byte[] out = new byte[44 + dataLen];
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(out).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        bb.put("RIFF".getBytes()); bb.putInt(36 + dataLen); bb.put("WAVE".getBytes());
        bb.put("fmt ".getBytes()); bb.putInt(16); bb.putShort((short) 1); bb.putShort((short) 1);
        bb.putInt(RATE); bb.putInt(RATE * 2); bb.putShort((short) 2); bb.putShort((short) 16);
        bb.put("data".getBytes()); bb.putInt(dataLen);
        for (short v : pcm) bb.putShort(v);
        return out;
    }

    // ---------------- Ambiyans ----------------
    public static final String[] AMBIENT = {"Sessiz", "Yağmur", "Dalgalar", "Beyaz Gürültü", "Derin Uğultu", "Şömine"};
    public static final String[] AMBIENT_EMOJI = {"🔇", "🌧️", "🌊", "📻", "🌌", "🔥"};

    /** Akış halinde gürültü üreteci. */
    public static final class Noise {
        final int type; final Random r = new Random();
        double b0, b1, b2, b3, b4, b5, b6, brown, t, lp;
        double dropEnv, dropFreq, dropPh; double crackEnv;
        public Noise(int type) { this.type = type; }

        double white() { return r.nextDouble() * 2 - 1; }

        double pink() {
            double w = white();
            b0 = 0.99886 * b0 + w * 0.0555179; b1 = 0.99332 * b1 + w * 0.0750759; b2 = 0.96900 * b2 + w * 0.1538520;
            b3 = 0.86650 * b3 + w * 0.3104856; b4 = 0.55000 * b4 + w * 0.5329522; b5 = -0.7616 * b5 - w * 0.0168980;
            double v = b0 + b1 + b2 + b3 + b4 + b5 + b6 + w * 0.5362; b6 = w * 0.115926;
            return v * 0.11;
        }

        double brownN() { brown = (brown + 0.02 * white()) / 1.02; return brown * 3.5; }

        public void fill(short[] out, float volume) {
            for (int i = 0; i < out.length; i++) {
                t += 1.0 / RATE;
                double v;
                switch (type) {
                    case 1: { // yağmur
                        v = pink() * 0.7;
                        lp += (white() - lp) * 0.5; v += lp * 0.08;
                        if (dropEnv < 0.001 && r.nextDouble() < 0.0009) { dropEnv = 0.25 + r.nextDouble() * 0.3; dropFreq = 1500 + r.nextDouble() * 2500; }
                        if (dropEnv > 0.001) { dropPh += 2 * Math.PI * dropFreq / RATE; dropFreq *= 0.9996; v += Math.sin(dropPh) * dropEnv; dropEnv *= 0.996; }
                        break;
                    }
                    case 2: { // dalgalar
                        double lfo = 0.5 + 0.5 * Math.sin(2 * Math.PI * t / 9.0) * Math.sin(2 * Math.PI * t / 13.0 + 1);
                        v = brownN() * (0.25 + 0.75 * lfo) + pink() * 0.25 * lfo;
                        break;
                    }
                    case 3: v = white() * 0.35; break;
                    case 4: v = brownN(); break;
                    case 5: { // şömine
                        v = brownN() * 0.6;
                        if (crackEnv < 0.001 && r.nextDouble() < 0.0006) crackEnv = 0.4 + r.nextDouble() * 0.5;
                        if (crackEnv > 0.001) { v += white() * crackEnv; crackEnv *= 0.985; }
                        break;
                    }
                    default: v = 0;
                }
                v *= volume;
                out[i] = (short) Math.max(-32767, Math.min(32767, v * 12000));
            }
        }
    }
}
