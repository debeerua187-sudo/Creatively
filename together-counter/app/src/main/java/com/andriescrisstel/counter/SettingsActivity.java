package com.andriescrisstel.counter;
import android.app.*;import android.appwidget.*;import android.content.*;import android.os.*;import android.widget.*;import android.graphics.Color;
public class SettingsActivity extends Activity {
 public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.settings);
 Spinner f=findViewById(R.id.font),h=findViewById(R.id.heartColor);
 f.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Default","Serif","Monospace","Sans Serif","Casual","Cursive"}));
 h.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Yellow 💛","Blue 💙","Green 💚","All three 💛💙💚"}));
 findViewById(R.id.save).setOnClickListener(v->{getSharedPreferences("style",0).edit().putInt("font",f.getSelectedItemPosition()).putInt("heart",h.getSelectedItemPosition()).apply();int id=getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);if(id!=AppWidgetManager.INVALID_APPWIDGET_ID){TogetherWidget.update(this,AppWidgetManager.getInstance(this),id);setResult(RESULT_OK,new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id));}finish();});
 }}
