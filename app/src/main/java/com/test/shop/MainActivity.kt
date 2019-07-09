package com.test.shop

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.*
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.row_fuction.view.*
import org.jetbrains.anko.*
import java.net.URL

class MainActivity : AppCompatActivity(), AnkoLogger {

    private val TAG = MainActivity::class.java.simpleName
    var signup = false
    var cacheService: Intent? = null
    val auth = FirebaseAuth.getInstance()
    private val RC_SIGNUP = 200
    private val RC_NICKNAME = 210
    val functions = listOf<String>(
        "Camera",
        "Invite friend",
        "Parking",
        "Download coupons",
        "News",
        "Movies",
        "Bus",
        "Maps"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        /*if (!signup) {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivityForResult(intent, RC_SIGNUP)
        }*/

        auth.addAuthStateListener { auth ->
            authChanged(auth)
        }

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        // Spinner
        val colors = arrayOf("Red", "Green", "Blue")
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, colors)
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d("MainActivity", "onItemSelected: ${colors[position]}")
            }

        }
        // RecyclerView

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.setHasFixedSize(true)
        recycler.adapter = FuctionAdapter()
    }

    inner class FuctionAdapter() : RecyclerView.Adapter<FunctionHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FunctionHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_fuction, parent, false)
            val holder = FunctionHolder(view)
            return holder
        }

        override fun getItemCount(): Int {
            return functions.size
        }

        override fun onBindViewHolder(holder: FunctionHolder, position: Int) {
            holder.nameText.text = functions.get(position)
            holder.itemView.setOnClickListener { view ->
                functionClicked(holder, position)
            }
        }

    }

    private fun functionClicked(holder: FunctionHolder, position: Int) {
        Log.d(TAG, "functionClicked: $position")
        when (position) {
            1 -> startActivity(Intent(this, ContactActivity::class.java))
            2 -> startActivity(Intent(this, ParkingActivity::class.java))
            4 -> startActivity(Intent(this, NewsActivity::class.java))
            5 -> startActivity(Intent(this, MovieActivity::class.java))
            6 -> startActivity(Intent(this, BusActivity::class.java))
            7 -> startActivity(Intent(this, MapsActivity::class.java))
        }
    }

    class FunctionHolder(view: View) : RecyclerView.ViewHolder(view) {
        var nameText: TextView = view.name
    }

    override fun onResume() {
        super.onResume()
        //nickname.text = getNickName()
        if (auth.currentUser != null) {
            FirebaseDatabase.getInstance()
                .getReference("users")
                .child(auth.currentUser!!.uid)
                .child("nickname")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.value != null)
                            nickname.text = dataSnapshot.value as String
                    }

                })
        }
    }

    private fun authChanged(auth: FirebaseAuth) {
        if (auth.currentUser == null) {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivityForResult(intent, RC_SIGNUP)
        } else {
            Log.d("MainActivity", "authChanged: ${auth.currentUser?.uid}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGNUP) {
            if (resultCode == Activity.RESULT_OK) {
                val intent = Intent(this, NicknameActivity::class.java)
                startActivityForResult(intent, RC_NICKNAME)
            }
        }
        if (requestCode == RC_NICKNAME) {

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    val broadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals(CacheService.ACTION_CACHE_DONE)) {
                //toast("MainActivity cache informed")
                info("MainActivity cache informed")
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_cache -> {
                doAsync {
                    val json = URL("https://api.myjson.com/bins/bs7p7").readText()
                    val movies = Gson().fromJson<List<Movie>>(
                        json,
                        object : TypeToken<List<Movie>>() {}.type
                    )
                    movies.forEach {
                        startService(
                            intentFor<CacheService>(
                                "TITLE" to it.Title,
                                "URL" to it.Poster
                            )
                        )
                    }
                    //val movie = movies.get(0)
                    /*val intent = Intent(this, CacheService::class.java)
                    intent.putExtra("TITLE", movie.Title)
                    intent.putExtra("URL", movie.Poster)
                    startService(intent)*/

                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(CacheService.ACTION_CACHE_DONE)
        registerReceiver(broadcastReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        //stopService(cacheService)
        unregisterReceiver(broadcastReceiver)
    }
}
