package de.rauschdo.animals.activities.detail

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import dagger.hilt.android.AndroidEntryPoint
import de.rauschdo.animals.Constants
import de.rauschdo.animals.R
import de.rauschdo.animals.architecture.BaseActivity
import de.rauschdo.animals.custom.pager2transformers.ParallaxTransformer
import de.rauschdo.animals.data.viewmodel.AnimalViewModel
import de.rauschdo.animals.database.Animal
import de.rauschdo.animals.databinding.ActivityDetailBinding
import de.rauschdo.animals.databinding.DialogDetailPagerDirectionBinding
import de.rauschdo.animals.fragments.detail.image.DetailImageFragment
import de.rauschdo.animals.utility.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class DetailActivity : BaseActivity() {

    val binding by viewBinding(ActivityDetailBinding::inflate)

    internal val animalViewModel: AnimalViewModel by viewModels()

    private var pageChangedCallback: ViewPager2.OnPageChangeCallback? = null

    private var pagerItems = listOf<Animal>()

    private var currentPagerDirection = ViewPager2.ORIENTATION_HORIZONTAL

    override fun onCreate(savedInstanceState: Bundle?) {
        with(binding) {
            intent?.let {
                val transitionId = intent?.getIntExtra(Constants.EXTRA_TRANSITION, 0)
                detailCoordinator.transitionName = transitionId.toString()
                setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
                window.sharedElementEnterTransition = buildContainerTransform()
                window.sharedElementReturnTransition = buildContainerTransform()

                setContentView(root)
                super.onCreate(savedInstanceState)

                addMenu()

                val idOfSelectedAnimal = it.getLongExtra(Constants.EXTRA_ANIMAL_ID, -1L)
                val idOfCategory = it.getLongExtra(Constants.EXTRA_CATEGORY_ID, -1L)
                val allowPaging = it.getBooleanExtra(Constants.EXTRA_ENABLE_PAGER, true)

                if (allowPaging) {
                    initAnimationSelector()
                }

                animalViewModel.itemList.observe(this@DetailActivity, observerOfNonNull { list ->
                    val categoryModel =
                        list.find { category -> category.category.categoryId == idOfCategory }
                    val animal =
                        categoryModel?.animals?.find { animal -> animal.id == idOfSelectedAnimal }

                    model = categoryModel

                    initUI(initialAnimal = animal, allowPaging)
                })
            }
        }
    }

    private fun addMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_detail, menu)
                menu.showOptionalIcons()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_directions -> {
                        val builder =
                            MaterialAlertDialogBuilder(this@DetailActivity, R.style.DialogAnimation)
                        val dialogBinding =
                            DialogDetailPagerDirectionBinding.inflate(layoutInflater)
                        builder.setView(dialogBinding.root)
                        builder.create().apply {
                            dialogBinding.radiogroup.apply {
                                check(
                                    when (currentPagerDirection) {
                                        ViewPager2.ORIENTATION_HORIZONTAL -> R.id.pagerDirectionHorizontal
                                        ViewPager2.ORIENTATION_VERTICAL -> R.id.pagerDirectionVertical
                                        else -> -1
                                    }
                                )
                                setOnCheckedChangeListener { _, checkedId ->
                                    when (checkedId) {
                                        R.id.pagerDirectionHorizontal -> {
                                            currentPagerDirection =
                                                ViewPager2.ORIENTATION_HORIZONTAL
                                        }
                                        R.id.pagerDirectionVertical -> {
                                            currentPagerDirection = ViewPager2.ORIENTATION_VERTICAL
                                        }
                                    }
                                    dismiss()
                                }
                            }
                            dialogBinding.btnClose.setOnClickListener {
                                dismiss()
                            }
                            setOnDismissListener {
                                binding.animalCategoryPager.orientation = currentPagerDirection
                            }
                            this.window?.setWindowAnimations(R.style.DialogAnimation)
                            show()
                        }
                    }
                    R.id.action_sheet -> {
                        openBottomSheet()
                    }
                    R.id.action_license -> {
                        startActivity(
                            Intent(
                                this@DetailActivity,
                                OssLicensesMenuActivity::class.java
                            )
                        )
                    }
                }
                return true
            }
        })
    }

    private fun initAnimationSelector() {
        with(binding) {
            animationSelector.visibility = View.VISIBLE
            animationSelection.apply {
                val adapter = ArrayAdapter(
                    this@DetailActivity,
                    R.layout.item_dropdown_popup,
                    resources.getStringArray(R.array.transformations)
                )

                setAdapter(adapter)

                val applyNewTransformer = { transformer: ViewPager2.PageTransformer? ->
                    initPager(
                        true,
                        null,
                        pageTransformer = transformer,
                        reset = true
                    )
                }
                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    when (val selectedText = getAdapter().getItem(position).toString()) {
                        getString(R.string.noTransformer) -> {
                            applyNewTransformer(null)
                        }
                        getString(R.string.ParallaxTransformer) -> {
                            applyNewTransformer(ParallaxTransformer(R.id.containerContent))
                        }
                        else -> {
                            try {
                                val transformerClass =
                                    Class
                                        .forName("de.rauschdo.animals.custom.pager2transformers.${selectedText}Transformer")
                                        .newInstance() as? ViewPager2.PageTransformer
                                applyNewTransformer(transformerClass)
                            } catch (e: ClassNotFoundException) {
                                Toast.makeText(
                                    this@DetailActivity,
                                    "Animator Class not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        binding.image.visibility = View.GONE
    }

    //Callback to know when sharedElementTransition is finished
    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        launch {
            delay(250)
            withContext(Dispatchers.Main) {
                with(binding) {
                    val animPager = {
                        animalCategoryPager.apply {
                            visibility = View.VISIBLE
                            startAnimation(
                                AnimationUtils.loadAnimation(
                                    this@DetailActivity,
                                    R.anim.text_fade_in
                                )
                            )
                        }
                    }
                    image.apply {
                        val anim =
                            AnimationUtils.loadAnimation(this@DetailActivity, R.anim.text_fade_out)
                                .apply {
                                    setAnimationListener(object : Animation.AnimationListener {
                                        override fun onAnimationStart(animation: Animation?) {
                                            animPager()
                                        }

                                        override fun onAnimationEnd(animation: Animation?) {
                                            image.apply {
                                                visibility = View.INVISIBLE
                                                clearAnimation()
                                            }
                                        }

                                        override fun onAnimationRepeat(animation: Animation?) {}
                                    })
                                }
                        startAnimation(anim)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        // revert animation from onEnterAnimationComplete before starting sharedElementTransition
        with(binding) {
            //optional TODO later first let pager return to page one
            val executeSuper = {
                super.onBackPressed()
            }

            if (!isBackstackEmpty()) {
                executeSuper()
            } else {
                val animImage = {
                    val anim =
                        AnimationUtils.loadAnimation(this@DetailActivity, R.anim.text_fade_in)
                            .apply {
                                setAnimationListener(object : Animation.AnimationListener {
                                    override fun onAnimationEnd(animation: Animation?) {
                                        binding.image.clearAnimation()
                                        executeSuper()
                                    }

                                    override fun onAnimationStart(animation: Animation?) {}
                                    override fun onAnimationRepeat(animation: Animation?) {}
                                })
                            }
                    image.apply {
                        visibility = View.VISIBLE
                        startAnimation(anim)
                    }
                }
                animalCategoryPager.apply {
                    val anim =
                        AnimationUtils.loadAnimation(this@DetailActivity, R.anim.text_fade_out)
                            .apply {
                                setAnimationListener(object : Animation.AnimationListener {
                                    override fun onAnimationStart(animation: Animation?) {
                                        animImage()
                                    }

                                    override fun onAnimationEnd(animation: Animation?) {
                                        animalCategoryPager.apply {
                                            visibility = View.GONE
                                            clearAnimation()
                                        }
                                    }

                                    override fun onAnimationRepeat(animation: Animation?) {}
                                })
                            }
                    startAnimation(anim)
                }
            }
        }
    }

    private fun initUI(initialAnimal: Animal?, allowPaging: Boolean) {
        with(binding) {
            setSupportActionBar(toolbar)
            toolbar.apply {
                setNavigationOnClickListener { onBackPressed() }
            }

            model?.let { itModel ->
                icon.loadImage(
                    this@DetailActivity,
                    this@DetailActivity.idOf(itModel.category.assetId, "drawable")
                )

                image.loadImageNetwork(
                    this@DetailActivity,
                    initialAnimal?.imageUrls?.get(0) ?: "",
                    R.drawable.ic_component_placeholder
                )

                //Pager
                initPager(allowPaging, initialAnimal)

                updateSource(initialAnimal)
            }
        }
    }

    private fun initPager(
        allowPaging: Boolean,
        initialAnimal: Animal?,
        pageTransformer: ViewPager2.PageTransformer? = null,
        reset: Boolean = false
    ) {
        with(binding) {
            model?.let { itModel ->
                pagerItems = if (allowPaging) itModel.animals else initialAnimal?.let { listOf(it) }
                    ?: emptyList()
                val adapterRef = AnimalDetailAdapter(object : AnimalDetailAdapter.ClickListener {
                    override fun onFullscreenImage(currentUrl: String?) {
                        currentUrl?.let {
                            replaceFragmentAndAddToBackStack(
                                containerFragment.id,
                                DetailImageFragment.newInstance(it)
                            )
                        }
                    }
                }).apply {
                    submitList(pagerItems)
                }

                animalCategoryPager.apply {
                    adapter = adapterRef
                    registerPageChangedCallback()
                    initialAnimal?.let {
                        currentItem = pagerItems.indexOf(it)
                    }
                    pageTransformer?.let {
                        setPageTransformer(it)
                    } ?: if (reset) setPageTransformer(null)
                }
            }
        }
    }

    private fun updateSource(newAnimal: Animal?) {
        newAnimal?.let {
            binding.source.apply {
                val preparedUrlSpans = SpanUtil.prepareShortendUrls(newAnimal.urls)
                val textToSpan =
                    getString(R.string.disclaimer_source, preparedUrlSpans.joinToString("\n"))
                text = textToSpan

                var mSpan: SpannableString? = null

                preparedUrlSpans.forEachIndexed { index, spanned ->
                    when (index) {
                        0 -> {
                            mSpan = SpanUtil.setDeeplinkSpan(
                                context = context,
                                stringToSearch = text.toString(),
                                spanText = spanned,
                                func = {
                                    openCustomTab(
                                        newAnimal.urls[index],
                                        activityWithNavHost = false,
                                        containerId = R.id.containerFragment
                                    )
                                }
                            )
                        }
                        else -> {
                            mSpan?.let { currentSpannedString ->
                                mSpan = SpanUtil.setAdditionalDeeplinkSpan(
                                    context = context,
                                    stringToSearch = currentSpannedString,
                                    spanText = spanned,
                                    func = {
                                        openCustomTab(
                                            newAnimal.urls[index],
                                            activityWithNavHost = false,
                                            containerId = R.id.containerFragment
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                text = mSpan
                movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    private fun buildContainerTransform() =
        MaterialContainerTransform().apply {
            addTarget(binding.detailCoordinator)
            duration = 300
            interpolator = FastOutSlowInInterpolator()
            fadeMode = MaterialContainerTransform.FADE_MODE_IN
        }

    private inline fun registerCallback(
        func: () -> Unit = {}
    ) {
        unregisterPageChangedCallback(pageChangedCallback)
        func()
        registerPageChangedCallback()
    }

    private fun registerPageChangedCallback() {
        definePageChangedCallback().let {
            binding.animalCategoryPager.registerOnPageChangeCallback(it)
        }
    }

    private fun unregisterPageChangedCallback(oldCallback: ViewPager2.OnPageChangeCallback?) {
        if (oldCallback != null) {
            binding.animalCategoryPager.unregisterOnPageChangeCallback(oldCallback)
        }
    }

    private fun definePageChangedCallback(): ViewPager2.OnPageChangeCallback {
        return object : ViewPager2.OnPageChangeCallback() {
            init {
                pageChangedCallback = this
            }

            override fun onPageSelected(position: Int) {
                binding.toolbar.title = pagerItems[position].name
                updateSource(pagerItems[position])
            }
        }
    }
}
