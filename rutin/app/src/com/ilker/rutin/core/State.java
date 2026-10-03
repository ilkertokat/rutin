package com.ilker.rutin.core;

import java.util.ArrayList;
import java.util.HashMap;

/** Uygulamanın tüm kalıcı durumu. JSON olarak saklanır. */
public class State {
    public int version = 1;
    public Profile p = new Profile();
    public ArrayList<Habit> habits = new ArrayList<>();
    public ArrayList<Task> tasks = new ArrayList<>();
    public ArrayList<DayRec> days = new ArrayList<>();
    public ArrayList<Template> templates = new ArrayList<>();
    public ArrayList<FocusLog> focus = new ArrayList<>();
    public ArrayList<BadgeEarn> badges = new ArrayList<>();
    public ArrayList<Reward> rewards = new ArrayList<>();
    public ArrayList<Insight> insights = new ArrayList<>();
    public ArrayList<Quest> quests = new ArrayList<>();
    public ArrayList<Integer> island = new ArrayList<>();   // ada yapıları (Island.* kodları)
    public ArrayList<Integer> generated = new ArrayList<>(); // görevleri üretilmiş günler
    public ArrayList<Chest> chests = new ArrayList<>();      // açılmamış sandıklar
    public ArrayList<String> notices = new ArrayList<>();
    public ArrayList<Integer> cards = new ArrayList<>();    // koleksiyon kartları (Game.CARDS indeksleri)    // sonraki açılışta gösterilecek olaylar (tip|değer|metin)

    // ---- Yardımcı erişimler (transient önbellekler) ----
    public transient HashMap<Integer, DayRec> dayIdx;

    public DayRec day(int d) {
        if (dayIdx == null) { dayIdx = new HashMap<>(); for (DayRec r : days) dayIdx.put(r.day, r); }
        DayRec r = dayIdx.get(d);
        if (r == null) { r = new DayRec(); r.day = d; days.add(r); dayIdx.put(d, r); }
        return r;
    }

    public DayRec dayOrNull(int d) {
        if (dayIdx == null) day(d);
        return dayIdx.get(d);
    }

    public Habit habit(long id) {
        for (Habit h : habits) if (h.id == id) return h;
        return null;
    }

    public Task task(long id) {
        for (int i = tasks.size() - 1; i >= 0; i--) if (tasks.get(i).id == id) return tasks.get(i);
        return null;
    }

    public ArrayList<Task> tasksOf(int d) {
        ArrayList<Task> r = new ArrayList<>();
        for (Task t : tasks) if (t.day == d && t.counts()) r.add(t);
        return r;
    }

    public ArrayList<Habit> activeHabits() {
        ArrayList<Habit> r = new ArrayList<>();
        for (Habit h : habits) if (!h.archived && h.type != Habit.ONCE) r.add(h);
        return r;
    }

    public long nextId() { return ++p.nextId; }

    public boolean hasBadge(String id, int tier) {
        for (BadgeEarn b : badges) if (b.id.equals(id) && b.tier >= tier) return true;
        return false;
    }

    public int badgeTier(String id) {
        int t = 0;
        for (BadgeEarn b : badges) if (b.id.equals(id)) t = Math.max(t, b.tier);
        return t;
    }

    public boolean owns(String item) { return p.owned.contains(item); }

    // ---------------- İç sınıflar ----------------
    public static class Profile {
        public boolean onboarded;
        public String name = "";
        public int wakeMin = 7 * 60 + 30;
        public int sleepMin = 23 * 60 + 30;
        public int coach = 0;
        public int companion = 0;
        public String companionName = "Pofuduk";
        public ArrayList<String> identities = new ArrayList<>();
        public String promise = "";
        public String signature = "";
        public String letter = "";
        public ArrayList<Integer> lettersOpened = new ArrayList<>();
        public int reasons;

        public long xp;
        public int coins;
        public int freezes = 1;
        public int freezeWeek = -1;

        public int streak, bestStreak;
        public int lastClosedDay = Integer.MIN_VALUE;
        public int sleepStreak, bestSleepStreak;
        public int goodnightDay = -1;
        public int repairOld, repairUntil = -1, repairProgress;

        public int theme = 0, soundPack = 0;
        public boolean soundOn = true, hapticOn = true, prepReminders = true;
        public int budget = 4;
        public ArrayList<String> owned = new ArrayList<>();
        public String hat = "", acc = "";
        public int restMask = 0;
        public int vacationUntil = -1, sickUntil = -1, softUntil = -1;
        public long activeTemplate;
        public boolean autoTemplate = true;

        public int firstDay = -1, lastActiveDay = -1, comebackDay = -1;
        public int lastMorningDay = -1, lastWindDay = -1, lastSummaryDay = -1, lastEveningWarnDay = -1;
        public int lastPlanNotifWeek = -1, lastPlanWeek = -1, lastReportWeek = -1, lastAdaptWeek = -1;
        public int lastMorningShownDay = -1;

        // Toplam istatistikler
        public int totalDone, totalEarly, totalFocusMin, maxFocusDay, perfectDays, questsDone, chestsOpened;
        public int repairs, stormSaves, lateSaves, flexDone, plans, reflections, bestCombo, redeemed, bought, graduates, comebacks;
        public long waterTotal;
        public int morningStarts;
        public int punctualRun, bestPunctual, weekendPerfect, morningTriple, balanceDays, silentDays, longestSession;
        public long boostUntil;
        public int dailyGoal = 50;
        public int spins, goalDays, heroWeeks;

        public long nextId = 100;

        // Bildirim bütçesi ve öğrenen zamanlama
        public ArrayList<Long> sentTimes = new ArrayList<>();
        public int[] hourSent = new int[24];
        public int[] hourActed = new int[24];

        // Kombo
        public long lastCompleteAt;
        public int combo;

        // Odak oturumu (çalışma zamanı durumu, milisaniye)
        public boolean focusOn;
        public int focusPhase;          // 0 odak, 1 mola
        public long focusStartMs, focusEndMs, focusPausedLeftMs;
        public int focusMin = 25, breakMin = 5, focusCycle;
        public long focusTask;
        public String focusSubject = "", focusIntention = "";
        public int focusAmbient;
        public boolean focusDnd;
        public int focusRate = -1;      // puanlanmayı bekleyen odak kaydı
        public long focusStartLocal;

        // Plan dışı olay
        public long shieldUntil;        // yerel dk
        public int shieldType = -1;
        public int shieldDay = -1;

        // Haftalık meydan okuma
        public Quest weekly;
        public int weeklyWeek = -1;
        public int questDay = -1;

        public long debugOffsetMin;
        public int installVersion = 1;
    }

    public static class Template {
        public long id;
        public String name = "", emoji = "📋";
        public ArrayList<TItem> items = new ArrayList<>();
    }

    public static class TItem {
        public long habitId;
        public int daysMask;
        public int targetMin = -1;
    }

    public static class FocusLog {
        public int day;
        public long startAt;       // yerel dk
        public int minutes, planned;
        public long taskId, habitId;
        public String subject = "";
        public int score;          // 1-5
        public boolean completed;  // süre tamamlandı
        public boolean achieved;   // niyet gerçekleşti
        public String intention = "", note = "";
        public int tree;           // ağaç türü
    }

    public static class Quest {
        public int kind, target, progress;
        public boolean done;
        public int day;
    }

    public static class BadgeEarn {
        public String id = "";
        public int tier, day;
        public boolean seen;
    }

    public static class Reward {
        public long id;
        public String name = "", emoji = "🎁";
        public int cost, redeemed;
    }

    public static class Insight {
        public long id;
        public int kind;           // Insights.K_*
        public long habitId;
        public int value;
        public String text = "";
        public int day;
        public boolean dismissed;
    }

    public static class Chest {
        public int rarity;         // 0 sıradan ... 3 efsanevi
        public int day;
        public String reason = "";
    }
}
