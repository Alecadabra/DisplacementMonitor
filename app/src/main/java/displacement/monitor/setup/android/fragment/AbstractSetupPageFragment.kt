package displacement.monitor.setup.android.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import displacement.monitor.setup.android.activity.SetupSlidePagerActivity

abstract class AbstractSetupPageFragment(private val title: String) : Fragment() {

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