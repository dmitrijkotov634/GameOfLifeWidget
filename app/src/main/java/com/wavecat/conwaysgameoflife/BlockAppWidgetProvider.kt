package com.wavecat.conwaysgameoflife

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.graphics.ColorUtils
import kotlin.random.Random


class BlockAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, BlockAppWidgetProvider::class.java).setAction(
                ACTION_UPDATE
            ), flags
        )

        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.block_widget).apply {
                setInt(
                    R.id.block, "setBackgroundColor", ColorUtils.HSLToColor(
                        floatArrayOf(
                            Random.nextInt(360).toFloat(),
                            .9F,
                            .5F
                        )
                    )
                )

                setOnClickPendingIntent(
                    R.id.block, pendingIntent
                )
            }

            appWidgetManager.updateAppWidget(id, views)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        if (intent?.action == ACTION_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidgetComponentName = ComponentName(context!!.packageName, javaClass.name)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_UPDATE = "ACTION_UPDATE"
    }
}