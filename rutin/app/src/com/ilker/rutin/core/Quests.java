package com.ilker.rutin.core;

import java.util.ArrayList;
import java.util.Random;

/** Günlük 3 görev ve haftalık meydan okuma. */
public final class Quests {
    private Quests() {}

    public static final int Q_DONE5 = 0, Q_EARLY = 1, Q_COMBO = 2, Q_FOCUS2 = 3, Q_FOCUS45 = 4, Q_WATER_NOON = 5,
            Q_WATER_20 = 6, Q_MORNING = 7, Q_FLEX = 8, Q_ANCHORS = 9, Q_REFLECT = 10, Q_START = 11;

    public static final int W_PERFECT = 100, W_FOCUS = 101, W_GOOD = 102, W_EARLY = 103, W_WATER = 104, W_REFLECT = 105;

    public static String text(State.Quest q) {
        switch (q.kind) {
            case Q_DONE5: return "Bugün " + q.target + " görev tamamla";
            case Q_EARLY: return "Bir görevi hedef saatinden önce bitir";
            case Q_COMBO: return q.target + "'lü kombo yap (1 saat içinde)";
            case Q_FOCUS2: return q.target + " odak oturumu tamamla";
            case Q_FOCUS45: return "Toplam " + q.target + " dk odaklan";
            case Q_WATER_NOON: return "Öğlene kadar " + Fmt.amount(q.target, "ml", Habit.COUNTER) + " su iç";
            case Q_WATER_20: return "Su hedefini 20:00'den önce tamamla";
            case Q_MORNING: return "Saat 10:00'dan önce " + q.target + " görev bitir";
            case Q_FLEX: return "Bir esnek görev yap";
            case Q_ANCHORS: return "Tüm çapa rutinlerini zamanında yap";
            case Q_REFLECT: return "Gün sonu değerlendirmesini yap";
            case Q_START: return "Güne Başla'ya bas";
            case W_PERFECT: return "Bu hafta " + q.target + " Mükemmel Gün";
            case W_FOCUS: return "Bu hafta " + Tm.dur(q.target) + " odaklan";
            case W_GOOD: return "Bu hafta " + q.target + " gün seriyi uzat";
            case W_EARLY: return "Bu hafta " + q.target + " görevi erken bitir";
            case W_WATER: return "Bu hafta " + q.target + " gün su hedefini tamamla";
            case W_REFLECT: return "Bu hafta " + q.target + " akşam değerlendirme yap";
        }
        return "";
    }

    public static String emoji(int kind) {
        switch (kind) {
            case Q_DONE5: return "✅";
            case Q_EARLY: case W_EARLY: return "🐦";
            case Q_COMBO: return "🎯";
            case Q_FOCUS2: case Q_FOCUS45: case W_FOCUS: return "⏱️";
            case Q_WATER_NOON: case Q_WATER_20: case W_WATER: return "💧";
            case Q_MORNING: return "🌅";
            case Q_FLEX: return "🌿";
            case Q_ANCHORS: return "⚓";
            case Q_REFLECT: case W_REFLECT: return "📓";
            case Q_START: return "☀️";
            case W_PERFECT: return "💎";
            case W_GOOD: return "🔥";
        }
        return "⭐";
    }

    static boolean hasWater(ArrayList<Task> ts) {
        for (Task t : ts) if (t.type == Habit.COUNTER && "ml".equals(t.unit)) return true;
        return false;
    }

    static int count(ArrayList<Task> ts, int type) { int n = 0; for (Task t : ts) if (t.type == type) n++; return n; }

    /** Güne ait 3 görevi üretir (deterministik: gün numarasıyla tohumlanır). */
    public static void generate(State s, int day) {
        s.quests.clear();
        ArrayList<Task> ts = s.tasksOf(day);
        ArrayList<int[]> pool = new ArrayList<>();
        int n = 0; for (Task t : ts) if (t.type != Habit.FLEX) n++;
        if (n >= 4) pool.add(new int[]{Q_DONE5, Math.min(5, n)});
        if (count(ts, Habit.ANCHOR) + count(ts, Habit.REQUIRED) > 0) pool.add(new int[]{Q_EARLY, 1});
        if (ts.size() >= 3) pool.add(new int[]{Q_COMBO, 3});
        pool.add(new int[]{Q_FOCUS2, 2});
        pool.add(new int[]{Q_FOCUS45, 45});
        if (hasWater(ts)) { pool.add(new int[]{Q_WATER_NOON, 1000}); pool.add(new int[]{Q_WATER_20, 1}); }
        if (ts.size() >= 3) pool.add(new int[]{Q_MORNING, 2});
        if (count(ts, Habit.FLEX) > 0) pool.add(new int[]{Q_FLEX, 1});
        int anchors = count(ts, Habit.ANCHOR);
        if (anchors > 0) pool.add(new int[]{Q_ANCHORS, anchors});
        pool.add(new int[]{Q_REFLECT, 1});
        pool.add(new int[]{Q_START, 1});
        Random r = new Random(day * 131L + 7);
        for (int i = 0; i < 3 && !pool.isEmpty(); i++) {
            int[] q = pool.remove(r.nextInt(pool.size()));
            State.Quest x = new State.Quest();
            x.kind = q[0]; x.target = q[1]; x.day = day;
            s.quests.add(x);
        }
        s.p.questDay = day;
    }

    public static void generateWeekly(State s, int weekStart) {
        ArrayList<int[]> pool = new ArrayList<>();
        pool.add(new int[]{W_PERFECT, 3});
        int durTarget = 0;
        boolean water = false;
        for (Habit h : s.activeHabits()) {
            if (h.type == Habit.DURATION) durTarget += h.full * Integer.bitCount(h.daysMask);
            if (h.type == Habit.COUNTER && "ml".equals(h.unit)) water = true;
        }
        pool.add(new int[]{W_FOCUS, Math.max(300, (int) (durTarget * 0.8 / 30) * 30)});
        pool.add(new int[]{W_GOOD, 6});
        pool.add(new int[]{W_EARLY, 10});
        if (water) pool.add(new int[]{W_WATER, 5});
        pool.add(new int[]{W_REFLECT, 5});
        Random r = new Random(weekStart * 17L + 3);
        int[] q = pool.get(r.nextInt(pool.size()));
        State.Quest w = new State.Quest();
        w.kind = q[0]; w.target = q[1]; w.day = weekStart;
        s.p.weekly = w;
        s.p.weeklyWeek = weekStart;
    }
}
