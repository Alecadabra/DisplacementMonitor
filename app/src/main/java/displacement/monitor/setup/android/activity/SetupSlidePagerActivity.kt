package displacement.monitor.setup.android.activity

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
import displacement.monitor.scheduling.android.activity.ScheduledMeasurementActivity
import displacement.monitor.scheduling.controller.SchedulingManager
import displacement.monitor.settings.model.Settings
import displacement.monitor.setup.android.fragment.*

class SetupSlidePagerActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    /** References to views. */
    private val views by lazy(this::Views)

    /**
     * Exit conformation dialog to show when back is pressed.
     */
    private val backDialog by lazy {
        AlertDialog.Builder(this).also { builder ->
            builder.setTitle("Leave app")
            builder.setMessage("Do you want to exit the app?")
            builder.setNegativeButton("No, Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            builder.setPositiveButton("Yes, exit") { _, _ ->
                super.onBackPressed()
            }
        }.create()
    }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_slide_pager)

        // Set up view pager
        this.views.viewPager.adapter = ScreenSlidePagerAdapter(this)
        this.views.viewPager.isUserInputEnabled = false
    }

    override fun onBackPressed() {
        // Show exit conformation dialog
        this.backDialog.show()
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
     * Go back one page, or presses back if on the first page.
     */
    fun pageBack() {
        if (this.views.viewPager.currentItem > 0) {
            this.views.viewPager.setCurrentItem(this.views.viewPager.currentItem - 1, true)
        } else {
            onBackPressed()
        }
    }

    /**
     * Go forward one page, or do nothing if at the last page.
     */
    fun pageNext() {
        if (this.views.viewPager.currentItem < Pages.lastIndex) {
            this.views.viewPager.setCurrentItem(this.views.viewPager.currentItem + 1, true)
        }
    }

    // Local constructs ----------------------------------------------------------------------------

    /**
     * Adapter for the [Views.viewPager].
     */
    private inner class ScreenSlidePagerAdapter(
        activity: FragmentActivity
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = Pages.size

        override fun createFragment(position: Int): Fragment = Pages[position]
    }

    /**
     * Wrapper for view references.
     */
    private inner class Views(
        val viewPager: ViewPager2 = findViewById(R.id.setupSlidePagerActivityPager)
    )

    /**
     * Access to all of the 'pages' of the [Views.viewPager]. A barebones implementation of a
     * [List&lt;AbstractSetupPageFragment&gt;][List]
     */
    private object Pages {

        // Members ---------------------------------------------------------------------------------

        /**
         * Backing implementation of classes to construct.
         */
        private val classes = listOf(
            PermissionsSetupFragment::class,
            SettingsSetupFragment::class,
            CalibrationSetupFragment::class,
            ScheduleSetupFragment::class,
        )

        // List functionality ----------------------------------------------------------------------

        operator fun get(index: Int): AbstractSetupPageFragment {
            // Construct the fragment at this index in the classes list
            return this.classes[index].java.newInstance()
        }

        // Delegate to the classes list
        val size: Int by this.classes::size
        val lastIndex: Int
            get() = this.size - 1
    }

    companion object {
        /**
         * Get an intent to use to start this activity.
         * @param c Context being called from
         * @return New intent to start this activity with
         */
        fun getIntent(c: Context) = Intent(c, SetupSlidePagerActivity::class.java)
    }
}