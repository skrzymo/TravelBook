package com.example.travelbook

import android.content.*
import android.os.Bundle
import android.support.design.widget.*
import android.support.v4.view.*
import android.support.v4.widget.*
import android.support.v7.app.*
import android.view.*
import android.widget.*
import com.facebook.*
import com.facebook.login.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.*
import com.squareup.picasso.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

var titlesArray = ArrayList<String>()
var countriesArray = ArrayList<String>()
var locationArray = ArrayList<LatLng>()
var snippetsArray = ArrayList<String>()

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var listView : ListView

    // Firebase Auth Object.
    var firebaseAuth: FirebaseAuth? = null

    lateinit var ivProfilePicture: ImageView
    lateinit var tvName: TextView
    lateinit var tvEmail: TextView

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if(item!!.itemId == R.id.add_place) {

            val intent = Intent(applicationContext, MapsActivity::class.java)
            firebaseAuth = FirebaseAuth.getInstance()
            val user = firebaseAuth?.currentUser
            val name : String = user?.displayName!!
            intent.putExtra("name", name)
            intent.putExtra("info", "new")
            startActivity(intent)

        } else if(item.itemId == R.id.delete_place) {

            val intent = Intent(this@MainActivity, DeleteActivity::class.java)
            startActivity(intent)
            true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.logout -> {
                LoginManager.getInstance().logOut()
                startActivity(Intent(this@MainActivity, CreateAccountActivity::class.java))
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onResume() {
        super.onResume()
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            val user = firebaseAuth?.currentUser
            val name : String = user?.displayName!!

            val database = openOrCreateDatabase("Places", Context.MODE_PRIVATE, null)
            val cursor = database.rawQuery("SELECT * FROM placesNew WHERE userName = \"$name\" ORDER BY country", null)

            val titleIndex = cursor.getColumnIndex("title")
            val countryIndex = cursor.getColumnIndex("country")
            val latitudeIndex = cursor.getColumnIndex("latitude")
            val longitudeIndex = cursor.getColumnIndex("longitude")
            val snippetIndex = cursor.getColumnIndex("snippet")

            cursor.moveToFirst()

            snippetsArray.clear()
            countriesArray.clear()
            titlesArray.clear()
            locationArray.clear()

            while(cursor != null) {
                val titleFromDatabase = cursor.getString(titleIndex)
                val snippetFromDatabase = cursor.getString(snippetIndex)
                val countryFromDatabase = cursor.getString(countryIndex)
                val latitudeFromDatabase = cursor.getString(latitudeIndex)
                val longitudeFromDatabase = cursor.getString(longitudeIndex)

                countriesArray.add(countryFromDatabase)
                titlesArray.add(titleFromDatabase)
                snippetsArray.add(snippetFromDatabase)

                val latitudeCoordinate = latitudeFromDatabase.toDouble()
                val longitudeCoordinate = longitudeFromDatabase.toDouble()

                val location = LatLng(latitudeCoordinate, longitudeCoordinate)

                locationArray.add(location)

                cursor.moveToNext()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        listView = findViewById(R.id.listView)

        val adapter = ListAdapter(this@MainActivity, countriesArray, titlesArray)
        listView.adapter = adapter

        listView.setOnItemClickListener { parent: AdapterView<*>?, _view: View?, position: Int, id: Long ->

            val intent = Intent(applicationContext, MapsActivity::class.java)
            intent.putExtra("info", "old")
            intent.putExtra("title", titlesArray[position])
            intent.putExtra("snippet", snippetsArray[position])
            intent.putExtra("latitude", locationArray[position].latitude)
            intent.putExtra("longitude", locationArray[position].longitude)

            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired

        if(!isLoggedIn) {
            startActivity(Intent(this@MainActivity, CreateAccountActivity::class.java))
        }

        setSupportActionBar(toolbar)

        mDrawerLayout = findViewById(R.id.drawer_layout)

        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth?.currentUser

        val navigationView: NavigationView = findViewById(R.id.nav_view)

        val hView : View = navigationView.inflateHeaderView(R.layout.nav_header_main)
        ivProfilePicture = hView.findViewById(R.id.imageView)
        tvName =  hView.findViewById(R.id.navbar_name)
        tvEmail = hView.findViewById(R.id.navbar_email) as TextView

        tvName.text = user?.displayName
        tvEmail.text = user?.email
        Picasso.with(this@MainActivity)
            .load(user?.photoUrl)
            .into(ivProfilePicture)

        navigationView.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
            this@MainActivity, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this@MainActivity)



    }
}
