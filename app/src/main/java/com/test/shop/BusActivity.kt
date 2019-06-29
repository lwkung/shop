package com.test.shop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_bus.*
import kotlinx.android.synthetic.main.row_bus.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class BusActivity : AppCompatActivity(), AnkoLogger {

    var buses: List<Bus>? = null
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.myjson.com/bins/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus)
        val busService = retrofit.create(BusService::class.java)
        doAsync {
            buses = busService.listBus()
                .execute()
                .body()
            buses?.forEach {
                info("${it.BusID}   ${it.RouteID}   ${it.Speed}")
            }
            uiThread {
                recycler.layoutManager = LinearLayoutManager(this@BusActivity)
                recycler.setHasFixedSize(true)
                recycler.adapter = BusAdapter()
            }
        }

    }

    inner class BusAdapter : RecyclerView.Adapter<BusHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_bus, parent, false)
            return BusHolder(view)
        }

        override fun getItemCount(): Int {
            val size = buses?.size ?: 0
            return size
        }

        override fun onBindViewHolder(holder: BusHolder, position: Int) {
            //val bus = buses?.get(position)
            //holder.bindBus(bus!!)

            buses?.get(position)?.let {
                holder.bindBus(it)
            }
        }

    }

    inner class BusHolder(view: View) : RecyclerView.ViewHolder(view) {
        val busId: TextView = view.bus_id
        val routeId: TextView = view.route_id
        val speed: TextView = view.speed
        fun bindBus(bus: Bus) {
            busId.text = bus.BusID
            routeId.text = bus.RouteID
            speed.text = bus.Speed
        }
    }
}

data class Bus(
    val Azimuth: String,
    val BusID: String,
    val BusStatus: String,
    val DataTime: String,
    val DutyStatus: String,
    val GoBack: String,
    val Latitude: String,
    val Longitude: String,
    val ProviderID: String,
    val RouteID: String,
    val Speed: String,
    val ledstate: String,
    val sections: String
)

interface BusService {
    @GET("9654j")
    fun listBus(): Call<List<Bus>>
}