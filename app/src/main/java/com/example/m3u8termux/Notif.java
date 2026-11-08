package com.example.m3u8termux;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;

final class Notif {
    private static final String CHANNEL = "play";
    private static final int ID = 1;

    private Notif() {}

    private static PendingIntent pi(Context c, String action, int req) {
        Intent i = new Intent(c, ControlReceiver.class).setAction(action);
        return PendingIntent.getBroadcast(c, req, i,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Notification.Action action(Context c, int icon, String title, PendingIntent p) {
        return new Notification.Action.Builder(Icon.createWithResource(c, icon), title, p).build();
    }

    static void show(Context c, String url, boolean paused) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);
        nm.createNotificationChannel(
                new NotificationChannel(CHANNEL, "播放控制", NotificationManager.IMPORTANCE_LOW));

        Notification.Action toggle = paused
                ? action(c, android.R.drawable.ic_media_play, "继续",
                        pi(c, ControlReceiver.ACT_TOGGLE, 1))
                : action(c, android.R.drawable.ic_media_pause, "暂停",
                        pi(c, ControlReceiver.ACT_TOGGLE, 1));
        Notification.Action stop = action(c, android.R.drawable.ic_menu_close_clear_cancel, "停止",
                pi(c, ControlReceiver.ACT_STOP, 2));

        Notification n = new Notification.Builder(c, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(paused ? "mpv 已暂停" : "mpv 播放中")
                .setContentText(url)
                .setContentIntent(PendingIntent.getActivity(c, 3,
                        new Intent(c, ControlActivity.class),
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .addAction(toggle)
                .addAction(stop)
                .build();
        nm.notify(ID, n);
    }

    static void cancel(Context c) {
        c.getSystemService(NotificationManager.class).cancel(ID);
    }
}
