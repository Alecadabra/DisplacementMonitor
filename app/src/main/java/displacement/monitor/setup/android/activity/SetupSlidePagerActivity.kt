package displacement.monitor.setup.android.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import displacement.monitor.R
import displacement.monitor.setup.android.fragment.PermissionsSetupFragment
import displacement.monitor.setup.android.fragment.AbstractSetupPageFragment
import displacement.monitor.setup.android.fragment.CalibrationSetupFragment
import displacement.monitor.setup.android.fragment.SettingsSetupFragment

class SetupSlidePagerActivity : AppCompatActivity() {

    private val views by lazy { Views() }

    private val pages: List<() -> AbstractSetupPageFragment> = listOf(
        { PermissionsSetupFragment() },
        { SettingsSetupFragment() },
        { CalibrationSetupFragment() },
        { ScheduleSetupFragment() },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_slide_pager)

        this.views.viewPager.adapter = ScreenSlidePagerAdapter(this)
        this.views.viewPager.isUserInputEnabled = false
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this).also { builder ->
            builder.setTitle("Leave app")
            builder.setMessage("Do you want to exit the app?")
            builder.setPositiveButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            builder.setNegativeButton("Yes, exit") { _, _ ->
                super.onBackPressed()
            }
        }.create().show()
    }

    fun pageBack() {
        if (this.views.viewPager.currentItem > 0) {
            this.views.viewPager.setCurrentItem(this.views.viewPager.currentItem - 1, true)
        } else {
            onBackPressed()
        }
    }

    fun pageNext() {
        if (this.views.viewPager.currentItem < this.pages.lastIndex) {
            this.views.viewPager.setCurrentItem(this.views.viewPager.currentItem + 1, true)
        }
    }

    private inner class ScreenSlidePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        private val pages by this@SetupSlidePagerActivity::pages

        override fun getItemCount(): Int = this.pages.size

        override fun createFragment(position: Int): Fragment = this.pages[position]()
    }

    private inner class Views(
        val viewPager: ViewPager2 = findViewById(R.id.setupSlidePagerActivityPager)
    )

    companion object {
        fun getIntent(context: Context) = Intent(context, SetupSlidePagerActivity::class.java)
    }
}