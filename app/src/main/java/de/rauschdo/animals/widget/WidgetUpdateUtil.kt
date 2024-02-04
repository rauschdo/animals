package de.rauschdo.animals.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.AppWidgetTarget
import com.bumptech.glide.request.transition.Transition
import de.rauschdo.animals.R
import de.rauschdo.animals.database.Animal
import de.rauschdo.animals.database.AnimalDao
import de.rauschdo.animals.utility.GlideApp
import de.rauschdo.animals.utility.SpanUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object WidgetUpdateUtil {

    private var currentAnimal: Animal? = null
    private var currentAnimalImage: String? = null

    private var currentFact: String? = null

    enum class UpdateType {
        //ANIMAL requires fact to be updated as well
//        ANIMAL,
        NOTHING,
        FACT,
        EVERYTHING
    }

    private enum class FlipperType {
        TEXT,
        IMAGE
    }

    /**
     * Helper Maps keeping track which page of ViewFlipper is shown
     */
    private var currentImageDisplayed: MutableMap<Int, Int> = mutableMapOf()
    private var currentTextDisplayed: MutableMap<Int, Int> = mutableMapOf()

    /**
     * Update a single app widget.  This is a helper method for the standard
     * onUpdate() callback that handles one widget update at a time.
     *
     * @param context
     * @param coroutineScope
     * @param dao              The link to the database injected to the widget via Hilt.
     * @param appWidgetManager The app widget manager.
     * @param appWidgetId      The current app widget id.
     * @param initial          Is this the first update of this Widget's provider
     */
    suspend fun updateAppWidget(
        context: Context,
        coroutineScope: CoroutineScope,
        dao: AnimalDao,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        initial: Boolean,
        updateType: UpdateType = UpdateType.EVERYTHING
    ) {
        val fullUpdate = {
            currentAnimal = rollNewAnimal(dao.getAll(), currentAnimal)
            currentAnimalImage = currentAnimal?.imageUrls?.random()
            currentFact = rollNewTrivia(currentAnimal?.triviaPoints, currentFact)
        }

        if (initial) {
            fullUpdate()
        } else {
            when (updateType) {
                UpdateType.FACT -> {
                    currentFact = rollNewTrivia(currentAnimal?.triviaPoints, currentFact)
                }
                UpdateType.EVERYTHING -> {
                    fullUpdate()
                }
                else -> Unit
            }
        }

        withContext(Dispatchers.Main) {
            val views = getRemoteViews(context, appWidgetManager, appWidgetId, updateType)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun handleResize(
        context: Context,
        coroutineScope: CoroutineScope,
        dao: AnimalDao,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        coroutineScope.launch {
            updateAppWidget(
                context,
                coroutineScope,
                dao,
                appWidgetManager,
                appWidgetId,
                initial = false,
                updateType = UpdateType.NOTHING
            )
        }
    }

    fun requestNewAnimalAndUpdate(
        context: Context,
        coroutineScope: CoroutineScope,
        dao: AnimalDao,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        coroutineScope.launch {
            updateAppWidget(
                context,
                coroutineScope,
                dao,
                appWidgetManager,
                appWidgetId,
                initial = false,
                updateType = UpdateType.EVERYTHING
            )
        }
    }

    fun loadNewAnimalFact(
        context: Context,
        coroutineScope: CoroutineScope,
        dao: AnimalDao,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        coroutineScope.launch {
            updateAppWidget(
                context,
                coroutineScope,
                dao,
                appWidgetManager,
                appWidgetId,
                initial = false,
                updateType = UpdateType.FACT
            )
        }
    }

    private fun initNextButton(context: Context, views: RemoteViews, appWidgetId: Int) {
        views.setOnClickPendingIntent(
            R.id.widget_next_animal_button,
            buildPendingIntent(
                context,
                intent = Intent(context, AnimalWidget::class.java).apply {
                    action = AnimalWidget.WIDGET_REQUEST_NEW_ANIMAL
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
            )
        )
    }

    private fun initNewFactButton(context: Context, views: RemoteViews, appWidgetId: Int) {
        views.setOnClickPendingIntent(
            R.id.widget_next_fact,
            buildPendingIntent(
                context,
                intent = Intent(context, AnimalWidget::class.java).apply {
                    action = AnimalWidget.WIDGET_LOAD_NEW_FACT
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
            )
        )
    }

    private fun initImageFlipper(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        updateType: UpdateType
    ) {
        val type = FlipperType.IMAGE
        when (currentImageDisplayed[appWidgetId]) {
            0 -> setFlipperPage(context, views, 1, appWidgetId, type)
            else -> setFlipperPage(context, views, 0, appWidgetId, type)
        }
    }

    private fun initTextFlipper(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        updateType: UpdateType
    ) {
        val type = FlipperType.TEXT
        when (currentTextDisplayed[appWidgetId]) {
            0 -> setFlipperPage(context, views, 1, appWidgetId, type)
            else -> setFlipperPage(context, views, 0, appWidgetId, type)
        }
    }

    private fun setFlipperPage(
        context: Context,
        views: RemoteViews,
        page: Int,
        appWidgetId: Int,
        type: FlipperType
    ) {
        when (type) {
            FlipperType.TEXT -> {
                loadTexts(context, views, appWidgetId)
                views.setDisplayedChild(R.id.widget_single_fact_flipper, page)
                views.setDisplayedChild(R.id.widget_animal_facts_flipper, page)
            }
            FlipperType.IMAGE -> {
                loadImage(context, views, appWidgetId)
                views.setDisplayedChild(R.id.widget_image_flipper, page)
            }
        }
    }

    private fun loadTexts(context: Context, views: RemoteViews, appWidgetId: Int) {
        // first = single fact (wide) ; second = bullet list (small)
        val idsSingleFact = listOf(R.id.widget_single_fact_0, R.id.widget_single_fact_1)
        val idsFullTrivia = listOf(R.id.widget_animal_facts_0, R.id.widget_animal_facts_1)

        // Workarounds
        views.setTextViewText(
            R.id.size_orientation_widget_animal_facts_flipper_wide,
            currentFact
        )
        views.setTextViewText(
            R.id.size_orientation_widget_animal_facts_flipper_small,
            SpanUtil.createBulletList(currentAnimal?.triviaPoints)
        )

        idsSingleFact.forEach { id ->
            views.setTextViewText(
                id,
                currentFact
            )
        }
        idsFullTrivia.forEach { id ->
            views.setTextViewText(
                id,
                SpanUtil.createBulletList(currentAnimal?.triviaPoints)
            )
        }
    }

    private fun loadImage(context: Context, views: RemoteViews, appWidgetId: Int) {
        listOf(R.id.widget_animal_image_0, R.id.widget_animal_image_1).forEach { id ->
            val awt: AppWidgetTarget =
                object : AppWidgetTarget(context, id, views, appWidgetId) {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        super.onResourceReady(resource, transition)
                    }
                }

            GlideApp
                .with(context.applicationContext)
                .asBitmap()
                .load(currentAnimalImage)
                .placeholder(R.drawable.ic_component_placeholder)
                .apply(RequestOptions().transform(CenterInside()))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .transition(BitmapTransitionOptions.withCrossFade())
                .into(awt)
        }
    }

    private fun initTrivia(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        updateType: UpdateType
    ) {
        views.setTextViewText(
            R.id.widget_animal,
            currentAnimal?.name
        )
        views.setTextViewText(
            R.id.widget_animal_wide,
            context.getString(R.string.widget_animal_facts_for, currentAnimal?.name)
        )
        initTextFlipper(context, views, appWidgetId, updateType)
    }

    /**
     * Roll for new animal until a new one is found
     */
    private fun rollNewAnimal(listOfAnimals: List<Animal>, oldAnimal: Animal?): Animal {
        val newAnimal = listOfAnimals.random()
        if (oldAnimal == newAnimal) {
            // If current animal has been hit remove it from the list so next is guaranteed to find a new one
            rollNewAnimal(listOfAnimals.toMutableList().apply { remove(newAnimal) }, oldAnimal)
        }
        return newAnimal
    }

    private fun rollNewTrivia(triviaList: List<String>?, oldTrivia: String?): String {
        val newTrivia = triviaList?.random()
        if (oldTrivia == newTrivia) {
            rollNewTrivia(triviaList?.toMutableList()?.apply { remove(newTrivia) }, oldTrivia)
        }
        currentFact = newTrivia
        return newTrivia ?: "---"
    }

    /**
     * Requests the Layout (option for multiple layouts if required)
     *
     * @param context
     * @return The corresponding layout as [RemoteViews] Object required in Widgets
     */
    private fun getRemoteViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        updateType: UpdateType
    ): RemoteViews {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            val viewMapping: Map<SizeF, RemoteViews> = mapOf(
//                // Specify the minimum width and height in dp and a layout, which you want to use for the
//                // specified size
//                // In the following case:
//                //   - R.layout.animal_widget_small is used from
//                //     180dp (or minResizeWidth) x 110dp (or minResizeHeight) to 239dp x 79dp (next cutoff point - 1)
//                //   - R.layout.animal_widget_wide is used from
//                //     240dp x 80dp to 570dp (specified as maxResizeWidth) x 240dp (specified as maxResizeHeight)
//                SizeF(180.0f, 40.0f) to constructRemoteViews(
//                    context,
//                    appWidgetId,
//                    R.layout.animal_widget_small,
//                    updateType
//                ),
//                SizeF(240.0f, 80.0f) to constructRemoteViews(
//                    context,
//                    appWidgetId,
//                    R.layout.animal_widget_wide,
//                    updateType
//                )
//            )
//            return RemoteViews(viewMapping)
//        } else {
        // first == width ; second == height
        val currentSize = WidgetDimenHelper.getWidgetsSize(
            context,
            appWidgetManager,
            appWidgetId
        )
        val layoutRes = when {
            currentSize.first < 240 -> R.layout.animal_widget_small
            else -> R.layout.animal_widget_wide
        }
        return constructRemoteViews(context, appWidgetId, layoutRes, updateType)
//        }
    }

    private fun constructRemoteViews(
        context: Context,
        appWidgetId: Int,
        @LayoutRes widgetLayoutId: Int,
        updateType: UpdateType
    ) = RemoteViews(context.packageName, widgetLayoutId).apply {
        initNewFactButton(context, this, appWidgetId)
        initNextButton(context, this, appWidgetId)
        initImageFlipper(context, this, appWidgetId, updateType)
        initTrivia(context, this, appWidgetId, updateType)
    }

    private fun buildPendingIntent(context: Context, intent: Intent) = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}
