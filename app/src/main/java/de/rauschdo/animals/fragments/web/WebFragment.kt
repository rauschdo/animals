package de.rauschdo.animals.fragments.web

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.webkit.HttpAuthHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rauschdo.animals.Constants
import de.rauschdo.animals.R
import de.rauschdo.animals.architecture.BaseFragment
import de.rauschdo.animals.databinding.FragmentWebBinding
import de.rauschdo.animals.utility.detailBinding

class WebFragment : BaseFragment<FragmentWebBinding>(R.layout.fragment_web) {

    private val args: WebFragmentArgs by navArgs()

    private var useNavHost = false

    companion object {
        fun create(url: String, useNavHost: Boolean) = WebFragment().apply {
            arguments = Bundle().apply {
                putString(Constants.EXTRA_URL, url)
                putBoolean(Constants.EXTRA_USES_NAV_HOST, useNavHost)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding {
            useNavHost = arguments?.getBoolean(Constants.EXTRA_USES_NAV_HOST, false) ?: false

            if (args.url != null || arguments?.containsKey(Constants.EXTRA_URL) == true) {
                webview.apply {
                    webViewClient = object : WebViewClient() {
                        override fun onReceivedHttpAuthRequest(
                            v: WebView?,
                            h: HttpAuthHandler?,
                            host: String?,
                            r: String?
                        ) {
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            webOverlayTitle.text = view?.title
                        }
                    }
                    settings.javaScriptEnabled = true

                    setOnKeyListener(object : View.OnKeyListener {
                        override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                            if (event.action == KeyEvent.ACTION_DOWN) {
                                (v as? WebView)?.let { webView ->
                                    when (keyCode) {
                                        KeyEvent.KEYCODE_BACK -> if (webView.canGoBack()) {
                                            webView.goBack()
                                            return true
                                        }
                                    }
                                }
                            }
                            return false
                        }
                    })

                    when {
                        args.url != null -> {
                            args.url?.let {
                                loadUrl(it)
                            }

                        }
                        arguments?.get(Constants.EXTRA_URL) != null -> {
                            arguments?.get(Constants.EXTRA_URL)?.let {
                                loadUrl(it as String)
                            }
                        }
                        else -> onBackPressed()
                    }
                }
            } else {
                destroyFragment()
            }
        }
    }

    private fun destroyFragment() {
        if (useNavHost) {
            findNavController().popBackStack()
        } else {
            detailBinding {
                containerFragment.visibility = View.GONE
            }
            parentFragmentManager.popBackStack()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                destroyFragment()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
