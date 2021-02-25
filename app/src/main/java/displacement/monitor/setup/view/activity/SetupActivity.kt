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
import kotlin.reflect.KClass

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

    override fun onBackPressed() {
        pageBack()
    }

    // Public entry points -------------------------------------------------------------------------

    /**
     * Go back one page. If at the first page and setup is complete, calls
     * [super.onBackPressed][AppCompatActivity.onBackPressed].
     */
    fun pageBack() {
        if (this.views.viewPager.currentItem > 0) {
            this.views.viewPager.setCurrentItem(this.views.viewPager.currentItem - 1, true)
        } else if (PAGE_CLASSES.all { it.construct().canAdvance(this) }) {
            super.onBackPressed()
        }
    }

    /**
     * Go forward one page. If at the last page, finishes the activity.
     */
    fun pageNext() {
        if (this.views.viewPager.currentItem < PAGE_CLASSES.lastIndex) {
            this.views.viewPager.setCurrentItem(this.views.viewPager.currentItem + 1, true)
        } else {
            finish()
        }
    }

    // Local constructs ----------------------------------------------------------------------------

    /**
     * Adapter for the [Views.viewPager].
     */
    private inner class ScreenSlidePagerAdapter(
        activity: FragmentActivity
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = PAGE_CLASSES.size

        override fun createFragment(position: Int): Fragment = PAGE_CLASSES[position].construct()
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
        val PAGE_CLASSES: List<KClass<out AbstractSetupPageFragment>> = listOf(
            PermissionsSetupFragment::class,
            SettingsSetupFragment::class,
            CalibrationSetupFragment::class,
        )

        /** Shortcut to initialise a subclass of [AbstractSetupPageFragment] from it's [KClass]. */
        fun KClass<out AbstractSetupPageFragment>.construct(): AbstractSetupPageFragment {
            return this.java.newInstance()
        }
    }
}