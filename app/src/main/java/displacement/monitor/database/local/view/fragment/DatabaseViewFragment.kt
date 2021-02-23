package displacement.monitor.database.local.view.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import displacement.monitor.R
import displacement.monitor.database.local.controller.MeasurementDatabase
import displacement.monitor.database.model.Measurement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DatabaseViewFragment : Fragment() {

    // Members -------------------------------------------------------------------------------------

    /** References to views. */
    private val views by lazy { Views(requireView()) }

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

        CoroutineScope(Dispatchers.IO).launch {
            val db = MeasurementDatabase { this@DatabaseViewFragment.requireContext() }
            val values = db.measurementDao().getAll()
            withContext(Dispatchers.Main) {
                this@DatabaseViewFragment.views.rv.adapter = MeasurementAdapter(values)
            }
        }
    }

    class MeasurementAdapter(
        private val values: Array<Measurement>,
    ) : RecyclerView.Adapter<MeasurementAdapter.MeasurementViewHolder>() {

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

        inner class MeasurementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val views: Views = Views(view)

            fun bind(measurement: Measurement) {
                val date = Date(measurement.time * 1000)
                this.views.time.text = SimpleDateFormat.getDateTimeInstance().format(date)

                @SuppressLint("SetTextI18n")
                this.views.distance.text = "${"%.2f".format(measurement.distance)}m"
            }

            private inner class Views(
                view: View,
                val time: TextView = view.findViewById(R.id.databaseViewFragmentElementTime),
                val distance: TextView = view.findViewById(R.id.databaseViewFragmentElementDistance),
            )
        }
    }

    /**
     * Wrapper for view references.
     */
    private inner class Views(
        view: View,
        val rv: RecyclerView = view.findViewById(R.id.databaseViewFragmentRV)
    )
}

