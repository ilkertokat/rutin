package com.ilker.rutin.sys;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import com.ilker.rutin.core.Ev;
import com.ilker.rutin.core.Synth;
import com.ilker.rutin.data.Repo;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Sentezlenmiş efekt sesleri ve odak ambiyansı. */
public final class Sound {
    private Sound() {}

    private static final HashMap<Integer, short[]> cache = new HashMap<>();
    private static final ExecutorService ex = Executors.newCachedThreadPool();

    static synchronized short[] pcm(int id, int pack) {
        int key = id * 10 + pack;
        short[] p = cache.get(key);
        if (p == null) { p = Synth.render(id, pack); cache.put(key, p); }
        return p;
    }

    public static void prewarm(Context c) {
        final int pack = Repo.get(c).s.p.soundPack;
        ex.execute(() -> {
            int[] ids = {Synth.DING, Synth.BLUP, Synth.SUCCESS, Synth.STREAK, Synth.POP, Synth.COMBO, Synth.TICK};
            for (int id : ids) pcm(id, pack);
        });
    }

    public static void play(Context c, int id) {
        Repo r = Repo.get(c);
        if (!r.s.p.soundOn) return;
        playPack(id, r.s.p.soundPack);
    }

    public static void playPack(final int id, final int pack) {
        ex.execute(() -> {
            AudioTrack at = null;
            try {
                short[] p = pcm(id, pack);
                at = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                        .setAudioFormat(new AudioFormat.Builder().setSampleRate(Synth.RATE)
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .setBufferSizeInBytes(Math.max(p.length * 2, 1024)).build();
                at.write(p, 0, p.length);
                at.play();
                Thread.sleep(p.length * 1000L / Synth.RATE + 150);
            } catch (Throwable ignored) {
            } finally {
                if (at != null) try { at.release(); } catch (Throwable ignored) {}
            }
        });
    }

    /** Olay listesindeki en önemli olaya uygun sesi çalar. */
    public static void forEvents(Context c, Ev.List ev) {
        int id = -1, rank = -1;
        for (Ev e : ev) {
            int s, rk;
            switch (e.type) {
                case Ev.LEVEL_UP: s = Synth.LEVELUP; rk = 9; break;
                case Ev.PERFECT: s = Synth.FANFARE; rk = 8; break;
                case Ev.STREAK_UP: case Ev.MILESTONE: s = Synth.STREAK; rk = 7; break;
                case Ev.BADGE: s = Synth.BADGE; rk = 6; break;
                case Ev.COUNTER_FULL: case Ev.GOAL: s = Synth.SUCCESS; rk = 5; break;
                case Ev.HABIT_LEVEL: s = Synth.BADGE; rk = 5; break;
                case Ev.QUESTS_ALL: case Ev.WEEKLY_DONE: s = Synth.CHEST; rk = 5; break;
                case Ev.COMBO: s = Synth.COMBO; rk = 4; break;
                case Ev.TASK_DONE: s = Synth.SUCCESS; rk = 3; break;
                case Ev.MINI_DONE: s = Synth.DING; rk = 3; break;
                case Ev.QUEST: s = Synth.POP; rk = 2; break;
                case Ev.HALFWAY: s = Synth.DING; rk = 2; break;
                case Ev.PROGRESS: s = Synth.BLUP; rk = 1; break;
                default: continue;
            }
            if (rk > rank) { rank = rk; id = s; }
        }
        if (id >= 0) play(c, id);
    }

    // ---------------- Ambiyans ----------------
    private static volatile Thread ambient;
    private static volatile boolean ambientRun;
    private static volatile int ambientType;

    public static synchronized void startAmbient(final int type, final float volume) {
        stopAmbient();
        if (type <= 0) return;
        ambientRun = true;
        ambientType = type;
        Thread th = new Thread(() -> {
            int buf = Math.max(AudioTrack.getMinBufferSize(Synth.RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT), 8820);
            AudioTrack at = null;
            try {
                at = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                        .setAudioFormat(new AudioFormat.Builder().setSampleRate(Synth.RATE)
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                        .setTransferMode(AudioTrack.MODE_STREAM).setBufferSizeInBytes(buf * 2).build();
                at.play();
                Synth.Noise n = new Synth.Noise(type);
                short[] chunk = new short[2205];
                float v = 0;
                while (ambientRun && ambientType == type) {
                    v = Math.min(volume, v + 0.02f);
                    n.fill(chunk, v);
                    at.write(chunk, 0, chunk.length);
                }
                // yumuşak kapanış
                for (int k = 0; k < 20; k++) { v *= 0.8f; n.fill(chunk, v); at.write(chunk, 0, chunk.length); }
            } catch (Throwable ignored) {
            } finally {
                if (at != null) try { at.stop(); at.release(); } catch (Throwable ignored) {}
            }
        }, "ambient");
        th.setPriority(Thread.MAX_PRIORITY);
        ambient = th;
        th.start();
    }

    public static synchronized void stopAmbient() {
        ambientRun = false;
        ambient = null;
    }

    public static boolean ambientOn() { return ambientRun; }
}
