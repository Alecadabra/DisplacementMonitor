package displacement.monitor.setup.android.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import displacement.monitor.setup.android.activity.SetupSlidePagerActivity

/**
 * An abstract implementation of a page of a view pager that has a custom title text for the
 * activity title bar. Designed to be placed in the frame of a [SetupSlidePagerActivity].
 * @param title The text to set as the activity's title
 */
abstract class AbstractSetupPageFragment(private val title: String) : Fragment() {

    /**
     * Convenience property to obtain the parent activity cast as a [SetupSlidePagerActivity].
     */
    protected val pagerActivity: SetupSlidePagerActivity
        get() {
            val localActivity = this.activity
                ?: error("Activity is null")
            return localActivity as? SetupSlidePagerActivity
                ?: error("Activity is not a ${SetupSlidePagerActivity::class.simpleName}")
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        this.activity?.title = this.title
    }
}