package com.example.m3u8termux;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PERM_TERMUX = "com.termux.permission.RUN_COMMAND";

    // 这些后缀当作音频/播放列表，交给 mpv；其余一律转给浏览器
    private static final String[] AUDIO_EXT = {
            ".m3u8", ".m3u", ".pls", ".m4a", ".mp3", ".aac", ".flac", ".ogg", ".opus", ".wav"};

    // 转发网页时优先使用的浏览器（按顺序），可自行增删
    private static final String[] BROWSERS = {
            "com.android.chrome", "com.brave.browser", "org.mozilla.firefox",
            "com.microsoft.emmx", "com.sec.android.app.sbrowser", "com.vivaldi.browser",
            "com.opera.browser", "com.duckduckgo.mobile.android"};

    private static final String SCRIPT = String.join("\n",
            "exec >" + Termux.HOME + "/m3u8termux.log 2>&1",
            "set -x",
            "S=" + Termux.SOCK,
            "B=" + Termux.BIN,
            "$B/pkill -x mpv; sleep 0.5",
            "rm -f \"$S\"",
            "$B/termux-wake-lock",
            "$B/mpv --no-video --ao=opensles --no-terminal --log-file=" + Termux.HOME
                    + "/mpv.log --input-ipc-server=\"$S\" \"$1\"",
            "$B/pgrep -x mpv >/dev/null || $B/termux-wake-unlock",
            "$B/am broadcast --user 0 -n com.example.m3u8termux/.ControlReceiver"
                    + " -a com.example.m3u8termux.ENDED --es id \"$2\" >/dev/null 2>&1");

    private Uri uri;
    private String url;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        uri = getIntent().getData();
        if (uri == null) { finish(); return; }
        url = uri.toString();

        if (!isAudio(uri)) {
            openInBrowser(uri);
            finish();
            return;
        }

        List<String> need = new ArrayList<>();
        if (checkSelfPermission(PERM_TERMUX) != PackageManager.PERMISSION_GRANTED)
            need.add(PERM_TERMUX);
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED)
            need.add(Manifest.permission.POST_NOTIFICATIONS);

        if (need.isEmpty()) play();
        else requestPermissions(need.toArray(new String[0]), 1);
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        if (checkSelfPermission(PERM_TERMUX) == PackageManager.PERMISSION_GRANTED) {
            play();
        } else {
            Toast.makeText(this, "需要「在 Termux 中执行命令」权限", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private static boolean isAudio(Uri u) {
        String p = u.getPath();
        if (p == null) return false;
        p = p.toLowerCase(Locale.ROOT);
        for (String e : AUDIO_EXT) if (p.endsWith(e)) return true;
        return false;
    }

    private void openInBrowser(Uri u) {
        Intent probe = new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"))
                .addCategory(Intent.CATEGORY_BROWSABLE);
        List<ResolveInfo> list = getPackageManager()
                .queryIntentActivities(probe, PackageManager.MATCH_ALL);

        String chosen = null;
        for (String known : BROWSERS) {
            for (ResolveInfo ri : list)
                if (ri.activityInfo.packageName.equals(known)) { chosen = known; break; }
            if (chosen != null) break;
        }
        if (chosen == null) {
            for (ResolveInfo ri : list) {
                String pkg = ri.activityInfo.packageName;
                // 排除自己和 Linksheet，避免又绕回来
                if (!pkg.equals(getPackageName()) && !pkg.toLowerCase(Locale.ROOT).contains("linksheet")) {
                    chosen = pkg;
                    break;
                }
            }
        }

        Intent open = new Intent(Intent.ACTION_VIEW, u).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (chosen != null) open.setPackage(chosen);
        try {
            startActivity(open);
        } catch (Exception e) {
            Toast.makeText(this, "找不到可用的浏览器", Toast.LENGTH_LONG).show();
        }
    }

    private void play() {
        String id = String.valueOf(System.currentTimeMillis());
        SharedPreferences sp = getSharedPreferences("state", MODE_PRIVATE);
        sp.edit().putString("play_id", id).putString("url", url).putBoolean("paused", false).apply();

        if (Termux.run(this, Termux.BIN + "bash", new String[]{"-c", SCRIPT, "_", url, id})) {
            Notif.show(this, url, false);
            Toast.makeText(this, "已交给 Termux 播放", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
