package displacement.monitor.database.local.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import displacement.monitor.R
import displacement.monitor.database.local.view.fragment.DatabaseViewFragment

/**
 * Wraps a [DatabaseViewFragment].
 */
class DatabaseViewActivity : AppCompatActivity() {

    // Members -------------------------------------------------------------------------------------

    /** References to views. */
    private val views by lazy(this::Views)

    // Android entry points ------------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database_view)

        this.title = "Local Database"
    }

    override fun onResume() {
        super.onResume()

        // Put the fragment in it's frame
        supportFragmentManager.beginTransaction().also { transaction ->
            transaction.replace(
                this.views.frame.id,
                DatabaseViewFragment()
            )
            transaction.commit()
        }
    }

    // Local constructs ----------------------------------------------------------------------------

    /**
     * Wrapper for view references.
     */
    private inner class Views(
        val frame: FrameLayout = findViewById(R.id.databaseViewActivityFrame)
    )

    companion object {
        /**
         * Get an intent to use to start this activity.
         * @param c Context being called from
         * @return New intent to start this activity with
         */
        fun getIntent(c: Context) = Intent(c, DatabaseViewActivity::class.java)
    }
}