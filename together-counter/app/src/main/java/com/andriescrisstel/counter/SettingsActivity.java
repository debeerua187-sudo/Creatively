package com.andriescrisstel.counter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SettingsActivity extends Activity {
    private final Calendar start = Calendar.getInstance();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.settings);

        EditText first = findViewById(R.id.firstName);
        EditText second = findViewById(R.id.secondName);
        Button dateButton = findViewById(R.id.startDate);
        Button timeButton = findViewById(R.id.startTime);
        Spinner font = findViewById(R.id.font);
        Spinner hearts = findViewById(R.id.heartColor);

        String[] fonts = {"Default", "Serif", "Monospace", "Sans Serif", "Casual", "Cursive"};
        String[] heartOptions = {"Yellow 💛", "Blue 💙", "Green 💚", "All three 💛💙💚"};
        font.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, fonts));
        hearts.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, heartOptions));

        android.content.SharedPreferences prefs = getSharedPreferences("style", 0);
        first.setText(prefs.getString("first", "Andries"));
        second.setText(prefs.getString("second", "Crisstel"));
        start.setTimeInMillis(prefs.getLong("start", defaultStart()));
        font.setSelection(prefs.getInt("font", 0));
        hearts.setSelection(prefs.getInt("heart", 3));
        refreshDate(dateButton);
        refreshTime(timeButton);

        dateButton.setOnClickListener(v -> new DatePickerDialog(this, (view, year, month, day) -> {
            start.set(Calendar.YEAR, year); start.set(Calendar.MONTH, month); start.set(Calendar.DAY_OF_MONTH, day);
            refreshDate(dateButton);
        }, start.get(Calendar.YEAR), start.get(Calendar.MONTH), start.get(Calendar.DAY_OF_MONTH)).show());

        timeButton.setOnClickListener(v -> new TimePickerDialog(this, (view, hour, minute) -> {
            start.set(Calendar.HOUR_OF_DAY, hour); start.set(Calendar.MINUTE, minute); start.set(Calendar.SECOND, 0);
            refreshTime(timeButton);
        }, start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE), false).show());

        findViewById(R.id.save).setOnClickListener(v -> {
            String a = first.getText().toString().trim();
            String b = second.getText().toString().trim();
            if (a.isEmpty()) first.setError("Enter the first name");
            if (b.isEmpty()) second.setError("Enter the second name");
            if (a.isEmpty() || b.isEmpty()) return;
            prefs.edit().putString("first", a).putString("second", b)
                .putLong("start", start.getTimeInMillis())
                .putInt("font", font.getSelectedItemPosition())
                .putInt("heart", hearts.getSelectedItemPosition()).apply();

            int id = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                TogetherWidget.update(this, AppWidgetManager.getInstance(this), id);
                setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id));
            }
            finish();
        });
    }

    private long defaultStart() {
        Calendar c = Calendar.getInstance();
        c.set(2026, Calendar.SEPTEMBER, 19, 20, 46, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
    private void refreshDate(Button b) {
        b.setText(DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(start.getTime()));
    }
    private void refreshTime(Button b) {
        b.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(start.getTime()));
    }
}