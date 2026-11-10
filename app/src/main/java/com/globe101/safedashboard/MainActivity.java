package com.globe101.safedashboard;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(5, 9, 7), PANEL = Color.rgb(14, 23, 18), GREEN = Color.rgb(57,255,20), WHITE = Color.rgb(230,240,232), MUTED = Color.rgb(145,165,150);
    private TextView connectionValue, speedValue, resultValue;
    private Button speedButton;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(BG);
        LinearLayout page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(dp(18),dp(24),dp(18),dp(28));
        scroll.addView(page);
        page.addView(text("GLOBE 101  /  RA_M1", 13, GREEN, true));
        TextView title = text("SAFE DASHBOARD", 27, WHITE, true); LinearLayout.LayoutParams tp = params(); tp.topMargin=dp(7); page.addView(title,tp);
        page.addView(text("Read-only tools • No firmware changes", 13, MUTED, false));
        page.addView(gap(20));
        LinearLayout device = card(page,"DEVICE PROFILE");
        addLine(device,"Equipment","The 101"); addLine(device,"Hardware","V1.0"); addLine(device,"Firmware","RA_M1_v5.2.00g"); addLine(device,"Router address","192.168.0.1");
        LinearLayout net = card(page,"CONNECTION");
        connectionValue = addLine(net,"Phone network","Checking…");
        addLine(net,"Modem signal","Not read — router API not configured");
        addLine(net,"4G / 5G","Shown by phone status bar; modem band not verified");
        LinearLayout speed = card(page,"INTERNET SPEED TEST");
        speedValue = addLine(speed,"Download","Not tested"); resultValue = addLine(speed,"Test status","Ready");
        speedButton = new Button(this); speedButton.setText("RUN DOWNLOAD TEST"); speedButton.setTextColor(Color.BLACK); speedButton.setBackground(round(GREEN,12));
        LinearLayout.LayoutParams bp=params(); bp.topMargin=dp(12); speed.addView(speedButton,bp); speedButton.setOnClickListener(v -> runSpeedTest());
        LinearLayout temp = card(page,"TEMPERATURE & COOLING");
        addLine(temp,"Modem temperature","Unavailable — no verified read-only sensor endpoint");
        addLine(temp,"Status","Do not assume a temperature reading");
        TextView tips = text("• Keep the modem upright with vents clear.\n• Keep it away from sunlight and enclosed spaces.\n• Use its original power adapter.\n• If unusually hot, disconnect power and let it cool before reuse.",14,WHITE,false);
        LinearLayout.LayoutParams tip=params(); tip.topMargin=dp(10); temp.addView(tips,tip);
        TextView footer=text("SAFETY: This app does not log in to the router, run shell commands, upload files, or flash firmware. Speed test uses a public HTTPS download endpoint and consumes mobile/Wi‑Fi data.",12,MUTED,false);
        LinearLayout.LayoutParams fp=params(); fp.topMargin=dp(18); page.addView(footer,fp);
        setContentView(scroll); refreshConnection();
    }
    private void refreshConnection() {
        try {
            ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            Network n=cm.getActiveNetwork(); NetworkCapabilities c=n==null?null:cm.getNetworkCapabilities(n); String s="Offline";
            if(c!=null){ if(c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) s="Connected via Wi‑Fi"; else if(c.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) s="Connected via mobile data"; else s="Connected"; }
            connectionValue.setText(s);
        } catch(Exception e){ connectionValue.setText("Unavailable"); }
    }
    private void runSpeedTest() {
        speedButton.setEnabled(false); speedButton.setText("TEST RUNNING…"); resultValue.setText("Downloading test data over HTTPS");
        new Thread(() -> {
            HttpURLConnection conn=null;
            try {
                URL url=new URL("https://speed.cloudflare.com/__down?bytes=5000000");
                conn=(HttpURLConnection)url.openConnection(); conn.setConnectTimeout(12000); conn.setReadTimeout(20000); conn.setRequestMethod("GET"); conn.connect();
                long start=System.nanoTime(), bytes=0; byte[] buf=new byte[8192]; java.io.InputStream in=conn.getInputStream(); int count;
                while((count=in.read(buf))!=-1) bytes+=count; in.close();
                double seconds=(System.nanoTime()-start)/1_000_000_000.0; double mbps=(bytes*8.0/1_000_000.0)/Math.max(seconds,0.001);
                final String value=String.format(java.util.Locale.US,"%.2f Mbps",mbps); final String detail=String.format(java.util.Locale.US,"Received %.2f MB in %.1f s",bytes/1_000_000.0,seconds);
                runOnUiThread(() -> { speedValue.setText(value); resultValue.setText(detail); speedButton.setEnabled(true); speedButton.setText("RUN AGAIN"); });
            } catch(Exception e) { runOnUiThread(() -> { resultValue.setText("Test failed: check internet connection"); speedButton.setEnabled(true); speedButton.setText("RETRY TEST"); }); }
            finally { if(conn!=null) conn.disconnect(); }
        }).start();
    }
    private LinearLayout card(LinearLayout parent,String heading){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(15),dp(14),dp(15),dp(15)); box.setBackground(round(PANEL,16));
        LinearLayout.LayoutParams p=params(); p.topMargin=dp(13); parent.addView(box,p); box.addView(text(heading,12,GREEN,true)); return box;
    }
    private TextView addLine(LinearLayout box,String label,String value){
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0,dp(9),0,dp(7));
        TextView l=text(label,13,MUTED,false); row.addView(l,new LinearLayout.LayoutParams(0,-2,1));
        TextView v=text(value,13,WHITE,true); v.setGravity(Gravity.RIGHT); row.addView(v,new LinearLayout.LayoutParams(0,-2,1)); box.addView(row); return v;
    }
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private View gap(int h){View v=new View(this);v.setLayoutParams(new LinearLayout.LayoutParams(1,dp(h)));return v;}
    private GradientDrawable round(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),Color.rgb(35,75,43));return d;}
    private LinearLayout.LayoutParams params(){return new LinearLayout.LayoutParams(-1,-2);}
    private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);}
}
