package de.rauschdo.animals.activities.detail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import de.rauschdo.animals.databinding.ItemAnimalImageBinding
import de.rauschdo.animals.utility.GlideApp

class DetailImageAdapter(
    val context: Context?,
    private var imageUrls: List<String>,
    private var listener: ImageClickListener
) : PagerAdapter() {

    override fun isViewFromObject(view: View, aObject: Any): Boolean {
        return view === aObject
    }

    override fun getCount(): Int {
        return imageUrls.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = ItemAnimalImageBinding.inflate(
            LayoutInflater.from(context),
            null,
            false
        ).apply {
            imageUrls.getOrNull(position)?.let { image ->
                context?.let { itContext ->
                    GlideApp
                        .with(itContext)
                        .asBitmap()
                        .load(image)
                        .transition(BitmapTransitionOptions.withCrossFade())
                        .into(ivImage)

                    ivImage.setOnClickListener {
                        listener.onImageClicked()
                    }

//                    btnNextPage.apply {
//                        visibility = (imageUrls.size != 1).getVisibility()
//                        setOnClickListener {
//                            listener.onNextClicked()
//                        }
//                    }
                }
            }
        }.root
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, aObject: Any) {
        container.removeView(aObject as? View)
    }

    interface ImageClickListener {
        fun onImageClicked()
//        fun onNextClicked()
    }
}