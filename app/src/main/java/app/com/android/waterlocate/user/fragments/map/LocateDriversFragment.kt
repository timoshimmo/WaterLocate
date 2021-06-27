package app.com.android.waterlocate.user.fragments.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import app.com.android.waterlocate.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LocateDriversFragment : Fragment(), OnMapReadyCallback {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var hashMap: HashMap<String, Any>? = null
    private var arr: ArrayList<HashMap<String, Any>>? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    var address: String = "Address"
    var geocoder: Geocoder? = null
    private val permissionCode = 101
    private lateinit var mMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_locate_drivers, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val btnClosetDriver: FloatingActionButton = root.findViewById(R.id.fbClosetDriver)

        database = Firebase.database.reference
        //auth = Firebase.auth

        val driversRef: DatabaseReference = database.child("drivers")
        val query: Query = driversRef.orderByChild("status").equalTo(true)

        query.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    arr = ArrayList()
                    hashMap = HashMap()
                    for(item in snapshot.children) {
                        hashMap!!["coordinates"] = LatLng(item.child("coordinates").child("latitude").value as Double,
                                item.child("coordinates").child("longitude").value as Double)
                        hashMap!!["name"] = item.child("name").value.toString()
                        hashMap!!["mobileNo"] = item.child("mobileNo").value.toString()
                        arr?.add(hashMap!!)
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                        requireContext(),
                        "Error retrieving available drivers",
                        Toast.LENGTH_LONG
                ).show()
            }

        })

        getCurrentLocation()


        return root
    }

    private fun getCurrentLocation() {

        if(ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    permissionCode
            )
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location ->
            // latitude = location.latitude
            //  longitude = location.longitude
            currentLocation = location
            address = getAddresss(currentLocation.latitude, currentLocation.longitude)

            val mapFragment = childFragmentManager
                    .findFragmentById(R.id.mapLocateDrivers) as SupportMapFragment
            mapFragment.getMapAsync(this)
            //  val currentAddress: TextView = findViewById(R.id.tvDeliveryCurrentLocation)
            //  currentAddress.text = address
        }
    }

    private fun getAddresss(lat: Double, lng: Double): String {

        val addy: String
        geocoder = Geocoder(requireContext(), Locale.getDefault())

        val addressList: List<Address> = geocoder!!.getFromLocation(lat, lng, 1)
        addy = addressList[0].getAddressLine(0)

        return addy

    }



    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.isMyLocationEnabled = true

        for(items in arr!!){

            mMap.addMarker(MarkerOptions()
                    .position(items["coordinates"] as LatLng)
                    .title(items["name"].toString())
                    .snippet(items["mobileNo"].toString()))
                    .tag = items

        }
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
    }

    private suspend fun requestDirection(requestedUrl: String?): String = withContext(Dispatchers.IO) {
        var responseString = ""
        var inputStream: InputStream? = null
        var httpURLConnection: HttpURLConnection? = null

        try {
            val url = URL(requestedUrl)
            httpURLConnection = url.openConnection() as HttpURLConnection?
            httpURLConnection?.connect()

            inputStream = httpURLConnection!!.inputStream
            val reader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(reader)

            val stringBuffer = StringBuffer()
            var line: String? = ""
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuffer.append(line)
            }
            responseString = stringBuffer.toString()

            bufferedReader.close()
            reader.close()

        }
        catch (e: Exception) {
            e.printStackTrace()
        } finally {

            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        }
        httpURLConnection!!.disconnect()
        responseString

    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
    }
}