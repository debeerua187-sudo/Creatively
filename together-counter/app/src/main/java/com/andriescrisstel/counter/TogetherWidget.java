package com.andriescrisstel.counter;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.SystemClock;
import android.widget.RemoteViews;
import java.util.concurrent.TimeUnit;

public class TogetherWidget extends AppWidgetProvider {
    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) update(context, manager, id);
    }

    static void update(Context c, AppWidgetManager manager, int id) {
        android.content.SharedPreferences p = c.getSharedPreferences("style", 0);
        long start = p.getLong("start", defaultStart());
        long elapsed = Math.max(0L, System.currentTimeMillis() - start);
        long days = TimeUnit.MILLISECONDS.toDays(elapsed);
        long remainder = elapsed - TimeUnit.DAYS.toMillis(days);
        long base = SystemClock.elapsedRealtime() - remainder;

        int heart = Math.max(0, Math.min(3, p.getInt("heart", 3)));
        int font = Math.max(0, Math.min(5, p.getInt("font", 0)));
        String hearts = new String[]{"💛", "💙", "💚", "💛💙💚"}[heart];
        String first = p.getString("first", "Andries");
        String second = p.getString("second", "Crisstel");
        int[] styles = {R.style.WidgetFontDefault, R.style.WidgetFontSerif,
            R.style.WidgetFontMono, R.style.WidgetFontSans, R.style.WidgetFontCasual,
            R.style.WidgetFontCursive};

        RemoteViews views = new RemoteViews(c.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.names, first + " " + hearts + " " + second);
        views.setTextViewText(R.id.days, days + " DAYS");
        views.setChronometer(R.id.clock, base, "%s", true);
        int style = styles[font];
        views.setInt(R.id.names, "setTextAppearance", style);
        views.setInt(R.id.days, "setTextAppearance", style);
        views.setInt(R.id.clock, "setTextAppearance", style);
        manager.updateAppWidget(id, views);
    }

    private static long defaultStart() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(2026, java.util.Calendar.SEPTEMBER, 19, 20, 46, 0);
        c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}