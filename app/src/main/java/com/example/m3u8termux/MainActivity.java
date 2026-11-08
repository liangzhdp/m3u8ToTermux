package com.example.m3u8termux;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String PERM = "com.termux.permission.RUN_COMMAND";
    private static final String PREFIX = "/data/data/com.termux/files/usr/bin/";
    private String url;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Uri data = getIntent().getData();
        if (data == null) { finish(); return; }
        url = data.toString();

        if (checkSelfPermission(PERM) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{PERM}, 1);
        } else {
            play();
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) {
            play();
        } else {
            Toast.makeText(this, "需要「在 Termux 中执行命令」权限", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void play() {
        // URL 作为 $1 传入，不拼进命令字符串，避免注入
        String script = PREFIX + "termux-wake-lock; "
                + PREFIX + "mpv --no-video --ao=opensles --no-terminal \"$1\"; "
                + PREFIX + "termux-wake-unlock";

        Intent i = new Intent("com.termux.RUN_COMMAND");
        i.setClassName("com.termux", "com.termux.app.RunCommandService");
        i.putExtra("com.termux.RUN_COMMAND_PATH", PREFIX + "bash");
        i.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{"-c", script, "_", url});
        i.putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home");
        i.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true);
        try {
            startForegroundService(i);
            Toast.makeText(this, "已交给 Termux 播放", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "启动失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        finish();
    }
}
