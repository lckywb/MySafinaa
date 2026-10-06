package id.safina.app;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;

import java.util.*;

public class ExposureMonitorService extends Service {
    Handler h=new Handler(Looper.getMainLooper()); WindowManager wm; View bubble; boolean shown=false; int thresholdSeconds=15*60; long lastTick=0; String currentPkg="";
    Set<String> monitored=new HashSet<>(Arrays.asList("com.instagram.android","com.zhiliaoapp.musically","com.google.android.youtube"));
    Runnable loop=new Runnable(){public void run(){tick();h.postDelayed(this,5000);}};
    @Override public void onCreate(){super.onCreate();wm=(WindowManager)getSystemService(WINDOW_SERVICE);h.post(loop);}
    void tick(){if(!Settings.canDrawOverlays(this))return;String pkg=foregroundPackage();long now=System.currentTimeMillis();if(monitored.contains(pkg)&&!pkg.equals(getPackageName())){if(lastTick>0){int add=(int)Math.min(10,(now-lastTick)/1000);if(add>0){android.content.SharedPreferences sp=getSharedPreferences("safina",MODE_PRIVATE);int sec=sp.getInt("exposureSeconds",0)+add;sp.edit().putInt("exposureSeconds",sec).apply();if(sec>=thresholdSeconds)showBubble();}}}lastTick=now;currentPkg=pkg;}
    String foregroundPackage(){try{UsageStatsManager usm=(UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);long end=System.currentTimeMillis(),start=end-10000;UsageEvents ev=usm.queryEvents(start,end);UsageEvents.Event e=new UsageEvents.Event();String last="";while(ev.hasNextEvent()){ev.getNextEvent(e);if(e.getEventType()==UsageEvents.Event.MOVE_TO_FOREGROUND){last=e.getPackageName();}}return last;}catch(Exception e){return "";}}
    void showBubble(){if(shown)return;shown=true;TextView b=new TextView(this);b.setText("⚓\nSAFINA");b.setTextColor(Color.WHITE);b.setTextSize(11);b.setGravity(Gravity.CENTER);b.setBackgroundColor(Color.rgb(42,167,161));int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;WindowManager.LayoutParams p=new WindowManager.LayoutParams(72,72,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);p.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;p.x=18;p.y=0;wm.addView(b,p);bubble=b;b.setOnClickListener(v->expand(p));}
    void expand(WindowManager.LayoutParams p){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(20,18,20,18);box.setBackgroundColor(Color.WHITE);TextView t=new TextView(this);t.setText("Apakah aktivitasmu sekarang masih sesuai dengan tujuan awalmu?");t.setTextSize(16);t.setTextColor(Color.rgb(23,35,52));box.addView(t);String[] opts={"Masih sesuai","Saya sudah menemukan yang dicari","Saya terdistraksi","Kembali ke tujuan","Pause","Tutup"};for(String o:opts){Button b=new Button(this);b.setText(o);b.setAllCaps(false);box.addView(b);b.setOnClickListener(v->{if(o.equals("Pause")){Intent i=new Intent(this,MainActivity.class);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}removeOverlay();});}int type=Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;WindowManager.LayoutParams q=new WindowManager.LayoutParams(-1,-2,type,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT);q.gravity=Gravity.CENTER;removeOverlay();wm.addView(box,q);bubble=box;shown=true;}
    void removeOverlay(){if(bubble!=null){try{wm.removeView(bubble);}catch(Exception ignored){}bubble=null;}shown=false;}
    @Override public int onStartCommand(Intent i,int flags,int id){return START_STICKY;}
    @Override public void onDestroy(){h.removeCallbacks(loop);removeOverlay();super.onDestroy();}
    @Override public android.os.IBinder onBind(Intent i){return null;}
}
