package com.ilker.rutin.sys;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.ilker.rutin.core.NotifEngine;
import com.ilker.rutin.data.Repo;

/**
 * Zincirleme zamanlayıcı: her çağrıda günleri kapatır, zamanı gelen bildirimleri gönderir
 * ve yalnızca bir sonraki uyanma için tek bir tam zamanlı alarm kurar.
 */
public final class Scheduler {
    private Scheduler() {}

    public static void reschedule(Context c, boolean fromAlarm) {
        Repo r = Repo.get(c);
        if (!r.s.p.onboarded) return;
        long now = r.now();
        r.e.tick(now);
        boolean focus = r.s.p.focusOn && r.s.p.focusPhase == 0;
        long focusEndMin = focus ? now + Math.max(1, (r.s.p.focusEndMs - System.currentTimeMillis()) / 60000L) : 0;
        NotifEngine.Plan pl = r.ne.plan(now, focus, focusEndMin);
        boolean changed = false;
        if (!pl.due.isEmpty()) {
            if (!r.foreground) Notifier.postTasks(c, pl.due, now);
            r.ne.recordSent(pl.due, now);
            changed = true;
        }
        for (int k : pl.sys) {
            if (!r.foreground || k == NotifEngine.SYS_PLAN || k == NotifEngine.SYS_WIND) Notifier.postSys(c, k, now);
            r.ne.recordSys(k, now);
            changed = true;
        }
        if (changed) {
            pl = r.ne.plan(now, focus, focusEndMin);
            r.save();
        }
        if (fromAlarm) {
            try { com.ilker.rutin.widget.Widgets.updateAll(c); } catch (Throwable ignored) {}
        }
        setAlarm(c, pl.nextAt == NotifEngine.NONE ? now + 60 : pl.nextAt);
    }

    static void setAlarm(Context c, long localMin) {
        Repo r = Repo.get(c);
        long at = r.msOf(localMin);
        if (at < System.currentTimeMillis() + 5000) at = System.currentTimeMillis() + 5000;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(c, 1, new Intent(c, AlarmReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            }
        } catch (SecurityException se) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        }
        Log.d("Rutin", "sonraki alarm: " + new java.util.Date(at));
    }

    public static boolean exactAllowed(Context c) {
        if (Build.VERSION.SDK_INT < 31) return true;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        return am.canScheduleExactAlarms();
    }
}
