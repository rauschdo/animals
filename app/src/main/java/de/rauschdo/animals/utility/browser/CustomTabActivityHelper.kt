package de.rauschdo.animals.utility.browser

import android.app.Activity
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession

/**
 * This is a helper class to manage the connection to the Custom Tabs Service.
 */
class CustomTabActivityHelper(private val activity: Activity) : ServiceConnectionCallback {

    private var mCustomTabsSession: CustomTabsSession? = null

    private var mClient: CustomTabsClient? = null

    private var mConnection: CustomTabsServiceConnection? = null

    private var mConnectionCallback: ConnectionCallback? = null

    /**
     * Creates or retrieves an exiting CustomTabsSession.
     *
     * @return a CustomTabsSession.
     */
    private val session: CustomTabsSession?
        get() {
            if (mClient == null) {
                mCustomTabsSession = null
            } else if (mCustomTabsSession == null) {
                mCustomTabsSession = mClient?.newSession(null)
            }
            return mCustomTabsSession
        }

    companion object {
        /**
         * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
         *
         * @param activity The host activity.
         * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available.
         * @param url the Url to be opened.
         * @param activityWithNavHost if the activity uses an navHostFragment or not.
         * @param fallback a CustomTabFallback to be used if Custom Tabs is not available.
         */
        fun openCustomTab(
            activity: Activity,
            customTabsIntent: CustomTabsIntent,
            url: String,
            activityWithNavHost: Boolean,
            fallback: CustomTabFallback
        ) {
            val packageName = CustomTabsHelper.getPackageNameToUse(activity)
            val uri = Uri.parse(url)

            //If we cant find a package name, it means theres no browser that supports
            //Chrome Custom Tabs installed. So, we fallback to the webview
            if (packageName == null) {
                fallback.openUri(uri, activityWithNavHost)
            } else {
                customTabsIntent.intent.setPackage(packageName)
                //This can easily crash if url in data object has no protocol
                //i.e "www.google.com" instead of "http(s)://www.google.com" which is correct
                try {
                    customTabsIntent.launchUrl(activity, uri)
                } catch (e: ActivityNotFoundException) {
                    fallback.openUri(uri, activityWithNavHost)
                }
            }
        }
    }

    /**
     * Unbinds the Activity from the Custom Tabs Service
     */
    fun unbindCustomTabsService() {
        mConnection?.let {
            activity.unbindService(it)
        }
        mClient = null
        mCustomTabsSession = null
        mConnection = null
    }

    /**
     * Register a Callback to be called when connected or disconnected from the Custom Tabs Service.
     */
    fun setConnectionCallback(connectionCallback: ConnectionCallback?) {
        mConnectionCallback = connectionCallback
    }

    /**
     * Binds the Activity to the Custom Tabs Service.
     * @param activity the activity to be binded to the service.
     */
    fun bindCustomTabsService() {
        if (mClient != null) return
        val packageName: String = CustomTabsHelper.getPackageNameToUse(activity) ?: return
        ServiceConnection(this).apply {
            mConnection = this
            CustomTabsClient.bindCustomTabsService(activity, packageName, this)
        }
    }

    override fun onServiceConnected(client: CustomTabsClient?) {
        mClient = client
        mClient?.warmup(0L)
        if (mConnectionCallback != null) mConnectionCallback?.onCustomTabsConnected()
    }

    override fun onServiceDisconnected() {
        mClient = null
        mCustomTabsSession = null
        if (mConnectionCallback != null) mConnectionCallback?.onCustomTabsDisconnected()
    }

    /**
     * A Callback for when the service is connected or disconnected. Use those callbacks to
     * handle UI changes when the service is connected or disconnected.
     */
    interface ConnectionCallback {
        /**
         * Called when the service is connected.
         */
        fun onCustomTabsConnected()

        /**
         * Called when the service is disconnected.
         */
        fun onCustomTabsDisconnected()
    }

    /**
     * To be used as a fallback to open the Uri when Custom Tabs is not available.
     */
    interface CustomTabFallback {
        /**
         * @param uri The uri to be opened by the fallback.
         * @param activityWithNavHost if the activity uses an navHostFragment or not.
         */
        fun openUri(uri: Uri, activityWithNavHost: Boolean)
    }
}