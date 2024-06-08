package co.kcagroforestry.app.revisit

import android.Manifest
import android.content.Context
import android.content.Intent
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
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.R
import co.kcagroforestry.app.databinding.ActivityCaptureDataRevisitBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

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
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.kosherclimate.userapp.TimerData
import java.text.DecimalFormat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class CaptureDataRevisit : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    lateinit var mMap: GoogleMap
    lateinit var binding: ActivityCaptureDataRevisitBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationPermissionRequestCode = 1001
    private lateinit var mapFragment: SupportMapFragment
    private var Polygon_lat_lng = ArrayList<String>()
    private val latLngslist = java.util.ArrayList<LatLng>()

    var handler: Handler = Handler()
    var runnable: Runnable? = null

    private var LAT = ArrayList<Double>()
    private var LNG = ArrayList<Double>()

    private var latitude: String = ""
    private var longitude: String = ""
    private var imageLat: String = ""
    private var imageLng: String = ""
    private var storelat: Double = 0.0
    private var storelng: Double = 0.0
    var token: String = ""
    var unique_id: String = ""
    var farmername: String = ""
    var plot_area: String = ""
    var totalarea = ""
    var plantedarea = ""
    var polygonid = ""
    var revisitNO = ""
    lateinit var polygon: Polygon
    private val markerList: ArrayList<Marker> = ArrayList()

    lateinit var timerData: TimerData
    var StartTime = 0;
    var StartTime1 = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCaptureDataRevisitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
       mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("farmer_id")!!
            Polygon_lat_lng = bundle.getStringArrayList("polygon_lat_lng")!!
            farmername = bundle.getString("farmer_name")!!
            plot_area = bundle.getString("plot_area")!!
            totalarea = bundle.getString("totalarea")!!
            plantedarea = bundle.getString("plantedarea")!!
            polygonid = bundle.getString("polygonid")!!
            revisitNO = bundle.getString("revisitNO")!!
            StartTime1 = bundle.getInt("StartTime")

        } else {
            Log.e("area", "Nope")
        }

        fusedLocationProviderClient =  LocationServices.getFusedLocationProviderClient(this@CaptureDataRevisit)

        timerData = TimerData(this@CaptureDataRevisit, binding.textTimer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

        binding.saveLocation.setOnClickListener {

            var isInside = isLatLngInsidePolygon(latLngslist)

            if (isInside){

                val intent = Intent(this@CaptureDataRevisit, RevisitePlotPhoto::class.java).apply {
                    putExtra("farmer_id", unique_id)
                    putStringArrayListExtra("polygon_lat_lng", Polygon_lat_lng)
                    putExtra("farmer_name", farmername)
                    putExtra("polygonid", polygonid)
                    putExtra("latitude", latitude)
                    putExtra("longitude", longitude)
                    putExtra("revisitNO", revisitNO)
                    putExtra("StartTime", StartTime)

                }
                startActivity(intent)

            }else{

                val WarningDialog = SweetAlertDialog(this@CaptureDataRevisit, SweetAlertDialog.WARNING_TYPE)
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Current location not inside \n the polygon"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }

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
        mMap.clear()

        mMap.uiSettings.isMapToolbarEnabled = false
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }


        for (i in 0 until Polygon_lat_lng.size){
            val dfgdg: String =  Polygon_lat_lng[i].replace("[^0-9,.-]".toRegex(), "")

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

        polygonOptions.addAll(latLngslist)
        Log.d("polygon123", polygonOptions.toString())
        polygonOptions.strokeColor(ContextCompat.getColor(this@CaptureDataRevisit, R.color.polygonstock))
        polygonOptions.strokeWidth(7f)
        polygonOptions.fillColor(Color.argb(130, 106, 193, 253))
        polygon = mMap.addPolygon(polygonOptions)

        val pickupMarkerDrawable =
            resources.getDrawable(R.drawable.location, null)

        for (i in 0 until LAT.size){
            val marker =  mMap.addMarker(MarkerOptions().icon(
                BitmapDescriptorFactory.fromBitmap(
                    pickupMarkerDrawable.toBitmap(
                        pickupMarkerDrawable.intrinsicWidth,
                        pickupMarkerDrawable.intrinsicHeight,
                        null
                    )
                )
            ).position(LatLng(LAT[i], LNG[i])))

            if (marker != null) {
                markerList.add(marker)
            }
        }

       /* mMap.addMarker(
            MarkerOptions().anchor(0.5f, 0.5f).position(LatLng(pipeLatitude, pipeLongitude))
                .icon(bitmapDescriptorFromVector(this@CaptureDataRevisit, com.google.android.gms.location.R.drawable.ic_plot_marker)))*/

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isCompassEnabled = true
//        mMap.setMinZoomPreference(15f)
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        showdistance(latLngslist)
    }

    private fun getCurrentLocation(latitude: String, longitude: String) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                mapFragment.getMapAsync {
                    val latLng = LatLng(latitude.toDouble(), longitude.toDouble())
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

//                    imageLat = latLng.latitude.toString()
//                    imageLng = latLng.longitude.toString()
//                    Log.e("getCurrentLocation", "$imageLat-$imageLng")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkLocationPermission()
    }

    override fun onStop() {
        super.onStop()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionRequestCode)
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .setMaxUpdateDelayMillis(1000)
            .build()


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private  val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {

            for (location in p0.locations) {
// Handle the retrieved location
                imageLat = location.latitude.toString()
                imageLng = location.longitude.toString()
// Do something with the latitude and longitude
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        val df = DecimalFormat("#.#####")
        imageLat = df.format(location.latitude).toString()
        imageLng = df.format(location.longitude).toString()
    }

    private fun isLatLngInsidePolygon(latLngslist: java.util.ArrayList<LatLng>): Boolean {
        val lat = imageLat.toDouble()
        val lng = imageLng.toDouble()

        val latlng = LatLng(lat, lng)
        Log.e("pipe_map_location", "pipe1  "+latlng.toString())
        Log.e("pipe_map_location", "pipe2  "+latLngslist.toString())
        storelat = lat
        storelng = lng
        return PolyUtil.containsLocation(latlng, latLngslist, false)
    }

    fun showdistance(latLngslist: java.util.ArrayList<LatLng>) {

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