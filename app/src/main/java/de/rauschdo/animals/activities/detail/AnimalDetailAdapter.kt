package de.rauschdo.animals.activities.detail

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolygonOptions
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import de.rauschdo.animals.R
import de.rauschdo.animals.database.Animal
import de.rauschdo.animals.databinding.ItemAnimalDetailBinding
import de.rauschdo.animals.utility.SpanUtil
import de.rauschdo.animals.utility.formattedDisplay
import de.rauschdo.animals.utility.getVisibility

class AnimalDetailAdapter(
    private val listener: ClickListener
) : BindingListAdapter<Animal, AnimalDetailAdapter.AnimalDetailHolder>(diffUtil) {

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Animal>() {
            override fun areItemsTheSame(
                oldItem: Animal,
                newItem: Animal
            ): Boolean = true

            override fun areContentsTheSame(
                oldItem: Animal,
                newItem: Animal
            ): Boolean = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalDetailHolder {
        val binding = parent.binding<ItemAnimalDetailBinding>(R.layout.item_animal_detail)
        return AnimalDetailHolder(binding, listener)
    }


    override fun onBindViewHolder(holder: AnimalDetailHolder, position: Int) =
        holder.bind(getItem(position), position)

    interface ClickListener {
        fun onFullscreenImage(currentUrl: String?)
    }

    inner class AnimalDetailHolder(
        private val binding: ItemAnimalDetailBinding,
        private val listener: ClickListener?,
    ) : RecyclerView.ViewHolder(binding.root), OnMapReadyCallback {

        private val ADAPTER_SCROLL_INTERVAL = 5000L

        private lateinit var map: GoogleMap

        private val mContext = binding.root.context

        fun bind(animal: Animal?, position: Int) {
            with(binding) {
                animal?.let {
                    this.animal = it
                    executePendingBindings()

                    initImagePager(mContext)

                    initDisplayData()

                    if (animal.coordinates.isNotEmpty()) {
                        containerCard.visibility = View.VISIBLE
                        with(map) {
                            // Initialise the MapView
                            onCreate(null)
                            // Set the map ready callback to receive the GoogleMap object
                            getMapAsync(this@AnimalDetailHolder)
                        }
                    }
                }
            }
        }

        private fun initImagePager(context: Context) {
            with(binding) {
                val imageAdapter = DetailImageAdapter(
                    context,
                    animal?.imageUrls ?: emptyList(),
                    object : DetailImageAdapter.ImageClickListener {
                        override fun onImageClicked() {}

//                    override fun onNextClicked() {
//                        imagePager.apply {
//                            //Restart interval to automatically scroll
//                            stopAutoScroll()
//                            startAutoScroll()
//                            currentItem = if (currentItem != adapter?.count?.minus(1) ?: 0) {
//                                currentItem + 1
//                            } else {
//                                0
//                            }
//                        }
//                    }
                    })

                imagePager.apply {
                    interval = ADAPTER_SCROLL_INTERVAL
                    isCycle = true
                    isStopScrollWhenTouch = true
                    startAutoScroll()
                    setAutoScrollDurationFactor(5.0)
                    adapter = imageAdapter
                    pagerIndicator.setViewPager(this)
                }

                enterFullscreen.setOnClickListener {
                    listener?.onFullscreenImage(
                        animal?.imageUrls?.get(imagePager.currentItem)
                    )
                }
            }
        }

        // some is handled automatically via databinding
        private fun initDisplayData() {
            with(binding) {
                containerHabitat.visibility =
                    (animal?.habitat?.isNotBlank() == true).getVisibility()
                containerEstimate.apply {
                    val estimated = animal?.estimatePopulation
                    visibility = (estimated != -1).getVisibility()
                    estimatedCount.text = estimated?.formattedDisplay()
                }
                containerLifespan.visibility =
                    (animal?.lifespan?.isNotBlank() == true).getVisibility()
                containerLifestyle.visibility =
                    (animal?.lifestyleDescription?.isNotBlank() == true).getVisibility()

                // Features
                containerFeatures.apply {
                    val mFeatures = animal?.features
                    visibility = (mFeatures?.isNotEmpty() == true).getVisibility()
                    features.text = SpanUtil.createBulletList(mFeatures)
                }

                // Temper
                containerTemper.apply {
                    val mTemper = animal?.temper
                    visibility = (mTemper?.isNotEmpty() == true).getVisibility()
                    temper.text = SpanUtil.createBulletList(mTemper)
                }

                // Food
                food.apply {
                    visibility = (animal?.prey?.isNotEmpty() == true).getVisibility()
                    text = SpanUtil.createBulletList(animal?.prey)
                }

                // Predators
                val mBiggestThread = animal?.biggestThread
                val predatorList = animal?.predators
                if (mBiggestThread?.isNotBlank() == true || predatorList?.isNotEmpty() == true) {
                    containerPredators.visibility = View.VISIBLE
                    if (mBiggestThread?.isNotBlank() == true) {
                        biggestThread.apply {
                            visibility = View.VISIBLE
                            animal?.biggestThread?.let { SpanUtil.createBulletList(listOf(it)) }
                        }
                    }
                    if (predatorList?.isNotEmpty() == true) {
                        predators.apply {
                            visibility = View.VISIBLE
                            SpanUtil.createBulletList(animal?.predators)
                        }
                    }
                }

                // Trivia
                trivia.text = SpanUtil.createBulletList(animal?.triviaPoints)
            }
        }

        private fun initGoogleMap() {
            if (!::map.isInitialized) return

            val polygons: MutableList<ArrayList<LatLng>> = mutableListOf()

            binding.animal?.coordinates?.forEach { polygon ->
                val currentPoints = arrayListOf<LatLng>()
                polygon.forEach { latlng ->
                    currentPoints.add(LatLng(latlng[0], latlng[1]))
                }
                polygons.add(currentPoints)
            }

            if (polygons.isNotEmpty()) {
                // Create bounds of first polygon
                val boundsBuilder = LatLngBounds.builder().apply {
                    polygons[0].forEach { point ->
                        include(point)
                    }
                }

                // Move camera to show all markers and locations
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 20))

                polygons.forEach {
                    map.addPolygon(
                        PolygonOptions()
                            .add(*it.toArray(arrayOf()))
                            .fillColor(Color.CYAN)
                            .strokeColor(Color.BLUE)
                            .strokeWidth(5f)
                    )
                }
            }
        }

        override fun onMapReady(googleMap: GoogleMap) {
            // return early if the map was not initialised properly
            map = googleMap
            initGoogleMap()
        }
    }
}
