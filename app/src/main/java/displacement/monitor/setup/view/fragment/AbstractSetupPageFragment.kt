package displacement.monitor.setup.view.fragment

import android.app.Activity
import androidx.fragment.app.Fragment
import displacement.monitor.settings.model.Settings
import displacement.monitor.setup.view.activity.SetupActivity

/*
   Copyright 2021 Alec Maughan

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

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