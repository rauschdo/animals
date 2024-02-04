package de.rauschdo.animals.utility

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import de.rauschdo.animals.R
import java.util.*

object SpanUtil {

    fun prepareShortendUrls(urls: List<String>): List<Spanned> {
        val resultStrings = mutableListOf<Spanned>()
        urls.forEach {
            val splits = it.replace("www.", "")
                .substringAfter("https://")
                .split(".")
            resultStrings.add(
                HtmlCompat.fromHtml(
                    "${BulletType.CLASSIC.stringCode} " +
                            splits[0].replaceFirstChar { char ->
                                if (char.isLowerCase()) {
                                    char.titlecase(Locale.getDefault())
                                } else {
                                    it
                                }
                            },
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            )
        }
        return resultStrings
    }

    fun createBulletList(
        listOfPoints: List<String>?,
        bulletType: BulletType = BulletType.CLASSIC
    ): Spanned {
        return HtmlCompat.fromHtml(
            StringBuilder().apply {
                listOfPoints?.forEach { triviaPoint ->
                    append("${bulletType.stringCode} $triviaPoint<br/>")
                }
            }.toString(),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    /**
     * Use this function if you have singular span in text
     * @return the [SpannableString] for further use, if required
     */
    fun setDeeplinkSpan(
        context: Context?,
        stringToSearch: String?,
        spanText: CharSequence?,
        spanColor: Int = if (context?.isNightMode() == true) R.color.white_text else R.color.black,
        func: (String) -> Unit
    ): SpannableString? {
        context?.let {
            val fullString = SpannableString(stringToSearch)
            val overviewIndex =
                if (spanText == null) 0 else fullString.lastIndexOf(spanText.toString())

            try {
                val clickable = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        func(spanText.toString())
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = ContextCompat.getColor(context, spanColor)
                        ds.isUnderlineText = true
                    }
                }
                fullString.setSpan(
                    clickable,
                    overviewIndex,
                    overviewIndex + (spanText?.length ?: stringToSearch?.length ?: 0),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                return fullString
            } catch (e: Exception) {
                //nothing
            }
        }
        return null
    }

    /**
     * Use this function if you already have a span defined and need additional ones
     * @return the [SpannableString] for further use, if required
     */
    fun setAdditionalDeeplinkSpan(
        context: Context?,
        stringToSearch: SpannableString,
        spanText: CharSequence?,
        spanColor: Int = if (context?.isNightMode() == true) R.color.white_text else R.color.black,
        func: (String) -> Unit
    ): SpannableString? {
        context?.let {
            val overviewIndex =
                if (spanText == null) 0 else stringToSearch.lastIndexOf(spanText.toString())

            try {
                val clickable = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        func(spanText.toString())
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = ContextCompat.getColor(context, spanColor)
                        ds.isUnderlineText = true
                    }
                }
                stringToSearch.setSpan(
                    clickable,
                    overviewIndex,
                    overviewIndex + (spanText?.length ?: stringToSearch.length),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                return stringToSearch
            } catch (e: Exception) {
                //nothing
            }
        }
        return null
    }

    /**
     * Use this function if you already have a span defined and need additional ones
     * @return the [SpannableString] for further use, if required
     */
    fun TextView.setAdditionalDeeplinkSpan(
        context: Context?,
        stringToSearch: SpannableString,
        spanText: CharSequence?,
        spanColor: Int = if (context?.isNightMode() == true) R.color.white_text else R.color.black,
        func: (String) -> Unit
    ): SpannableString? {
        context?.let {
            val overviewIndex =
                if (spanText == null) 0 else stringToSearch.lastIndexOf(spanText.toString())

            try {
                val clickable = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        func(spanText.toString())
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = ContextCompat.getColor(context, spanColor)
                        ds.isUnderlineText = true
                    }
                }
                stringToSearch.setSpan(
                    clickable,
                    overviewIndex,
                    overviewIndex + (spanText?.length ?: stringToSearch.length),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                return stringToSearch
            } catch (e: Exception) {
                //nothing
            }
        }
        return null
    }
}