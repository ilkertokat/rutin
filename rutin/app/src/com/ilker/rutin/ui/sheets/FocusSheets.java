package com.ilker.rutin.ui.sheets;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilker.rutin.core.Habit;
import com.ilker.rutin.core.State;
import com.ilker.rutin.core.Synth;
import com.ilker.rutin.core.Task;
import com.ilker.rutin.core.Tm;
import com.ilker.rutin.data.Repo;
import com.ilker.rutin.sys.FocusService;
import com.ilker.rutin.sys.Sound;
import com.ilker.rutin.ui.FocusOverlay;
import com.ilker.rutin.ui.MainActivity;
import com.ilker.rutin.ui.Sheet;
import com.ilker.rutin.ui.Th;
import com.ilker.rutin.ui.U;
import com.ilker.rutin.ui.Views;

import java.util.ArrayList;

/** Odak başlatma ve oturum puanlama panelleri. */
public final class FocusSheets {
    private FocusSheets() {}

    static final int[][] MODES = {{25, 5}, {50, 10}, {90, 20}, {5, 0}, {-1, -1}};
    static final String[] MODE_NAMES = {"🍅 25 / 5", "📘 50 / 10", "🧠 90 / 20", "🌱 Sadece 5 dk", "⚙️ Özel"};

    public static void start(final MainActivity a, long taskId) {
        final Repo r = a.r;
        if (r.s.p.focusOn) { FocusOverlay.show(a); return; }
        final long now = r.now();
        final Sheet s = new Sheet(a, "⏱️ Odak oturumu");
        // konu
        s.add(Sheets.label(a, "NE ÜZERİNE ÇALIŞACAKSIN?"));
        final long[] task = {taskId};
        final String[] subject = {""};
        Views.Flow sf = new Views.Flow(a);
        final ArrayList<TextView> subs = new ArrayList<>();
        ArrayList<Task> durs = new ArrayList<>();
        for (Task t : r.s.tasksOf(r.e.today(now))) if (t.type == Habit.DURATION) durs.add(t);
        if (taskId == 0 && !durs.isEmpty()) {
            for (Task t : durs) if (t.isOpen()) { task[0] = t.id; break; }
        }
        for (final Task t : durs) {
            if (t.id == task[0]) subject[0] = t.title;
            TextView p = U.pill(a, t.emoji + " " + t.title + " · " + Tm.durShort(t.progress) + "/" + Tm.durShort(t.full), t.id == task[0]);
            p.setOnClickListener(v -> { task[0] = t.id; subject[0] = t.title; for (TextView x : subs) U.setPill(x, x == v); });
            subs.add(p);
            sf.addView(p);
        }
        TextView free = U.pill(a, "🌀 Serbest", task[0] == 0);
        free.setOnClickListener(v -> { task[0] = 0; subject[0] = ""; for (TextView x : subs) U.setPill(x, x == v); });
        subs.add(free);
        sf.addView(free);
        s.add(sf);
        // mod
        s.add(Sheets.label(a, "SÜRE"));
        final int[] mode = {r.s.p.focusMin == 50 ? 1 : r.s.p.focusMin == 90 ? 2 : 0};
        final int[] custom = {r.s.p.focusMin, r.s.p.breakMin};
        final LinearLayout customBox = U.v(a);
        Views.Flow mf = new Views.Flow(a);
        final ArrayList<TextView> ms = new ArrayList<>();
        for (int i = 0; i < MODES.length; i++) {
            final int k = i;
            TextView p = U.pill(a, MODE_NAMES[i], i == mode[0]);
            p.setOnClickListener(v -> { mode[0] = k; for (TextView x : ms) U.setPill(x, x == v); customBox.setVisibility(k == 4 ? View.VISIBLE : View.GONE); });
            ms.add(p);
            mf.addView(p);
        }
        s.add(mf);
        final Views.NumStepper cf = new Views.NumStepper(a, Math.max(5, custom[0]), 5, 5, 180, v -> v + " dk odak");
        cf.onChange = () -> custom[0] = cf.value;
        final Views.NumStepper cb = new Views.NumStepper(a, custom[1], 1, 0, 45, v -> v + " dk mola");
        cb.onChange = () -> custom[1] = cb.value;
        customBox.addView(cf);
        customBox.addView(cb);
        customBox.setVisibility(View.GONE);
        s.add(customBox);
        TextView tip = U.tv(a, "💡 Başlamakta zorlanıyorsan \"Sadece 5 dk\" seç. Çoğu zaman devam edersin.", 12, Th.dim, 700);
        s.add(tip, U.m(U.lpMW(), 0, 8, 0, 0));
        // ambiyans
        s.add(Sheets.label(a, "AMBİYANS"));
        final int[] amb = {r.s.p.focusAmbient};
        Views.Flow af = new Views.Flow(a);
        final ArrayList<TextView> as = new ArrayList<>();
        for (int i = 0; i < Synth.AMBIENT.length; i++) {
            final int k = i;
            TextView p = U.pill(a, Synth.AMBIENT_EMOJI[i] + " " + Synth.AMBIENT[i], i == amb[0]);
            p.setOnClickListener(v -> {
                amb[0] = k;
                for (TextView x : as) U.setPill(x, x == v);
                if (k > 0) { Sound.startAmbient(k, 0.5f); v.postDelayed(() -> { if (!r.s.p.focusOn) Sound.stopAmbient(); }, 2500); }
                else Sound.stopAmbient();
            });
            as.add(p);
            af.addView(p);
        }
        s.add(af);
        // niyet
        s.add(Sheets.label(a, "🎯 BU OTURUMDA NE BİTİRECEKSİN?"));
        final EditText intent = U.input(a, "Örn: 3. bölümün sorularını çöz", "", false);
        s.add(intent);
        // rahatsız etme
        final boolean[] dnd = {r.s.p.focusDnd};
        final NotificationManager nm = (NotificationManager) a.getSystemService(Context.NOTIFICATION_SERVICE);
        final TextView dt = U.pill(a, "🔕 Rahatsız Etme modunu aç", dnd[0]);
        dt.setOnClickListener(v -> {
            if (!nm.isNotificationPolicyAccessGranted()) {
                a.toast("Rutin'e 'Rahatsız Etme erişimi' ver, sonra geri dön");
                try { a.startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)); } catch (Exception ignored) {}
                return;
            }
            dnd[0] = !dnd[0];
            U.setPill(dt, dnd[0]);
        });
        s.add(dt, U.m(U.lpWW(), 0, 12, 0, 0));
        U.Btn go = U.btn(a, "▶  BAŞLA", Th.focus);
        go.setOnClickListener(v -> {
            int fm = mode[0] == 4 ? custom[0] : MODES[mode[0]][0];
            int bm = mode[0] == 4 ? custom[1] : MODES[mode[0]][1];
            if (mode[0] == 3) bm = 3;
            Sound.stopAmbient();
            FocusService.start(a, task[0], subject[0], fm, Math.max(1, bm), amb[0], intent.getText().toString().trim(), dnd[0] && nm.isNotificationPolicyAccessGranted());
            s.dismiss();
            v.postDelayed(() -> FocusOverlay.show(a), 250);
        });
        s.add(go, U.m(U.lpMW(), 0, 20, 0, 0));
        s.onClose = () -> { if (!r.s.p.focusOn) Sound.stopAmbient(); };
        a.show(s);
    }

    static boolean rateOpen;

    /** Biten oturumu puanlama (odak skoru + niyet gerçekleşti mi). */
    public static void maybeRate(final MainActivity a) {
        final Repo r = a.r;
        final int idx = r.s.p.focusRate;
        if (idx < 0 || idx >= r.s.focus.size()) return;
        final State.FocusLog f = r.s.focus.get(idx);
        if (f.score > 0) { r.s.p.focusRate = -1; return; }
        if (rateOpen) return;
        rateOpen = true;
        final Sheet s = new Sheet(a, f.completed ? "🍅 Oturum tamam!" : "🥀 Oturum yarıda kaldı");
        s.add(U.tv(a, (f.subject.isEmpty() ? "Serbest odak" : f.subject) + " · " + Tm.dur(f.minutes)
                + (f.completed ? " · 🌳 ormanına bir ağaç eklendi" : " · ağaç soldu ama dakikaların sayıldı"), 14, Th.dim, 700));
        s.add(Sheets.label(a, "NE KADAR ODAKLANDIN?"));
        final int[] score = {0};
        LinearLayout stars = U.h(a);
        stars.setGravity(Gravity.CENTER);
        final ArrayList<TextView> st = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int k = i;
            TextView t = U.tv(a, "☆", 38, Th.gold, 400);
            t.setPadding(U.dp(6), 0, U.dp(6), 0);
            t.setOnClickListener(v -> { score[0] = k; for (int j = 0; j < st.size(); j++) st.get(j).setText(j < k ? "★" : "☆"); U.bounce(v); U.haptic(v); });
            st.add(t);
            stars.addView(t);
        }
        s.add(stars);
        final boolean[] ach = {false};
        if (!f.intention.isEmpty()) {
            s.add(Sheets.label(a, "NİYETİN: " + f.intention));
            LinearLayout yn = U.h(a);
            final TextView yes = U.pill(a, "✅ Bitirdim", false), no = U.pill(a, "Yarısı / hayır", false);
            yes.setOnClickListener(v -> { ach[0] = true; U.setPill(yes, true); U.setPill(no, false); });
            no.setOnClickListener(v -> { ach[0] = false; U.setPill(yes, false); U.setPill(no, true); });
            yn.addView(yes);
            yn.addView(U.space(a, 8));
            yn.addView(no);
            s.add(yn);
        }
        final EditText note = U.input(a, "Kısa not (isteğe bağlı)", "", false);
        s.add(note, U.m(U.lpMW(), 0, 12, 0, 0));
        U.Btn save = U.btn(a, "KAYDET", Th.focus);
        save.setOnClickListener(v -> {
            f.score = Math.max(1, score[0]);
            f.achieved = ach[0];
            f.note = note.getText().toString().trim();
            r.s.p.focusRate = -1;
            if (ach[0]) { com.ilker.rutin.core.Ev.List ev = new com.ilker.rutin.core.Ev.List(); r.e.gainXp(5, "Niyet gerçekleşti", f.day, r.now(), ev); a.celebrate(ev, v); }
            r.commit();
            s.dismiss();
        });
        s.add(save, U.m(U.lpMW(), 0, 16, 0, 0));
        s.onClose = () -> { rateOpen = false; if (r.s.p.focusRate == idx) { r.s.p.focusRate = -1; r.save(); } };
        a.show(s);
    }
}
