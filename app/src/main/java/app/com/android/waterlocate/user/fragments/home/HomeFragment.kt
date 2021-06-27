package app.com.android.waterlocate.user.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import app.com.android.waterlocate.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var tankerWater: Long = 0
    private var tankerNoWater: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_user_home, container, false)

        val tvWelcomeUser: TextView = root.findViewById(R.id.tvWelcomeUser)
        val tvFullWaterTankersAmount: TextView = root.findViewById(R.id.tvFullWaterTankersAmount)
        val tvEmptyWaterTankersAmount: TextView = root.findViewById(R.id.tvEmptyWaterTankersAmount)

        val shimmerUserName: ShimmerFrameLayout = root.findViewById(R.id.shimmerLayoutUserName)
        val shimmerFullWater: ShimmerFrameLayout = root.findViewById(R.id.shimmerLayoutFullWater)
        val shimmerLayoutNoWater: ShimmerFrameLayout = root.findViewById(R.id.shimmerLayoutNoWater)

        val btnOpenMap:CardView = root.findViewById(R.id.crdUserOpenMap)

        database = Firebase.database.reference
        auth = Firebase.auth

        val curUser = auth.currentUser
        curUser?.let {
            // Name, email address, and profile photo Url
            val mName = curUser.displayName

            tvWelcomeUser.text = String.format(
                requireActivity().resources.getString(R.string.str_welcome_user),
                mName
            )
            shimmerUserName.visibility = View.GONE
            tvWelcomeUser.visibility = View.VISIBLE
        }

        val driversRef: DatabaseReference = database.child("drivers")
        val wQuery: Query = driversRef.orderByChild("status").equalTo(true)
        val nQuery: Query = driversRef.orderByChild("status").equalTo(false)

        //System.out.println("Log System:" wQuery + )

        wQuery.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    if(snapshot.childrenCount > 0) {
                        tankerWater = snapshot.childrenCount
                    }
                    tvFullWaterTankersAmount.text = tankerWater.toString()
                    shimmerFullWater.visibility = View.GONE
                    tvFullWaterTankersAmount.visibility = View.VISIBLE
                }
                else {
                    shimmerFullWater.visibility = View.GONE
                    tvFullWaterTankersAmount.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Error retrieving drivers with water",
                    Toast.LENGTH_LONG
                ).show()
            }

        })

        nQuery.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    if(snapshot.childrenCount > 0) {
                        tankerNoWater = snapshot.childrenCount
                    }
                    tvEmptyWaterTankersAmount.text = tankerNoWater.toString()
                    shimmerLayoutNoWater.visibility = View.GONE
                    tvEmptyWaterTankersAmount.visibility = View.VISIBLE
                }
                else {
                    shimmerLayoutNoWater.visibility = View.GONE
                    tvEmptyWaterTankersAmount.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Error retrieving drivers with no water",
                    Toast.LENGTH_LONG
                ).show()
            }

        })

        btnOpenMap.setOnClickListener {
            openMap()
        }

       /* if(shimmerLayoutNoWater.visibility == View.VISIBLE) {

        } */

        return root
    }

    private fun openMap() {
        findNavController().navigate(R.id.action_nav_home_to_nav_map)
    }
}