package de.rauschdo.animals.fragments.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.transition.MaterialFadeThrough
import de.rauschdo.animals.BuildConfig
import de.rauschdo.animals.R
import de.rauschdo.animals.activities.MainActivity
import de.rauschdo.animals.architecture.BaseActivity
import de.rauschdo.animals.architecture.BaseFragment
import de.rauschdo.animals.custom.OptionItem
import de.rauschdo.animals.databinding.FragmentSettingsBinding
import de.rauschdo.animals.utility.*
import de.rauschdo.animals.utility.browser.CustomTabsHelper
import de.rauschdo.animals.utility.preference.NotificationPreference
import de.rauschdo.animals.workers.Notification
import org.threeten.bp.LocalDateTime
import java.util.*

class SettingsFragment : BaseFragment<FragmentSettingsBinding>(R.layout.fragment_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(false)

        activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_app_settings, menu)
                menu.showOptionalIcons()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    android.R.id.home -> {
                        (activity as? MainActivity)?.onSupportNavigateUp()
                    }
                    R.id.action_sheet -> {
                        (activity as? BaseActivity)?.openBottomSheet()
                    }
                    R.id.action_license -> {
                        startActivity(
                            Intent(
                                this@SettingsFragment.context,
                                OssLicensesMenuActivity::class.java
                            )
                        )
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding {
            devLayout.root.apply {
                playAnimation(
                    animResId = R.anim.item_animation_slide_from_right
                )
                visibility = View.VISIBLE
            }

            devLayout.settingsShimmer.apply {
                startShimmer()
                showShimmer(true)
            }

            initWebLinks()

            ordering.setOnClickListener { navigate(NavigationPath.SETTINGS_TO_ORDER()) }

            notificationEnabler.apply {
                val notificationEnabled = NotificationPreference.hasNotificationEnabled(context)
                setChecked(notificationEnabled)
                handleDatePicker(notificationEnabled)

                setToggleListener { _, isChecked ->
                    NotificationPreference.saveNotificationEnabled(context, isChecked)
                    handleDatePicker(isChecked)
                    if (!isChecked) {
                        Notification.cancelAlarm(context)
                    } else {
                        Notification.setAlarm(context)
                    }
                }
                setOnClickListener {
                    toggle()
                }
            }

            contactDevEmail.setOnAntiSpamClick {
                activity?.let { itActivity ->
                    val version =
                        getString(R.string.app_info_version) + ": " + "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})" + "\n"
                    val deviceName =
                        getString(R.string.app_info_device_name) + ": " + Build.MANUFACTURER + " " + Build.MODEL
                    val androidVersion =
                        getString(R.string.app_info_android_version) + ": " + Build.VERSION.RELEASE + "\n"
                    val mailText = version + androidVersion + deviceName + "\n"

                    try {
                        startActivity(
                            Intent.createChooser(
                                itActivity.shareIntentBuilder {
                                    addEmailTo(getString(R.string.dev_email))
                                    setType("message/rfc822")
                                    setSubject(getString(R.string.app_name))
                                    setText(mailText)
                                },
                                getString(R.string.get_in_contact)
                            ),
                            null
                        )
                    } catch (ex: ActivityNotFoundException) {
                        Toast.makeText(activity, "Keine Mail-App installiert", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }

    private fun initWebLinks() {
        val weblink = { url: String ->
            (activity as? BaseActivity)?.openCustomTab(url, true)
        }
        binding {
            devLayout.insta.setOnAntiSpamClick { weblink(it.contentDescription.toString()) }
            devLayout.twitter.setOnAntiSpamClick { weblink(it.contentDescription.toString()) }
            devLayout.linkedin.setOnAntiSpamClick { weblink(it.contentDescription.toString()) }
            devLayout.github.setOnAntiSpamClick { weblink(it.contentDescription.toString()) }
        }
    }

    private fun handleDatePicker(needsInit: Boolean) {
        binding {
            val setNewSubtitle = { view: OptionItem, date: LocalDateTime ->
                view.setTexts(
                    pSubtitle = SpanUtil.setDeeplinkSpan(
                        context = context,
                        stringToSearch = getString(
                            R.string.settings_notification_description,
                            DateUtil.convertDateTimeToString(
                                date,
                                DateUtil.FORMAT_TIME
                            )
                        ),
                        spanText = getString(R.string.notification_url_span),
                        func = {
                            CustomTabsHelper.openCustomTab(
                                activity as BaseActivity,
                                url = getString(R.string.notification_url),
                                activityWithNavHost = true
                            )
                        }
                    )
                )
            }

            notificationTime.apply {
                visibility = needsInit.getVisibility()

                val millis = NotificationPreference.getNotificationTime(context)
                if (millis == 0L) {
                    NotificationPreference.saveNotificationTime(context, Date().time)
                }
                val date = DateUtil.convertMillisecondsToLocalDateTime(millis)

                setNewSubtitle(this, date)

                if (needsInit) {
                    setOnAntiSpamClick {
                        val notificationDate = DateUtil.convertMillisecondsToLocalDateTime(
                            NotificationPreference.getNotificationTime(context)
                        )

                        val timePicker = MaterialTimePicker.Builder().apply {
                            setTimeFormat(TimeFormat.CLOCK_24H)
                            setHour(notificationDate.hour)
                            setMinute(notificationDate.minute)
                        }.build()

                        timePicker.show(childFragmentManager, "time_picker")

                        timePicker.addOnPositiveButtonClickListener {
                            val now = LocalDateTime.now()

                            val newDate = LocalDateTime.of(
                                now.year,
                                now.month,
                                now.dayOfMonth,
                                timePicker.hour,
                                timePicker.minute
                            )

                            NotificationPreference.saveNotificationTime(
                                context,
                                newDate.toMillis()
                            )

                            setNewSubtitle(this, newDate)

//                            NotificationWorker.scheduleWork(context)
                            Notification.setAlarm(context)
                        }
                    }
                }
            }

            settingUseFavorites.apply {
                visibility = needsInit.getVisibility()

                val notificationFavortieEnabled =
                    NotificationPreference.shouldNotificationUseFavorites(context)
                setChecked(notificationFavortieEnabled)

                setToggleListener { _, isChecked ->
                    NotificationPreference.saveNotificationUseFavorites(context, isChecked)
                }
                setOnClickListener {
                    toggle()
                }
            }
        }
    }
}
