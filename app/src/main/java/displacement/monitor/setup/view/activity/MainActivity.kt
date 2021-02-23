package displacement.monitor.setup.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import displacement.monitor.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start setup if needed
        startSetup()

        setContentView(R.layout.activity_main)
    }

    /**
     * Starts the setup screens if required.
     */
    private fun startSetup() {
        val needSetup = SetupActivity.PAGE_CLASSES.all { pageClass ->
            pageClass.java.newInstance().canAdvance(this)
        }

        if (needSetup) {
            startActivity(SetupActivity.getIntent(this))
        }
    }
}