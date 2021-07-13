package app.com.android.waterlocate.user.fragments.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import app.com.android.waterlocate.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
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
    //private lateinit var auth: FirebaseAuth

    private var hashMap: HashMap<String, Any>? = null
    private var arr: ArrayList<HashMap<String, Any>>? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    var address: String = "Address"
    var geocoder: Geocoder? = null
    private val permissionCode = 101
    private lateinit var mMap: GoogleMap
    private var tvClosestDriverName: TextView? = null
    private var tvClosestDriverMobileNo: TextView? = null
    private var crdClosestDriverDetailsBody: CardView? = null
    private var tvCloseClosestDriverPopup: TextView? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_locate_drivers, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val btnClosetDriver: FloatingActionButton = root.findViewById(R.id.fbClosetDriver)
        tvClosestDriverName = root.findViewById(R.id.tvClosestDriverName)
        tvClosestDriverMobileNo = root.findViewById(R.id.tvClosestDriverMobileNo)
        crdClosestDriverDetailsBody = root.findViewById(R.id.crdClosestDriverDetailsBody)
        tvCloseClosestDriverPopup = root.findViewById(R.id.tvCloseClosestDriverPopup)

        database = Firebase.database.reference
        //auth = Firebase.auth

        val driversRef: DatabaseReference = database.child("drivers")
        val query: Query = driversRef.orderByChild("status").equalTo(true)

        query.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    arr = ArrayList()
                    for(item in snapshot.children) {
                        hashMap = HashMap()
                        hashMap!!["coordinates"] = LatLng(item.child("coordinates").child("latitude").value as Double,
                                item.child("coordinates").child("longitude").value as Double)
                        hashMap!!["name"] = item.child("name").value.toString()
                        hashMap!!["mobileNo"] = item.child("mobileNo").value.toString()
                        arr?.add(hashMap!!)
                        Log.i("RETRIEVED_ROW", hashMap.toString())
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

        btnClosetDriver.setOnClickListener {
            lifecycleScope.launch {
                getClosestDirection()
            }
        }

        tvCloseClosestDriverPopup?.setOnClickListener {
            crdClosestDriverDetailsBody?.visibility = View.GONE
        }

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

    private suspend fun getClosestDirection() {

        val distance = FloatArray(1)
        var leastValue = 0.0f
        var selected: LatLng? = null
        var selectedDriver: HashMap<String, Any>? = null
        val jsonObject: JSONObject?

        for((index, hashRow) in arr!!.withIndex()) {
           // val arrlatlng:LatLng = arr!![index]["coordinates"] as LatLng

            val hashlatlng:LatLng = hashRow["coordinates"] as LatLng

            Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                        hashlatlng.latitude, hashlatlng.longitude, distance)

            //Log.i("INDEX_ROW_VALUE", index.toString())

            if(arr!!.size > 0) {
                if(index == 0) {
                    leastValue = distance[0]
                    selected = hashRow["coordinates"] as LatLng
                    selectedDriver = hashRow
                }
                else {
                    if(leastValue > distance[0]) {
                        leastValue = distance[0]
                        selected = hashRow["coordinates"] as LatLng
                        selectedDriver = hashRow
                    }
                }
            }
            else {
                selected = hashRow["coordinates"] as LatLng
                selectedDriver = hashRow
            }


        }

        val strOrigin = "origin=" + currentLocation.latitude + "," + currentLocation.longitude
        val strDestination = "destination=" + selected!!.latitude + "," + selected.longitude
        val sensor = "sensor=false"
        val mode = "mode=driving"

        val param = "$strOrigin&$strDestination&$sensor&$mode&"
        val output = "json"
        val APIKEY = "key=AIzaSyDs_8LnDD8HGjgkPO5hLk08MTFOk6FJus8"

        val url = "https://maps.googleapis.com/maps/api/directions/$output?$param$APIKEY"

        val strResult = requestDirection(url)
        jsonObject = JSONObject(strResult)

        val points: List<LatLng>? = PolyUtil.decode(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points"))
        mMap.addPolyline(
                PolylineOptions()
                        .addAll(points)
                        .width(5f)
                        .color(Color.RED))

        Log.i("SELECTED_USER_ROW", selectedDriver.toString())
        Log.i("SELECTED_NAME_ROW", selectedDriver?.get("name") as String)
        tvClosestDriverName?.text = selectedDriver["name"].toString()
        tvClosestDriverMobileNo?.text = selectedDriver["mobileNo"].toString()
        crdClosestDriverDetailsBody?.visibility = View.VISIBLE

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