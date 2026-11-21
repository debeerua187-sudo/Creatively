package com.andriescrisstel.counter;
import android.appwidget.*;import android.content.*;import android.os.SystemClock;import android.widget.RemoteViews;import java.time.*;
public class TogetherWidget extends AppWidgetProvider {
 public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids)update(c,m,id);}
 static void update(Context c,AppWidgetManager m,int id){
 long start=LocalDateTime.of(2026,9,19,20,46).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
 long elapsed=Math.max(0,System.currentTimeMillis()-start),days=elapsed/86400000L,base=SystemClock.elapsedRealtime()-elapsed;
 int heart=c.getSharedPreferences("style",0).getInt("heart",3),font=c.getSharedPreferences("style",0).getInt("font",0);
 String hearts=new String[]{"💛","💙","💚","💛💙💚"}[Math.min(heart,3)];
 RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget);
 v.setTextViewText(R.id.names,"Andries "+hearts+" Crisstel");
 v.setTextViewText(R.id.days,days+" days");
 v.setChronometer(R.id.clock,base,"%s",true);
 int tf=new int[]{0,1,3,0,4,2}[Math.min(font,5)];
 v.setInt(R.id.names,"setTypeface",tf);v.setInt(R.id.days,"setTypeface",tf);v.setInt(R.id.clock,"setTypeface",tf);
 m.updateAppWidget(id,v);
 }
}
