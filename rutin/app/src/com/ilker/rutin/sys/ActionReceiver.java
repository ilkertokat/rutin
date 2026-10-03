package com.ilker.rutin.sys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.ilker.rutin.core.Coach;
import com.ilker.rutin.core.Ev;
import com.ilker.rutin.core.Habit;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.data.Repo;

/** Bildirim ve widget butonları: uygulamayı açmadan tamamlama, sayaç ekleme, erteleme. */
public class ActionReceiver extends BroadcastReceiver {
    public static final String DONE = "com.ilker.rutin.DONE", MINI = "com.ilker.rutin.MINI", SNOOZE = "com.ilker.rutin.SNOOZE",
            ADD = "com.ilker.rutin.ADD", SNOOZE_ALL = "com.ilker.rutin.SNOOZE_ALL", WATER = "com.ilker.rutin.WATER",
            WTOGGLE = "com.ilker.rutin.WTOGGLE";

    @Override
    public void onReceive(Context c, Intent in) {
        String a = in.getAction();
        if (a == null) return;
        final PendingResult pr = goAsync();
        Repo r = Repo.get(c);
        long now = r.now();
        long id = in.getLongExtra("task", 0);
        int amount = in.getIntExtra("amount", 0);
        Ev.List ev = new Ev.List();
        r.e.tick(now);
        Task t = id != 0 ? r.s.task(id) : null;
        if (WATER.equals(a)) {
            // Su widget'ı: bugünün ml sayaçlı görevini bul
            for (Task x : r.s.tasksOf(r.e.today(now))) if (x.type == Habit.COUNTER && "ml".equals(x.unit)) { t = x; break; }
            if (t == null) for (Task x : r.s.tasksOf(r.e.today(now))) if (x.type == Habit.COUNTER) { t = x; break; }
            if (t != null) ev = r.e.addProgress(t.id, amount > 0 ? amount : t.step, now);
        } else if (DONE.equals(a) || WTOGGLE.equals(a)) {
            if (t != null) {
                if (t.type == Habit.COUNTER) ev = r.e.addProgress(t.id, t.step, now);
                else if (t.isOpen()) ev = r.e.complete(t.id, false, now);
            }
        } else if (MINI.equals(a)) {
            if (t != null) ev = r.e.complete(t.id, true, now);
        } else if (ADD.equals(a)) {
            if (t != null) ev = r.e.addProgress(t.id, amount, now);
        } else if (SNOOZE.equals(a)) {
            if (t != null) r.e.snooze(t.id, amount > 0 ? amount : 15, now);
            Notifier.cancelTask(c, id);
        } else if (SNOOZE_ALL.equals(a)) {
            for (Task x : r.s.tasksOf(r.e.today(now))) if (x.isOpen() && x.lastNotif > now - 30) r.e.snooze(x.id, amount > 0 ? amount : 15, now);
            Notifier.cancel(c, Notifier.ID_GROUP);
        }
        r.ne.recordActed(now);
        if (t != null && !SNOOZE.equals(a)) {
            String title;
            if (ev.has(Ev.COUNTER_FULL)) title = "🎉 " + t.title + " hedefi tamam!";
            else if (t.type == Habit.COUNTER) title = t.emoji + " " + t.amount(t.progress) + " / " + t.amount(t.full);
            else if (ev.has(Ev.MINI_DONE)) title = "🟡 " + t.title + ": mini tamam, seri güvende";
            else title = "✅ " + t.title;
            int xp = ev.xp();
            String text = Coach.msg(Coach.CHEER, r.s.p.coach, r.s, t, null, 0, 0) + (xp > 0 ? "  +" + xp + " XP" : "");
            if (ev.has(Ev.COMBO)) text += "  🎯 Kombo!";
            Notifier.cheer(c, t.id, title, text);
            if (id == 0) Notifier.cancel(c, Notifier.ID_GROUP);
        }
        for (Ev e : ev) if (e.type == Ev.STREAK_UP) Notifier.streakUp(c, e.value);
        // Uygulama kapalıyken gelen önemli olayları sonraki açılışta göster
        for (Ev e : ev) if (e.type == Ev.LEVEL_UP || e.type == Ev.BADGE || e.type == Ev.PERFECT || e.type == Ev.MILESTONE || e.type == Ev.QUESTS_ALL)
            r.e.notice(e.type, e.value, e.text);
        Sound.forEvents(c, ev);
        r.commit();
        new Handler(Looper.getMainLooper()).postDelayed(pr::finish, 2500);
    }
}
