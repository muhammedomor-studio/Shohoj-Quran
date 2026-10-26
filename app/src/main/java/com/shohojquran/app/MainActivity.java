package com.shohojquran.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private LinearLayout root, content;
    private boolean dark = true;
    private SharedPreferences prefs;
    private int bg, fg, card, accent;
    private JSONArray surahs;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        dark = prefs.getBoolean("dark", true);
        loadSurahMetadata();
        showHome();
    }

    private void loadSurahMetadata() {
        try (InputStream in = getAssets().open("surahs.json")) {
            byte[] b = new byte[in.available()]; in.read(b);
            surahs = new JSONArray(new String(b, StandardCharsets.UTF_8));
        } catch (Exception e) { surahs = new JSONArray(); }
    }

    private void colors() {
        bg = dark ? Color.rgb(5,8,7) : Color.rgb(250,248,240);
        fg = dark ? Color.WHITE : Color.rgb(24,31,28);
        card = dark ? Color.rgb(19,27,24) : Color.WHITE;
        accent = dark ? Color.rgb(20,190,137) : Color.rgb(0,130,91);
    }

    private void base(String title) {
        colors();
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(bg);
        TextView heading = new TextView(this); heading.setText(title); heading.setTextColor(fg); heading.setTextSize(25); heading.setTypeface(null, Typeface.BOLD); heading.setPadding(20,24,20,16);
        root.addView(heading);
        ScrollView scroll = new ScrollView(this); content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(14,4,14,24);
        scroll.addView(content); root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    }

    private TextView text(String s, int size) {
        TextView t = new TextView(this); t.setText(s); t.setTextColor(fg); t.setTextSize(size); t.setPadding(14,12,14,12); return t;
    }
    private Button button(String s, Runnable action) {
        Button b = new Button(this); b.setText(s); b.setTextColor(fg); b.setAllCaps(false); b.setOnClickListener(v -> action.run()); return b;
    }
    private void showHome() {
        base("সহজ কোরআন  |  Shohoj Quran");
        TextView intro = text("الْقُرْآنُ الْكَرِيمُ\n\nপবিত্র কোরআন সহজভাবে পড়ুন", 25); intro.setGravity(Gravity.CENTER); intro.setPadding(12,22,12,22); content.addView(intro);
        TextView note = text("প্রথম সংস্করণ\nএই অ্যাপের মূল কাঠামো তৈরি আছে। পূর্ণ আরবি আয়াত, বাংলা অনুবাদ ও অডিও ডেটা প্যাকেজ পরে যুক্ত করতে হবে।", 16); note.setBackgroundColor(card); content.addView(note);
        content.addView(button("সূরার তালিকা খুলুন", this::showSurahs));
        content.addView(button("ডাউনলোড ও সেটিংস", this::showSettings));
        content.addView(button(dark ? "লাইট মোড চালু করুন" : "ডার্ক মোড চালু করুন", () -> { dark = !dark; prefs.edit().putBoolean("dark",dark).apply(); showHome(); }));
    }
    private void showSurahs() {
        base("সূরার তালিকা");
        content.addView(text("এখানে আরবি সূরার নাম ও আয়াতসংখ্যা দেখানো হচ্ছে। সম্পূর্ণ আয়াতের ডেটা প্যাকেজ যোগ হলে পড়ার পেজে প্রদর্শিত হবে।", 15));
        for (int i=0; i<surahs.length(); i++) {
            JSONObject s = surahs.optJSONObject(i); if (s == null) continue;
            final int index=i;
            LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(8,6,8,6); row.setBackgroundColor(card);
            TextView label = text((i+1)+". "+s.optString("bn")+"   |   "+s.optString("ar")+"\nআয়াত: "+s.optInt("ayahs"), 19);
            row.addView(label); row.setOnClickListener(v -> showSurah(index));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,8); content.addView(row,lp);
        }
        content.addView(button("← হোম", this::showHome));
    }
    private void showSurah(int index) {
        JSONObject s = surahs.optJSONObject(index); if (s==null) return;
        base(s.optString("bn"));
        TextView arabic = text(s.optString("ar")+"\n\nبِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", 29); arabic.setGravity(Gravity.CENTER); arabic.setTextDirection(View.TEXT_DIRECTION_RTL); content.addView(arabic);
        TextView msg = text("এই সূরার সম্পূর্ণ আরবি আয়াত এখনো ডেটা প্যাকেজে যোগ করা হয়নি। ডেটা যুক্ত হলে অ্যাপ আপডেট না করেই রিপোজিটরির ডেটা প্যাকেজ থেকে পড়া যাবে।", 16); msg.setBackgroundColor(card); content.addView(msg);
        content.addView(button("← সূরার তালিকা", this::showSurahs));
    }
    private void showSettings() {
        base("সেটিংস ও ডাউনলোড");
        content.addView(text("ডাউনলোড ম্যানেজার প্রস্তুত করার জন্য এই সংস্করণে কাঠামো রাখা হয়েছে। ডেটা URL কনফিগার করার পর নিচের প্যাকেজগুলো সক্রিয় করা হবে।",16));
        content.addView(button("বাংলা অনুবাদ — ডেটা এখনো যুক্ত হয়নি", () -> Toast.makeText(this,"বাংলা অনুবাদ প্যাকেজ এখনো রিপোজিটরিতে যোগ করা হয়নি",Toast.LENGTH_LONG).show()));
        content.addView(button("মিশারি আল-আফাসি অডিও — ডেটা এখনো যুক্ত হয়নি", () -> Toast.makeText(this,"অডিও প্যাকেজ ও অনুমোদিত উৎসের URL পরে যুক্ত হবে",Toast.LENGTH_LONG).show()));
        content.addView(button(dark ? "লাইট মোড" : "ডার্ক মোড", () -> { dark=!dark; prefs.edit().putBoolean("dark",dark).apply(); showSettings(); }));
        content.addView(button("← হোম",this::showHome));
    }
}
