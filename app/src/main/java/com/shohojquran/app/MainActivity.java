package com.shohojquran.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final String REPO = "https://github.com/muhammedomor-studio/Shohoj-Quran";
    private static final String DATA_REPO = "https://github.com/muhammedomor-studio/Shohoj-Quran-Data";
    private static final String DATA_MANIFEST_URL = "https://raw.githubusercontent.com/muhammedomor-studio/Shohoj-Quran-Data/main/manifest.json";
    private JSONArray downloadedSurahs;
    private boolean downloading = false;
    private LinearLayout root, content, nav;
    private boolean dark;
    private SharedPreferences prefs;
    private int bg, fg, muted, panel, accent, gold, stroke;
    private JSONArray surahs;
    private String currentPage = "home";
    private int selectedSurah = 0;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("shohoj_quran", MODE_PRIVATE);
        dark = prefs.getBoolean("dark", false);
        loadSurahs();
        loadDownloadedQuran();
        showPage("home");
    }

    private void loadSurahs() {
        try (InputStream in = getAssets().open("surahs.json")) {
            byte[] b = new byte[in.available()]; in.read(b);
            surahs = new JSONArray(new String(b, StandardCharsets.UTF_8));
        } catch (Exception e) { surahs = new JSONArray(); }
    }
    private void palette() {
        bg = dark ? Color.rgb(8,18,15) : Color.rgb(247,249,246);
        fg = dark ? Color.rgb(244,248,245) : Color.rgb(24,43,35);
        muted = dark ? Color.rgb(164,184,174) : Color.rgb(104,124,113);
        panel = dark ? Color.rgb(18,35,28) : Color.WHITE;
        accent = Color.rgb(13,119,82); gold = Color.rgb(207,164,70);
        stroke = dark ? Color.rgb(42,68,55) : Color.rgb(226,234,227);
    }
    private GradientDrawable shape(int color, int radius, int border) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius));
        if (border != 0) d.setStroke(dp(1), border); return d;
    }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }
    private LinearLayout vertical() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout horizontal() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    private TextView label(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        t.setGravity(Gravity.CENTER_VERTICAL); return t;
    }
    private void pad(View v, int l, int t, int r, int b) { v.setPadding(dp(l),dp(t),dp(r),dp(b)); }
    private LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h)); }
    private void gap(LinearLayout parent, int height) { View v = new View(this); parent.addView(v, lp(1,height)); }
    private TextView pill(String s) {
        TextView t = label(s,12,accent,true); pad(t,11,7,11,7); t.setBackground(shape(dark?Color.rgb(22,59,43):Color.rgb(229,244,234),30,0)); return t;
    }
    private LinearLayout card() {
        LinearLayout c = vertical(); pad(c,16,15,16,15); c.setBackground(shape(panel,20,stroke)); return c;
    }
    private void showPage(String page) {
        currentPage = page; palette();
        root = vertical(); root.setBackgroundColor(bg);
        getWindow().setStatusBarColor(bg); getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setSystemUiVisibility(dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        if (page.equals("donate")) { showDonation(); setContentView(root); return; }
        if (page.equals("reader")) { showReader(); setContentView(root); return; }
        LinearLayout top = horizontal(); pad(top,20,14,20,12);
        TextView brand = label("সহজ কোরআন",23,fg,true); top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView moon = label(dark ? "☀" : "☾",23,gold,true); moon.setGravity(Gravity.CENTER); moon.setBackground(shape(panel,40,stroke));
        LinearLayout.LayoutParams moonLp=lp(44,44); top.addView(moon,moonLp); moon.setOnClickListener(v->{dark=!dark;prefs.edit().putBoolean("dark",dark).apply();showPage(currentPage);});
        root.addView(top);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(false); content=vertical(); pad(content,18,4,18,22); scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        buildPage(page);
        buildNav(page);
    }
    private void buildPage(String page) {
        switch(page) {
            case "quran": showQuranList(); break;
            case "bookmarks": showBookmarks(); break;
            case "settings": showSettings(); break;
            default: showHome();
        }
    }
    private void buildNav(String page) {
        nav=horizontal(); nav.setGravity(Gravity.CENTER); nav.setBackgroundColor(panel); pad(nav,5,8,5,8);
        addNav("⌂","হোম","home",page); addNav("۞","কুরআন","quran",page); addNav("♡","বুকমার্ক","bookmarks",page); addNav("⚙","সেটিংস","settings",page);
        root.addView(nav,lp(-1,68));
    }
    private void addNav(String icon, String title, String page, String active) {
        LinearLayout item=vertical(); item.setGravity(Gravity.CENTER); int color=page.equals(active)?accent:muted;
        TextView i=label(icon,22,color,true); i.setGravity(Gravity.CENTER); TextView t=label(title,11,color,page.equals(active)); t.setGravity(Gravity.CENTER);
        item.addView(i); item.addView(t); nav.addView(item,new LinearLayout.LayoutParams(0,-1,1)); item.setOnClickListener(v->showPage(page));
    }
    private void showHome() {
        TextView eyebrow=label("আপনার প্রতিদিনের কোরআন সঙ্গী",13,muted,true); content.addView(eyebrow); gap(content,12);
        LinearLayout hero=vertical(); pad(hero,20,20,20,20);
        GradientDrawable gd=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(8,92,62),Color.rgb(5,49,36)}); gd.setCornerRadius(dp(24)); hero.setBackground(gd);
        LinearLayout row=horizontal(); LinearLayout left=vertical(); left.addView(label("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",20,Color.WHITE,true)); gap(left,10);
        TextView welcome=label("শান্তি খুঁজুন\nকোরআনের আলোয়",23,Color.WHITE,true); left.addView(welcome); gap(left,8); left.addView(label("সহজভাবে পড়ুন, বুঝুন ও শুনুন",13,Color.rgb(208,231,217),false));
        row.addView(left,new LinearLayout.LayoutParams(0,-2,1));
        ImageView logo=new ImageView(this); int id=getResources().getIdentifier("shohoj_logo","drawable",getPackageName()); if(id!=0) logo.setImageResource(id); logo.setScaleType(ImageView.ScaleType.FIT_CENTER); row.addView(logo,lp(94,94)); hero.addView(row); gap(hero,18);
        TextView continueBtn=label("কুরআন পড়া শুরু করুন     →",15,Color.rgb(17,55,39),true); continueBtn.setGravity(Gravity.CENTER); pad(continueBtn,12,13,12,13); continueBtn.setBackground(shape(Color.rgb(226,190,106),14,0)); hero.addView(continueBtn); continueBtn.setOnClickListener(v->showPage("quran")); content.addView(hero);
        gap(content,20); content.addView(sectionTitle("দ্রুত প্রবেশ","আপনার প্রয়োজনীয় অংশ")); gap(content,10);
        LinearLayout shortcuts=horizontal(); addShortcut(shortcuts,"📖","সূরার তালিকা","114টি সূরা",()->showPage("quran")); addShortcut(shortcuts,"♡","বুকমার্ক","সংরক্ষিত পাঠ",()->showPage("bookmarks")); addShortcut(shortcuts,"⚙","সেটিংস","অ্যাপ সাজান",()->showPage("settings")); content.addView(shortcuts);
        gap(content,20); content.addView(sectionTitle("আজকের স্মরণ","একটু সময় কোরআনের জন্য")); gap(content,10);
        LinearLayout reminder=card(); reminder.addView(label("প্রতিদিন অল্প হলেও নিয়মিত কোরআন পড়ার অভ্যাস গড়ে তুলুন।",16,fg,true)); gap(reminder,7); reminder.addView(label("আপনার শেষ পড়া সূরা দ্রুত খুলতে সূরার পাশে বুকমার্ক ব্যবহার করুন।",13,muted,false)); content.addView(reminder);
        gap(content,20); LinearLayout donation=horizontal(); pad(donation,16,14,16,14); donation.setBackground(shape(dark?Color.rgb(42,39,24):Color.rgb(255,247,225),18,0)); LinearLayout dtext=vertical(); dtext.addView(label("সদকায়ে জারিয়ার অংশ হোন",15,fg,true)); dtext.addView(label("সাহায্য ও দানের কিছু অনুপ্রেরণা",12,muted,false)); donation.addView(dtext,new LinearLayout.LayoutParams(0,-2,1)); TextView db=label("দান করুন  →",13,accent,true); donation.addView(db); donation.setOnClickListener(v->showPage("donate")); content.addView(donation);
    }
    private TextView sectionTitle(String title,String sub) { LinearLayout holder=vertical(); holder.addView(label(title,19,fg,true)); holder.addView(label(sub,12,muted,false)); TextView t=label(title+"\n"+sub,19,fg,true); return t; }
    private void addShortcut(LinearLayout parent,String icon,String title,String sub,Runnable run) {
        LinearLayout c=vertical(); c.setGravity(Gravity.CENTER); pad(c,5,13,5,13); c.setBackground(shape(panel,17,stroke));
        TextView ic=label(icon,23,accent,true); ic.setGravity(Gravity.CENTER); c.addView(ic); TextView tx=label(title,13,fg,true); tx.setGravity(Gravity.CENTER); c.addView(tx); TextView st=label(sub,10,muted,false); st.setGravity(Gravity.CENTER); c.addView(st);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(104),1); p.setMargins(dp(3),0,dp(3),0); parent.addView(c,p); c.setOnClickListener(v->run.run());
    }
    private void showQuranList() {
        content.addView(label("আল-কুরআন",24,fg,true)); gap(content,4); content.addView(label("সূরা খুঁজুন অথবা তালিকা থেকে নির্বাচন করুন",13,muted,false)); gap(content,14);
        EditText search=new EditText(this); search.setSingleLine(true); search.setTextColor(fg); search.setHintTextColor(muted); search.setHint("⌕  সূরার নাম লিখুন..."); search.setTextSize(15); pad(search,14,4,14,4); search.setBackground(shape(panel,14,stroke)); content.addView(search,lp(-1,52)); gap(content,12);
        LinearLayout list=vertical(); content.addView(list); Runnable refresh=()->{list.removeAllViews();String q=search.getText().toString().trim().toLowerCase(); for(int i=0;i<surahs.length();i++){JSONObject s=surahs.optJSONObject(i);if(s==null)continue;String bn=s.optString("bn"),ar=s.optString("ar");if(!q.isEmpty()&&!bn.toLowerCase().contains(q)&&!ar.contains(q)&&!String.valueOf(i+1).contains(q))continue;final int ix=i;
            LinearLayout row=horizontal();pad(row,13,12,13,12);row.setBackground(shape(panel,16,stroke));
            TextView number=label(String.valueOf(i+1),14,accent,true);number.setGravity(Gravity.CENTER);number.setBackground(shape(dark?Color.rgb(27,60,44):Color.rgb(232,244,235),12,0));row.addView(number,lp(40,40));
            LinearLayout names=vertical();pad(names,12,0,0,0);names.addView(label(bn,15,fg,true));names.addView(label("আয়াত: "+s.optInt("ayahs"),12,muted,false));row.addView(names,new LinearLayout.LayoutParams(0,-2,1));TextView arab=label(ar,23,gold,true);arab.setGravity(Gravity.CENTER);row.addView(arab);LinearLayout.LayoutParams rp=lp(-1,-2);rp.bottomMargin=dp(8);list.addView(row,rp);row.setOnClickListener(v->{selectedSurah=ix;showPage("reader");});
        }}; search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){refresh.run();}public void afterTextChanged(android.text.Editable e){}}); refresh.run();
    }
    private void showBookmarks() {
        content.addView(label("আপনার বুকমার্ক",24,fg,true)); gap(content,5); content.addView(label("পরে পড়ার জন্য সংরক্ষিত সূরা",13,muted,false)); gap(content,14);
        int saved=prefs.getInt("bookmark_surah",-1);
        if(saved<0 || saved>=surahs.length()) { LinearLayout c=card(); TextView icon=label("♡",34,gold,true);icon.setGravity(Gravity.CENTER);c.addView(icon);TextView msg=label("এখনো কোনো বুকমার্ক নেই",17,fg,true);msg.setGravity(Gravity.CENTER);c.addView(msg);TextView sub=label("সূরার তালিকা থেকে একটি সূরা খুলে ‘বুকমার্ক’ চাপুন।",13,muted,false);sub.setGravity(Gravity.CENTER);c.addView(sub);content.addView(c); }
        else { JSONObject s=surahs.optJSONObject(saved); if(s!=null){LinearLayout c=card();c.addView(label(s.optString("bn")+"  ·  "+s.optString("ar"),20,fg,true));gap(c,5);c.addView(label("আয়াত: "+s.optInt("ayahs"),13,muted,false));gap(c,10);c.addView(actionButton("পড়া চালিয়ে যান",()->{selectedSurah=saved;showPage("reader");}));gap(c,8);c.addView(actionButton("বুকমার্ক সরান",()->{prefs.edit().remove("bookmark_surah").apply();showPage("bookmarks");}));content.addView(c);} }
    }
    private void showReader() {
        JSONObject s=surahs.optJSONObject(selectedSurah); if(s==null){showPage("quran");return;}
        LinearLayout top=horizontal();pad(top,18,12,18,12);TextView back=label("‹  সূরার তালিকা",14,accent,true);top.addView(back,new LinearLayout.LayoutParams(0,-2,1));TextView save=label(prefs.getInt("bookmark_surah",-1)==selectedSurah?"♥":"♡",26,gold,true);top.addView(save);save.setOnClickListener(v->{if(prefs.getInt("bookmark_surah",-1)==selectedSurah)prefs.edit().remove("bookmark_surah").apply();else prefs.edit().putInt("bookmark_surah",selectedSurah).apply();showPage("reader");});back.setOnClickListener(v->showPage("quran"));root.addView(top);
        ScrollView scroll=new ScrollView(this);LinearLayout body=vertical();pad(body,18,10,18,24);scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout title=card();title.setGravity(Gravity.CENTER);title.addView(label(s.optString("bn"),23,fg,true));title.addView(label(s.optString("ar"),27,gold,true));title.addView(label("সূরা "+(selectedSurah+1)+"  •  আয়াত "+s.optInt("ayahs"),12,muted,false));body.addView(title);gap(body,18);
        JSONArray ayahs = getAyahsForSurah(selectedSurah + 1);
        if (ayahs == null || ayahs.length() == 0) {
            LinearLayout msg=card();msg.addView(label("আরবি আয়াতের ডেটা এখনো ডাউনলোড হয়নি",17,fg,true));gap(msg,7);msg.addView(label("ডেটা রিপোজিটরিতে যাচাইকৃত quran-ar.json যুক্ত করে সেটিংস থেকে ‘আরবি কুরআন ডাউনলোড’ চাপুন।",14,muted,false));gap(msg,12);msg.addView(actionButton("ডেটা ডাউনলোডের সেটিংস",()->showPage("settings")));body.addView(msg);
        } else {
            for (int i=0;i<ayahs.length();i++) {
                JSONObject a=ayahs.optJSONObject(i); if(a==null) continue;
                LinearLayout verse=card(); TextView arabic=label(a.optString("text"),27,fg,false); arabic.setGravity(Gravity.RIGHT); arabic.setTextDirection(View.TEXT_DIRECTION_RTL); arabic.setLineSpacing(dp(8),1.1f); verse.addView(arabic); gap(verse,8);
                TextView num=label("۞  "+a.optInt("number",i+1),12,gold,true); num.setGravity(Gravity.LEFT); verse.addView(num);
                LinearLayout.LayoutParams vp=lp(-1,-2);vp.bottomMargin=dp(10);body.addView(verse,vp);
            }
        }
    }
    private void showSettings() {
        content.addView(label("সেটিংস",24,fg,true));gap(content,4);content.addView(label("আপনার অভিজ্ঞতা নিজের মতো সাজিয়ে নিন",13,muted,false));gap(content,16);
        LinearLayout appearance=card();appearance.addView(label("চেহারা ও প্রদর্শন",16,fg,true));gap(appearance,8);appearance.addView(label("থিম",13,muted,false));gap(appearance,8);appearance.addView(actionButton(dark?"☀  লাইট মোড চালু করুন":"☾  ডার্ক মোড চালু করুন",()->{dark=!dark;prefs.edit().putBoolean("dark",dark).apply();showPage("settings");}));content.addView(appearance);gap(content,12);
        LinearLayout data=card();data.addView(label("কুরআন ডেটা",16,fg,true));gap(data,7);data.addView(label("ডেটা আলাদা GitHub রিপোজিটরিতে থাকবে। এই অ্যাপ সেই রিপোজিটরির manifest.json পড়ে আরবি পাঠ ডাউনলোড করে ফোনে অফলাইনে সংরক্ষণ করবে।",13,muted,false));gap(data,10);data.addView(actionButton(downloading?"ডাউনলোড হচ্ছে…":"আরবি কুরআন ডাউনলোড / আপডেট",()->downloadArabicData()));gap(data,7);data.addView(actionButton("ডেটা রিপোজিটরি খুলুন  ↗",()->openUrl(DATA_REPO)));gap(data,7);data.addView(actionButton("অ্যাপের সোর্স রিপোজিটরি খুলুন  ↗",()->openUrl(REPO)));content.addView(data);gap(content,12);
        LinearLayout links=card();links.addView(label("প্রজেক্ট ও অবদান",16,fg,true));gap(links,8);links.addView(actionButton("⌘  সোর্স কোড — GitHub",()->openUrl(REPO)));gap(links,7);links.addView(actionButton("♡  দান ও সহায়তা",()->showPage("donate")));gap(links,7);links.addView(actionButton("অ্যাপ সম্পর্কে",()->showAbout()));content.addView(links);gap(content,16);
        TextView ver=label("Shohoj Quran  •  Version 1.0.0\nসবার জন্য সহজ কোরআন পাঠ",12,muted,false);ver.setGravity(Gravity.CENTER);content.addView(ver);
    }
    private void showDonation() {
        LinearLayout top=horizontal();pad(top,18,18,18,12);TextView back=label("‹  ফিরে যান",15,accent,true);top.addView(back);back.setOnClickListener(v->showPage("home"));root.addView(top);
        ScrollView scroll=new ScrollView(this);LinearLayout body=vertical();pad(body,22,12,22,28);scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView icon=label("♡",52,gold,true);icon.setGravity(Gravity.CENTER);body.addView(icon);gap(body,8);TextView title=label("মানবতার পাশে দাঁড়ান",25,fg,true);title.setGravity(Gravity.CENTER);body.addView(title);gap(body,12);
        LinearLayout panel=card();panel.addView(label("আপনার সামর্থ্য অনুযায়ী কারও পাশে দাঁড়ান",18,accent,true));gap(panel,12);
        panel.addView(label("আপনার পরিচিত কিংবা অপরিচিত—এমন কোনো মানুষকে সাহায্য করুন, যার সত্যিই সাহায্যের প্রয়োজন। আপনার সামান্য সহযোগিতাও কারও জীবনে স্বস্তি ও আশার আলো হয়ে উঠতে পারে।",16,fg,false));gap(panel,14);
        panel.addView(label("আপনি চাইলে বিশ্বস্ত ও প্রয়োজনভিত্তিক উদ্যোগের মাধ্যমে মসজিদ, মাদ্রাসা, এতিমখানা কিংবা অসহায় মানুষের কল্যাণে দান করতে পারেন। দান করার আগে প্রতিষ্ঠান ও প্রয়োজনের সত্যতা যাচাই করে নিন।",15,muted,false));body.addView(panel);gap(body,14);
        LinearLayout note=card();note.addView(label("একটি আন্তরিক অনুরোধ",16,fg,true));gap(note,6);note.addView(label("এই অ্যাপ কোনো নির্দিষ্ট ব্যক্তি বা প্রতিষ্ঠানের কাছে অর্থ সংগ্রহ করছে না। আপনার এলাকার বিশ্বস্ত ব্যক্তি বা প্রতিষ্ঠানের মাধ্যমে সরাসরি সহায়তা পৌঁছে দিন।",13,muted,false));body.addView(note);gap(body,18);
        TextView done=label("আল্লাহ আপনার ভালো কাজ কবুল করুন",15,gold,true);done.setGravity(Gravity.CENTER);body.addView(done);
    }
    private File quranDataFile() { return new File(getFilesDir(), "quran-ar.json"); }
    private void loadDownloadedQuran() {
        try {
            File f=quranDataFile(); if(!f.exists()) { downloadedSurahs=null; return; }
            try(InputStream in=new FileInputStream(f); ByteArrayOutputStream out=new ByteArrayOutputStream()) { byte[] buf=new byte[8192]; int n; while((n=in.read(buf))!=-1) out.write(buf,0,n); JSONObject root=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8)); downloadedSurahs=root.optJSONArray("surahs"); }
        } catch(Exception e) { downloadedSurahs=null; }
    }
    private JSONArray getAyahsForSurah(int surahNo) {
        if(downloadedSurahs==null) return null;
        for(int i=0;i<downloadedSurahs.length();i++) { JSONObject s=downloadedSurahs.optJSONObject(i); if(s!=null && s.optInt("number")==surahNo) return s.optJSONArray("ayahs"); }
        return null;
    }
    private byte[] fetchBytes(String address) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection(); c.setConnectTimeout(15000); c.setReadTimeout(25000); c.setRequestProperty("User-Agent","ShohojQuran/1.0");
        try { int status=c.getResponseCode(); if(status<200||status>=300) throw new Exception("HTTP "+status); try(InputStream in=c.getInputStream(); ByteArrayOutputStream out=new ByteArrayOutputStream()) { byte[] buf=new byte[8192]; int n; while((n=in.read(buf))!=-1) out.write(buf,0,n); return out.toByteArray(); } } finally { c.disconnect(); }
    }
    private String sha256(byte[] data) throws Exception { MessageDigest md=MessageDigest.getInstance("SHA-256"); byte[] hash=md.digest(data); StringBuilder out=new StringBuilder(); for(byte b:hash) out.append(String.format(java.util.Locale.US,"%02x",b)); return out.toString(); }
    private void downloadArabicData() {
        if(downloading) { Toast.makeText(this,"ডাউনলোড চলছে",Toast.LENGTH_SHORT).show(); return; }
        downloading=true; showPage("settings");
        new Thread(() -> {
            String message; boolean ok=false;
            try {
                JSONObject manifest=new JSONObject(new String(fetchBytes(DATA_MANIFEST_URL),StandardCharsets.UTF_8));
                JSONObject file=manifest.getJSONObject("files").getJSONObject("quran_ar");
                String url=file.getString("url"); if(!url.startsWith("http://")&&!url.startsWith("https://")) url="https://raw.githubusercontent.com/muhammedomor-studio/Shohoj-Quran-Data/main/"+url.replaceFirst("^/", "");
                byte[] bytes=fetchBytes(url); String expected=file.optString("sha256","").trim();
                if(!expected.isEmpty()&&!sha256(bytes).equalsIgnoreCase(expected)) throw new Exception("SHA-256 যাচাই মেলেনি");
                JSONObject parsed=new JSONObject(new String(bytes,StandardCharsets.UTF_8)); JSONArray list=parsed.optJSONArray("surahs");
                if(list==null||list.length()!=114) throw new Exception("ডেটা ফাইলে ১১৪টি সূরা পাওয়া যায়নি");
                int totalAyahs=0; for(int i=0;i<list.length();i++){JSONObject surah=list.optJSONObject(i);if(surah==null||surah.optInt("number")!=i+1||surah.optJSONArray("ayahs")==null)throw new Exception("ডেটার কাঠামো সঠিক নয়"); JSONArray verseList=surah.optJSONArray("ayahs"); if(verseList.length()==0) throw new Exception("সূরা "+(i+1)+"-এর আয়াতের ডেটা খালি"); totalAyahs+=verseList.length(); for(int j=0;j<verseList.length();j++){JSONObject verse=verseList.optJSONObject(j); if(verse==null||verse.optString("text").trim().isEmpty()) throw new Exception("আয়াতের টেক্সট অনুপস্থিত");}} if(totalAyahs!=6236) throw new Exception("আয়াতের মোট সংখ্যা ৬২৩৬ নয়; ডেটা অসম্পূর্ণ");
                try(FileOutputStream out=new FileOutputStream(quranDataFile())) { out.write(bytes); out.flush(); }
                downloadedSurahs=list; ok=true; message="আরবি কুরআনের ডেটা ডাউনলোড ও যাচাই সম্পন্ন হয়েছে। এখন অফলাইনে পড়তে পারবেন।";
            } catch(Exception e) { message="ডাউনলোড হয়নি: "+e.getMessage()+"\nGitHub-এর data রিপোজিটরিতে manifest.json ও quran-ar.json আছে কি না দেখুন।"; }
            final boolean success=ok; final String result=message;
            runOnUiThread(() -> { downloading=false; showPage("settings"); new android.app.AlertDialog.Builder(this).setTitle(success?"ডাউনলোড সম্পন্ন":"ডেটা ডাউনলোড সমস্যা").setMessage(result).setPositiveButton("ঠিক আছে",(d,w)->{if(success)showPage("quran");}).show(); });
        }).start();
    }
    private void showAbout() {
        new android.app.AlertDialog.Builder(this).setTitle("সহজ কোরআন সম্পর্কে").setMessage("সহজ কোরআন একটি সরল, বিজ্ঞাপনমুক্ত কোরআন পাঠের অভিজ্ঞতা তৈরির উদ্যোগ।\n\nসোর্স কোড উন্মুক্ত GitHub রিপোজিটরিতে দেখা যাবে।\n\nমনে রাখবেন: এই বিল্ডে সম্পূর্ণ কুরআন পাঠ, বাংলা অনুবাদ, অডিও ও ডেটা ডাউনলোডার এখনো অন্তর্ভুক্ত নয়।").setPositiveButton("GitHub খুলুন",(d,w)->openUrl(REPO)).setNegativeButton("বন্ধ",null).show();
    }
    private TextView actionButton(String title,Runnable action) {
        TextView b=label(title,14,fg,true);pad(b,14,13,14,13);b.setBackground(shape(dark?Color.rgb(24,48,37):Color.rgb(241,247,241),13,stroke));b.setOnClickListener(v->action.run());return b;
    }
    private void openUrl(String url) { try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch(Exception e) { Toast.makeText(this,"লিংক খোলা যায়নি",Toast.LENGTH_SHORT).show(); } }
    @Override public void onBackPressed() { if(currentPage.equals("donate")||currentPage.equals("reader")) showPage(currentPage.equals("donate")?"home":"quran"); else if(!currentPage.equals("home")) showPage("home"); else super.onBackPressed(); }
}
