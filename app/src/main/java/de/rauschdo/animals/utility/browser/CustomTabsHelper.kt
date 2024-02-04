package de.rauschdo.animals.utility.browser

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation.findNavController
import de.rauschdo.animals.Constants
import de.rauschdo.animals.R
import de.rauschdo.animals.architecture.BaseActivity
import de.rauschdo.animals.fragments.web.WebFragment
import java.util.*

const val STABLE_PACKAGE = "com.android.chrome"
const val BETA_PACKAGE = "com.chrome.beta"
const val DEV_PACKAGE = "com.chrome.dev"
const val LOCAL_PACKAGE = "com.google.android.apps.chrome"

/**
 * Helper class for Custom Tabs.
 */
object CustomTabsHelper {

    private const val TAG = "CustomTabsHelper"

    val unsupportedList: List<String>
        get() = listOf("com.sec.android.app.sbrowser")

    /**
     * @return All possible chrome package names that provide custom tabs feature.
     */
    val packages: List<String>
        get() = listOf("", STABLE_PACKAGE, BETA_PACKAGE, DEV_PACKAGE, LOCAL_PACKAGE)

    private const val ACTION_CUSTOM_TABS_CONNECTION =
        "android.support.customtabs.action.CustomTabsService"

    private var sPackageNameToUse: String? = null

    /**
     * Builder function for [CustomTabsIntent]
     * @param activity              - The activity to launch the CustomTab in
     * @param url                   - The browser url to open
     * @param activityWithNavHost   - Determines how to handle fallback behaviour if customTabs cant be executed
     * @param containerId           - Fragment container for fallback
     */
    fun openCustomTab(
        activity: Activity,
        url: String,
        activityWithNavHost: Boolean,
        @IdRes containerId: Int? = null
    ) {
        val builder = CustomTabsIntent.Builder().apply {
            setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder().apply {
                    setNavigationBarColor(ContextCompat.getColor(activity, R.color.green_main))
                    setNavigationBarDividerColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.black
                        )
                    )
                    setToolbarColor(ContextCompat.getColor(activity, R.color.green_main))
                }.build()
            )


            setShareState(CustomTabsIntent.SHARE_STATE_ON)
            setShowTitle(true)
            setUrlBarHidingEnabled(false)

            // back button icon (can't set color)
            setCloseButtonIcon(
                BitmapFactory.decodeResource(
                    activity.resources,
                    R.drawable.ic_close
                )
            )

            // animation for enter and exit of tab
            setStartAnimations(activity, android.R.anim.fade_in, android.R.anim.fade_out)
            setExitAnimations(activity, android.R.anim.fade_in, android.R.anim.fade_out)
        }

        CustomTabActivityHelper.openCustomTab(
            activity,
            builder.build(),
            url,
            activityWithNavHost = activityWithNavHost,
            //A Fallback that opens a Webview when Custom Tabs is not available
            fallback = object : CustomTabActivityHelper.CustomTabFallback {
                override fun openUri(uri: Uri, activityWithNavHost: Boolean) {
                    if (activityWithNavHost) {
                        findNavController(activity, R.id.navHostFragment).navigate(
                            R.id.webFragment,
                            Bundle().apply {
                                putString(Constants.EXTRA_URL, url)
                            }
                        )
                    } else {
                        containerId?.let {
                            (activity as? BaseActivity)?.replaceFragmentAndAddToBackStack(
                                containerId = it,
                                fragment = WebFragment.create(url, activityWithNavHost)
                            )
                        } ?: executeLastResort(activity, url)
                    }
                }
            }
        )
    }

    private fun executeLastResort(activity: Activity, url: String) {
        with(activity) {
            Toast.makeText(
                this,
                "Keine Option für In-App Browsing gefunden.\nFallback auf externen Browser",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                )
            )
        }
    }

    /**
     * Goes through all apps that handle VIEW intents and have a warmup service. Picks
     * the one chosen by the user if there is one, otherwise makes a best effort to return a
     * valid package name.
     *
     * This is **not** threadsafe.
     *
     * @param context [Context] to use for accessing [PackageManager].
     * @return The package name recommended to use for connecting to custom tabs related components.
     */
    fun getPackageNameToUse(context: Context): String? {
        if (sPackageNameToUse != null) return sPackageNameToUse
        val pm = context.packageManager
        // Get default VIEW intent handler.
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
        val defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0)
        var defaultViewHandlerPackageName: String? = null
        if (defaultViewHandlerInfo != null) {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName
        }

        // Get all apps that can handle VIEW intents.
        val resolvedActivityList = pm.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs: MutableList<String?> = ArrayList()
        for (info in resolvedActivityList) {
            val serviceIntent = Intent()
            serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(info.activityInfo.packageName)
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName)
            }
        }

        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        if (packagesSupportingCustomTabs.isEmpty()) {
            sPackageNameToUse = null
        } else if (packagesSupportingCustomTabs.size == 1) {
            sPackageNameToUse = packagesSupportingCustomTabs[0]
        } else if (!TextUtils.isEmpty(defaultViewHandlerPackageName)
            && !hasSpecializedHandlerIntents(context, activityIntent)
            && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)
        ) {
            sPackageNameToUse = defaultViewHandlerPackageName
        } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
            sPackageNameToUse = STABLE_PACKAGE
        } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
            sPackageNameToUse = BETA_PACKAGE
        } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
            sPackageNameToUse = DEV_PACKAGE
        } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
            sPackageNameToUse = LOCAL_PACKAGE
        }
        return sPackageNameToUse
    }

    /**
     * Used to check whether there is a specialized handler for a given intent.
     * @param intent The intent to check with.
     * @return Whether there is a specialized handler for the given intent.
     */
    private fun hasSpecializedHandlerIntents(context: Context, intent: Intent): Boolean {
        try {
            val pm = context.packageManager
            val handlers = pm.queryIntentActivities(
                intent,
                PackageManager.GET_RESOLVED_FILTER
            )
            if (handlers.size == 0) {
                return false
            }
            for (resolveInfo in handlers) {
                val filter = resolveInfo.filter ?: continue
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue
                if (resolveInfo.activityInfo == null) continue
                return true
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, "Runtime exception while getting specialized handlers")
        }
        return false
    }
}