package co.kcagroforestry.app.polygon

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import co.kcagroforestry.app.R
import co.kcagroforestry.app.databinding.ActivityPolygonAlreadySubmitedBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.SphericalUtil
import com.kosherclimate.userapp.TimerData
import java.text.DecimalFormat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class PolygonAlreadySubmited : AppCompatActivity(),OnMapReadyCallback, LocationListener {

    lateinit var binding: ActivityPolygonAlreadySubmitedBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    private var Polygon_lat_lng = ArrayList<String>()
    private val latLngslist = java.util.ArrayList<LatLng>()

    var handler: Handler = Handler()
    var runnable: Runnable? = null

    var token: String = ""
    var unique_id: String = ""
    var farmername: String = ""
    var plot_area: String = ""
    var totalarea = ""
    var plantedarea = ""
    private var latitude: String = ""
    private var longitude: String = ""
    private var imageLat: String = ""
    private var imageLng: String = ""

    private var LAT = ArrayList<Double>()
    private var LNG = ArrayList<Double>()

    lateinit var polygon: Polygon
    private val markerList:ArrayList<Marker> = ArrayList()

    lateinit var timerData: TimerData
    var StartTime = 0;
    var StartTime1 = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_polygon_already_submited)

        binding = ActivityPolygonAlreadySubmitedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("farmer_id")!!
            Polygon_lat_lng = bundle.getStringArrayList("polygon_lat_lng")!!
            farmername = bundle.getString("farmer_name")!!
            plot_area = bundle.getString("plot_area")!!
            totalarea = bundle.getString("totalarea")!!
            plantedarea = bundle.getString("plantedarea")!!
            StartTime1 = bundle.getInt("StartTime")

            binding.polygonSubmitted.text = "Polygon already submitted"

        } else {
            Log.e("area", "Nope")
        }

        mapFragment =
            supportFragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@PolygonAlreadySubmited)

        timerData = TimerData(this@PolygonAlreadySubmited, binding.textTimer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

        binding.polygonSubmittedBack.setOnClickListener {
            mMap.clear()
            runnable?.let { handler.removeCallbacks(it) } //stop handler when activity not visible
            super.onBackPressed()
            finish()
        }

        binding.polygonSubmittedOk.setOnClickListener {
            super.onBackPressed()
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = false
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        for (i in 0 until Polygon_lat_lng.size) {
            val dfgdg: String = Polygon_lat_lng[i].replace("[^0-9,.]".toRegex(), "")

            val lat: Double = dfgdg.split(",").first().toDouble()
            val lng: Double = dfgdg.split(",").last().toDouble()
            latLngslist.add(LatLng(lat, lng))

            LAT.add(lat)
            LNG.add(lng)
        }

        latitude = LAT[0].toString()
        longitude = LNG[0].toString()
        getCurrentLocation(latitude, longitude)

        var polygonOptions = PolygonOptions()
//        for (i in latLngslist.indices) if (i == 0) {
//            polygonOptions = PolygonOptions().add(latLngslist[0])
//        } else {
        mMap.clear()
        polygonOptions.addAll(latLngslist)
        Log.d("polygon123", polygonOptions.toString())
        polygonOptions.strokeColor(ContextCompat.getColor(this@PolygonAlreadySubmited, R.color.polygonstock))
        polygonOptions.strokeWidth(7f)
        polygonOptions.fillColor(Color.argb(130, 106, 193, 253))
        polygon = mMap.addPolygon(polygonOptions)
        // }

        for (i in 0 until LAT.size) {
            val pickupMarkerDrawable = resources.getDrawable(R.drawable.location, null)
            val marker = mMap.addMarker(MarkerOptions().icon( BitmapDescriptorFactory.fromBitmap(
                pickupMarkerDrawable.toBitmap(
                    pickupMarkerDrawable.intrinsicWidth,
                    pickupMarkerDrawable.intrinsicHeight,
                    null
                )
            )).position(LatLng(LAT[i], LNG[i])))

            if (marker != null) {
                markerList.add(marker)
            }
        }

        mMap.setMinZoomPreference(15f)
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        showdistance(latLngslist)
    }

    private fun getCurrentLocation(latitude: String, longitude: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                mapFragment.getMapAsync(OnMapReadyCallback {
                    val latLng = LatLng(latitude.toDouble(), longitude.toDouble())
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

                    imageLat = latLng.latitude.toString()
                    imageLng = latLng.longitude.toString()
                    Log.e("getCurrentLocation", "$imageLat-$imageLng")
                })
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        val df = DecimalFormat("#.#####")
        imageLat = df.format(location.latitude).toString()
        imageLng = df.format(location.longitude).toString()
    }

    fun showdistance(latLngslist: ArrayList<LatLng>) {

        for (i in 0 until latLngslist.size -1){

            val pointdiastance = calculateDistance1(latLngslist[i],latLngslist[i+1])
            var distancevalue = "${latLngslist[i]} ${latLngslist[i + 1]}"

            Log.d("userdistance","$distancevalue"+ pointdiastance.toString())
            val df = DecimalFormat("####0.00")
            var distance = df.format(pointdiastance)
            val distamce_m = distance.toDouble() * 1000
            val showdistance = "$distamce_m  m"
            val midpoint = SphericalUtil.interpolate(latLngslist[i], latLngslist[i+1], 0.5)
            val marker = addText(applicationContext, mMap, midpoint, showdistance, 2, 18)
            if (marker != null) {
                markerList.add(marker)
            }
        }
        Log.d("userdistance","polygon  "+latLngslist.toString())
        val lastValue = latLngslist.get(latLngslist.size - 1)
        val lastValue1 = "${latLngslist.get(latLngslist.size - 1)}"
        Log.d("lastvaluehepolygon",lastValue.toString())

        val pointdiastance = calculateDistance1(latLngslist[0],lastValue)
        Log.d("userdistance","$lastValue1  "+ pointdiastance.toString())
        val df = DecimalFormat("####0.00")
        var distance1 = df.format(pointdiastance)
        val distamce_m = distance1.toDouble() * 1000
        val showdistance = "$distamce_m  m"
        val midpoint = SphericalUtil.interpolate(latLngslist[0], lastValue, 0.5)
        val marker1 = addText1(applicationContext, mMap, midpoint, showdistance, 2, 18)
        if (marker1 != null) {
            markerList.add(marker1)
        }
    }

    fun calculateDistance1(point1: LatLng, point2: LatLng): Double {

        val earthRadius = 6371 // Earth's radius in kilometers
        val lat1 = Math.toRadians(point1.latitude)
        val lon1 = Math.toRadians(point1.longitude)
        val lat2 = Math.toRadians(point2.latitude)
        val lon2 = Math.toRadians(point2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c // Distance in kilometers
    }

    fun addText(
        context: Context?,
        map: GoogleMap?,
        location: LatLng?,
        text: String?,
        padding: Int,
        fontSize: Int
    ): Marker? {
        var marker: Marker? = null
        if (context == null || map == null || location == null || text == null || fontSize <= 0) {
            return marker
        }
        val textView = TextView(context)
        textView.text = text
        textView.textSize = fontSize.toFloat()
        textView.setTextColor(Color.WHITE)
        textView.setBackgroundResource(R.color.blue)
        val paintText: Paint = textView.paint
        val boundsText = Rect()
        paintText.getTextBounds(text, 0, textView.length(), boundsText)
        paintText.textAlign = Paint.Align.CENTER
        val conf = Bitmap.Config.ARGB_8888
        val bmpText = Bitmap.createBitmap(
            boundsText.width() + 2 * padding,
            boundsText.height() + 2 * padding, conf
        )
        val canvasText = Canvas(bmpText)
        paintText.color = Color.WHITE
        canvasText.drawText(
            text,
            bmpText.width / 2f,
            bmpText.height / 2f - boundsText.exactCenterY(),
            paintText
        )
        val markerOptions = MarkerOptions()
            .position(location)
            .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
            .anchor(1f, 1f)
        marker = map.addMarker(markerOptions)
        marker!!.tag = textView
        return marker
    }

    fun addText1(
        context: Context?, map: GoogleMap?,
        location: LatLng?, text: String?, padding: Int,
        fontSize: Int
    ): Marker? {
        var marker: Marker? = null
        if (context == null || map == null || location == null || text == null || fontSize <= 0) {
            return marker
        }
        val textView = TextView(context)
        textView.text = text
        textView.textSize = fontSize.toFloat()
        val paintText: Paint = textView.paint
        val boundsText = Rect()
        paintText.getTextBounds(text, 0, textView.length(), boundsText)
        paintText.textAlign = Paint.Align.CENTER
        paintText.style = Paint.Style.FILL
        paintText.isAntiAlias = true;
        paintText.isSubpixelText = true
        val conf = Bitmap.Config.ARGB_8888
        val bmpText = Bitmap.createBitmap(
            boundsText.width() + 2
                    * padding, boundsText.height() + 2 * padding, conf
        )
        val canvasText = Canvas(bmpText)
        paintText.color = Color.WHITE
        canvasText.drawText(text, (canvasText.width / 2).toFloat(), (
                canvasText.height - padding - boundsText.bottom).toFloat(), paintText)
        val markerOptions = MarkerOptions()
            .position(location)
            .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
            .anchor(0.5f, 1f)
        marker = map.addMarker(markerOptions)
        return marker
    }
}