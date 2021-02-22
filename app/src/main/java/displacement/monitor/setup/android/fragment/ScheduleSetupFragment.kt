package displacement.monitor.setup.android.fragment

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import displacement.monitor.R
import displacement.monitor.permissions.controller.Permission
import displacement.monitor.scheduling.android.activity.ScheduledMeasurementActivity
import displacement.monitor.scheduling.controller.SchedulingManager
import displacement.monitor.settings.android.activity.SettingsActivity
import displacement.monitor.settings.model.Settings
import displacement.monitor.setup.android.activity.CalibrationActivity
import displacement.monitor.setup.android.activity.RealTimeMeasurementActivity

class ScheduleSetupFragment : AbstractSetupPageFragment("Schedule Measurements") {

    private val views by lazy { Views(requireView()) }

    private val scheduleManager by lazy {
        val localContext = requireContext()
        SchedulingManager(
            context = localContext,
            settings = Settings(localContext),
            scheduledIntent = ScheduledMeasurementActivity.getIntent(localContext)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_schedule_setup, container, false)
    }

    override fun onResume() {
        super.onResume()

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
            this.pagerActivity.pageBack()
        }
        this.views.scheduleBtn.setOnClickListener {
            this.scheduleManager.start()
            // Lock screen
            try {
                val localContext = requireContext()
                if (Permission.ADMIN.isGrantedTo(localContext)) {
                    val policyKey = Context.DEVICE_POLICY_SERVICE
                    val policyManager = localContext.getSystemService(policyKey) as DevicePolicyManager
                    policyManager.lockNow()
                } else {
                    Log.e(TAG, "Cannot lock screen (Not admin)")
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Could not turn off screen (Security Exception)", e)
            }
        }
    }

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