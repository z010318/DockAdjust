package com.example.dockadjust;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.SharedPreferences;

public final class MainActivity extends Activity {
    private static final int LIMIT = 120;
    private SeekBar slider;
    private EditText number;
    private TextView status;
    private boolean syncing;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        SharedPreferences prefs = getSharedPreferences("dock_settings", MODE_PRIVATE);
        float initial = prefs.getFloat("offset_dp", 0f);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 28, 24, 24);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("Dock 垂直位置调节");
        title.setTextSize(22);
        root.addView(title, match());

        TextView hint = new TextView(this);
        hint.setText("负数 = 向上；正数 = 向下。单位：dp，范围 -120 至 +120。保存后约 1 秒内生效；若未刷新，请重启桌面进程。");
        hint.setTextSize(14);
        hint.setPadding(0, 18, 0, 18);
        root.addView(hint, match());

        number = new EditText(this);
        number.setSingleLine(true);
        number.setInputType(8194);
        number.setText(String.valueOf(Math.round(initial)));
        number.setTextSize(20);
        root.addView(number, match());

        slider = new SeekBar(this);
        slider.setMax(LIMIT * 2);
        slider.setProgress(clamp(Math.round(initial)) + LIMIT);
        root.addView(slider, match());

        status = new TextView(this);
        status.setText("当前设置：" + Math.round(initial) + " dp");
        status.setTextSize(16);
        status.setPadding(0, 12, 0, 20);
        root.addView(status, match());

        Button save = new Button(this);
        save.setText("保存并应用");
        root.addView(save, match());
        Button reset = new Button(this);
        reset.setText("恢复默认（0 dp）");
        root.addView(reset, match());

        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar s) { }
            public void onStopTrackingTouch(SeekBar s) { }
            public void onProgressChanged(SeekBar s, int p, boolean user) {
                if (!user || syncing) return;
                int v = p - LIMIT;
                syncing = true; number.setText(String.valueOf(v)); syncing = false;
                status.setText("当前设置：" + v + " dp");
            }
        });
        number.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a) { }
            public void onTextChanged(CharSequence s,int st,int before,int count) {
                if (syncing) return;
                try {
                    int v = clamp(Integer.parseInt(s.toString()));
                    syncing = true; slider.setProgress(v + LIMIT); syncing = false;
                    status.setText("当前设置：" + v + " dp");
                } catch (NumberFormatException ignored) { }
            }
            public void afterTextChanged(Editable e) { }
        });
        save.setOnClickListener(v -> saveValue(prefs));
        reset.setOnClickListener(v -> { number.setText("0"); slider.setProgress(LIMIT); saveValue(prefs); });
        setContentView(root);
    }

    private void saveValue(SharedPreferences prefs) {
        int value;
        try { value = clamp(Integer.parseInt(number.getText().toString())); }
        catch (NumberFormatException e) { value = 0; }
        prefs.edit().putFloat("offset_dp", (float) value).commit();
        number.setText(String.valueOf(value));
        status.setText("已保存：" + value + " dp。若桌面未刷新，请重启 Launcher。");
    }
    private static int clamp(int n) { return Math.max(-LIMIT, Math.min(LIMIT, n)); }
    private LinearLayout.LayoutParams match() { return new LinearLayout.LayoutParams(-1, -2); }
}
