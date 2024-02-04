package de.rauschdo.animals.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import de.rauschdo.animals.database.Animal
import de.rauschdo.animals.database.AnimalDao
import de.rauschdo.animals.utility.DataUtil
import de.rauschdo.animals.utility.preference.PreferencesUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AnimalWidget : AppWidgetProvider() {

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var animalDao: AnimalDao

    private var isInit = false
    private lateinit var initialAnimal: Animal

    companion object {
        const val WIDGET_REQUEST_NEW_ANIMAL = "de.rauschdo.animals.WIDGET_REQUEST_NEW_ANIMAL"
        const val WIDGET_LOAD_NEW_FACT = "de.rauschdo.animals.WIDGET_LOAD_NEW_FACT"
        var updateRequest: WidgetUpdateUtil.UpdateType = WidgetUpdateUtil.UpdateType.NOTHING
    }

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)

        context?.let {
            // CASE: App was installed but not been opened yet
            //       in this case fill database with animals, categories are not created to keep work at a minimum
            //       we do not save that we imported something so when app start it will create the categories
            if (!PreferencesUtil.hasImportedData(context)) {
                coroutineScope.launch {
                    DataUtil.loadAnimalData(context.resources)?.forEach { animalFamily ->
                        animalDao.insertAll(
                            DataUtil.mapToAnimalDbData(
                                null,
                                animalFamily.categoryKinds
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)

        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                onUpdate(context)
            }
            WIDGET_REQUEST_NEW_ANIMAL -> {
                WidgetUpdateUtil.requestNewAnimalAndUpdate(
                    context,
                    coroutineScope,
                    animalDao,
                    AppWidgetManager.getInstance(context),
                    intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                )
            }
            WIDGET_LOAD_NEW_FACT -> {
                WidgetUpdateUtil.loadNewAnimalFact(
                    context,
                    coroutineScope,
                    animalDao,
                    AppWidgetManager.getInstance(context),
                    intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                )
            }
            else -> Unit
        }
    }

    override fun onDisabled(context: Context?) {
        super.onDisabled(context)
        isInit = false
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        coroutineScope.launch {
            for (appWidgetId in appWidgetIds) {
                WidgetUpdateUtil.updateAppWidget(
                    context,
                    coroutineScope,
                    animalDao,
                    appWidgetManager,
                    appWidgetId,
                    isInit
                )
            }
            if (!isInit) {
                isInit = true
            }
        }
    }

    /**
     * A general technique for calling the onUpdate method,
     * requiring only the context parameter.
     */
    private fun onUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisAppWidgetComponentName = ComponentName(context.packageName, javaClass.name)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName)
        onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        WidgetUpdateUtil.handleResize(
            context,
            coroutineScope,
            animalDao,
            appWidgetManager,
            appWidgetId
        )
    }
}
