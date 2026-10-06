package id.safina.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    static final int NAVY = Color.rgb(8,25,45), NAVY2=Color.rgb(14,41,69), TEAL=Color.rgb(42,167,161);
    static final int OFF=Color.rgb(247,249,252), DARK=Color.rgb(23,35,52), MUTED=Color.rgb(102,112,133);
    LinearLayout root, content, bottom; TextView title, subtitle; SharedPreferences sp;
    String goal=""; long sessionStart=0; int currentTab=0;
    final Handler handler = new Handler(Looper.getMainLooper());
    final int[] tabIds={100,101,102,103};

    @Override public void onCreate(Bundle b){ super.onCreate(b); sp=getSharedPreferences("safina",MODE_PRIVATE); goal=sp.getString("goal",""); buildShell(); showHome(); requestNotificationPermission(); }

    void buildShell(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(OFF);
        LinearLayout head=new LinearLayout(this); head.setOrientation(LinearLayout.VERTICAL); head.setPadding(24,18,24,14); head.setBackgroundColor(NAVY);
        title=tv("SAFINA",26,Color.WHITE); subtitle=tv("Ruang untuk memilih di tengah banjir informasi",13,Color.rgb(210,225,239)); head.addView(title); head.addView(subtitle);
        root.addView(head,new LinearLayout.LayoutParams(-1,Wrap(86)));
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(18,18,18,8); ScrollView scroll=new ScrollView(this); scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        bottom=new LinearLayout(this); bottom.setOrientation(LinearLayout.HORIZONTAL); bottom.setBackgroundColor(Color.WHITE); root.addView(bottom,new LinearLayout.LayoutParams(-1,Wrap(64)));
        String[] labels={"Home","Cek","Report","Edukasi"}; for(int i=0;i<labels.length;i++){ final int n=i; TextView x=tv(labels[i],12,DARK); x.setGravity(Gravity.CENTER); x.setPadding(4,4,4,4); x.setId(tabIds[i]); x.setOnClickListener(v->{currentTab=n; if(n==0)showHome(); else if(n==1)showCheck(); else if(n==2)showReport(); else showEducation();}); bottom.addView(x,new LinearLayout.LayoutParams(0,-1,1)); }
        setContentView(root);
    }
    int Wrap(int x){return (int)(x*getResources().getDisplayMetrics().density);}
    TextView tv(String s,float size,int color){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setFontFeatureSettings("kern"); return t; }
    Button btn(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(13); b.setAllCaps(false); return b; }
    LinearLayout card(){ LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(18,16,18,16); c.setBackgroundColor(Color.WHITE); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,0,0,14); c.setLayoutParams(p); return c; }
    void clear(){content.removeAllViews();}
    void heading(String h,String sub){ TextView a=tv(h,23,DARK); a.setTypeface(null,1); content.addView(a); if(sub!=null){TextView b=tv(sub,13,MUTED); b.setPadding(0,5,0,16); content.addView(b);} }
    TextView body(String s){ TextView t=tv(s,14,DARK); t.setPadding(0,5,0,8); return t; }
    void addButton(LinearLayout c,String text,View.OnClickListener l){ Button b=btn(text); b.setOnClickListener(l); c.addView(b,new LinearLayout.LayoutParams(-1,Wrap(48))); }

    void showHome(){ clear(); heading("Beranda","Tujuan → paparan → cek → pause → refleksi");
        LinearLayout g=card(); TextView gt=tv("⚓  Epistemic Goal Anchor",17,DARK); gt.setTypeface(null,1); g.addView(gt); g.addView(body(goal.isEmpty()?"Belum ada tujuan aktif.":goal)); addButton(g,goal.isEmpty()?"Set tujuan":"Ubah tujuan",v->goalDialog()); content.addView(g);
        LinearLayout s=card(); s.addView(tv("Sesi aktivitas",17,DARK)); if(sessionStart==0) s.addView(body("Belum ada sesi aktif. Mulai sesi agar SAFINA menghubungkan aktivitasmu dengan tujuan.")); else s.addView(body("Sesi aktif sejak "+time(sessionStart))); addButton(s,sessionStart==0?"Mulai Activity Session":"Akhiri sesi",v->{if(sessionStart==0){sessionStart=System.currentTimeMillis();sp.edit().putLong("sessionStart",sessionStart).apply();toast("Sesi dimulai");}else{sessionStart=0;sp.edit().remove("sessionStart").apply();toast("Sesi diakhiri");}showHome();}); content.addView(s);
        LinearLayout quick=card(); quick.addView(tv("Ruang untuk memilih",17,DARK)); quick.addView(body("SAFINA tidak mengambil keputusan dari kamu. Ia membantu mengatur paparan dan memberi ruang untuk memeriksa, berhenti, lalu memilih.")); addButton(quick,"SAFINA Cek",v->showCheck()); addButton(quick,"SAFINA Pause",v->showPause()); addButton(quick,"Exposure Control",v->showExposure()); content.addView(quick);
        LinearLayout shield=card(); shield.addView(tv("🛡 SAFINA Shield",17,DARK)); shield.addView(body("Periksa risiko teknis sebuah URL tanpa otomatis memutuskan bahwa isinya benar atau salah.")); addButton(shield,"Periksa URL",v->showShield()); content.addView(shield);
    }
    void goalDialog(){ final EditText e=new EditText(this); e.setHint("Contoh: Saya ingin mengetahui apakah beasiswa X benar-benar tersedia."); e.setText(goal); e.setSingleLine(false); e.setMinLines(3); new AlertDialog.Builder(this).setTitle("⚓ Tetapkan tujuan").setMessage("Buat tujuan epistemik, bukan target produktivitas.").setView(e).setNegativeButton("Batal",null).setPositiveButton("Simpan",(d,w)->{String x=e.getText().toString().trim(); if(!x.isEmpty()){goal=x;sp.edit().putString("goal",x).apply();toast("Tujuan disimpan");showHome();}}).show(); }

    void showCheck(){ clear(); heading("SAFINA Cek","Triage klaim: status + alasan + langkah berikutnya"); LinearLayout c=card(); addButton(c,"📷 Pilih screenshot / gambar",v->pickImage()); addButton(c,"✍ Masukkan teks klaim",v->textClaimDialog()); addButton(c,"🔗 Periksa URL",v->urlCheckDialog()); content.addView(c);
        LinearLayout q=card(); q.addView(tv("Cek Nanti",17,DARK)); int n=sp.getInt("queue",0); q.addView(body(n+" item tersimpan untuk diperiksa nanti.")); addButton(q,"Lihat ringkasan",v->toast("Antrian tersimpan: "+n+" item")); content.addView(q);
        LinearLayout r=card(); r.addView(tv("Prinsip SAFINA Cek",17,DARK)); r.addView(body("SAFINA bukan mesin vonis hoaks. Jika bukti belum cukup, status tetap 'Belum Cukup Data' atau 'Perlu Verifikasi'.")); content.addView(r); }

    void textClaimDialog(){ LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL); EditText e=new EditText(this);e.setHint("Tulis klaim yang ingin diperiksa");e.setMinLines(4);box.addView(e); new AlertDialog.Builder(this).setTitle("Pilih klaim").setView(box).setNegativeButton("Batal",null).setPositiveButton("Cek",(d,w)->triage(e.getText().toString(),"Teks pengguna")).show(); }
    void urlCheckDialog(){ EditText e=new EditText(this); e.setHint("https://contoh.com"); new AlertDialog.Builder(this).setTitle("Cek URL").setView(e).setNegativeButton("Batal",null).setPositiveButton("Periksa",(d,w)->{String u=e.getText().toString().trim(); showUrlResult(u);}).show(); }
    void triage(String claim,String source){ claim=claim.trim(); if(claim.isEmpty()){toast("Klaim masih kosong");return;} String low=claim.toLowerCase(Locale.ROOT); String status="⚪ Belum Cukup Data"; String reason="Klaim belum memiliki bukti primer yang dapat diperiksa dari input ini."; String next="Cari sumber primer, cek tanggal, dan bandingkan minimal dua sumber."; if(low.contains("hadiah")||low.contains("transfer")||low.contains("otp")||low.contains("pin")||low.contains("klik link")){status="🔴 Risiko Tinggi";reason="Klaim mengandung pola yang umum digunakan dalam penipuan atau permintaan data sensitif.";next="Jangan berikan OTP/PIN dan verifikasi melalui kanal resmi.";} else if(low.contains("resmi")||low.contains("pemerintah")||low.contains("beasiswa")){status="🟡 Perlu Verifikasi";reason="Klaim menyebut otoritas/program yang seharusnya dapat diverifikasi melalui sumber primer.";next="Cari pengumuman resmi dari institusi yang disebut.";} else if(low.length()>25){status="🟡 Perlu Verifikasi";reason="Ada klaim substantif, tetapi input belum menyertakan bukti atau sumber pembanding.";next="Pisahkan klaim menjadi pernyataan kecil lalu cek sumber primer.";} saveAction("check"); resultDialog("SAFINA Cek",claim,status,reason,next,source); }
    void pickImage(){ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,77); }
    @Override protected void onActivityResult(int req,int res,Intent data){super.onActivityResult(req,res,data); if(req==77&&res==RESULT_OK&&data!=null){try{Uri u=data.getData(); InputImage img=InputImage.fromFilePath(this,u); TextRecognizer rec=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS); toast("Membaca teks…"); rec.process(img).addOnSuccessListener(x->{String t=x.getText(); if(t==null||t.trim().isEmpty()) toast("Teks tidak terbaca. Coba gambar yang lebih jelas."); else {final EditText e=new EditText(this);e.setText(t);e.setMinLines(6);new AlertDialog.Builder(this).setTitle("Hasil OCR — pilih/ubah klaim").setView(e).setNegativeButton("Batal",null).setPositiveButton("Cek",(d,w)->triage(e.getText().toString(),"OCR screenshot")).show();}}).addOnFailureListener(e->toast("OCR gagal: "+e.getMessage()));}catch(Exception e){toast("Gambar tidak dapat dibaca");}}}
    void resultDialog(String title,String claim,String status,String reason,String next,String source){ LinearLayout box=card(); box.addView(tv(status,20,DARK));box.addView(body("Klaim: "+claim));box.addView(body("Alasan: "+reason));box.addView(body("Langkah berikutnya: "+next));box.addView(body("Sumber input: "+source)); addButton(box,"Simpan untuk Cek Nanti",v->{sp.edit().putInt("queue",sp.getInt("queue",0)+1).apply();toast("Disimpan ke Cek Nanti");}); new AlertDialog.Builder(this).setTitle(title).setView(box).setPositiveButton("Selesai",null).show(); }

    void showPause(){ clear(); heading("⚓ SAFINA Pause","Berikan ruang untuk mengambil keputusan."); LinearLayout c=card(); c.addView(body("Tujuan aktif: "+(goal.isEmpty()?"Belum ditetapkan":goal))); TextView timer=tv("03:00",48,DARK); timer.setGravity(Gravity.CENTER);c.addView(timer,new LinearLayout.LayoutParams(-1,Wrap(100))); addButton(c,"Mulai Pause 3 menit",v->startPause(timer,c)); content.addView(c); }
    void startPause(TextView timer,LinearLayout c){ final long end=System.currentTimeMillis()+180000; c.removeViews(2,c.getChildCount()-2); addButton(c,"Batalkan",v->showPause()); Runnable r=new Runnable(){public void run(){long left=end-System.currentTimeMillis(); if(left<=0){timer.setText("00:00"); saveAction("pause"); new AlertDialog.Builder(MainActivity.this).setTitle("Pause selesai").setMessage("Apa yang ingin kamu lakukan sekarang?").setItems(new String[]{"Kembali ke tujuan","Lanjutkan aktivitas","Akhiri sesi"},(d,w)->{if(w==0)toast("Kembali ke tujuan: "+(goal.isEmpty()?"tetapkan tujuan dulu":goal)); else if(w==1)toast("Kamu memilih melanjutkan dengan sadar."); else {sessionStart=0;sp.edit().remove("sessionStart").apply();toast("Sesi diakhiri");}}).show();}else{timer.setText(String.format(Locale.getDefault(),"%02d:%02d",left/60000,(left/1000)%60));handler.postDelayed(this,1000);}}};handler.post(r); }

    void showReport(){ clear(); heading("Epistemic Report","Yang diamati ≠ otomatis sama dengan yang kamu alami."); int checks=sp.getInt("checks",0), pauses=sp.getInt("pauses",0), saves=sp.getInt("queue",0); LinearLayout a=card();a.addView(tv("Aktivitas epistemik",17,DARK));a.addView(body("Cek: "+checks+"  •  Pause: "+pauses+"  •  Cek Nanti: "+saves));a.addView(body("Status ini berasal dari aktivitas yang dicatat SAFINA, bukan skor kesehatan epistemik."));content.addView(a);
        LinearLayout b=card();b.addView(tv("Pola paparan",17,DARK));int exposure=sp.getInt("exposureSeconds",0);String st=exposure<900?"🟢 Paparan relatif terkendali":exposure<3600?"🟡 Pola perlu perhatian":"🟠 Paparan tinggi";b.addView(body(st));b.addView(body("Waktu terpantau pada aplikasi yang dipilih: "+(exposure/60)+" menit. Ini adalah proxy aktivitas aplikasi, bukan bukti bahwa seluruhnya merupakan paparan epistemik."));content.addView(b);
        LinearLayout c=card();c.addView(tv("Refleksi",17,DARK));c.addView(body("Apakah pola ini membantu tujuanmu?"));addButton(c,"Ya",v->{sp.edit().putString("reflection","Ya").apply();toast("Refleksi tersimpan");});addButton(c,"Tidak",v->{sp.edit().putString("reflection","Tidak").apply();toast("Refleksi tersimpan");});addButton(c,"Tidak yakin",v->{sp.edit().putString("reflection","Tidak yakin").apply();toast("Refleksi tersimpan");});content.addView(c);
        LinearLayout d=card();d.addView(tv("Adapt",17,DARK));d.addView(body("Pilih penyesuaian strategi berikutnya."));String[] opts={"Lebih selektif","Simpan dulu sebelum cek","Cek sumber","Gunakan Pause","Tidak mengubah apa pun"};for(String o:opts)addButton(d,o,v->{sp.edit().putString("adapt",((Button)v).getText().toString()).apply();toast("Adaptasi disimpan");});content.addView(d);
    }

    void showExposure(){clear();heading("Exposure Control","Mengatur paparan tanpa mengambil alih keputusan.");LinearLayout c=card();c.addView(body("SAFINA memantau waktu penggunaan aplikasi yang kamu pilih sebagai konteks. Ia tidak membaca DM, password, atau isi layar."));addButton(c,"Buka akses Usage Access",v->{startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));});addButton(c,"Buka izin Overlay",v->{try{startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));}catch(Exception e){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));}});addButton(c,"Mulai monitor",v->{if(!hasUsageAccess()){toast("Aktifkan Usage Access dulu");return;}if(!Settings.canDrawOverlays(this)){toast("Aktifkan izin Overlay dulu");return;}startService(new Intent(this,ExposureMonitorService.class));toast("Exposure monitor berjalan");});addButton(c,"Hentikan monitor",v->{stopService(new Intent(this,ExposureMonitorService.class));toast("Monitor dihentikan");});content.addView(c);
        LinearLayout p=card();p.addView(tv("Check-in",17,DARK));p.addView(body("Setelah ambang waktu tercapai, SAFINA menampilkan bubble. Tidak ada auto-close atau auto-block."));content.addView(p); }
    boolean hasUsageAccess(){android.app.AppOpsManager a=(android.app.AppOpsManager)getSystemService(APP_OPS_SERVICE);int mode=a.checkOpNoThrow("android:get_usage_stats",android.os.Process.myUid(),getPackageName());return mode==android.app.AppOpsManager.MODE_ALLOWED;}

    void showShield(){clear();heading("SAFINA Shield","Pemeriksaan risiko teknis URL.");LinearLayout c=card();EditText e=new EditText(this);e.setHint("https://contoh.com");c.addView(e);addButton(c,"Periksa",v->showUrlResult(e.getText().toString().trim()));content.addView(c);}
    void showUrlResult(String url){String u=url.trim();if(u.isEmpty()){toast("URL kosong");return;}String low=u.toLowerCase(Locale.ROOT);boolean https=low.startsWith("https://");boolean ip=Pattern.compile("https?://(\\d{1,3}\\.){3}\\d{1,3}").matcher(low).find();boolean puny=low.contains("xn--");boolean badChars=low.contains("@")||low.contains("%40");int risk=(https?0:1)+(ip?2:0)+(puny?2:0)+(badChars?2:0);String status=risk>=3?"🔴 Risiko teknis tinggi":risk>0?"🟡 Perlu perhatian":"🟢 Tidak ada indikator teknis dasar yang ditemukan";String reason="Indikator: "+(https?"HTTPS aktif":"bukan HTTPS")+", "+(ip?"host berupa IP, ":"")+(puny?"punycode, ":"")+(badChars?"karakter URL mencurigakan.":"tidak ada karakter URL mencurigakan.");new AlertDialog.Builder(this).setTitle("SAFINA Shield").setMessage(status+"\n\n"+reason+"\n\nCatatan: pemeriksaan teknis bukan bukti bahwa isi situs benar atau aman sepenuhnya.").setPositiveButton("Buka di browser",(d,w)->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception ignored){}}).setNegativeButton("Tutup",null).show();}

    void showEducation(){clear();heading("SAFINA Edukasi","Microlearning untuk membangun kebiasaan epistemik.");String[] titles={"Tidak Semua Informasi Harus Kamu Buka","Kenali Kapan Kamu Mulai Terdistraksi","Bedakan Klaim dan Fakta","Siapa Sumber Informasinya?","Tidak Semua Waktu Luang Harus Diisi Scrolling","Berikan Ruang untuk Berhenti"};int[] vids={R.raw.edu1,R.raw.edu2,R.raw.edu3,R.raw.edu4,R.raw.edu5,R.raw.edu6};for(int i=0;i<titles.length;i++){final int idx=i;LinearLayout c=card();c.addView(tv(titles[i],16,DARK));c.addView(body("30–60 detik • Cermat Berselancar"));addButton(c,"▶ Tonton",v->playEducation(idx,titles[idx],vids[idx]));content.addView(c);}}
    void playEducation(int idx,String title,int raw){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);VideoView video=new VideoView(this);video.setVideoURI(Uri.parse("android.resource://"+getPackageName()+"/"+raw));box.addView(video,new LinearLayout.LayoutParams(-1,Wrap(220)));box.addView(body("Apa yang bisa kamu lakukan?\nPilih satu tindakan kecil setelah menonton."));String[] acts={"Simpan dulu sebelum cek","Kembali ke tujuan","Cari sumber primer","Gunakan Pause","Batasi paparan berikutnya"};for(String a:acts){Button b=btn(a);b.setOnClickListener(v->{sp.edit().putInt("eduActions",sp.getInt("eduActions",0)+1).apply();toast("Tindakan disimpan: "+a);});box.addView(b);}new AlertDialog.Builder(this).setTitle(title).setView(box).setPositiveButton("Selesai",null).show();video.setOnPreparedListener(mp->{mp.setLooping(false);video.start();});}

    void saveAction(String type){if(type.equals("check"))sp.edit().putInt("checks",sp.getInt("checks",0)+1).apply();if(type.equals("pause"))sp.edit().putInt("pauses",sp.getInt("pauses",0)+1).apply();}
    String time(long t){return new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(t));}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    void requestNotificationPermission(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},88);}
}
