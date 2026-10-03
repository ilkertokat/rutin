package com.ilker.rutin.sys;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import com.ilker.rutin.R;
import com.ilker.rutin.core.Coach;
import com.ilker.rutin.core.DayRec;
import com.ilker.rutin.core.Habit;
import com.ilker.rutin.core.NotifEngine;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.data.Repo;
import com.ilker.rutin.ui.MainActivity;

import java.util.ArrayList;

/** Bildirim kanalları ve bildirim oluşturma. */
public final class Notifier {
    private Notifier() {}

    public static final String CH_SOFT = "tasks_soft2", CH_PUSH = "tasks_push2", CH_CRIT = "tasks_crit2", CH_DAILY = "daily2",
            CH_FOCUS = "focus", CH_CHEER = "cheer";
    public static final int ID_GROUP = 9000, ID_SYS = 9100, ID_STREAK = 9200, ID_FOCUS = 9300, ID_FOCUS_DONE = 9301;

    static final int C_PRIMARY = 0xFFFF7A45;

    public static void channels(Context c) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        AudioAttributes aa = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        String pkg = c.getPackageName();
        NotificationChannel soft = new NotificationChannel(CH_SOFT, "Hatırlatıcılar", NotificationManager.IMPORTANCE_DEFAULT);
        soft.setDescription("Zamanı gelen işler için nazik hatırlatmalar");
        soft.setSound(Uri.parse("android.resource://" + pkg + "/" + R.raw.notif_soft), aa);
        soft.enableVibration(true);
        soft.setVibrationPattern(new long[]{0, 120, 80, 120});
        NotificationChannel push = new NotificationChannel(CH_PUSH, "Israrlı hatırlatıcılar", NotificationManager.IMPORTANCE_HIGH);
        push.setDescription("Zaman daralırken daha dikkat çekici hatırlatmalar");
        push.setSound(Uri.parse("android.resource://" + pkg + "/" + R.raw.notif_push), aa);
        push.enableVibration(true);
        push.setVibrationPattern(new long[]{0, 200, 100, 200});
        NotificationChannel crit = new NotificationChannel(CH_CRIT, "Kritik hatırlatıcılar", NotificationManager.IMPORTANCE_HIGH);
        crit.setDescription("Son dakika uyarıları (seri tehlikede)");
        crit.setSound(Uri.parse("android.resource://" + pkg + "/" + R.raw.notif_crit), aa);
        crit.enableVibration(true);
        crit.setVibrationPattern(new long[]{0, 400, 150, 400, 150, 600});
        crit.enableLights(true);
        crit.setLightColor(0xFFFF4040);
        NotificationChannel daily = new NotificationChannel(CH_DAILY, "Günlük döngü", NotificationManager.IMPORTANCE_DEFAULT);
        daily.setDescription("Günaydın, akşam kontrolü, gün sonu özeti ve haftalık planlama");
        daily.setSound(Uri.parse("android.resource://" + pkg + "/" + R.raw.notif_morning), aa);
        NotificationChannel focus = new NotificationChannel(CH_FOCUS, "Odak modu", NotificationManager.IMPORTANCE_LOW);
        focus.setDescription("Çalışan odak zamanlayıcısı");
        focus.setSound(null, null);
        focus.setShowBadge(false);
        NotificationChannel cheer = new NotificationChannel(CH_CHEER, "Kutlamalar", NotificationManager.IMPORTANCE_LOW);
        cheer.setDescription("Tamamlama ve seri kutlamaları");
        cheer.setSound(null, null);
        nm.createNotificationChannel(soft);
        nm.createNotificationChannel(push);
        nm.createNotificationChannel(crit);
        nm.createNotificationChannel(daily);
        nm.createNotificationChannel(focus);
        nm.createNotificationChannel(cheer);
    }

    static NotificationManager nm(Context c) { return (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE); }

    public static PendingIntent openApp(Context c, String open, long taskId, int req) {
        Intent i = new Intent(c, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (open != null) i.putExtra("open", open);
        if (taskId != 0) i.putExtra("task", taskId);
        return PendingIntent.getActivity(c, req, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static PendingIntent action(Context c, String action, long taskId, int amount, int req) {
        Intent i = new Intent(c, ActionReceiver.class);
        i.setAction(action);
        i.putExtra("task", taskId);
        i.putExtra("amount", amount);
        return PendingIntent.getBroadcast(c, req, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static Notification.Builder base(Context c, String ch) {
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(c, ch) : new Notification.Builder(c);
        b.setSmallIcon(R.drawable.ic_stat).setColor(C_PRIMARY).setAutoCancel(true).setShowWhen(true);
        return b;
    }

    static String channelFor(int level) {
        if (level >= NotifEngine.L_CRIT) return CH_CRIT;
        if (level >= NotifEngine.L_PUSH) return CH_PUSH;
        return CH_SOFT;
    }

    static String streakSub(Repo r) {
        int s = r.e.displayStreak(r.now());
        return s > 0 ? "🔥 " + s + " gün seri" : "Rutin";
    }

    public static void postTasks(Context c, ArrayList<NotifEngine.Due> due, long now) {
        if (due.size() == 1) { postTask(c, due.get(0).t, due.get(0).level, now); return; }
        Repo r = Repo.get(c);
        int maxLevel = 0;
        for (NotifEngine.Due d : due) maxLevel = Math.max(maxLevel, d.level);
        Notification.InboxStyle st = new Notification.InboxStyle();
        for (NotifEngine.Due d : due) st.addLine(d.t.emoji + "  " + d.t.title + "  ·  " + statusText(r, d.t, now));
        String title = Coach.msg(Coach.GROUP, r.s.p.coach, r.s, null, null, 0, due.size());
        st.setBigContentTitle(title);
        Notification.Builder b = base(c, channelFor(maxLevel))
                .setContentTitle(title)
                .setContentText(due.get(0).t.emoji + " " + due.get(0).t.title + (due.size() > 1 ? " ve " + (due.size() - 1) + " iş daha" : ""))
                .setStyle(st)
                .setSubText(streakSub(r))
                .setContentIntent(openApp(c, "today", 0, 900))
                .setCategory(Notification.CATEGORY_REMINDER);
        // En acil iş için hızlı eylem
        Task top = due.get(0).t;
        if (!top.isQuantity()) b.addAction(new Notification.Action.Builder(null, "✅ " + shortTitle(top.title), action(c, ActionReceiver.DONE, top.id, 0, (int) top.id * 10 + 1)).build());
        else if (top.type == Habit.COUNTER) b.addAction(new Notification.Action.Builder(null, "+" + top.amount(top.step) + " " + shortTitle(top.title), action(c, ActionReceiver.ADD, top.id, top.step, (int) top.id * 10 + 2)).build());
        b.addAction(new Notification.Action.Builder(null, "⏰ 15 dk hepsi", action(c, ActionReceiver.SNOOZE_ALL, 0, 15, 901)).build());
        for (NotifEngine.Due d : due) nm(c).cancel((int) d.t.id);
        nm(c).notify(ID_GROUP, b.build());
    }

    static String shortTitle(String s) { return s.length() > 14 ? s.substring(0, 13) + "…" : s; }

    static String statusText(Repo r, Task t, long now) {
        if (t.type == Habit.COUNTER) return t.amount(t.progress) + " / " + t.amount(t.full);
        if (t.type == Habit.DURATION) return Tm.durShort(t.progress) + " / " + Tm.durShort(t.full);
        int late = r.e.lateness(t, now);
        if (late == Integer.MIN_VALUE) return "bugün";
        if (late > 0) return late + " dk gecikti";
        return Tm.hhmm(t.targetMin);
    }

    public static void postTask(Context c, Task t, int level, long now) {
        Repo r = Repo.get(c);
        int coach = r.s.p.coach;
        String text, title = t.emoji + " " + t.title;
        int cat;
        String rem = null;
        int minutesLeft = 0;
        if (t.type == Habit.COUNTER) {
            rem = t.amount(Math.max(0, t.full - t.progress));
            double gap = r.ne.gapRatio(t, now);
            if (t.progress == 0 && r.e.nowDm(now) < r.e.wakeDm() + 90) cat = Coach.W_START;
            else if (t.progress >= t.full * 0.75) cat = Coach.W_LAST;
            else if (gap > 0.25) cat = Coach.W_FAR;
            else cat = Coach.W_BEHIND;
            title += "  ·  " + t.amount(t.progress) + " / " + t.amount(t.full);
        } else if (t.type == Habit.DURATION) {
            rem = Tm.dur(Math.max(0, t.full - t.progress));
            double pr = r.ne.pressure(t, now);
            boolean block = false;
            for (int bl : t.blocks) if (Math.abs(r.e.dmOf(bl) - r.e.nowDm(now)) <= 3) block = true;
            cat = block ? Coach.BLOCK : pr > 0.9 ? Coach.D_HIGH : pr > 0.3 ? Coach.D_PRESS : Coach.D_ON;
            title += "  ·  " + Tm.durShort(t.progress) + " / " + Tm.durShort(t.full);
        } else {
            cat = level == NotifEngine.L_PREP ? Coach.PREP : level == NotifEngine.L_ON ? Coach.ON : level == NotifEngine.L_NUDGE ? Coach.NUDGE
                    : level == NotifEngine.L_PUSH ? Coach.PUSH : Coach.CRIT;
            minutesLeft = (int) Math.max(1, r.e.deadlineDm(t) - r.e.nowDm(now));
            if (t.targetMin >= 0) title += "  ·  " + Tm.hhmm(t.targetMin);
            if (level >= NotifEngine.L_PUSH) title += "  ·  son " + Tm.hhmm(Tm.norm(r.e.deadlineDm(t)));
        }
        text = Coach.msg(cat, coach, r.s, t, rem, minutesLeft, 0);
        Habit h = r.s.habit(t.habitId);
        String big = text;
        if (level >= NotifEngine.L_NUDGE && h != null && h.why != null && !h.why.isEmpty()) big += "\n💭 Neden: " + h.why;
        if (level >= NotifEngine.L_PUSH && t.hasMini() && !t.isQuantity()) big += "\n🟡 Mini versiyon: " + t.miniText;
        if (level >= NotifEngine.L_NUDGE && h != null && h.ifThen != null && !h.ifThen.isEmpty()) big += "\n🔀 Planın: " + h.ifThen;
        Notification.Builder b = base(c, channelFor(level))
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(big))
                .setSubText(streakSub(r))
                .setContentIntent(openApp(c, "task", t.id, (int) t.id))
                .setCategory(level >= NotifEngine.L_CRIT ? Notification.CATEGORY_ALARM : Notification.CATEGORY_REMINDER);
        if (level >= NotifEngine.L_PUSH) b.setPriority(Notification.PRIORITY_HIGH);
        if (t.isQuantity()) b.setProgress(Math.max(1, t.full), Math.min(t.full, t.progress), false);
        int rq = (int) (t.id * 10);
        if (t.type == Habit.COUNTER) {
            b.addAction(new Notification.Action.Builder(null, "+" + t.amount(t.step), action(c, ActionReceiver.ADD, t.id, t.step, rq + 2)).build());
            b.addAction(new Notification.Action.Builder(null, "+" + t.amount(t.step * 2), action(c, ActionReceiver.ADD, t.id, t.step * 2, rq + 3)).build());
            b.addAction(new Notification.Action.Builder(null, "⏰ 30 dk", action(c, ActionReceiver.SNOOZE, t.id, 30, rq + 4)).build());
        } else if (t.type == Habit.DURATION) {
            b.addAction(new Notification.Action.Builder(null, "▶️ Odak başlat", openApp(c, "focus", t.id, rq + 5)).build());
            b.addAction(new Notification.Action.Builder(null, "⏰ 30 dk", action(c, ActionReceiver.SNOOZE, t.id, 30, rq + 4)).build());
        } else {
            b.addAction(new Notification.Action.Builder(null, "✅ Tamamladım", action(c, ActionReceiver.DONE, t.id, 0, rq + 1)).build());
            if (t.hasMini()) b.addAction(new Notification.Action.Builder(null, "🟡 Mini yaptım", action(c, ActionReceiver.MINI, t.id, 0, rq + 6)).build());
            b.addAction(new Notification.Action.Builder(null, "⏰ 15 dk", action(c, ActionReceiver.SNOOZE, t.id, 15, rq + 4)).build());
        }
        nm(c).notify((int) t.id, b.build());
    }

    public static void postSys(Context c, int kind, long now) {
        Repo r = Repo.get(c);
        int coach = r.s.p.coach;
        int td = r.e.today(now);
        ArrayList<Task> open = new ArrayList<>();
        for (Task t : r.e.sorted(td)) if (t.isOpen() && t.type != Habit.FLEX) open.add(t);
        String title, text, openWhat;
        String ch = CH_DAILY;
        switch (kind) {
            case NotifEngine.SYS_MORNING: {
                title = "Günaydın" + (r.s.p.name.isEmpty() ? "" : " " + r.s.p.name) + " ☀️";
                text = Coach.msg(Coach.MORNING, coach, r.s, null, null, 0, open.size());
                StringBuilder sb = new StringBuilder(text);
                int n = 0;
                for (Task t : open) { if (n++ >= 3) break; sb.append("\n• ").append(t.emoji).append(" ").append(t.title); }
                DayRec prev = r.s.dayOrNull(td - 1);
                if (prev != null && prev.intention != null && !prev.intention.isEmpty()) sb.append("\n🎯 Dünkü niyetin: ").append(prev.intention);
                text = sb.toString();
                openWhat = "morning";
                break;
            }
            case NotifEngine.SYS_EVENING_RISK: {
                title = "🔥 " + r.s.p.streak + " günlük serin tehlikede!";
                int req = 0;
                StringBuilder sb = new StringBuilder();
                for (Task t : r.e.required(td)) if (!r.e.satisfied(t)) { req++; sb.append("\n• ").append(t.emoji).append(" ").append(t.title); if (t.hasMini()) sb.append(" (mini: ").append(t.isQuantity() ? t.amount(t.mini) : t.miniText).append(")"); }
                text = Coach.msg(Coach.RISK, coach, r.s, null, null, 0, req) + sb;
                openWhat = "today";
                ch = CH_PUSH;
                break;
            }
            case NotifEngine.SYS_WIND: {
                title = "🌙 Rüzgârı azaltma vakti";
                text = Coach.msg(Coach.WIND, coach, r.s, null, null, 0, 0);
                if (!open.isEmpty()) text += "\nKalan " + open.size() + " işi mini versiyonla kurtarabilirsin.";
                openWhat = "today";
                break;
            }
            case NotifEngine.SYS_SUMMARY: {
                title = "✨ Gün sonu özeti";
                text = Coach.msg(Coach.SUMMARY, coach, r.s, null, null, 0, 0);
                openWhat = "summary";
                break;
            }
            case NotifEngine.SYS_PLAN: {
                title = "🗺️ Haftalık planlama";
                text = Coach.msg(Coach.PLAN, coach, r.s, null, null, 0, 0);
                openWhat = "plan";
                break;
            }
            default: return;
        }
        Notification.Builder b = base(c, ch)
                .setContentTitle(title)
                .setContentText(text.split("\n")[0])
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setSubText(streakSub(r))
                .setContentIntent(openApp(c, openWhat, 0, 800 + kind));
        nm(c).notify(ID_SYS + kind, b.build());
    }

    /** Eylemden sonra bildirimi kısa bir kutlamaya çevirir. */
    public static void cheer(Context c, long taskId, String title, String text) {
        Notification.Builder b = base(c, CH_CHEER)
                .setContentTitle(title)
                .setContentText(text)
                .setOnlyAlertOnce(true)
                .setContentIntent(openApp(c, "today", 0, 902));
        if (Build.VERSION.SDK_INT >= 26) b.setTimeoutAfter(6000);
        nm(c).notify((int) taskId, b.build());
    }

    public static void streakUp(Context c, int streak) {
        Notification.Builder b = base(c, CH_CHEER)
                .setContentTitle("🔥 Seri " + streak + " gün!")
                .setContentText("Bugünün gereksinimleri tamam. Serin güvende 🎉")
                .setContentIntent(openApp(c, "today", 0, 903));
        if (Build.VERSION.SDK_INT >= 26) b.setTimeoutAfter(15000);
        nm(c).notify(ID_STREAK, b.build());
    }

    public static void cancel(Context c, int id) { nm(c).cancel(id); }

    public static void cancelTask(Context c, long taskId) { nm(c).cancel((int) taskId); }
}
