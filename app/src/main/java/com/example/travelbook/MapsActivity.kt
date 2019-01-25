package com.example.travelbook

import android.*
import android.content.*
import android.content.pm.*
import android.content.res.*
import android.location.*
import android.location.LocationListener
import android.net.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.*
import android.support.v4.content.*
import android.text.*
import android.util.*
import android.view.*
import android.view.inputmethod.*
import android.widget.*
import com.google.android.gms.common.*
import com.google.android.gms.common.api.*
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.*
import java.io.*
import java.lang.Exception
import java.util.*
import com.google.android.gms.location.places.*
import java.lang.NullPointerException
import java.lang.RuntimeException
import android.widget.TextView
import android.widget.AutoCompleteTextView
import android.text.Html
import android.text.Spanned






class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {


    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var country : String

    private lateinit var mMap: GoogleMap
    var locationManager : LocationManager? = null
    var locationListener : LocationListener? = null
    private lateinit var placeAutocompleteAdapter : PlaceAutocompleteAdapter
    protected lateinit var mGeoDataClient : GeoDataClient
    private lateinit var mMarker : Marker

    private lateinit var mPlaceDetailsAttribution: TextView

    private lateinit var searchText : AutoCompleteTextView
    private lateinit var gps : ImageView
    private lateinit var add : ImageView

    private lateinit var mFusedLocationProviderClient : FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        searchText = findViewById(R.id.input_search)
        mPlaceDetailsAttribution = findViewById(R.id.place_attribution)
        gps = findViewById(R.id.ic_gps)
        add = findViewById(R.id.ic_add)
    }

    private fun init() {

        mGeoDataClient = Places.getGeoDataClient(this@MapsActivity)

        searchText.onItemClickListener = mAutoCompleteClickListener

        placeAutocompleteAdapter = PlaceAutocompleteAdapter(this@MapsActivity, mGeoDataClient, LAT_LNG_BOUNDS, null)
        searchText.setAdapter(placeAutocompleteAdapter)

        searchText.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE  || event?.action == KeyEvent.ACTION_DOWN || event?.action == KeyEvent.KEYCODE_ENTER) {
                geoLocate()
            }
            false
        }

        gps.setOnClickListener { getDeviceLocation() }
        add.setOnClickListener { addToDatabase() }

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
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this@MapsActivity))

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                if(location != null) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17f))
                }

            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onProviderEnabled(provider: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onProviderDisabled(provider: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }

        if(ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MapsActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 2f, locationListener)

            init()

            val intent = intent
            val info = intent.getStringExtra("info")

            if(info == "new") {

                mMap.clear()
                getDeviceLocation()
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = false

            } else {

                mMap.clear()
                val latitude = intent.getDoubleExtra("latitude", 0.0)
                val longitude = intent.getDoubleExtra("longitude", 0.0)
                val title = intent.getStringExtra("title")
                val snippet = intent.getStringExtra("snippet")


                val location = LatLng(latitude, longitude)
                mMarker = mMap.addMarker(MarkerOptions().position(location).title(title).snippet(snippet))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
            }
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(grantResults.isNotEmpty()) {
            if(ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 2f, locationListener)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@MapsActivity)

        try {

            if(ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val location : Task<Location> = mFusedLocationProviderClient.lastLocation
                location.addOnCompleteListener { p0 ->
                    if(p0.isSuccessful) {
                        val currentLocation : Location = p0.result as Location
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation.latitude, currentLocation.longitude), 17f))
                    } else {
                        Toast.makeText(this@MapsActivity, "Nie można znaleźć aktualnej lokalizacji", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun geoLocate() {
        val searchString : String = searchText.text.toString()

        val geocoder = Geocoder(this@MapsActivity)
        var list = ArrayList<Address>()

        try {

            list = geocoder.getFromLocationName(searchString, 1) as ArrayList<Address>

        } catch (e: IOException) {
            e.printStackTrace()
        }

        if(list.isNotEmpty()) {
            val address : Address = list[0]

            val location = LatLng(address.latitude, address.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
            mMarker = mMap.addMarker(MarkerOptions().position(location).title(address.featureName))

            //Toast.makeText(this@MapsActivity, address.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideSoftKeyboard() {
        val view : View = this@MapsActivity.currentFocus
        if(view != null) {
            val imm : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    companion object {
        private val LAT_LNG_BOUNDS = LatLngBounds(LatLng(-34.041458, 150.790100), LatLng(-33.682247, 151.383362))
    }

    private var mAutoCompleteClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        hideSoftKeyboard()

        val item : AutocompletePrediction = placeAutocompleteAdapter.getItem(position)
        val placeID : String = item.placeId!!
        val primaryText : CharSequence = item.getPrimaryText(null)

        var placeResult : Task<PlaceBufferResponse>? = mGeoDataClient.getPlaceById(placeID)
        placeResult?.addOnCompleteListener(mUpdatePlaceDetailsCallback)

        hideSoftKeyboard()
        searchText.text = null

        Toast.makeText(applicationContext, "Wybrano: $primaryText", Toast.LENGTH_SHORT).show()
    }

    private var mUpdatePlaceDetailsCallback = object : OnCompleteListener<PlaceBufferResponse> {
        override fun onComplete(p0: Task<PlaceBufferResponse>) {
            try {
                val places : PlaceBufferResponse = p0.result!!

                //Get the Place object from the buffer
                val place : Place = places.get(0)
                val geocoder = Geocoder(this@MapsActivity)
                val list : List<Address> = geocoder.getFromLocation(place.latLng.latitude, place.latLng.longitude, 1)

                val address : Address = list[0]

                country = address.countryName

                var markerDetails = ""

                if(place.address != null) {
                    markerDetails += "Adres: ${place.address}\n"
                    if(place.phoneNumber != null) {
                        markerDetails += "Tel: ${place.phoneNumber}\n"
                        if(place.websiteUri != null) {
                            markerDetails += "Url: ${place.websiteUri}\n"
                            if(place.rating != null) {
                                markerDetails += "Ocena: ${place.rating}"

                            }
                        }
                    }
                }

                // Display the third party attributions if set.
                val thirdPartyAttribution : CharSequence? = places.attributions
                if (thirdPartyAttribution == null) {
                    mPlaceDetailsAttribution.visibility = View.GONE
                } else {
                    mPlaceDetailsAttribution.visibility = View.VISIBLE
                    mPlaceDetailsAttribution.text = Html.fromHtml(thirdPartyAttribution.toString())
                }

                mMap.clear()
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 17f))
                mMarker = mMap.addMarker(MarkerOptions().position(place.latLng).title(place.name as String).snippet(markerDetails))

                places.release()

            } catch (e: RuntimeRemoteException) {
                e.printStackTrace()
                return
            }

        }

    }

    private fun addToDatabase() {
        val markerPosition : LatLng = mMarker.position
        val title : String = mMarker.title
        val snippet : String = mMarker.snippet

        countriesArray.add(country)
        titlesArray.add(title)
        locationArray.add(markerPosition)

        val intent = intent
        val name = intent.getStringExtra("name")


        try {

            val latitude = markerPosition.latitude.toString()
            val longitude = markerPosition.longitude.toString()

            val database = openOrCreateDatabase("Places", Context.MODE_PRIVATE, null)

            database.execSQL("CREATE TABLE IF NOT EXISTS placesNew (userName VARCHAR, country VARCHAR, title VARCHAR, snippet VARCHAR, latitude VARCHAR, longitude VARCHAR)")

            val toCompile = "INSERT INTO placesNew (userName, country, title, snippet, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?)"

            val sqlLiteStatement = database.compileStatement(toCompile)

            sqlLiteStatement.bindString(1, name)
            sqlLiteStatement.bindString(2, country)
            sqlLiteStatement.bindString(3, title)
            sqlLiteStatement.bindString(4, snippet)
            sqlLiteStatement.bindString(5, latitude)
            sqlLiteStatement.bindString(6, longitude)

            sqlLiteStatement.execute()

            Toast.makeText(applicationContext, "Nowe miejsce dodane", Toast.LENGTH_LONG).show()

        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

}
