package de.rauschdo.animals.fragments.detail.image

import android.os.Bundle
import android.view.View
import de.rauschdo.animals.R
import de.rauschdo.animals.architecture.BaseFragment
import de.rauschdo.animals.databinding.FragmentDetailImageBinding
import de.rauschdo.animals.utility.detailBinding
import de.rauschdo.animals.utility.loadImageNetwork

class DetailImageFragment :
    BaseFragment<FragmentDetailImageBinding>(R.layout.fragment_detail_image) {

    companion object {
        private val IMAGE = "image"

        fun newInstance(url: String) = DetailImageFragment().apply {
            arguments = Bundle().apply {
                putString(IMAGE, url)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding {
            arguments?.getString(IMAGE)?.let { url ->
                imageFullscreen.loadImageNetwork(
                    context,
                    url,
                    null
                )
            }

            exitFullscreen.setOnClickListener {
                activity?.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        detailBinding {
            containerFragment.visibility = View.GONE
        }
        super.onDestroy()
    }
}
