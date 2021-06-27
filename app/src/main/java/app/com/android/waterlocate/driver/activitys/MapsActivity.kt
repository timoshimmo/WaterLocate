package app.com.android.waterlocate.driver.activitys

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import kotlin.collections.HashMap
import com.google.maps.android.PolyUtil


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    var redcrossNsukka = LatLng(6.438181475372473, 7.4881479681198755)
    var igbagwaRoad = LatLng(6.902132063451491, 7.367596996958411)
    var nopicStation = LatLng(6.460837842933547, 7.509229131518811)
    var sobrockFillingStation = LatLng(6.486290144184889, 7.503196672777366)
    var metroFillingStation = LatLng(6.430470626486421, 7.480587737822788)
    var agbaniRoad = LatLng(6.417859, 7.490008)
    var newAnglicanRoad = LatLng(6.848884, 7.385550)
    var tectennisRoad = LatLng(6.463960, 7.523318)
    var mechanicVillage = LatLng(6.844495, 7.378770)
    var edemRoad = LatLng(6.896358555939424, 7.342082996958288)
 //   var oldAirportJos = LatLng(9.88050626453098, 8.874339932948107)

    private var hashMap: HashMap<String, LatLng>? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location

    var address: String = "Address"
    var geocoder: Geocoder? = null
    private val permissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
       /* val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this) */

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val btnDirect: FloatingActionButton = findViewById(R.id.fbDirections)

        hashMap = HashMap()
        hashMap!!["Gomet borehole at Redcross Nsukka"] = redcrossNsukka
        hashMap!!["Igbagwa road borehole"] = igbagwaRoad
        hashMap!!["Borehole at Ogba road nopic filling station"] = nopicStation
        hashMap!!["Borehole at sobrock filling station"] = sobrockFillingStation
        hashMap!!["Borehole at Ogugbo road at metro filling station"] = metroFillingStation
        hashMap!!["Borehole at agbani road opposite agbani plaza"] = agbaniRoad
        hashMap!!["Borehole at new Anglican road"] = newAnglicanRoad
        hashMap!!["Borehole at tectennis road"] = tectennisRoad
        hashMap!!["Borehole at mechanic village close to building materials"] = mechanicVillage
        hashMap!!["Borehole at mechanic village close to building materials"] = edemRoad

        getCurrentLocation()

        btnDirect.setOnClickListener {
            lifecycleScope.launch {
                getClosestDirection()
            }
        }
    }

    private fun getCurrentLocation() {

        if(ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    permissionCode
            )
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location ->
            // latitude = location.latitude
            //  longitude = location.longitude
            currentLocation = location
            address = getAddresss(currentLocation.latitude, currentLocation.longitude)

            val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
            //  val currentAddress: TextView = findViewById(R.id.tvDeliveryCurrentLocation)
            //  currentAddress.text = address
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        for(key in hashMap!!.keys){
            mMap.addMarker(hashMap!![key]?.let { MarkerOptions().position(it).title(key) })
           // mMap.moveCamera(CameraUpdateFactory.newLatLng(hashMap!![key]))

        }
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> getCurrentLocation()
                PackageManager.PERMISSION_DENIED -> Toast.makeText(
                        this,
                        "Permission required to access current location",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun getAddresss(lat: Double, lng: Double): String {

        val addy: String
        geocoder = Geocoder(this, Locale.getDefault())

        val addressList: List<Address> = geocoder!!.getFromLocation(lat, lng, 1)
        addy = addressList[0].getAddressLine(0)

        return addy

    }

    private suspend fun getClosestDirection() {

        val distance = FloatArray(1)
        var leastValue = 0.0f
        var selected: LatLng? = null
        val jsonObject: JSONObject?


        for((count, key) in hashMap!!.keys.withIndex()) {

            Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                    hashMap!![key]!!.latitude, hashMap!![key]!!.longitude, distance)
            if(count == 0) {
                leastValue = distance[0]
            }
            else {
                if(leastValue > distance[0]) {
                    leastValue = distance[0]
                    selected = hashMap!![key]
                }
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

       // val drawonMap = drawDirectionLines(strResult!!)
       // mMap.addPolyline(drawonMap)
        val points: List<LatLng>? = PolyUtil.decode(jsonObject.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points"))
        mMap.addPolyline(PolylineOptions()
                .addAll(points)
                .width(5f)
                .color(Color.RED))
    /* mMap.addPolyline(PolylineOptions()
                .add(LatLng(currentLocation.latitude, currentLocation.longitude), LatLng(selected!!.latitude, selected.longitude))
                .width(5f)
                .color(Color.RED)) */


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
}