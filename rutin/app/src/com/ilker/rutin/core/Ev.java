package com.ilker.rutin.core;

import java.util.ArrayList;

/** Motorun ürettiği olaylar: arayüz bunları kutlamaya, sese ve animasyona çevirir. */
public class Ev {
    public static final int XP = 1, TASK_DONE = 2, COUNTER_FULL = 3, STREAK_UP = 4, PERFECT = 5, LEVEL_UP = 6,
            BADGE = 7, CHEST = 8, QUEST = 9, QUESTS_ALL = 10, COMBO = 11, MILESTONE = 12, STREAK_BROKEN = 13,
            FROZEN = 14, REPAIR_OPEN = 15, REPAIRED = 16, GRADUATED = 17, WEEKLY_DONE = 18, EARLY = 19,
            HALFWAY = 20, MINI_DONE = 21, PROGRESS = 22, SLEEP_STREAK = 23, COINS = 24, LETTER = 25, COMEBACK = 26,
            GOAL = 27, HABIT_LEVEL = 28, CARD = 29, SPIN = 30, FREEZE_EARNED = 31, HERO_WEEK = 32;

    public final int type;
    public int value;
    public String text = "";
    public String extra = "";

    public Ev(int type, int value, String text) { this.type = type; this.value = value; this.text = text == null ? "" : text; }

    public static final class List extends ArrayList<Ev> {
        public Ev add(int type, int value, String text) { Ev e = new Ev(type, value, text); add(e); return e; }
        public boolean has(int type) { for (Ev e : this) if (e.type == type) return true; return false; }
        public int xp() { int s = 0; for (Ev e : this) if (e.type == XP) s += e.value; return s; }
    }
}
