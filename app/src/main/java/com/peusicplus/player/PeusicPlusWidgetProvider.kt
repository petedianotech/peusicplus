package com.peusicplus.player

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class PeusicPlusWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_peusicplus).apply {
                setOnClickPendingIntent(
                    R.id.widgetOpen,
                    android.app.PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, MainActivity::class.java),
                        android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }

            appWidgetManager.updateAppWidget(id, views)
        }
    }
}