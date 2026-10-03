package com.ilker.rutin.sys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Zincirleme alarm: tetiklenince durumu yeniden değerlendirir ve bir sonraki alarmı kurar. */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        Notifier.channels(c);
        Scheduler.reschedule(c, true);
    }
}
