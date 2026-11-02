package com.agrani.seeitdoit;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/** The home-screen widget: shows your handwritten Today and This week lists. */
public class SeeItWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIds) {
        if (appWidgetIds == null || appWidgetIds.length == 0) return;
        manager.updateAppWidget(appWidgetIds, build(context));
    }

    /** Called by the writing sheet after you press Done. */
    static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, SeeItWidget.class));
        if (ids.length == 0) return;
        manager.updateAppWidget(ids, build(context));
    }

    private static RemoteViews build(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setImageViewBitmap(R.id.img_today, Sheet.render(context, Sheet.TODAY));
        views.setImageViewBitmap(R.id.img_week, Sheet.render(context, Sheet.WEEK));

        Intent open = new Intent(context, WriteActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tap = PendingIntent.getActivity(
                context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        views.setOnClickPendingIntent(R.id.root, tap);
        views.setOnClickPendingIntent(R.id.img_today, tap);
        views.setOnClickPendingIntent(R.id.img_week, tap);
        return views;
    }
}
