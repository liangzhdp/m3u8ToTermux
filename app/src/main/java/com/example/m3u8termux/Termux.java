package com.example.m3u8termux;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

final class Termux {
    static final String HOME = "/data/data/com.termux/files/home";
    static final String BIN = "/data/data/com.termux/files/usr/bin/";
    static final String FIFO = HOME + "/.mpv-fifo";

    private Termux() {}

    static boolean run(Context c, String path, String[] args) {
        Intent i = new Intent("com.termux.RUN_COMMAND");
        i.setClassName("com.termux", "com.termux.app.RunCommandService");
        i.putExtra("com.termux.RUN_COMMAND_PATH", path);
        i.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", args);
        i.putExtra("com.termux.RUN_COMMAND_WORKDIR", HOME);
        i.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true);
        try {
            c.startForegroundService(i);
            return true;
        } catch (Exception e) {
            Toast.makeText(c, "启动 Termux 失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /** 往 mpv 的 input-file FIFO 写一条命令，2 秒内没人读就放弃（mpv 已退出时） */
    static void sendMpv(Context c, String cmd) {
        run(c, BIN + "timeout",
                new String[]{"2", BIN + "bash", "-c", "echo \"$1\" > " + FIFO, "_", cmd});
    }

    static void stop(Context c) {
        run(c, BIN + "pkill", new String[]{"-x", "mpv"});
    }
}
