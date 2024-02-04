package de.rauschdo.animals.architecture

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.skydoves.bindables.BindingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BaseFragment<T : ViewDataBinding>(
    layoutId: Int = 0
) : BindingFragment<T>(layoutId), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel job on activity destroy. After destroy all children jobs will be cancelled automatically
    }

    fun getCurrentContext(): Context? = context

    fun withAppCompatActivity(func: AppCompatActivity.(ActionBar?) -> Unit) {
        (activity as? AppCompatActivity)?.let {
            it.func(it.supportActionBar)
        }
    }

    open fun onBackPressed() {
        findNavController().navigateUp()
    }

    fun navigate(directions: NavDirections, options: NavOptions? = null) =
        with(findNavController()) {
            currentDestination?.getAction(directions.actionId)
                ?.let { navigate(directions, options) }
        }

    fun navigate(
        destinationId: Int,
        bundle: Bundle?,
        popBackstack: Boolean = false,
        options: NavOptions? = null
    ) =
        with(findNavController()) {
            if (popBackstack) {
                popBackStack()
            }
            navigate(destinationId, bundle, options)
        }

    fun createOptions(func: NavOptions.Builder.() -> Unit): NavOptions {
        return NavOptions.Builder().let {
            it.func()
            it.build()
        }
    }
}