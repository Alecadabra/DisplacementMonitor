package displacement.monitor.setup.view.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import displacement.monitor.R
import displacement.monitor.scheduling.view.activity.ScheduledMeasurementActivity
import displacement.monitor.scheduling.controller.SchedulingManager
import displacement.monitor.settings.model.Settings
import displacement.monitor.setup.view.fragment.*

/**
 * SetupActivity to hold and manage the view pager that shows all of the setup screen pages.
 */
class SetupActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    /** References to views. */
    private val views by lazy(this::Views)

    // Android entry points ------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_slide_pager)

        // Set up view pager
        this.views.viewPager.adapter = ScreenSlidePagerAdapter(this)
        this.views.viewPager.isUserInputEnabled = false
    }

    override fun finish() {
        // Cancel any scheduling
        SchedulingManager(
            this, Settings(this), ScheduledMeasurementActivity.getIntent(this)
        ).cancel()

        super.finish()
    }

    // Public entry points -------------------------------------------------------------------------

    /**
     * Go back one page.
     * @throws IllegalStateException If at the first page and cannot go back
     */
    fun pageBack() {
        check(this.views.viewPager.currentItem > 0) {
            "Cannot go back any further"
        }
        this.views.viewPager.setCurrentItem(this.views.viewPager.currentItem - 1, true)
    }

    /**
     * Go forward one page.
     * @throws IllegalStateException If at the last page and cannot go to next
     */
    fun pageNext() {
        check(this.views.viewPager.currentItem < PAGE_CLASSES.lastIndex) {
            "Cannot go forward any further"
        }
        this.views.viewPager.setCurrentItem(this.views.viewPager.currentItem + 1, true)
    }

    // Local constructs ----------------------------------------------------------------------------

    /**
     * Adapter for the [Views.viewPager].
     */
    private inner class ScreenSlidePagerAdapter(
        activity: FragmentActivity
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = PAGE_CLASSES.size

        override fun createFragment(position: Int): Fragment = PAGE_CLASSES[position].java.newInstance()
    }

    /**
     * Wrapper for view references.
     */
    private inner class Views(
        val viewPager: ViewPager2 = findViewById(R.id.setupSlidePagerActivityPager)
    )

    companion object {
        /**
         * Get an intent to use to start this activity.
         * @param c Context being called from
         * @return New intent to start this activity with
         */
        fun getIntent(c: Context) = Intent(c, SetupActivity::class.java)

        /**
         * Access to all of the 'pages' of the [Views.viewPager].
         */
        val PAGE_CLASSES = listOf(
            PermissionsSetupFragment::class,
            SettingsSetupFragment::class,
            CalibrationSetupFragment::class,
        )
    }
}