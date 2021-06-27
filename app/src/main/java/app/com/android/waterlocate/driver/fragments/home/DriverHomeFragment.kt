package app.com.android.waterlocate.driver.fragments.home

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import app.com.android.waterlocate.R
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DriverHomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var currentLocation: Location
    private lateinit var locationRequest: LocationRequest
    private val PERMISSION_REQUEST_CODE = 100
    private val REQUEST_CHECK_SETTINGS = 200

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_driver_home, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val btnToggleWaterStatus: ToggleButton = root.findViewById(R.id.tgBtnWaterTankerStatus)
        val btnOpenMap: CardView = root.findViewById(R.id.crdDriverOpenMap)
        val btnOpenSettings: CardView = root.findViewById(R.id.crdDriverOpenSettings)

        val tvLiveStatus: TextView = root.findViewById(R.id.tvLiveStatus)
        val tvWelcomeDriver: TextView = root.findViewById(R.id.tvWelcomeDriver)

        val shimmerDriverName: ShimmerFrameLayout = root.findViewById(R.id.shimmerLayoutDriverName)
        val shimmerLayoutDriverStatus: ShimmerFrameLayout = root.findViewById(R.id.shimmerLayoutDriverStatus)

        database = Firebase.database.reference
        auth = Firebase.auth

        val curUser = auth.currentUser

        if(curUser != null) {

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    val locationList = locationResult?.locations
                    if (locationList!!.isNotEmpty()){
                        val latestLocation = locationList.last()
                        database.child("drivers").child(curUser.uid).child("coordinates").setValue(latestLocation)
                        // Update UI with location data
                        // ...
                    }
                }
            }

            val driversRef: DatabaseReference = database.child("drivers").child(curUser.uid)

            driversRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        tvWelcomeDriver.text = String.format(
                            requireActivity().resources.getString(R.string.str_welcome_user),
                            snapshot.child("name").value.toString()
                        )
                        shimmerDriverName.visibility = View.GONE
                        tvWelcomeDriver.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Error retrieving user data",
                        Toast.LENGTH_LONG
                    ).show()
                }

            })

            driversRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val status = snapshot.child("status").value as Boolean
                        if(status) {
                            tvLiveStatus.text = requireContext().resources.getString(R.string.str_online)
                            tvLiveStatus.setTextColor(
                                    ContextCompat.getColor(
                                            requireContext(),
                                            R.color.green
                                    )
                            )
                            tvLiveStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_live, 0, 0, 0)
                            btnToggleWaterStatus.isChecked = true
                        }
                        else {
                            tvLiveStatus.text = requireContext().resources.getString(R.string.str_offline)
                            tvLiveStatus.setTextColor(
                                    ContextCompat.getColor(
                                            requireContext(),
                                            R.color.red
                                    )
                            )
                            tvLiveStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_record__1_, 0, 0, 0)
                            btnToggleWaterStatus.isChecked = false
                        }
                        shimmerLayoutDriverStatus.visibility = View.GONE
                        tvLiveStatus.visibility = View.VISIBLE

                    }
                    else {
                        tvLiveStatus.text = requireContext().resources.getString(R.string.str_offline)
                        tvLiveStatus.setTextColor(
                                ContextCompat.getColor(
                                        requireContext(),
                                        R.color.red
                                )
                        )
                        tvLiveStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_record__1_, 0, 0, 0)
                        shimmerLayoutDriverStatus.visibility = View.GONE
                        tvLiveStatus.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                            requireContext(),
                            "Error retrieving driver data",
                            Toast.LENGTH_LONG
                    ).show()
                }

            })
        }

        createLocationRequest()

        btnToggleWaterStatus.setOnCheckedChangeListener {_, isChecked ->
            if (isChecked) {
                if(curUser != null) {
                    database.child("drivers").child(curUser.uid).child("status").setValue(true)

                }

            } else {
                // The toggle is disabled
                if(curUser != null) {
                    database.child("drivers").child(curUser.uid).child("status").setValue(false)
                  //  database.child("drivers").child(curUser.uid).child("coordinates").setValue(currentLocation)
                }

            }
        }
        btnOpenMap.setOnClickListener {
            openMap()
        }

        btnOpenSettings.setOnClickListener {
            openSettings()
        }

        return root
    }

    private fun getCurrentLocation() {

        if(ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_CODE
            )
        }
        else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if(location != null) {
                    currentLocation = location

                }
            }
        }

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> getCurrentLocation()
                PackageManager.PERMISSION_DENIED -> Toast.makeText(
                        requireContext(),
                        "Permission required to access current location",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createLocationRequest() {

        locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }!!

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(requireContext())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(requireActivity(),
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSION_REQUEST_CODE
            )
        }
        else {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper())
        }

    }

    private fun openMap() {
        findNavController().navigate(R.id.action_driverHomeFragment_to_locateBoreholeFragment)
    }

    private fun openSettings() {
        findNavController().navigate(R.id.action_driverHomeFragment_to_driverSettingsFragment)
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        //fusedLocationClient.asGoogleApiClient().isConnected
        startLocationUpdates()
    }

}