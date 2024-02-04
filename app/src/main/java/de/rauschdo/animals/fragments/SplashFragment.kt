package de.rauschdo.animals.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import androidx.fragment.app.activityViewModels
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.transition.MaterialSharedAxis
import de.rauschdo.animals.R
import de.rauschdo.animals.architecture.BaseFragment
import de.rauschdo.animals.data.viewmodel.AnimalViewModel
import de.rauschdo.animals.databinding.FragmentSplashBinding
import de.rauschdo.animals.databinding.LayoutIntroDialogBinding
import de.rauschdo.animals.utility.DataUtil
import de.rauschdo.animals.utility.MotionUtil
import de.rauschdo.animals.utility.NavigationPath
import de.rauschdo.animals.utility.mainBinding
import de.rauschdo.animals.utility.preference.AppPreference
import de.rauschdo.animals.utility.preference.PreferencesUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SplashFragment : BaseFragment<FragmentSplashBinding>(R.layout.fragment_splash) {

    private val viewModel by activityViewModels<AnimalViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MotionUtil.createAxisTransition(
            startId = R.id.splashRootAnimStart,
            endId = R.id.categoryRootAnimEnd,
            axis = MaterialSharedAxis.Z,
            entering = false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { itContext ->
            launch {
                withContext(Dispatchers.Main) {
                    // Workaround: IllegalStateException: Method addObserver must be called on the main thread.
                    //             because of lazy viewModel Init and room per default not accessible
                    //             from MainThread when not explicitly set to be allowed
                    viewModel.toString()

                    if (!AppPreference.hasShownIntro(itContext)) {
                        suspendCoroutine<Boolean> { continuation ->
                            showIntroDialog(continuation)
                        }
                    }

                    binding.animationView.apply {
                        visibility = View.VISIBLE
                        playAnimation()
                    }
                }

                //Fill Database
                if (!PreferencesUtil.hasImportedData(itContext)) {
                    withContext(Dispatchers.IO) {
                        val success = DataUtil.readDataAndInsertInDB(resources, viewModel)
                        if (!success) {
                            Toast.makeText(
                                itContext,
                                "Error populating Database",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            PreferencesUtil.saveDataImport(itContext)
                        }
                    }
                }

                delay(2500)

                //navigate
                withContext(Dispatchers.Main) {
                    navigate(NavigationPath.SPLASH_TO_CATEGORY())
                }

                delay(125)

                withContext(Dispatchers.Main) {
                    mainBinding {
                        navHostFragment.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            updateMargins(
                                0,
                                top = activity?.actionBar?.height
                                    ?: resources.getDimensionPixelSize(R.dimen.view_size_56),
                                0,
                                bottom = resources.getDimensionPixelSize(R.dimen.view_size_56)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showIntroDialog(continuation: Continuation<Boolean>) {
        context?.let { itContext ->
            AlertDialog.Builder(itContext, R.style.DialogTheme_Compat).apply {
                setView(LayoutIntroDialogBinding.inflate(LayoutInflater.from(itContext)).root)
                setCancelable(false)
            }.also {
                val dialog = it.create()
                dialog.show()

                val timerTask = object : TimerTask() {
                    override fun run() {
                        launch(Dispatchers.Main) {
                            dialog.findViewById<LottieAnimationView>(R.id.anim)?.apply {
                                playAnimation()
                            }
                        }
                    }
                }
                val syncTimer = Timer()
                syncTimer.schedule(timerTask, 0L, 4000L)

                dialog.findViewById<View>(R.id.btnNext)?.setOnClickListener {
                    dialog.dismiss()
                    AppPreference.saveIntroShown(itContext)
                    continuation.resume(true)
                }
            }
        } ?: continuation.resume(true)
    }
}
