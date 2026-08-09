package com.devempire.fushakeyboard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    static final String PREF_API_KEY = "anthropic_api_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);

        TextView intro = new TextView(this);
        intro.setText("فصحى — فقاعة عائمة\n\n"
                + "فقاعة صغيرة تطلع فوق أي تطبيق (Discord مثلاً). اضغطها وهي تاخذ النص من "
                + "الحقل المفعّل حاليًا وتحوّله إلى فصحى صحيحة تلقائيًا، بدون نسخ أو لصق.\n\n"
                + "خطوات التفعيل بالترتيب:");
        intro.setTextSize(15);
        layout.addView(intro);

        Button overlayBtn = new Button(this);
        overlayBtn.setText("1) منح صلاحية النافذة العائمة");
        overlayBtn.setOnClickListener(v -> startActivity(new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()))));
        layout.addView(overlayBtn);

        Button accessibilityBtn = new Button(this);
        accessibilityBtn.setText("2) تفعيل خدمة الوصول (Accessibility)");
        accessibilityBtn.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        layout.addView(accessibilityBtn);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        TextView keyLabel = new TextView(this);
        keyLabel.setText("\n3) Anthropic API Key (اختياري، بدونه يُستخدم قاموس محلي بسيط):");
        layout.addView(keyLabel);

        EditText keyInput = new EditText(this);
        keyInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        keyInput.setText(prefs.getString(PREF_API_KEY, ""));
        keyInput.setHint("sk-ant-...");
        layout.addView(keyInput);

        Button saveKey = new Button(this);
        saveKey.setText("حفظ المفتاح");
        saveKey.setOnClickListener(v -> {
            prefs.edit().putString(PREF_API_KEY, keyInput.getText().toString().trim()).apply();
            Toast.makeText(this, "تم الحفظ", Toast.LENGTH_SHORT).show();
        });
        layout.addView(saveKey);

        Button startBubble = new Button(this);
        startBubble.setText("4) تشغيل الفقاعة");
        startBubble.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "امنح صلاحية النافذة العائمة أولاً (الخطوة 1)", Toast.LENGTH_SHORT).show();
                return;
            }
            startService(new Intent(this, BubbleService.class));
            Toast.makeText(this, "تم تشغيل الفقاعة، دوّر عليها فوق الشاشة", Toast.LENGTH_SHORT).show();
        });
        layout.addView(startBubble);

        setContentView(layout);
    }
}
