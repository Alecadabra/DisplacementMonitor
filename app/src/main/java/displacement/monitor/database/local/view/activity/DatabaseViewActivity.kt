package displacement.monitor.database.local.view.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import displacement.monitor.R
import displacement.monitor.database.local.view.fragment.DatabaseViewFragment

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