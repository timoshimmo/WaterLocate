package app.com.android.waterlocate.user.fragments.drivers

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import app.com.android.waterlocate.R
import app.com.android.waterlocate.user.fragments.drivers.model.DriverListModel
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class DriverListRecyclerViewAdapter(
        options: FirebaseRecyclerOptions<DriverListModel>)
    : FirebaseRecyclerAdapter<DriverListModel, DriverListRecyclerViewAdapter.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_driver_list_row, parent, false)
        return ViewHolder(view)
    }

   /* override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.id
        holder.contentView.text = item.content
    }*/

    //override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val driverName: TextView = view.findViewById(R.id.tvDriverListName)
        val driverPhoneNo: TextView = view.findViewById(R.id.tvDriverListPhoneNo)
        val driverStatus: TextView = view.findViewById(R.id.tvDriverListStatus)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: DriverListModel) {
        holder.driverName.text = model.name
        holder.driverPhoneNo.text = model.mobileNo
        when(model.status) {
            true -> {
                holder.driverStatus.text = holder.itemView.context.resources.getString(R.string.str_online)
                holder.driverStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            }
            false -> {
                holder.driverStatus.text = holder.itemView.context.resources.getString(R.string.str_offline)
                holder.driverStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            }

        }
    }
}