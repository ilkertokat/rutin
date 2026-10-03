package com.ilker.rutin.ui;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Ev;
import com.ilker.rutin.core.Habit;
import com.ilker.rutin.core.State;
import com.ilker.rutin.core.Synth;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.data.Repo;
import com.ilker.rutin.sys.FocusService;
import com.ilker.rutin.ui.sheets.Sheets;

/** Tam ekran odak zamanlayıcısı: büyüyen bitki, kalan süre, mola önerileri. */
@SuppressLint("ViewConstructor")
public class FocusOverlay extends FrameLayout {
    final MainActivity a;
    final Repo r;
    final Views.FocusRing ring;
    final TextView title, intent, pauseBtn, ambBtn;
    final LinearLayout breakBox;
    final Runnable tick = this::tick;
    final Runnable onChange = this::sync;
    int lastPhase = -1;
    boolean wasOn;

    public static void show(MainActivity a) {
        for (int i = 0; i < a.overlay.getChildCount(); i++) if (a.overlay.getChildAt(i) instanceof FocusOverlay) return;
        a.showFull(new FocusOverlay(a));
    }

    public FocusOverlay(MainActivity a) {
        super(a);
        this.a = a;
        this.r = a.r;
        setClickable(true);
        setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0xFF1B1433, 0xFF0B0B14}));
        LinearLayout c = U.v(a);
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        c.setPadding(U.dp(24), a.topInset + U.dp(20), U.dp(24), a.bottomInset + U.dp(24));
        addView(c, new LayoutParams(-1, -1));
        LinearLayout topRow = U.h(a);
        TextView min = U.tv(a, "⌄", 28, 0xAAFFFFFF, 900);
        min.setPadding(U.dp(8), 0, U.dp(8), 0);
        min.setOnClickListener(v -> close());
        topRow.addView(min);
        topRow.addView(U.fill(a));
        ambBtn = U.chip(a, "", 0x22FFFFFF, 0xFFFFFFFF);
        ambBtn.setOnClickListener(v -> {
            State.Profile p = r.s.p;
            p.focusAmbient = (p.focusAmbient + 1) % Synth.AMBIENT.length;
            r.save();
            if (p.focusAmbient == 0) com.ilker.rutin.sys.Sound.stopAmbient();
            else FocusService.send(a, FocusService.AMBIENT);
            sync();
        });
        topRow.addView(ambBtn);
        c.addView(topRow, U.lpMW());
        title = U.tv(a, "", 22, 0xFFFFFFFF, 900);
        title.setGravity(Gravity.CENTER);
        c.addView(title, U.m(U.lpMW(), 0, 16, 0, 0));
        intent = U.tv(a, "", 15, 0xBBFFFFFF, 700);
        intent.setGravity(Gravity.CENTER);
        c.addView(intent, U.m(U.lpMW(), 0, 4, 0, 0));
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        ring = new Views.FocusRing(a);
        c.addView(ring, new LinearLayout.LayoutParams(-1, U.dp(320)));
        breakBox = U.v(a);
        c.addView(breakBox, U.lpMW());
        c.addView(U.fill(a), new LinearLayout.LayoutParams(1, 0, 1));
        LinearLayout btns = U.h(a);
        pauseBtn = U.tv(a, "❚❚", 22, 0xFFFFFFFF, 900);
        pauseBtn.setGravity(Gravity.CENTER);
        pauseBtn.setBackground(U.rr(0x33FFFFFF, 50));
        pauseBtn.setOnClickListener(v -> {
            U.haptic(v);
            FocusService.send(a, r.s.p.focusPausedLeftMs > 0 ? FocusService.RESUME : FocusService.PAUSE);
            v.postDelayed(this::sync, 150);
        });
        U.squish(pauseBtn);
        TextView stop = U.tv(a, "■", 26, 0xFFFFFFFF, 900);
        stop.setGravity(Gravity.CENTER);
        stop.setBackground(U.rr(0x33FF6B6B, 50));
        stop.setOnClickListener(v -> confirmStop());
        U.squish(stop);
        btns.setGravity(Gravity.CENTER);
        btns.addView(pauseBtn, new LinearLayout.LayoutParams(U.dp(76), U.dp(76)));
        btns.addView(U.space(a, 28));
        btns.addView(stop, new LinearLayout.LayoutParams(U.dp(76), U.dp(76)));
        c.addView(btns, U.lpMW());
        TextView hint = U.tv(a, "Telefonu bırak. Bitkin sen odaklandıkça büyüyor 🌱", 13, 0x88FFFFFF, 700);
        hint.setGravity(Gravity.CENTER);
        c.addView(hint, U.m(U.lpMW(), 0, 16, 0, 0));
        sync();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        r.listen(onChange);
        post(tick);
    }

    @Override
    protected void onDetachedFromWindow() {
        r.unlisten(onChange);
        removeCallbacks(tick);
        super.onDetachedFromWindow();
    }

    public void close() { a.closeFull(this); }

    void confirmStop() {
        State.Profile p = r.s.p;
        if (p.focusPhase == 1) { FocusService.send(a, FocusService.STOP); close(); return; }
        final Sheet s = new Sheet(a, "Oturumu bitirmek mi istiyorsun?");
        long left = FocusService.remainingMs(p);
        s.add(U.tv(a, "Sadece " + Math.max(1, left / 60000) + " dakika kaldı! Şimdi bırakırsan bitkin solar 🥀 (yaptığın dakikalar yine de sayılır).", 15, Th.text, 700));
        U.Btn cont = U.btn(a, "DEVAM EDİYORUM 💪", Th.focus);
        cont.setOnClickListener(v -> s.dismiss());
        s.add(cont, U.m(U.lpMW(), 0, 18, 0, 0));
        U.Btn stop = U.ghost(a, "Yine de bitir");
        stop.setOnClickListener(v -> { s.dismiss(); FocusService.send(a, FocusService.STOP); close(); postDelayed(() -> Sheets.maybeRateFocus(a), 600); });
        s.add(stop, U.m(U.lpMW(), 0, 10, 0, 0));
        a.show(s);
    }

    void sync() {
        State.Profile p = r.s.p;
        if (!p.focusOn) {
            if (wasOn) { close(); postDelayed(() -> Sheets.maybeRateFocus(a), 500); }
            return;
        }
        wasOn = true;
        boolean brk = p.focusPhase == 1;
        title.setText(brk ? "☕ Mola" : "🍅 " + (p.focusSubject.isEmpty() ? "Serbest odak" : p.focusSubject));
        intent.setText(brk ? "Harika iş çıkardın! Biraz nefes al." : p.focusIntention.isEmpty() ? "" : "🎯 " + p.focusIntention);
        ring.color = brk ? 0xFF3DDC84 : 0xFFA78BFA;
        ring.breakMode = brk;
        ring.treeType = (int) (p.focusStartMs / 1000 % 5);
        pauseBtn.setText(p.focusPausedLeftMs > 0 ? "▶" : "❚❚");
        ambBtn.setText(Synth.AMBIENT_EMOJI[p.focusAmbient] + " " + Synth.AMBIENT[p.focusAmbient]);
        if (lastPhase != p.focusPhase) {
            lastPhase = p.focusPhase;
            buildBreak(brk);
            if (brk) a.confetti.rain(80);
        }
    }

    void buildBreak(boolean brk) {
        breakBox.removeAllViews();
        if (!brk) return;
        LinearLayout row = U.h(a);
        row.setGravity(Gravity.CENTER);
        Task water = null;
        for (Task t : r.s.tasksOf(r.e.today(r.now()))) if (t.type == Habit.COUNTER && t.isOpen()) { water = t; break; }
        if (water != null) {
            final Task w = water;
            TextView b = U.chip(a, "💧 +" + w.amount(w.step), Th.water, 0xFF06223A);
            b.setOnClickListener(v -> { Ev.List ev = r.e.addProgress(w.id, w.step, r.now()); a.celebrate(ev, v); a.floatText(v, "+" + w.amount(w.step), Th.water); r.commit(); });
            U.squish(b);
            row.addView(b);
            row.addView(U.space(a, 8));
        }
        TextView skip = U.chip(a, "⏭ Molayı geç", 0x33FFFFFF, 0xFFFFFFFF);
        skip.setOnClickListener(v -> FocusService.send(a, FocusService.SKIP));
        row.addView(skip);
        breakBox.addView(row);
        TextView tips = U.tv(a, "🙆 Ayağa kalk ve esne   👀 20 sn uzağa bak   🌬️ 3 derin nefes", 13, 0xAAFFFFFF, 700);
        tips.setGravity(Gravity.CENTER);
        breakBox.addView(tips, U.m(U.lpMW(), 0, 12, 0, 0));
    }

    void tick() {
        State.Profile p = r.s.p;
        if (p.focusOn) {
            long left = FocusService.remainingMs(p);
            long total = (p.focusPhase == 0 ? p.focusMin : p.breakMin) * 60000L;
            ring.frac = total <= 0 ? 0 : 1f - left / (float) total;
            long sec = (left + 999) / 1000;
            ring.big = String.format("%02d:%02d", sec / 60, sec % 60);
            ring.small = p.focusPausedLeftMs > 0 ? "DURAKLATILDI" : p.focusPhase == 0 ? "ODAK · " + (p.focusCycle + 1) + ". blok" : "MOLA";
            ring.paused = p.focusPausedLeftMs > 0;
        }
        postDelayed(tick, 250);
    }
}
