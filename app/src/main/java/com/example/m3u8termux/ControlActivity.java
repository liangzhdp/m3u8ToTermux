package com.example.m3u8termux;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** 点通知栏弹出的简易控制面板：快退/快进/暂停/停止 */
public class ControlActivity extends Activity {
    private Button pauseBtn;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("state", Context.MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView title = new TextView(this);
        title.setText("mpv 控制");
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dp(8));
        root.addView(title);

        root.addView(row(seekBtn("−5分", -300), seekBtn("−1分", -60), seekBtn("−10秒", -10)));
        root.addView(row(seekBtn("+10秒", 10), seekBtn("+1分", 60), seekBtn("+5分", 300)));

        pauseBtn = button("", v -> togglePause());
        Button stop = button("停止", v -> {
            Termux.stop(this);
            Notif.cancel(this);
            finish();
        });
        root.addView(row(pauseBtn, stop));

        setContentView(root);
        refreshPauseLabel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pauseBtn != null) refreshPauseLabel();
    }

    private void togglePause() {
        Termux.pauseToggle(this);
        boolean paused = !sp.getBoolean("paused", false);
        sp.edit().putBoolean("paused", paused).apply();
        Notif.show(this, sp.getString("url", ""), paused);
        refreshPauseLabel();
    }

    private void refreshPauseLabel() {
        pauseBtn.setText(sp.getBoolean("paused", false) ? "继续" : "暂停");
    }

    private Button seekBtn(String text, int secs) {
        return button(text, v -> Termux.seek(this, secs));
    }

    private Button button(String text, View.OnClickListener l) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setOnClickListener(l);
        btn.setLayoutParams(new LinearLayout.LayoutParams(0, dp(56), 1f));
        return btn;
    }

    private LinearLayout row(Button... btns) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        for (Button x : btns) r.addView(x);
        return r;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
