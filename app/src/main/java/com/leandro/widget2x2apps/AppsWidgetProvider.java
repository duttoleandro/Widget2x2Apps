package com.leandro.widget2x2apps;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;

public class AppsWidgetProvider extends AppWidgetProvider {
    public static final String PREFS = "widget_prefs";
    public static final String[] APP_KEYS = {"app0","app1","app2","app3"};
    public static final String ALPHA_PREFIX = "alpha_";

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) updateWidget(context, manager, id);
    }

    @Override public void onDeleted(Context context, int[] ids) {
        SharedPreferences.Editor e = context.getSharedPreferences(PREFS, 0).edit();
        for (int id : ids) {
            for (String key : APP_KEYS) e.remove(key + "_" + id);
            e.remove(ALPHA_PREFIX + id);
        }
        e.apply();
    }

    public static void updateWidget(Context context, AppWidgetManager manager, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_2x2);
        SharedPreferences p = context.getSharedPreferences(PREFS, 0);
        int alpha = p.getInt(ALPHA_PREFIX + id, 210);
        views.setInt(R.id.widget_background, "setBackgroundColor",
                Color.argb(alpha, 110, 110, 110));

        int[] iconIds = {R.id.icon_0,R.id.icon_1,R.id.icon_2,R.id.icon_3};
        for (int i=0;i<4;i++) {
            String pkg = p.getString(APP_KEYS[i] + "_" + id, "");
            if (!pkg.isEmpty()) {
                try {
                    Drawable icon = context.getPackageManager().getApplicationIcon(pkg);
                    views.setImageViewBitmap(iconIds[i], DrawableUtils.drawableToBitmap(icon));
                    Intent launch = context.getPackageManager().getLaunchIntentForPackage(pkg);
                    if (launch != null) {
                        PendingIntent pi = PendingIntent.getActivity(context, id*10+i, launch,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                        views.setOnClickPendingIntent(iconIds[i], pi);
                    }
                    continue;
                } catch (Exception ignored) {}
            }
            views.setImageViewResource(iconIds[i], android.R.drawable.sym_def_app_icon);
            Intent config = new Intent(context, WidgetConfigActivity.class);
            config.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
            PendingIntent pi = PendingIntent.getActivity(context, id*10+i, config,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(iconIds[i], pi);
        }
        manager.updateAppWidget(id, views);
    }
}
