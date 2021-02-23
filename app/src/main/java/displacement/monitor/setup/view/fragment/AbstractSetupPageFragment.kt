package displacement.monitor.setup.view.fragment

import android.app.Activity
import androidx.fragment.app.Fragment
import displacement.monitor.settings.model.Settings
import displacement.monitor.setup.view.activity.SetupActivity

/**
 * An abstract implementation of a page of a view pager that has a custom title text for the
 * activity title bar. Designed to be placed in the frame of a [SetupActivity].
 */
abstract class AbstractSetupPageFragment : Fragment() {

    // Convenience members -------------------------------------------------------------------------

    /**
     * Convenience property to obtain the parent activity cast as a [SetupActivity].
     */
    protected val pagerSetupActivity: SetupActivity
        get() {
            val localActivity = this.activity
                ?: error("SetupActivity is null")
            return localActivity as? SetupActivity
                ?: error("SetupActivity is not a ${SetupActivity::class.simpleName}")
        }

    /** Access to app settings. */
    protected val settings: Settings by lazy { Settings(requireContext()) }

    // Android entry points ------------------------------------------------------------------------

    override fun onResume() {
        super.onResume()

        // Set the title from the field
        this.activity?.title = this.title

        // Update the state of the child
        updateState(canAdvance(this.pagerSetupActivity))
    }

    // Abstract functionality ----------------------------------------------------------------------

    /** The text to set as the activity's title. */
    abstract val title: String

    /**
     * Determines if the user can advance to the next setup page given the activity context.
     * @param activity The activity to provide context for the check
     * @return True if this setup page can be advanced from
     */
    abstract fun canAdvance(activity: Activity = this.pagerSetupActivity): Boolean

    /**
     * Update the current state of the views based on whether or not this setup page can be
     * advanced, based on the result from [canAdvance]. This is called in [onResume].
     * @param canAdvance Result from [canAdvance]
     */
    protected abstract fun updateState(canAdvance: Boolean)
}