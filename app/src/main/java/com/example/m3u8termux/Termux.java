package com.example.m3u8termux;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

final class Termux {
    static final String HOME = "/data/data/com.termux/files/home";
    static final String BIN = "/data/data/com.termux/files/usr/bin/";
    static final String SOCK = HOME + "/.mpv-sock";

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

    /** 通过 mpv 的 IPC socket 发一条 JSON 命令（需要 Termux 装 socat），2 秒超时 */
    static void sendMpv(Context c, String json) {
        run(c, BIN + "timeout",
                new String[]{"2", BIN + "bash", "-c",
                        "printf '%s\\n' \"$1\" | " + BIN + "socat - \"UNIX-CONNECT:$2\"",
                        "_", json, SOCK});
    }

    static void seek(Context c, int secs) {
        sendMpv(c, "{\"command\":[\"seek\"," + secs + ",\"relative\"]}");
    }

    static void pauseToggle(Context c) {
        sendMpv(c, "{\"command\":[\"cycle\",\"pause\"]}");
    }

    /** 先让 mpv 自己退出，1 秒后再 pkill mpv 兜底 */
    static void stop(Context c) {
        String script = "printf '%s\\n' '{\"command\":[\"quit\"]}' | " + BIN
                + "socat - \"UNIX-CONNECT:$1\"; sleep 1; " + BIN + "pkill mpv";
        run(c, BIN + "timeout", new String[]{"5", BIN + "bash", "-c", script, "_", SOCK});
    }
}
