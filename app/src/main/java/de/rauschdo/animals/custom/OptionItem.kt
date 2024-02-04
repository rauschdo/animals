package de.rauschdo.animals.custom

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import de.rauschdo.animals.R
import de.rauschdo.animals.databinding.ItemOptionTileBinding
import de.rauschdo.animals.utility.isNightMode

class OptionItem : LinearLayout {

    private lateinit var binding: ItemOptionTileBinding

    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(attrs)
    }

    private fun initView(attrs: AttributeSet?) {
        ItemOptionTileBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        ).apply {
            binding = this
        }

        if (attrs != null) {
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.OptionItem, 0, 0)

            with(binding) {
                val titleId = R.styleable.OptionItem_title
                val subtitleId = R.styleable.OptionItem_subtitle
                val iconId: Int = R.styleable.OptionItem_icon
                val iconTint: Int = R.styleable.OptionItem_iconTint
                val hasToggle: Boolean =
                    typedArray.getBoolean(R.styleable.OptionItem_hasToggle, false)

                val hasTitle: Boolean = typedArray.hasValue(titleId)
                val hasSubtitle: Boolean = typedArray.hasValue(subtitleId)

                when {
                    hasTitle && !hasSubtitle -> {
                        title.apply {
                            updateLayoutParams<RelativeLayout.LayoutParams> {
                                addRule(RelativeLayout.CENTER_VERTICAL)
                            }
                            visibility = View.VISIBLE
                            text = typedArray.getText(titleId)
                        }
                    }
                    hasTitle && hasSubtitle -> {
                        title.apply {
                            visibility = View.VISIBLE
                            text = typedArray.getText(titleId)
                        }
                        subtitle.apply {
                            visibility = View.VISIBLE
                            text = typedArray.getText(subtitleId)
                        }
                    }
                }

                if (typedArray.hasValue(iconId) && !hasToggle) {
                    val iconRes = typedArray.getDrawable(iconId)
                    icon.setImageDrawable(iconRes)
                }

                if (typedArray.hasValue(iconTint)) {
                    val tint = typedArray.getColor(
                        iconTint,
                        ContextCompat.getColor(context, R.color.black)
                    )
                    icon.colorFilter = PorterDuffColorFilter(
                        tint,
                        PorterDuff.Mode.SRC_ATOP
                    )
                } else {
                    icon.colorFilter = PorterDuffColorFilter(
                        ContextCompat.getColor(
                            context,
                            if (context.isNightMode()) {
                                R.color.white
                            } else {
                                R.color.black
                            }
                        ),
                        PorterDuff.Mode.SRC_ATOP
                    )
                }

                if (hasToggle) {
                    icon.visibility = View.GONE
                    optionSwitch.apply {
                        visibility = View.VISIBLE
                    }
                }
            }
            typedArray.recycle()
        }
        invalidate()
    }

    fun setChecked(isChecked: Boolean) {
        binding.optionSwitch.isChecked = isChecked
    }

    /**
     * Function to set text programmatically for cases of Spans
     * or Strings with Placeholders
     */
    fun setTexts(pTitle: CharSequence? = null, pSubtitle: CharSequence? = null) {
        with(binding) {
            pTitle?.let {
                title.apply {
                    visibility = View.VISIBLE
                    text = it
                }
            }
            pSubtitle?.let {
                subtitle.apply {
                    visibility = View.VISIBLE
                    text = it
                    if (title.visibility == View.VISIBLE) {
                        title.updateLayoutParams<RelativeLayout.LayoutParams> {
                            removeRule(RelativeLayout.CENTER_VERTICAL)
                        }
                    }
                }
            }
        }
    }

    fun setToggleListener(listener: CompoundButton.OnCheckedChangeListener) =
        binding.optionSwitch.setOnCheckedChangeListener(listener)

    fun toggle() = binding.optionSwitch.toggle()
}
