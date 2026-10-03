package com.ilker.rutin.ui;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Area;
import com.ilker.rutin.core.Ev;
import com.ilker.rutin.core.Habit;
import com.ilker.rutin.core.NotifEngine;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.data.Repo;
import com.ilker.rutin.ui.sheets.Sheets;

/** Zaman çizelgesindeki görev satırı: dokun = ayrıntı, sağa kaydır = tamamla, onay kutusu, sayaç butonu. */
@SuppressLint("ViewConstructor")
public class TaskRow extends FrameLayout {
    final MainActivity a;
    final Repo r;
    public final Task t;
    final LinearLayout card;
    final TextView under;
    Views.CheckView check;
    Views.WaveBar bar;
    TextView sub;
    final boolean today;

    public TaskRow(MainActivity a, Task t, boolean interactive) {
        super(a);
        this.a = a;
        this.r = a.r;
        this.t = t;
        long now = r.now();
        today = t.day == r.e.today(now);
        under = U.tv(a, "✓  Tamamla", 16, 0xFF0B2A18, 900);
        under.setGravity(Gravity.CENTER_VERTICAL);
        under.setPadding(U.dp(22), 0, 0, 0);
        under.setBackground(U.rr(Th.success, 22));
        under.setAlpha(0);
        addView(under, new LayoutParams(-1, -1));
        card = U.h(a);
        card.setPadding(U.dp(10), U.dp(12), U.dp(12), U.dp(12));
        boolean done = t.isComplete() || t.status == Task.SKIPPED || t.status == Task.MISSED;
        int accent = 0;
        int level = -1;
        if (t.isOpen() && today) {
            if (t.type == Habit.COUNTER || t.type == Habit.DURATION) level = r.ne.levelAt(t, now) >= NotifEngine.L_PUSH ? 3 : -1;
            else {
                int late = r.e.lateness(t, now);
                if (late != Integer.MIN_VALUE && late > t.graceMin) level = r.ne.levelAt(t, now);
            }
            if (level >= NotifEngine.L_CRIT) accent = Th.crit;
            else if (level >= NotifEngine.L_NUDGE) accent = Th.warn;
        }
        card.setBackground(accent != 0 ? U.rrs(Th.surface, 22, Art.alpha(accent, 0.7f), 1.5f) : U.rr(Th.surface, 22));
        addView(card, new LayoutParams(-1, -2));
        // saat sütunu
        TextView time = U.tv(a, t.targetMin >= 0 ? Tm.hhmm(t.targetMin) : t.type == Habit.FLEX ? "🌿" : "gün\nboyu", t.targetMin >= 0 ? 13 : 10, Th.dim, 800);
        time.setGravity(Gravity.CENTER);
        card.addView(time, new LinearLayout.LayoutParams(U.dp(46), -2));
        TextView em = U.emojiBox(a, t.emoji, Area.COLOR[Math.max(0, Math.min(5, t.area))], 44);
        card.addView(em);
        card.addView(U.space(a, 12));
        LinearLayout mid = U.v(a);
        TextView title = U.tv(a, t.title, 16, done ? Th.dim : Th.text, 800);
        if (t.isComplete()) title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        title.setMaxLines(2);
        mid.addView(title);
        sub = U.tv(a, status(now), 12.5f, level >= NotifEngine.L_CRIT ? Th.crit : level >= NotifEngine.L_NUDGE ? Th.warn : Th.dim, 700);
        mid.addView(sub);
        if (t.isQuantity()) {
            bar = new Views.WaveBar(a, t.type == Habit.COUNTER ? Th.water : Th.focus);
            bar.set((float) t.ratio(), false);
            if (t.mini > 0 && t.mini < t.full) bar.mini = t.mini / (float) t.full;
            mid.addView(bar, U.m(new LinearLayout.LayoutParams(-1, U.dp(10)), 0, 6, 0, 0));
        }
        card.addView(mid, U.lpW(1));
        card.addView(U.space(a, 8));
        // sağ eylem
        if (interactive && today && t.status != Task.MISSED) {
            if (t.type == Habit.COUNTER && t.isOpen()) {
                TextView plus = U.tv(a, "+" + t.amount(t.step), 14, 0xFF06223A, 900);
                plus.setGravity(Gravity.CENTER);
                plus.setBackground(U.rr(Th.water, 16));
                plus.setPadding(U.dp(12), U.dp(10), U.dp(12), U.dp(10));
                plus.setOnClickListener(v -> addStep(v, t.step));
                plus.setOnLongClickListener(v -> { Sheets.task(a, t, null); return true; });
                U.squish(plus);
                card.addView(plus);
            } else if (t.type == Habit.DURATION && t.isOpen()) {
                TextView play = U.tv(a, "▶", 18, 0xFFFFFFFF, 900);
                play.setGravity(Gravity.CENTER);
                play.setBackground(U.rr(Th.focus, 50));
                play.setOnClickListener(v -> { U.haptic(v); Sheets.focusStart(a, t.id); });
                U.squish(play);
                card.addView(play, new LinearLayout.LayoutParams(U.dp(44), U.dp(44)));
            } else {
                check = new Views.CheckView(a);
                check.color = Th.success;
                check.set(t.isComplete(), t.status == Task.MINI, false);
                check.setOnClickListener(v -> {
                    if (t.isOpen()) complete(false);
                    else if (t.isComplete()) Sheets.task(a, t, null);
                });
                check.setOnLongClickListener(v -> { if (t.isOpen() && t.hasMini()) complete(true); return true; });
                card.addView(check, new LinearLayout.LayoutParams(U.dp(40), U.dp(40)));
            }
        } else if (t.status == Task.MISSED) {
            card.addView(U.tv(a, "✗", 18, Th.crit, 900));
        }
        if (interactive) {
            card.setOnClickListener(v -> { U.haptic(v); Sheets.task(a, t, null); });
            if (today && t.isOpen() && t.type != Habit.DURATION) enableSwipe();
        }
        if (done) card.setAlpha(0.72f);
    }

    String status(long now) {
        switch (t.status) {
            case Task.DONE: return "✅ Tamamlandı · " + Tm.hhmm((int) (t.doneAt % 1440)) + (t.early ? " · 🐦 erken" : "") + (t.xpGiven > 0 ? " · +" + t.xpGiven + " XP" : "");
            case Task.MINI: return "🟡 Mini tamamlandı · seri güvende";
            case Task.MISSED: return "Kaçırıldı · olur böyle, yarın yeni gün";
            case Task.SKIPPED: return "⏭️ Atlandı";
            case Task.MOVED: return "➡️ Taşındı";
        }
        StringBuilder sb = new StringBuilder();
        if (t.type == Habit.COUNTER) {
            sb.append(t.amount(t.progress)).append(" / ").append(t.amount(t.full));
            if (t.mini > 0 && t.mini < t.full) sb.append(t.progress >= t.mini ? " · 🟡 mini tamam, seri güvende" : " · mini " + t.amount(t.mini));
            if (today && r.ne.gapRatio(t, now) <= 0.001 && t.progress > 0) sb.append(" · 👍 plandasın");
            return sb.toString();
        }
        if (t.type == Habit.DURATION) {
            sb.append(Tm.durShort(t.progress)).append(" / ").append(Tm.durShort(t.full));
            if (t.origFull > 0 && t.origFull != t.full) sb.append(" (bugün düşürüldü)");
            if (t.mini > 0 && t.progress >= t.mini) sb.append(" · 🟡 mini tamam");
            if (!t.blocks.isEmpty()) {
                sb.append(" · bloklar ");
                for (int i = 0; i < t.blocks.size() && i < 3; i++) sb.append(i > 0 ? ", " : "").append(Tm.hhmm(t.blocks.get(i)));
            }
            return sb.toString();
        }
        if (!today) return t.targetMin >= 0 ? "Planlandı · " + Tm.hhmm(t.targetMin) : Habit.TYPE_NAME[t.type];
        if (t.snoozeUntil > now) return "💤 " + Tm.hhmm((int) (t.snoozeUntil % 1440)) + "'e ertelendi";
        int late = r.e.lateness(t, now);
        String dl = " · son " + Tm.hhmm(Tm.norm(r.e.deadlineDm(t)));
        if (late == Integer.MIN_VALUE) return (t.type == Habit.FLEX ? "Boş zamanında · bonus XP" : "Bugün" + dl) + (t.hasMini() ? " · mini: " + t.miniText : "");
        if (late > t.graceMin) return "⚠️ " + Tm.dur(late) + " gecikti" + dl;
        if (late >= -5) return "⏰ Şimdi zamanı!" + (t.hasMini() ? " · mini: " + t.miniText : "");
        if (-late <= 180) return "⏳ " + Tm.dur(-late) + " sonra";
        return Habit.TYPE_EMOJI[t.type] + " " + Habit.TYPE_NAME[t.type] + (t.hasMini() ? " · mini: " + t.miniText : "");
    }

    void complete(boolean mini) {
        long now = r.now();
        Ev.List ev = r.e.complete(t.id, mini, now);
        if (check != null) check.set(true, mini, true);
        U.hapticStrong(this);
        a.celebrate(ev, check != null ? check : card);
        card.animate().alpha(0.72f).setDuration(300).start();
        postDelayed(r::commit, 650);
    }

    void addStep(View v, int amount) {
        long now = r.now();
        Ev.List ev = r.e.addProgress(t.id, amount, now);
        U.haptic(v);
        if (bar != null) bar.set((float) t.ratio(), true);
        sub.setText(status(now));
        a.floatText(v, "+" + t.amount(amount), Th.water);
        a.celebrate(ev, ev.has(Ev.TASK_DONE) ? v : null);
        postDelayed(r::commit, ev.has(Ev.COUNTER_FULL) ? 700 : 250);
    }

    @SuppressLint("ClickableViewAccessibility")
    void enableSwipe() {
        final float slop = ViewConfiguration.get(a).getScaledTouchSlop();
        final float[] st = new float[3];
        card.setOnTouchListener((v, e) -> {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: st[0] = e.getRawX(); st[1] = e.getRawY(); st[2] = 0; break;
                case MotionEvent.ACTION_MOVE: {
                    float dx = e.getRawX() - st[0], dy = e.getRawY() - st[1];
                    if (st[2] == 0 && dx > slop * 1.5f && Math.abs(dx) > Math.abs(dy) * 1.5f) { st[2] = 1; getParent().requestDisallowInterceptTouchEvent(true); v.setPressed(false); }
                    if (st[2] == 1) {
                        float tx = Math.max(0, dx);
                        v.setTranslationX(tx);
                        under.setAlpha(Math.min(1, tx / (getWidth() * 0.3f)));
                        return true;
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: case MotionEvent.ACTION_CANCEL:
                    if (st[2] == 1) {
                        st[2] = 0;
                        if (v.getTranslationX() > getWidth() * 0.3f && e.getActionMasked() == MotionEvent.ACTION_UP) {
                            v.animate().translationX(getWidth()).setDuration(180).withEndAction(() -> {
                                if (t.type == Habit.COUNTER) addStep(v, t.step); else complete(false);
                                v.animate().translationX(0).setDuration(250).start();
                                under.animate().alpha(0).setDuration(250).start();
                            }).start();
                        } else {
                            v.animate().translationX(0).setDuration(200).start();
                            under.animate().alpha(0).setDuration(200).start();
                        }
                        return true;
                    }
                    break;
            }
            return false;
        });
    }
}
