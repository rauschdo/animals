package de.rauschdo.animals.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.*
import android.util.Size
import android.util.SizeF
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.annotation.AnimRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import de.rauschdo.animals.R
import de.rauschdo.animals.activities.MainActivity
import de.rauschdo.animals.activities.detail.DetailActivity
import de.rauschdo.animals.databinding.ActivityDetailBinding
import de.rauschdo.animals.databinding.ActivityMainBinding
import java.io.Serializable
import java.util.*

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) =
    lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }

fun Boolean.getVisibility() =
    if (this) View.VISIBLE else View.GONE

fun Boolean.toInt() =
    if (this) 1 else 0

fun AppCompatActivity.isBackstackEmpty() = supportFragmentManager.backStackEntryCount == 0

inline fun Fragment.mainBinding(func: ActivityMainBinding.() -> Unit) {
    (activity as? MainActivity)?.let {
        it.binding.func()
    }
}

inline fun Fragment.detailBinding(func: ActivityDetailBinding.() -> Unit) {
    (activity as? DetailActivity)?.let {
        it.binding.func()
    }
}

fun Context.isLandscape() =
    this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

fun Context.isTablet() = this.resources.configuration.smallestScreenWidthDp >= 600

fun Context.isNightMode() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Activity.getCurrentDestinationId() =
    this.findNavController(R.id.navHostFragment).currentDestination?.id

fun AppCompatActivity.getCurrentFragment() =
    this.supportFragmentManager
        .findFragmentById(R.id.navHostFragment)
        ?.childFragmentManager?.fragments?.getOrNull(0)

fun Activity.shareIntentBuilder(func: ShareCompat.IntentBuilder.() -> Unit): Intent {
    val sharedIntent = ShareCompat.IntentBuilder(this)
    sharedIntent.func()
    return sharedIntent.intent
}

fun Context.idOf(name: String?, type: String) = resources.getIdentifier(name, type, packageName)

fun Int.formattedDisplay(): String {
    val factor = this / 100// % 1000;
    val converted = String.format(Locale.US, "%,d", factor).replace(",", ".")
    return String.format(Locale.GERMAN, "%1\$s", converted)
}

@SuppressLint("RestrictedApi")
fun Menu.showOptionalIcons() {
    this as MenuBuilder
    setOptionalIconsVisible(true)
}

fun <T> observerOfNonNull(func: (T) -> Unit): Observer<T?> {
    return Observer<T?> { t -> t?.let(func) }
}

fun ImageView.loadImageNetwork(context: Context?, url: String, @DrawableRes placeholder: Int?) {
    context?.let {
        val glideUrl = GlideUrl(if (url.isEmpty()) " " else url)
        var requestOptions = RequestOptions()
        requestOptions = requestOptions.transform(CenterInside())

        placeholder?.let {
            requestOptions.placeholder(it)
        }

        GlideApp.with(context)
            .load(glideUrl)
            .apply(requestOptions)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }
}

fun ImageView.loadImage(
    context: Context?,
    @DrawableRes image: Int,
    requestOptions: RequestOptions? = null
) {
    context?.let {
        var options = RequestOptions()
        requestOptions?.let {
            options = it
        }
        GlideApp.with(context)
            .load(image)
            .apply(options)
            .into(this)
    }
}

private class AnimationListener(
    private val onAnimationRepeat: () -> Unit,
    private val onAnimationStart: () -> Unit,
    private val onAnimationEnd: () -> Unit
) : Animation.AnimationListener {
    override fun onAnimationRepeat(p0: Animation?) = onAnimationRepeat()
    override fun onAnimationStart(p0: Animation?) = onAnimationStart()
    override fun onAnimationEnd(p0: Animation?) = onAnimationEnd()
}

/** Extension function to easily add animation
 * and precise what to do on animation events.
 *
 * Example Usage
 * my_view.playAnimation(
 *   animResId = R.anim.rotate,
 *   onAnimationEnd = { your action here }
 * )
 */
fun View.playAnimation(
    @AnimRes animResId: Int,
    onAnimationRepeat: () -> Unit = {},
    onAnimationStart: () -> Unit = {},
    onAnimationEnd: () -> Unit = {}
) = with(AnimationUtils.loadAnimation(context, animResId)) {
    setAnimationListener(AnimationListener(onAnimationRepeat, onAnimationStart, onAnimationEnd))
    startAnimation(this)
}

fun Intent.putPairExtras(pair: Pair<String, Any?>) = apply {
    putExtras(Utility.bundleOf(pair))
}

fun View.defineOnAntiSpamClick(threshold: Long = 1000, func: (View) -> Unit): View.OnClickListener {
    return object : View.OnClickListener {
        private var lastClickTime = 0L
        override fun onClick(v: View) {
            if (lastClickTime == 0L) {
                lastClickTime = SystemClock.elapsedRealtime() + threshold
                func(v)
                return
            }
            if (SystemClock.elapsedRealtime() - lastClickTime < threshold) {
                return
            }
            lastClickTime = SystemClock.elapsedRealtime()
            func(v)
        }
    }
}

fun View.setOnAntiSpamClick(threshold: Long = 1000, func: (View) -> Unit) {
    this.setOnClickListener(defineOnAntiSpamClick(threshold, func))
}

object Utility {

    @SuppressLint("ObsoleteSdkInt")
    internal fun bundleOf(vararg pairs: Pair<String, Any?>) = Bundle(pairs.size).apply {
        for ((key, value) in pairs) {
            when (value) {
                null -> putString(key, null) // Any nullable type will suffice.

                // Scalars
                is Boolean -> putBoolean(key, value)
                is Byte -> putByte(key, value)
                is Char -> putChar(key, value)
                is Double -> putDouble(key, value)
                is Float -> putFloat(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Short -> putShort(key, value)

                // References
                is Bundle -> putBundle(key, value)
                is CharSequence -> putCharSequence(key, value)
                is Parcelable -> putParcelable(key, value)

                // Scalar arrays
                is BooleanArray -> putBooleanArray(key, value)
                is ByteArray -> putByteArray(key, value)
                is CharArray -> putCharArray(key, value)
                is DoubleArray -> putDoubleArray(key, value)
                is FloatArray -> putFloatArray(key, value)
                is IntArray -> putIntArray(key, value)
                is LongArray -> putLongArray(key, value)
                is ShortArray -> putShortArray(key, value)

                // Reference arrays
                is Array<*> -> {
                    val componentType = value::class.java.componentType!!
                    @Suppress("UNCHECKED_CAST") // Checked by reflection.
                    when {
                        Parcelable::class.java.isAssignableFrom(componentType) -> {
                            putParcelableArray(key, value as Array<Parcelable>)
                        }
                        String::class.java.isAssignableFrom(componentType) -> {
                            putStringArray(key, value as Array<String>)
                        }
                        CharSequence::class.java.isAssignableFrom(componentType) -> {
                            putCharSequenceArray(key, value as Array<CharSequence>)
                        }
                        Serializable::class.java.isAssignableFrom(componentType) -> {
                            putSerializable(key, value)
                        }
                        else -> {
                            val valueType = componentType.canonicalName
                            throw IllegalArgumentException(
                                "Illegal value array type $valueType for key \"$key\""
                            )
                        }
                    }
                }

                is Serializable -> putSerializable(key, value)

                else -> {
                    if (Build.VERSION.SDK_INT >= 18 && value is IBinder) {
                        putBinder(key, value)
                    } else if (Build.VERSION.SDK_INT >= 21 && value is Size) {
                        putSize(key, value)
                    } else if (Build.VERSION.SDK_INT >= 21 && value is SizeF) {
                        putSizeF(key, value)
                    } else {
                        val valueType = value.javaClass.canonicalName
                        throw IllegalArgumentException("Illegal value type $valueType for key \"$key\"")
                    }
                }
            }
        }
    }
}
