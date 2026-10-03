package com.ilker.rutin.sys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ilker.rutin.widget.Widgets;

/** Yeniden başlatma / saat değişimi / güncelleme sonrası alarmları yeniden kurar. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        Notifier.channels(c);
        Scheduler.reschedule(c, false);
        Widgets.updateAll(c);
    }
}
