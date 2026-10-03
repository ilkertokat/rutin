package com.ilker.rutin.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ilker.rutin.core.Engine;
import com.ilker.rutin.core.Json;
import com.ilker.rutin.core.NotifEngine;
import com.ilker.rutin.core.State;
import com.ilker.rutin.sys.Scheduler;
import com.ilker.rutin.widget.Widgets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.TimeZone;

/** Tek veri kaynağı: durumu yükler/kaydeder, motoru barındırır, değişiklikleri yayınlar. */
public final class Repo {
    private static Repo inst;

    public final Context app;
    public State s;
    public Engine e;
    public NotifEngine ne;
    public boolean foreground;
    private final ArrayList<Runnable> listeners = new ArrayList<>();
    private final Handler main = new Handler(Looper.getMainLooper());

    public static synchronized Repo get(Context c) {
        if (inst == null) inst = new Repo(c.getApplicationContext());
        return inst;
    }

    private Repo(Context app) {
        this.app = app;
        load();
    }

    File file() { return new File(app.getFilesDir(), "state.json"); }
    File backup() { return new File(app.getFilesDir(), "state.bak.json"); }

    void load() {
        State st = read(file());
        if (st == null) st = read(backup());
        if (st == null) st = new State();
        setState(st);
    }

    void setState(State st) {
        s = st;
        e = new Engine(s);
        ne = new NotifEngine(e);
    }

    static State read(File f) {
        if (!f.exists()) return null;
        try (InputStream in = new FileInputStream(f)) {
            byte[] b = new byte[(int) f.length()];
            int off = 0;
            while (off < b.length) { int n = in.read(b, off, b.length - off); if (n < 0) break; off += n; }
            return Json.decode(Json.parse(new String(b, 0, off, StandardCharsets.UTF_8)), State.class);
        } catch (Throwable t) {
            Log.e("Rutin", "okuma hatası " + f, t);
            return null;
        }
    }

    public synchronized void save() {
        try {
            String js = Json.write(s);
            File tmp = new File(app.getFilesDir(), "state.tmp");
            try (FileOutputStream o = new FileOutputStream(tmp)) {
                o.write(js.getBytes(StandardCharsets.UTF_8));
                o.getFD().sync();
            }
            File f = file();
            if (f.exists()) {
                File b = backup();
                // günde bir yedek
                if (!b.exists() || System.currentTimeMillis() - b.lastModified() > 12 * 3600_000L) {
                    if (b.exists()) b.delete();
                    f.renameTo(b);
                }
            }
            if (!tmp.renameTo(f)) {
                f.delete();
                tmp.renameTo(f);
            }
        } catch (Throwable t) {
            Log.e("Rutin", "kaydetme hatası", t);
        }
    }

    public String exportJson() { return Json.write(s); }

    public boolean importJson(String js) {
        try {
            State st = Json.decode(Json.parse(js), State.class);
            if (st == null || st.p == null) return false;
            setState(st);
            commit();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Yerel epoch dakikası (hata ayıklama ofseti dahil). */
    public long now() {
        long ms = System.currentTimeMillis();
        return (ms + TimeZone.getDefault().getOffset(ms)) / 60000L + s.p.debugOffsetMin;
    }

    /** Yerel dakikayı gerçek epoch milisaniyeye çevirir. */
    public long msOf(long localMin) {
        long approx = (localMin - s.p.debugOffsetMin) * 60000L;
        int off = TimeZone.getDefault().getOffset(approx);
        return approx - off;
    }

    /** Kaydet + bildirimleri yeniden planla + widget'ları güncelle + arayüzü yenile. */
    public void commit() {
        save();
        try { Scheduler.reschedule(app, false); } catch (Throwable t) { Log.e("Rutin", "zamanlama", t); }
        try { Widgets.updateAll(app); } catch (Throwable t) { Log.e("Rutin", "widget", t); }
        changed();
    }

    public void changed() {
        main.post(() -> { for (Runnable r : new ArrayList<>(listeners)) r.run(); });
    }

    public interface EvListener { void on(com.ilker.rutin.core.Ev.List ev); }
    private final ArrayList<EvListener> evListeners = new ArrayList<>();

    /** Arka planda (servis/alıcı) oluşan olayları arayüze iletir; arayüz yoksa not olarak saklar. */
    public void emit(final com.ilker.rutin.core.Ev.List ev) {
        if (ev == null || ev.isEmpty()) return;
        if (evListeners.isEmpty() || !foreground) {
            for (com.ilker.rutin.core.Ev x : ev) {
                int t = x.type;
                if (t == com.ilker.rutin.core.Ev.LEVEL_UP || t == com.ilker.rutin.core.Ev.BADGE || t == com.ilker.rutin.core.Ev.PERFECT
                        || t == com.ilker.rutin.core.Ev.MILESTONE || t == com.ilker.rutin.core.Ev.QUESTS_ALL || t == com.ilker.rutin.core.Ev.WEEKLY_DONE)
                    e.notice(t, x.value, x.text);
            }
            return;
        }
        main.post(() -> { for (EvListener l : new ArrayList<>(evListeners)) l.on(ev); });
    }

    public void listenEv(EvListener l) { evListeners.add(l); }
    public void unlistenEv(EvListener l) { evListeners.remove(l); }

    public void listen(Runnable r) { listeners.add(r); }
    public void unlisten(Runnable r) { listeners.remove(r); }
}
