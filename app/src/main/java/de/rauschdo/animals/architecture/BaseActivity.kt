package de.rauschdo.animals.architecture

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup
import de.rauschdo.animals.R
import de.rauschdo.animals.utility.browser.CustomTabActivityHelper
import de.rauschdo.animals.utility.browser.CustomTabsHelper
import de.rauschdo.animals.utility.preference.AppPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BaseActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    private lateinit var job: Job

    private var mCustomTabActivityHelper: CustomTabActivityHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
        mCustomTabActivityHelper = CustomTabActivityHelper(this)
        if (AppPreference.shouldShowBottomSheet(this)) {
            openBottomSheet()
            AppPreference.deleteBottomSheetReminder(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel job on activity destroy. After destroy all children jobs will be cancelled automatically
    }

    override fun onStart() {
        super.onStart()
        mCustomTabActivityHelper?.bindCustomTabsService()
    }

    override fun onStop() {
        super.onStop()
        mCustomTabActivityHelper?.unbindCustomTabsService()
    }

    fun openCustomTab(url: String, activityWithNavHost: Boolean, containerId: Int? = null) {
        CustomTabsHelper.openCustomTab(
            activity = this,
            url = url,
            activityWithNavHost = activityWithNavHost,
            containerId = containerId
        )
    }

    fun openBottomSheet(activityRef: Activity? = this) {
        BottomSheetDialog(this).apply {
            setContentView(R.layout.settings_bottomsheet_dialog)
            dismissWithAnimation = true
        }.also { dialog ->
            dialog.show()
            dialog.findViewById<MaterialButtonToggleGroup>(R.id.groupNightModeDelegate)?.apply {
                check(
                    when (AppPreference.getPreferedUiMode(this@BaseActivity)) {
                        AppCompatDelegate.MODE_NIGHT_NO -> R.id.nightModeNo
                        AppCompatDelegate.MODE_NIGHT_YES -> R.id.nightModeYes
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> R.id.nightModeSystemDefault
                        else -> -1
                    }
                )

                val checkedChangedFunc = { constant: Int ->
                    dialog.dismiss()
                    AppPreference.rememberBottomSheetRecreate(this@BaseActivity)
                    AppPreference.savePreferedUiMode(this@BaseActivity, constant)
                    AppCompatDelegate.setDefaultNightMode(constant)
                    activityRef?.recreate()
                }
                addOnButtonCheckedListener { group, checkedId, isChecked ->
                    // only process triggered event of button that is checked
                    if (isChecked) {
                        when (checkedId) {
                            R.id.nightModeNo -> checkedChangedFunc(AppCompatDelegate.MODE_NIGHT_NO)
                            R.id.nightModeYes -> checkedChangedFunc(AppCompatDelegate.MODE_NIGHT_YES)
                            R.id.nightModeSystemDefault -> checkedChangedFunc(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        }
                    }
                }
            }
        }
    }

    fun replaceFragmentAndAddToBackStack(containerId: Int, fragment: Fragment) {
        findViewById<View>(containerId).visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }
}
