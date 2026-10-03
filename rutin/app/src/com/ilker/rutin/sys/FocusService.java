package com.ilker.rutin.sys;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.ilker.rutin.R;
import com.ilker.rutin.core.Ev;
import com.ilker.rutin.core.State;
import com.ilker.rutin.core.Synth;
import com.ilker.rutin.data.Repo;

/** Odak (pomodoro) zamanlayıcısı: ekran kapalıyken de doğru çalışır, ambiyans sesini çalar. */
public class FocusService extends Service {
    public static final String START = "focus.START", PAUSE = "focus.PAUSE", RESUME = "focus.RESUME", STOP = "focus.STOP",
            NEXT = "focus.NEXT", SKIP = "focus.SKIP", AMBIENT = "focus.AMBIENT", SYNC = "focus.SYNC";

    private final Handler h = new Handler(Looper.getMainLooper());
    private final Runnable phaseEnd = this::onPhaseEnd;

    // ---------------- Arayüzden çağrılan API ----------------
    public static void start(Context c, long taskId, String subject, int focusMin, int breakMin, int ambient, String intention, boolean dnd) {
        Repo r = Repo.get(c);
        State.Profile p = r.s.p;
        p.focusOn = true; p.focusPhase = 0;
        p.focusTask = taskId; p.focusSubject = subject == null ? "" : subject;
        p.focusMin = focusMin; p.breakMin = breakMin; p.focusAmbient = ambient;
        p.focusIntention = intention == null ? "" : intention; p.focusDnd = dnd;
        p.focusStartMs = System.currentTimeMillis();
        p.focusEndMs = p.focusStartMs + focusMin * 60000L;
        p.focusPausedLeftMs = 0;
        p.focusStartLocal = r.now();
        r.save();
        send(c, START);
    }

    public static void send(Context c, String action) {
        Intent i = new Intent(c, FocusService.class).setAction(action);
        if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(i); else c.startService(i);
    }

    public static long remainingMs(State.Profile p) {
        if (!p.focusOn) return 0;
        if (p.focusPausedLeftMs > 0) return p.focusPausedLeftMs;
        return Math.max(0, p.focusEndMs - System.currentTimeMillis());
    }

    // ---------------- Servis ----------------
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notifier.channels(this);
        Repo r = Repo.get(this);
        State.Profile p = r.s.p;
        String a = intent != null && intent.getAction() != null ? intent.getAction() : START;
        goForeground();
        switch (a) {
            case START:
                if (!p.focusOn) { stopAll(); return START_NOT_STICKY; }
                setDnd(p.focusDnd && p.focusPhase == 0);
                if (p.focusPhase == 0) Sound.startAmbient(p.focusAmbient, 0.6f);
                Sound.play(this, Synth.POP);
                arm();
                break;
            case PAUSE:
                if (p.focusOn && p.focusPausedLeftMs == 0) {
                    p.focusPausedLeftMs = Math.max(1000, p.focusEndMs - System.currentTimeMillis());
                    h.removeCallbacks(phaseEnd);
                    Sound.stopAmbient();
                    r.save();
                }
                break;
            case RESUME:
                if (p.focusOn && p.focusPausedLeftMs > 0) {
                    p.focusEndMs = System.currentTimeMillis() + p.focusPausedLeftMs;
                    p.focusPausedLeftMs = 0;
                    if (p.focusPhase == 0) Sound.startAmbient(p.focusAmbient, 0.6f);
                    r.save();
                    arm();
                }
                break;
            case SYNC:
                if (!p.focusOn) { stopAll(); return START_NOT_STICKY; }
                if (p.focusPausedLeftMs == 0) {
                    if (p.focusPhase == 0 && !Sound.ambientOn()) Sound.startAmbient(p.focusAmbient, 0.6f);
                    arm();
                }
                break;
            case AMBIENT:
                if (p.focusOn && p.focusPhase == 0 && p.focusPausedLeftMs == 0) Sound.startAmbient(p.focusAmbient, 0.6f);
                break;
            case SKIP:
                if (p.focusOn && p.focusPhase == 1) { h.removeCallbacks(phaseEnd); onPhaseEnd(); return START_STICKY; }
                break;
            case NEXT:
                Notifier.cancel(this, Notifier.ID_FOCUS_DONE);
                start(this, p.focusTask, p.focusSubject, p.focusMin, p.breakMin, p.focusAmbient, "", p.focusDnd);
                return START_STICKY;
            case STOP:
                stopEarly();
                return START_NOT_STICKY;
        }
        updateNotification();
        r.changed();
        return START_STICKY;
    }

    void goForeground() {
        Notification n = build();
        if (Build.VERSION.SDK_INT >= 34) startForeground(Notifier.ID_FOCUS, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        else startForeground(Notifier.ID_FOCUS, n);
    }

    void arm() {
        h.removeCallbacks(phaseEnd);
        State.Profile p = Repo.get(this).s.p;
        long left = p.focusEndMs - System.currentTimeMillis();
        h.postDelayed(phaseEnd, Math.max(200, left));
    }

    void updateNotification() {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(Notifier.ID_FOCUS, build());
    }

    Notification build() {
        State.Profile p = Repo.get(this).s.p;
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, Notifier.CH_FOCUS) : new Notification.Builder(this);
        boolean paused = p.focusPausedLeftMs > 0;
        String subj = p.focusSubject.isEmpty() ? "Serbest odak" : p.focusSubject;
        b.setSmallIcon(R.drawable.ic_stat).setColor(p.focusPhase == 0 ? 0xFFA78BFA : 0xFF3DDC84).setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(Notifier.openApp(this, "focus", 0, 950))
                .setContentTitle(p.focusPhase == 0 ? "🍅 Odak · " + subj : "☕ Mola zamanı")
                .setContentText(paused ? "⏸ Duraklatıldı · " + fmt(p.focusPausedLeftMs) + " kaldı"
                        : p.focusPhase == 0 ? (p.focusIntention.isEmpty() ? "Telefonu bırak, sadece bu iş 🌱" : "🎯 " + p.focusIntention)
                        : "Su iç 💧, esne 🙆, gözlerini dinlendir 👀");
        if (!paused) {
            b.setUsesChronometer(true).setWhen(p.focusEndMs).setShowWhen(true);
            if (Build.VERSION.SDK_INT >= 24) b.setChronometerCountDown(true);
        }
        if (paused) b.addAction(new Notification.Action.Builder(null, "▶️ Devam", pi(RESUME, 1)).build());
        else b.addAction(new Notification.Action.Builder(null, "⏸ Duraklat", pi(PAUSE, 2)).build());
        if (p.focusPhase == 1) b.addAction(new Notification.Action.Builder(null, "⏭ Molayı geç", pi(SKIP, 4)).build());
        b.addAction(new Notification.Action.Builder(null, "⏹ Bitir", pi(STOP, 3)).build());
        return b.build();
    }

    PendingIntent pi(String action, int req) {
        Intent i = new Intent(this, FocusService.class).setAction(action);
        if (Build.VERSION.SDK_INT >= 26) return PendingIntent.getForegroundService(this, 700 + req, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return PendingIntent.getService(this, 700 + req, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static String fmt(long ms) {
        long s = ms / 1000;
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    void onPhaseEnd() {
        Repo r = Repo.get(this);
        State.Profile p = r.s.p;
        if (!p.focusOn) { stopAll(); return; }
        long now = r.now();
        if (p.focusPhase == 0) {
            Ev.List ev = r.e.focusDone(p.focusTask, p.focusSubject, p.focusMin, p.focusMin, true, 0, false, p.focusIntention, "", p.focusStartLocal, now);
            p.focusRate = r.s.focus.size() - 1;
            p.focusCycle++;
            Sound.stopAmbient();
            setDnd(false);
            Sound.play(this, Synth.SUCCESS);
            int xp = ev.xp();
            // Mola
            p.focusPhase = 1;
            p.focusStartMs = System.currentTimeMillis();
            p.focusEndMs = p.focusStartMs + p.breakMin * 60000L;
            p.focusPausedLeftMs = 0;
            postDone("🍅 Odak tamamlandı!  +" + xp + " XP", "🌳 Ormanına bir ağaç eklendi. Şimdi " + p.breakMin + " dk mola: su iç, esne, gözlerini dinlendir.", false);
            r.emit(ev);
            r.commit();
            arm();
            updateNotification();
        } else {
            Sound.play(this, Synth.DING);
            p.focusOn = false;
            p.focusPhase = 0;
            r.commit();
            postDone("☕ Mola bitti!", "Bir blok daha? Momentumu koru 🔥", true);
            stopAll();
        }
    }

    void postDone(String title, String text, boolean offerNext) {
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, Notifier.CH_SOFT) : new Notification.Builder(this);
        b.setSmallIcon(R.drawable.ic_stat).setColor(0xFFA78BFA).setAutoCancel(true)
                .setContentTitle(title).setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text))
                .setContentIntent(Notifier.openApp(this, "focus", 0, 951));
        if (offerNext) b.addAction(new Notification.Action.Builder(null, "▶️ Sonraki blok", pi(NEXT, 5)).build());
        if (Build.VERSION.SDK_INT >= 26) b.setTimeoutAfter(20 * 60000L);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(Notifier.ID_FOCUS_DONE, b.build());
    }

    void stopEarly() {
        Repo r = Repo.get(this);
        State.Profile p = r.s.p;
        if (p.focusOn && p.focusPhase == 0) {
            long leftMs = p.focusPausedLeftMs > 0 ? p.focusPausedLeftMs : Math.max(0, p.focusEndMs - System.currentTimeMillis());
            int done = (int) ((p.focusMin * 60000L - leftMs) / 60000L);
            if (done >= 1) {
                Ev.List ev = r.e.focusDone(p.focusTask, p.focusSubject, done, p.focusMin, false, 0, false, p.focusIntention, "", p.focusStartLocal, r.now());
                p.focusRate = r.s.focus.size() - 1;
                r.emit(ev);
            }
            Sound.play(this, Synth.WITHER);
        }
        p.focusOn = false;
        p.focusPhase = 0;
        p.focusPausedLeftMs = 0;
        r.commit();
        stopAll();
    }

    void stopAll() {
        h.removeCallbacks(phaseEnd);
        Sound.stopAmbient();
        setDnd(false);
        if (Build.VERSION.SDK_INT >= 24) stopForeground(STOP_FOREGROUND_REMOVE); else stopForeground(true);
        stopSelf();
    }

    void setDnd(boolean on) {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (!nm.isNotificationPolicyAccessGranted()) return;
            if (on) nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
            else if (nm.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_PRIORITY && Repo.get(this).s.p.focusDnd)
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        } catch (Throwable ignored) {}
    }

    @Override
    public void onDestroy() {
        h.removeCallbacks(phaseEnd);
        Sound.stopAmbient();
        super.onDestroy();
    }
}
