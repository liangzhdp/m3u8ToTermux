package com.example.m3u8termux;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class ControlReceiver extends BroadcastReceiver {
    static final String ACT_TOGGLE = "com.example.m3u8termux.TOGGLE";
    static final String ACT_STOP = "com.example.m3u8termux.STOP";
    static final String ACT_ENDED = "com.example.m3u8termux.ENDED";

    @Override
    public void onReceive(Context c, Intent intent) {
        String a = intent.getAction();
        if (a == null) return;
        SharedPreferences sp = c.getSharedPreferences("state", Context.MODE_PRIVATE);

        switch (a) {
            case ACT_TOGGLE: {
                Termux.sendMpv(c, "{\"command\":[\"cycle\",\"pause\"]}");
                boolean paused = !sp.getBoolean("paused", false);
                sp.edit().putBoolean("paused", paused).apply();
                Notif.show(c, sp.getString("url", ""), paused);
                break;
            }
            case ACT_STOP:
                Termux.stop(c);
                Notif.cancel(c);
                break;
            case ACT_ENDED: {
                // 只处理当前这次播放的结束，旧进程被新链接顶掉时发来的忽略
                String id = intent.getStringExtra("id");
                if (id != null && id.equals(sp.getString("play_id", ""))) {
                    Notif.cancel(c);
                }
                break;
            }
        }
    }
}
