package displacement.monitor.database.local.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import displacement.monitor.R
import displacement.monitor.database.local.controller.LocalMeasurementDatabase
import displacement.monitor.database.model.Measurement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment to display all measurements in the [LocalMeasurementDatabase] and give the option
 * to clear all data.
 */
class DatabaseViewFragment : Fragment() {

    // Members -------------------------------------------------------------------------------------

    /** References to views. */
    private val views by lazy { Views(requireView()) }

    /** Dialog to confirm deletion of all local data. */
    private val deleteDialog: AlertDialog by lazy {
        AlertDialog.Builder(requireContext()).also { builder ->
            builder.setTitle("Delete all measurements?")
            builder.setMessage(
                "This is irreversible. It will not effect any data on any remote databases."
            )
            builder.setPositiveButton("Yes, delete") { dialog, _ ->
                val db = LocalMeasurementDatabase { requireContext() }
                db.measurementDao().clear()
                dialog.dismiss()
            }
            builder.setNegativeButton("No, cancel") { dialog, _ -> dialog.dismiss() }
        }.create()
    }

    // Android entry points ------------------------------------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_database_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.views.rv.layoutManager = LinearLayoutManager(requireContext())

        // Grab the data out and set the RV adapter
        CoroutineScope(Dispatchers.IO).launch {
            val db = LocalMeasurementDatabase { this@DatabaseViewFragment.requireContext() }
            val measurements: List<Measurement> = db.measurementDao().getAll().toList()
            withContext(Dispatchers.Main) {
                this@DatabaseViewFragment.views.rv.adapter = MeasurementAdapter(measurements)
            }
        }

        this.views.deleteBtn.setOnClickListener {
            // Show an 'are you sure?'
            this.deleteDialog.show()
        }
    }


    // Local constructs ----------------------------------------------------------------------------

    /**
     * Adapts the [values] list of [Measurements][Measurement] to a [RecyclerView].
     */
    private class MeasurementAdapter(
        private val values: List<Measurement>,
    ) : RecyclerView.Adapter<MeasurementViewHolder>() {

        // Adapter overrides -----------------------------------------------------------------------

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.fragment_database_view_element,
                parent,
                false
            )
            return MeasurementViewHolder(view)
        }

        override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {
            holder.bind(this.values[position])
        }

        override fun getItemCount(): Int = values.size
    }

    /** [View holder][RecyclerView.ViewHolder] for a [Measurement]. */
    private class MeasurementViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        // Members ---------------------------------------------------------------------------------

        /** References to views. */
        private val views: Views = Views(view)

        // Public entry points ---------------------------------------------------------------------

        /** Bind this view holder to a new [Measurement]. */
        fun bind(measurement: Measurement) {
            val date = Date(measurement.time * 1000)
            this.views.time.text = SimpleDateFormat.getDateTimeInstance().format(date)

            @SuppressLint("SetTextI18n")
            this.views.distance.text = "${"%.2f".format(measurement.distance)}m"
        }

        // Local constructs ------------------------------------------------------------------------

        /**
         * Wrapper for view references.
         */
        private inner class Views(
            view: View,
            val time: TextView = view.findViewById(R.id.databaseViewFragmentElementTime),
            val distance: TextView = view.findViewById(R.id.databaseViewFragmentElementDistance),
        )
    }

    /**
     * Wrapper for view references.
     */
    private inner class Views(
        view: View,
        val rv: RecyclerView = view.findViewById(R.id.databaseViewFragmentRV),
        val deleteBtn: Button = view.findViewById(R.id.databaseViewFragmentDeleteButton),
    )
}

