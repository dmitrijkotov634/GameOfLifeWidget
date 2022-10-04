package com.wavecat.conwaysgameoflife

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlin.random.Random


class GameAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, GameAppWidgetProvider::class.java).setAction(
                ACTION_UPDATE
            ), flags
        )

        appWidgetIds.forEach { id ->
            val fieldString = preferences.getString(id.toString(), "")

            if (fieldString.isNullOrEmpty()) return

            val width = preferences.getInt("width$id", 1)
            val height = preferences.getInt("height$id", 1)
            val resetInterval = preferences.getInt("resetInterval$id", 0)

            val timestamp = preferences.getLong("timestamp$id", 0)

            val field = GameOfLifeField.createFromString(
                fieldString,
                width,
                height
            )

            val views = RemoteViews(context.packageName, R.layout.game_widget).apply {
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                bitmap.eraseColor(Color.TRANSPARENT)

                repeat(field.height) { y ->
                    repeat(field.width) { x ->
                        if (field[x, y])
                            bitmap.setPixel(x, y, Color.argb(25, 255, 255, 255))
                    }
                }

                var newField = field.next()

                repeat(newField.height) { y ->
                    repeat(newField.width) { x ->
                        if (newField[x, y])
                            bitmap.setPixel(x, y, Color.WHITE)
                    }
                }

                preferences.edit {
                    if (newField.fieldEquals(field)
                        || ((System.currentTimeMillis() - timestamp) > resetInterval * 60000
                                && resetInterval > 0
                                )
                    ) {
                        newField = GameOfLifeField(
                            BooleanArray(width * height) { Random.nextBoolean() },
                            width,
                            height
                        )
                        putLong("timestamp$id", System.currentTimeMillis())
                    }

                    putString(id.toString(), newField.saveToString())
                    apply()
                }

                val resizedBitmap =
                    Bitmap.createScaledBitmap(bitmap, 400, height * 400 / width, false)

                setImageViewBitmap(R.id.result, resizedBitmap)

                setOnClickPendingIntent(
                    R.id.game, pendingIntent
                )
            }

            appWidgetManager.updateAppWidget(id, views)
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context!!)

        appWidgetIds?.forEach { id ->
            preferences.edit {
                remove(id.toString())
                remove("width$id")
                remove("height$id")
                remove("timestamp$id")
                remove("resetInterval$id")
                apply()
            }
        }

        super.onDeleted(context, appWidgetIds)
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