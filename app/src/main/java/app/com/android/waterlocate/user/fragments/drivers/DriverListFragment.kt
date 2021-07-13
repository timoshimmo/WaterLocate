package app.com.android.waterlocate.user.fragments.drivers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import app.com.android.waterlocate.R
import app.com.android.waterlocate.user.fragments.drivers.model.DriverListModel
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class DriverListFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private var adpt: DriverListRecyclerViewAdapter? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_driver_list, container, false)

        database = Firebase.database.reference

        val driversRef: DatabaseReference = database.child("drivers")
        val dQuery: Query = driversRef.orderByKey()

        val options: FirebaseRecyclerOptions<DriverListModel> = FirebaseRecyclerOptions.Builder<DriverListModel>()
            .setQuery(dQuery) {
                Log.i("QUERY RESULT", it.toString())
                Log.i("DRIVER LIST", it.child("name").value.toString())
                val result = DriverListModel(
                    it.child("name").value.toString(),
                    it.child("mobileNo").value.toString(),
                    it.child("status").value as Boolean
                )

                result

            }.build()

        if (view is RecyclerView) {
            with(view) {
                Log.i("DRIVER LIST RESULT", options.snapshots.toString())
                layoutManager = LinearLayoutManager(context)
                val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                addItemDecoration(itemDecorator)
                adpt = DriverListRecyclerViewAdapter(options)
                adapter = adpt
            }
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        adpt?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adpt?.stopListening()
    }

}