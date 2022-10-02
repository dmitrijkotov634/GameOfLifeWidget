package com.wavecat.conwaysgameoflife

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.wavecat.conwaysgameoflife.databinding.ActivityConfigureBinding
import kotlin.random.Random


class GameConfigureActivity : AppCompatActivity() {

    private val binding: ActivityConfigureBinding by lazy {
        ActivityConfigureBinding.inflate(layoutInflater)
    }

    private val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(this)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        binding.floatingActionButton.setOnClickListener {
            preferences.edit {
                val width = binding.width.value.toInt()
                val height = binding.height.value.toInt()

                putInt("width$appWidgetId", width)
                putInt("height$appWidgetId", height)

                val field = GameOfLifeField(
                    BooleanArray(width * height) { Random.nextBoolean() },
                    width,
                    height
                )

                putString(appWidgetId.toString(), field.saveToString())

                val resultValue =
                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

                sendBroadcast(
                    Intent(
                        this@GameConfigureActivity,
                        GameAppWidgetProvider::class.java
                    ).setAction(GameAppWidgetProvider.ACTION_UPDATE)
                )

                apply()
                setResult(RESULT_OK, resultValue)
                finish()
            }
        }

        setResult(RESULT_CANCELED)
    }
}