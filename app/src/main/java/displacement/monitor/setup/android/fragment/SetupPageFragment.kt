package displacement.monitor.setup.android.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import displacement.monitor.R
import displacement.monitor.permissions.model.Permission

sealed class SetupPageFragment(private val title: String) : Fragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        this.activity?.title = this.title
    }
}

class PermissionsSetupFragment : SetupPageFragment("Obtain Permissions") {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_permissions_setup, container, false)

        return view
    }

    inner class PermissionAdapter : RecyclerView.Adapter<PermissionAdapter.PermissionViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
            val view = LayoutInflater.from(context).inflate(
                R.layout.fragment_permissions_setup_element,
                parent
            )
            return PermissionViewHolder(view)
        }

        override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
            holder.bind(Permission.allPerms[position])
        }

        override fun getItemCount(): Int = Permission.allPerms.size

        inner class PermissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(permission: Permission) {
                val title = when (permission) {
                    Permission.ADMIN -> "Admin Permission"
                    Permission.CAMERA -> "Camera Permission"
                    Permission.SETTINGS -> "Settings Write Permission"
                }
                val description = when (permission) {
                    Permission.ADMIN -> "Allows the app to lock the phone after a measurement is taken"
                    Permission.CAMERA -> "Allows the app to use the camera"
                    Permission.SETTINGS -> "Allows the app to change system settings"
                }
                val granted = context?.let { permission.isGrantedTo(it) } ?: false
            }
        }
    }

    private inner class Views(
        val rv: RecyclerView = view!!.findViewById(R.id.permissionsSetupFragmentRecyclerView)
    )
}