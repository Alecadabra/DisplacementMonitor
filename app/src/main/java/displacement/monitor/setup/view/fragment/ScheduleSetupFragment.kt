package displacement.monitor.setup.view.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import displacement.monitor.R
import displacement.monitor.scheduling.view.activity.ScheduledMeasurementActivity
import displacement.monitor.scheduling.controller.DeviceStateController
import displacement.monitor.scheduling.controller.SchedulingManager
import displacement.monitor.settings.view.activity.SettingsActivity
import displacement.monitor.settings.model.Settings
import displacement.monitor.setup.view.activity.CalibrationActivity
import displacement.monitor.setup.view.activity.RealTimeMeasurementActivity

/**
 * An [AbstractSetupPageFragment] for performing final tests/configuration and starting the
 * scheduling, as the final part of the setup procedure.
 */
class ScheduleSetupFragment : AbstractSetupPageFragment() {

    // Members -------------------------------------------------------------------------------------

    /** References to views. */
    private val views by lazy { Views(requireView()) }

    /** Handles scheduling the measurements. */
    private val scheduleManager: SchedulingManager by lazy {
        val localContext = requireContext()
        SchedulingManager(
            context = localContext,
            settings = Settings(localContext),
            scheduledIntent = ScheduledMeasurementActivity.getIntent(localContext)
        )
    }

    /** Handles locking the screen once scheduling starts. */
    private val deviceStateController: DeviceStateController by lazy {
        DeviceStateController(requireActivity())
    }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule_setup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.views.realTimeBtn.setOnClickListener {
            startActivity(RealTimeMeasurementActivity.getIntent(requireContext()))
        }
        this.views.calibrateBtn.setOnClickListener {
            startActivity(CalibrationActivity.getIntent(requireContext()))
        }
        this.views.measurementBtn.setOnClickListener {
            startActivity(ScheduledMeasurementActivity.getIntent(requireContext(), false))
        }
        this.views.settingsBtn.setOnClickListener {
            startActivity(SettingsActivity.getIntent(requireContext()))
        }
        this.views.stopBtn.setOnClickListener {
            this.scheduleManager.cancel()
        }
        this.views.backBtn.setOnClickListener {
            this.pagerSetupActivity.pageBack()
        }
        this.views.scheduleBtn.setOnClickListener {
            this.scheduleManager.start()
            this.deviceStateController.lockScreen()
        }
    }

    // Setup page overrides ------------------------------------------------------------------------

    override val title: String = "Schedule Measurements"

    override fun canAdvance(activity: Activity): Boolean = true

    override fun updateState(canAdvance: Boolean) = Unit

    // Local constructs ----------------------------------------------------------------------------

    /**
     * Wrapper for view references.
     */
    private inner class Views(
        view: View,
        val realTimeBtn: Button = view.findViewById(R.id.scheduleSetupFragmentRealTimeBtn),
        val calibrateBtn: Button = view.findViewById(R.id.scheduleSetupFragmentCalibrateBtn),
        val measurementBtn: Button = view.findViewById(R.id.scheduleSetupFragmentMeasurementBtn),
        val settingsBtn: Button = view.findViewById(R.id.scheduleSetupFragmentSettingsBtn),
        val stopBtn: Button = view.findViewById(R.id.scheduleSetupFragmentStopBtn),
        val backBtn: Button = view.findViewById(R.id.scheduleSetupFragmentBackButton),
        val scheduleBtn: Button = view.findViewById(R.id.scheduleSetupFragmentScheduleButton),
    )

    companion object {
        private const val TAG = "ScheduleSetup"
    }
}