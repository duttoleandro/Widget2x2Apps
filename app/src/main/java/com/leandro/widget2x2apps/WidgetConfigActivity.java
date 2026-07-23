package com.leandro.widget2x2apps;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WidgetConfigActivity extends Activity {
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private final List<String> packageNames = new ArrayList<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_widget_config);

        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return; }

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = new ArrayList<>();
        for (ApplicationInfo info : pm.getInstalledApplications(PackageManager.GET_META_DATA)) {
            if (pm.getLaunchIntentForPackage(info.packageName) != null &&
                    !info.packageName.equals(getPackageName())) apps.add(info);
        }
        Collections.sort(apps, new Comparator<ApplicationInfo>() {
            public int compare(ApplicationInfo a, ApplicationInfo b) {
                return pm.getApplicationLabel(a).toString()
                        .compareToIgnoreCase(pm.getApplicationLabel(b).toString());
            }
        });

        List<String> labels = new ArrayList<>();
        for (ApplicationInfo info : apps) {
            labels.add(pm.getApplicationLabel(info).toString());
            packageNames.add(info.packageName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner[] spinners = {findViewById(R.id.spinner_0),findViewById(R.id.spinner_1),
                findViewById(R.id.spinner_2),findViewById(R.id.spinner_3)};
        for (Spinner s : spinners) s.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences(AppsWidgetProvider.PREFS, 0);
        for (int i=0;i<4;i++) {
            int pos = packageNames.indexOf(prefs.getString(
                    AppsWidgetProvider.APP_KEYS[i] + "_" + widgetId, ""));
            if (pos >= 0) spinners[i].setSelection(pos);
        }

        SeekBar seek = findViewById(R.id.transparency_seek);
        TextView value = findViewById(R.id.transparency_value);
        int savedAlpha = prefs.getInt(AppsWidgetProvider.ALPHA_PREFIX + widgetId, 210);
        seek.setProgress(255 - savedAlpha);
        value.setText((255 - savedAlpha) + "%");

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b,int progress,boolean fromUser) {
                value.setText(progress + "%");
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });

        Button save = findViewById(R.id.save_button);
        save.setOnClickListener(v -> {
            SharedPreferences.Editor e = prefs.edit();
            for (int i=0;i<4;i++) {
                if (!packageNames.isEmpty())
                    e.putString(AppsWidgetProvider.APP_KEYS[i] + "_" + widgetId,
                            packageNames.get(spinners[i].getSelectedItemPosition()));
            }
            e.putInt(AppsWidgetProvider.ALPHA_PREFIX + widgetId, 255 - seek.getProgress());
            e.apply();

            AppsWidgetProvider.updateWidget(this, AppWidgetManager.getInstance(this), widgetId);
            Intent result = new Intent();
            result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            setResult(RESULT_OK, result);
            finish();
        });
    }
}
