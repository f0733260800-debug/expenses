package com.example.expenses;
import android.app.*;import android.content.*;import android.database.*;import android.database.sqlite.*;import android.graphics.Color;import android.os.*;import android.text.InputType;import android.view.*;import android.widget.*;import java.io.*;import java.util.*;

public class MainActivity extends Activity {
 static final long EP=-1373427L;
 static final String[] NM={"תשרי","חשוון","כסלו","טבת","שבט","אדר א׳","אדר","ניסן","אייר","סיוון","תמוז","אב","אלול"};
 static final int[] BL={30,29,30,29,30,30,29,30,29,30,29,30,29};
 static final String[] ON={"","א","ב","ג","ד","ה","ו","ז","ח","ט"},TN={"","י","כ","ל"};
 SQLiteDatabase db;SharedPreferences sp;int cy,ci;boolean daily;long today;AlertDialog cur;
 ArrayList<Row> rows=new ArrayList<Row>();LinearLayout body;TextView title,sum;Button mb,tg;
 static class Row{long id,rd;int type,th,rec,hd;String name;double amt;}

 static long el(long y){long m=(235*y-234)/19,d=m*29+(12084+13753*m)/25920;if((3*(d+1))%7<3)d++;return d;}
 static long ny(long y){long a=el(y-1),b=el(y),c=el(y+1);return EP+b+(c-b==356?2:(b-a==382?1:0));}
 static boolean lp(long y){return (7*y+1)%19<7;}
 static int cnt(int y){return lp(y)?13:12;}
 static int id(int y,int i){return (!lp(y)&&i>=5)?i+1:i;}
 static int ml(int y,int i){int k=id(y,i),l=BL[k];long yl=ny(y+1)-ny(y);if(k==1&&yl%10==5)l=30;if(k==2&&yl%10==3)l=29;return l;}
 static String mn(int y,int i){int k=id(y,i);return (k==6&&lp(y))?"אדר ב׳":NM[k];}
 static long ms(int y,int i){long s=ny(y);for(int j=0;j<i;j++)s+=ml(y,j);return s;}
 static int[] toH(long rd){int y=(int)((rd-EP)*98496/35975351)+1;while(ny(y+1)<=rd)y++;while(ny(y)>rd)y--;long o=rd-ny(y);int i=0;while(o>=ml(y,i)){o-=ml(y,i);i++;}return new int[]{y,i,(int)o+1};}
 static String gr(long rd){GregorianCalendar c=new GregorianCalendar(TimeZone.getTimeZone("UTC"));c.setTimeInMillis((rd-719163L)*86400000L);return c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1);}
 static String hd(int d){return d==15?"טו":d==16?"טז":TN[d/10]+ON[d%10];}
 static String f(double v){return v==Math.floor(v)?String.valueOf((long)v):String.format(Locale.US,"%.2f",v);}
 static long todayRd(){GregorianCalendar l=new GregorianCalendar();GregorianCalendar u=new GregorianCalendar(TimeZone.getTimeZone("UTC"));u.clear();u.set(l.get(Calendar.YEAR),l.get(Calendar.MONTH),l.get(Calendar.DAY_OF_MONTH));return u.getTimeInMillis()/86400000L+719163L;}

 protected void onCreate(Bundle b){
  super.onCreate(b);sp=getSharedPreferences("p",0);
  db=new SQLiteOpenHelper(this,"x.db",null,1){
   public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE t(id INTEGER PRIMARY KEY AUTOINCREMENT,rd INTEGER,type INTEGER,name TEXT,amt REAL,th INTEGER,rec INTEGER)");}
   public void onUpgrade(SQLiteDatabase d,int a,int c){}}.getWritableDatabase();
  today=todayRd();int[] h=toH(today);cy=h[0];ci=h[1];buildUi();render();
 }
 void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 Button btn(String t,View.OnClickListener l){Button x=new Button(this);x.setText(t);x.setOnClickListener(l);return x;}
 LinearLayout.LayoutParams w1(){return new LinearLayout.LayoutParams(0,-2,1);}
 void buildUi(){
  LinearLayout r=new LinearLayout(this);r.setOrientation(1);
  LinearLayout top=new LinearLayout(this);
  top.addView(btn("◄",v->step(1)));
  title=new TextView(this);title.setTextSize(20);title.setGravity(Gravity.CENTER);top.addView(title,w1());
  top.addView(btn("►",v->step(-1)));r.addView(top);
  LinearLayout t2=new LinearLayout(this);
  tg=btn("יומי",v->{daily=!daily;render();});t2.addView(tg,w1());
  t2.addView(btn("היום",v->{int[] h=toH(today);cy=h[0];ci=h[1];render();}),w1());
  t2.addView(btn("גיבוי",v->backup()),w1());
  t2.addView(btn("שחזור",v->new AlertDialog.Builder(this).setMessage("לשחזר מהגיבוי? הנתונים הנוכחיים יוחלפו.").setPositiveButton("כן",(x,y)->restore()).setNegativeButton("לא",null).show()),w1());
  r.addView(t2);
  ScrollView sv=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(1);sv.addView(body);r.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
  sum=new TextView(this);sum.setPadding(8,8,8,8);sum.setTextSize(15);sum.setGravity(Gravity.CENTER);r.addView(sum);
  mb=btn("",v->maDlg());mb.setTextColor(Color.WHITE);mb.setBackgroundColor(Color.rgb(200,0,0));r.addView(mb);
  setContentView(r);
 }
 void step(int d){if(d>0){ci++;if(ci>=cnt(cy)){cy++;ci=0;}}else{ci--;if(ci<0){cy--;ci=cnt(cy)-1;}}render();}
 void load(){rows.clear();Cursor c=db.rawQuery("SELECT id,rd,type,name,amt,th,rec FROM t ORDER BY id",null);
  while(c.moveToNext()){Row r=new Row();r.id=c.getLong(0);r.rd=c.getLong(1);r.type=c.getInt(2);r.name=c.getString(3);r.amt=c.getDouble(4);r.th=c.getInt(5);r.rec=c.getInt(6);r.hd=toH(r.rd)[2];rows.add(r);}c.close();}
 boolean on(Row r,long rd,int d,int len){return r.rec==0?r.rd==rd:(rd>=r.rd&&(r.hd==d||(r.hd>len&&d==len)));}

 void render(){
  load();int len=ml(cy,ci);long m0=ms(cy,ci);
  title.setText(mn(cy,ci)+" "+cy);tg.setText(daily?"חודשי":"יומי");
  ArrayList<ArrayList<Row>> per=new ArrayList<ArrayList<Row>>();per.add(null);double in=0,ex=0;double[] net=new double[len+1];
  for(int d=1;d<=len;d++){ArrayList<Row> l=new ArrayList<Row>();
   for(Row r:rows)if(on(r,m0+d-1,d,len)){l.add(r);if(r.type==1){in+=r.amt;net[d]+=r.amt;}else{ex+=r.amt;net[d]-=r.amt;}}per.add(l);}
  body.removeAllViews();
  if(!daily){
   String[] wd={"א","ב","ג","ד","ה","ו","ש"};LinearLayout hr=new LinearLayout(this);
   for(int c=6;c>=0;c--){TextView t=new TextView(this);t.setText(wd[c]);t.setGravity(Gravity.CENTER);hr.addView(t,w1());}body.addView(hr);
   int first=(int)(m0%7);
   for(int w=0;w*7-first+1<=len;w++){LinearLayout row=new LinearLayout(this);
    for(int c=6;c>=0;c--){int dd=w*7+c-first+1;View v;
     if(dd<1||dd>len)v=new View(this);
     else{Button x=new Button(this);x.setTextSize(10);x.setMinHeight(0);x.setMinWidth(0);x.setPadding(0,4,0,4);
      x.setText(hd(dd)+"\n"+gr(m0+dd-1)+(net[dd]!=0?"\n"+f(net[dd]):""));
      if(m0+dd-1==today)x.setBackgroundColor(0xFFFFEE88);
      x.setOnClickListener(q->dayDlg(dd));v=x;}
     row.addView(v,w1());}
    body.addView(row);}
  }else{
   for(int d=1;d<=len;d++){final int dd=d;TextView t=new TextView(this);t.setTextSize(15);t.setPadding(12,10,12,10);
    StringBuilder sb=new StringBuilder(hd(d)+" "+mn(cy,ci)+" ("+gr(m0+d-1)+")");
    for(Row r:per.get(d))sb.append("\n  • ").append(r.name).append(": ").append(r.type==1?"+":"-").append(f(r.amt));
    t.setText(sb.toString());t.setBackgroundColor(m0+d-1==today?0xFFFFEE88:(d%2==0?0xFFF0F0F0:0xFFFFFFFF));
    t.setOnClickListener(q->dayDlg(dd));body.addView(t);}
  }
  sum.setText("הכנסות: "+f(in)+"   הוצאות: "+f(ex)+"   יתרה: "+f(in-ex));
  mb.setText("לא מעושר: "+f(Math.max(maaser(),0))+" ₪  (לחץ לעדכון)");
 }
 void dayDlg(int d){
  long rd=ms(cy,ci)+d-1;int len=ml(cy,ci);
  LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(16,8,16,8);
  for(final Row r:rows)if(on(r,rd,d,len)){TextView t=new TextView(this);t.setTextSize(17);t.setPadding(0,8,0,8);
   t.setText((r.type==1?"הכנסה: ":"הוצאה: ")+r.name+" — "+f(r.amt)+(r.th==1?"  ✓ מעושר":"")+(r.rec==1?"  (קבוע)":""));
   t.setOnLongClickListener(v->{del(r);return true;});l.addView(t);}
  TextView h=new TextView(this);h.setTextSize(12);h.setText(l.getChildCount()==0?"אין רישומים ליום זה":"לחיצה ארוכה על שורה - מחיקה");l.addView(h);
  cur=new AlertDialog.Builder(this).setTitle(hd(d)+" "+mn(cy,ci)+" ("+gr(rd)+")").setView(l)
   .setPositiveButton("הוסף",(x,y)->addDlg(rd)).setNegativeButton("סגור",null).show();
 }
 void del(Row r){new AlertDialog.Builder(this).setMessage("למחוק?"+(r.rec==1?" (רישום קבוע יימחק מכל החודשים)":"")).setPositiveButton("מחק",(x,y)->{db.delete("t","id=?",new String[]{""+r.id});if(cur!=null)cur.dismiss();render();}).setNegativeButton("ביטול",null).show();}
 CheckBox cb(String s){CheckBox c=new CheckBox(this);c.setText(s);return c;}
 void addDlg(long rd){
  LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(16,8,16,8);
  RadioGroup g=new RadioGroup(this);g.setOrientation(0);RadioButton e=new RadioButton(this),inc=new RadioButton(this);
  e.setText("הוצאה");inc.setText("הכנסה");g.addView(e);g.addView(inc);e.setChecked(true);
  EditText n=new EditText(this);n.setHint("שם ההוצאה / ההכנסה");
  EditText a=new EditText(this);a.setHint("סכום");a.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
  CheckBox t=cb("מעושר"),rc=cb("קבוע (חודשי)");t.setVisibility(View.GONE);
  inc.setOnCheckedChangeListener((x,ch)->t.setVisibility(ch?View.VISIBLE:View.GONE));
  l.addView(g);l.addView(n);l.addView(a);l.addView(t);l.addView(rc);
  new AlertDialog.Builder(this).setTitle("הוספה").setView(l).setPositiveButton("שמור",(x,y)->{
   double v;try{v=Double.parseDouble(a.getText().toString());}catch(Exception z){toast("סכום לא תקין");return;}
   ContentValues cv=new ContentValues();cv.put("rd",rd);cv.put("type",inc.isChecked()?1:0);cv.put("name",n.getText().toString());cv.put("amt",v);
   cv.put("th",inc.isChecked()&&t.isChecked()?1:0);cv.put("rec",rc.isChecked()?1:0);db.insert("t",null,cv);render();
  }).setNegativeButton("ביטול",null).show();
 }
 double paid(){try{return Double.parseDouble(sp.getString("paid","0"));}catch(Exception e){return 0;}}
 double maaser(){double t=0;int[] th=toH(today);
  for(Row r:rows){if(r.type!=1||r.th==1)continue;
   if(r.rec==0){if(r.rd<=today)t+=r.amt;}
   else{int[] h=toH(r.rd);int y=h[0],i=h[1];
    while(y<th[0]||(y==th[0]&&i<=th[1])){long o=ms(y,i)+Math.min(r.hd,ml(y,i))-1;if(o<=today&&o>=r.rd)t+=r.amt;i++;if(i>=cnt(y)){y++;i=0;}}}}
  return t*0.1-paid();}
 void maDlg(){
  EditText e=new EditText(this);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setText(f(paid()));
  new AlertDialog.Builder(this).setTitle("מעשר").setMessage("10% מההכנסות שלא סומנו 'מעושר': "+f(maaser()+paid())+"\nסך מה שעישרת בנוסף (עדכן):").setView(e)
   .setPositiveButton("שמור",(x,y)->{try{sp.edit().putString("paid",""+Double.parseDouble(e.getText().toString())).apply();}catch(Exception z){}render();}).setNegativeButton("ביטול",null).show();
 }
 File bf(){File s=new File("/storage/extSdCard");if(!s.exists()||!s.canWrite())s=Environment.getExternalStorageDirectory();return new File(s,"expenses_backup.csv");}
 void backup(){try{PrintWriter w=new PrintWriter(new OutputStreamWriter(new FileOutputStream(bf()),"UTF-8"));
  w.println("P,"+paid());for(Row r:rows)w.println("R,"+r.rd+","+r.type+","+r.name.replace(',',';').replace('\n',' ')+","+r.amt+","+r.th+","+r.rec);
  w.close();toast("נשמר: "+bf().getPath());}catch(Exception e){toast("הגיבוי נכשל");}}
 void restore(){try{BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(bf()),"UTF-8"));
  db.delete("t",null,null);String s;
  while((s=br.readLine())!=null){String[] p=s.split(",");
   if(p[0].equals("P"))sp.edit().putString("paid",p[1]).apply();
   else if(p.length>=7){ContentValues cv=new ContentValues();cv.put("rd",Long.parseLong(p[1]));cv.put("type",Integer.parseInt(p[2]));cv.put("name",p[3]);cv.put("amt",Double.parseDouble(p[4]));cv.put("th",Integer.parseInt(p[5]));cv.put("rec",Integer.parseInt(p[6]));db.insert("t",null,cv);}}
  br.close();render();toast("שוחזר");}catch(Exception e){toast("השחזור נכשל");}}
}
