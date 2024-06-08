package co.kcagroforestry.app.polygon

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.R
import co.kcagroforestry.app.databinding.ActivityCaptureDataBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import com.google.android.gms.location.FusedLocationProviderClient
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
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.kosherclimate.userapp.TimerData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt


class CaptureData : AppCompatActivity(), OnMapReadyCallback {

    lateinit var mMap: GoogleMap
    lateinit var binding: ActivityCaptureDataBinding
    lateinit var ivSaveLocation: ImageView
    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var firstLat: Double = 0.0
    var firstLng: Double = 0.0
    var polygon_area: Double = 0.0
    var currentLat: Double = 0.0
    var currentLng: Double = 0.0
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var progress: SweetAlertDialog
    var token: String = ""
    var unique_id: String = ""
    var farmername: String = ""
    var plot_area: String = ""
    var totalarea = ""
    var plantedarea = ""
    var arrayList = ArrayList<String>()
    var nearbyPolygonList = ArrayList<LatLng>()
    var one = ArrayList<LatLng>()

    var insideNearbyPolygonList = ArrayList<ArrayList<LatLng>>()
    var overlapsList = ArrayList<LatLng>()
    private var editable: Boolean = false
    var latLngArrayListPolygon = ArrayList<LatLng>()
    var two = ArrayList<LatLng>()
    private var mCurrLocationMarker: Marker? = null
    private var polygon_date_time: String = ""
    var polygonOptions: PolygonOptions? = null
    var polygon: Polygon? = null
    private var KO: String = ""
    private val markerList: ArrayList<Marker> = ArrayList()
    private var Polygon_lat_lng = ArrayList<String>()

    var handler: Handler = Handler()
    var runnable: Runnable? = null

    lateinit var timerData: TimerData
    var StartTime = 0;
    var StartTime1 = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCaptureDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@CaptureData, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@CaptureData)

        gpsCheck()

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("farmer_id")!!
            // arrayList = bundle.getStringArrayList("polygon_lat_lng")!!
            farmername = bundle.getString("farmer_name")!!
            plot_area = bundle.getString("plot_area")!!
            totalarea = bundle.getString("totalarea")!!
            plantedarea = bundle.getString("plantedarea")!!
            StartTime1 = bundle.getInt("StartTime")

            binding.areaAcres.text = "$totalarea  hectare"

        } else {
            Log.e("area", "Nope")
        }

        timerData = TimerData(this@CaptureData, binding.textTimer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

//        binding.ivSaveLocation.setOnClickListener {
//            startActivity(Intent(this@CaptureData,PlotPhotoDetails::class.java))
//        }

        binding.edit.setOnClickListener {
//Creating the instance of PopupMenu
            val popup = PopupMenu(this@CaptureData, binding.edit)

//Inflating the Popup using xml file
            popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)

//registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.multipleMarker -> {
                        editable = true
                        true
                    }

                    else -> false
                }
            }
//showing popup menu
            popup.show()
        }
        binding.delete.setOnClickListener {

            try {

                if (polygon != null) {
                    polygon!!.remove()
                }

                for (marker in markerList) {
                    marker.remove()
                }

                latLngArrayListPolygon.clear()
                two.clear()
                markerList.clear()

                Polygon_lat_lng.clear()
                Polygon_lat_lng.removeAll(Polygon_lat_lng)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            binding.polygonArea.text = ""
            editable = false
            mMap.clear()

            getRadiusPolygon(firstLat.toString(), firstLng.toString())
        }

        binding.saveLocation.setOnClickListener {

            if (latLngArrayListPolygon.size == 0){

                val WarningDialog =
                    SweetAlertDialog(this@CaptureData, SweetAlertDialog.WARNING_TYPE)

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Draw The Polygon First"
                WarningDialog.confirmText = " OK "
                WarningDialog.showCancelButton(false)
                WarningDialog.setCancelable(false)
                WarningDialog.setConfirmClickListener {
                    WarningDialog.cancel()
                }.show()

            }else{

                for (i in 0 until latLngArrayListPolygon.size - 1) {

                    val pointdiastance =
                        calculateDistance1(latLngArrayListPolygon[i], latLngArrayListPolygon[i + 1])
                    var distancevalue = "${latLngArrayListPolygon[i]} ${latLngArrayListPolygon[i + 1]}"

                    Log.d("userdistance", "$distancevalue" + pointdiastance.toString())
                    val df = DecimalFormat("####0.00")
                    var distance = df.format(pointdiastance)
                    val distamce_m = distance.toDouble() * 1000
                    val showdistance = "$distamce_m  m"
                    val midpoint = SphericalUtil.interpolate(
                        latLngArrayListPolygon[i],
                        latLngArrayListPolygon[i + 1],
                        0.5
                    )
                    val marker = addText(applicationContext, mMap, midpoint, showdistance, 2, 18)
                    if (marker != null) {
                        markerList.add(marker)
                    }
                }
                Log.d("userdistance", "polygon  " + latLngArrayListPolygon.toString())
                val lastValue = latLngArrayListPolygon.get(latLngArrayListPolygon.size - 1)
                val lastValue1 = "${latLngArrayListPolygon.get(latLngArrayListPolygon.size - 1)}"
                Log.d("lastvaluehepolygon", lastValue.toString())

                val pointdiastance = calculateDistance1(latLngArrayListPolygon[0], lastValue)
                Log.d("userdistance", "$lastValue1  " + pointdiastance.toString())
                val df = DecimalFormat("####0.00")
                var distance1 = df.format(pointdiastance)
                val distamce_m = distance1.toDouble() * 1000
                val showdistance = "$distamce_m  m"
                val midpoint = SphericalUtil.interpolate(latLngArrayListPolygon[0], lastValue, 0.5)
                val marker1 = addText1(applicationContext, mMap, midpoint, showdistance, 2, 18)
                if (marker1 != null) {
                    markerList.add(marker1)
                }

                binding.viewsave.visibility = View.GONE
                binding.saveLocation.visibility = View.GONE

                binding.viewnext.visibility = View.VISIBLE
                binding.nextLocation.visibility = View.VISIBLE

            }

        }

        binding.nextLocation.setOnClickListener {

            if (Polygon_lat_lng.size < 3) {

                Toast.makeText(this@CaptureData, "Markers less than 3", Toast.LENGTH_SHORT).show()

            } else {

                val m = SphericalUtil.computeArea(latLngArrayListPolygon)

                Log.e("m", "computeArea $m")
                Log.e("NEW_TEST", "in Else")

// converting meters to acers
                val df = DecimalFormat("#.#####")
                val polygonarea = df.format(m * 0.0001).toDouble()
                Log.e("a", "computeArea $polygonarea")

                if (polygonarea < totalarea.toDouble()){

                    runnable?.let { handler.removeCallbacks(it) } //stop handler when activity not visible

                    val stringList = convertLatLngListToStringList(latLngArrayListPolygon)

                    val intent = Intent(this, PlotPhotoDetails::class.java).apply {
                        putExtra("locationList", stringList)
                        putExtra("area", totalarea)
                        putExtra("unique_id", unique_id)
                        putExtra("polygon_area", polygon_area)
                        putExtra("polygon_date_time", polygon_date_time)
                        putExtra("farmer_name", farmername)
                        putExtra("StartTime", StartTime)
                    }

                    startActivity(intent)
                    finish()


                }else{
4
                    val WarningDialog =
                        SweetAlertDialog(this@CaptureData, SweetAlertDialog.WARNING_TYPE)
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = "Area drawn is more than plot \n area"
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

                    binding.viewsave.visibility = View.VISIBLE
                    binding.saveLocation.visibility = View.VISIBLE

                    binding.viewnext.visibility = View.GONE
                    binding.nextLocation.visibility = View.GONE

                }


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

        getLocationAccuracy()

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        mMap.setOnMapClickListener { latLng ->
            if (editable) {
                progress.progressHelper.barColor = Color.parseColor("#06c238")
                progress.titleText = resources.getString(R.string.loading)
                progress.contentText = resources.getString(R.string.data_load)
                progress.setCancelable(false)
                progress.show()

                checkCoordinates(latLng)
            }
        }

        binding.undo.setOnClickListener {

//            for (i in 0 until latLngArrayListPolygon.size -1){
//
//                val pointdiastance = calculateDistance1(latLngArrayListPolygon[i],latLngArrayListPolygon[i+1])
//                var distancevalue = "${latLngArrayListPolygon[i]} ${latLngArrayListPolygon[i + 1]}"
//
//                Log.d("userdistance","$distancevalue"+ pointdiastance.toString())
//                val df = DecimalFormat("####0.00")
//                var distance = df.format(pointdiastance)
//                val distamce_m = distance.toDouble() * 1000
//                val showdistance = "$distamce_m  m"
//                val midpoint = SphericalUtil.interpolate(latLngArrayListPolygon[i], latLngArrayListPolygon[i+1], 0.5)
//                val marker = addText(applicationContext, mMap, midpoint, showdistance, 2, 18)
//                if (marker != null) {
//                    markerList.add(marker)
//                }
//            }
//            Log.d("userdistance","polygon  "+latLngArrayListPolygon.toString())
//            val lastValue = latLngArrayListPolygon.get(latLngArrayListPolygon.size - 1)
//            val lastValue1 = "${latLngArrayListPolygon.get(latLngArrayListPolygon.size - 1)}"
//            Log.d("lastvaluehepolygon",lastValue.toString())
//
//            val pointdiastance = calculateDistance1(latLngArrayListPolygon[0],lastValue)
//            Log.d("userdistance","$lastValue1  "+ pointdiastance.toString())
//            val df = DecimalFormat("####0.00")
//            var distance1 = df.format(pointdiastance)
//            val distamce_m = distance1.toDouble() * 1000
//            val showdistance = "$distamce_m  m"
//            val midpoint = SphericalUtil.interpolate(latLngArrayListPolygon[0], lastValue, 0.5)
//            val marker1 = addText1(applicationContext, mMap, midpoint, showdistance, 2, 18)
//            if (marker1 != null) {
//                markerList.add(marker1)
//            }

            try {
                if (!markerList.isEmpty()) {
                    Log.e("latLngArrayListPolygon", latLngArrayListPolygon.size.toString())
                    Log.e("Polygon_lat_lng", Polygon_lat_lng.size.toString())

                    val lastMarker: Marker = markerList.removeAt(markerList.size - 1)
                    lastMarker.remove()
                    Polygon_lat_lng.removeAt(Polygon_lat_lng.size - 1)
                    latLngArrayListPolygon.removeAt(latLngArrayListPolygon.size - 1)
                    two.removeAt(two.size - 1)

                    polygon?.remove()

                    val pickupMarkerDrawable =
                        resources.getDrawable(R.drawable.location, null)
                    for (i in latLngArrayListPolygon.indices)
                        if (i == 0) {
                            polygonOptions = PolygonOptions().add(latLngArrayListPolygon[0])
                            mMap.clear()
                        } else {
                            Log.e("Polygon_lat_lng", "else")
                            polygonOptions!!.add(latLngArrayListPolygon[i])
                            mMap.clear()
                        }

                    for (i in latLngArrayListPolygon.indices) {
                        mCurrLocationMarker = mMap.addMarker(
                            MarkerOptions().position(latLngArrayListPolygon[i])
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        pickupMarkerDrawable.toBitmap(
                                            pickupMarkerDrawable.intrinsicWidth,
                                            pickupMarkerDrawable.intrinsicHeight,
                                            null
                                        )
                                    )
                                )
                        )
                    }

                    polygonOptions!!.strokeColor(ContextCompat.getColor(this@CaptureData, R.color.polygonstock))
                    polygonOptions!!.strokeWidth(7f)
                    polygonOptions!!.fillColor(Color.argb(130, 106, 193, 253))
                    polygon = mMap.addPolygon(polygonOptions!!)

                    binding.polygonArea.text = ""

// Calculating meters from polygon list
                    val m = SphericalUtil.computeArea(latLngArrayListPolygon)
                    Log.e("m", "computeArea $m")

// converting meters to acers
                    val df = DecimalFormat("#.#####")
                    polygon_area = df.format(m * 0.0001).toDouble()
                    Log.e("a", "computeArea $polygon_area")

                    binding.polygonArea.text = polygon_area.toString() + "  hectare"

                    if (Polygon_lat_lng.size == 0) {
                        mMap.clear()
                    }

                    getRadiusPolygon(firstLat.toString(), firstLng.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    }

    private fun gpsCheck() {
        val active = isLocationEnabled(this@CaptureData)
        Log.e("active", active.toString())

        if (!active) {
            android.app.AlertDialog.Builder(this@CaptureData)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(R.string.yes) { paramDialogInterface, paramInt ->
                    this@CaptureData.startActivity(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                }
                .setNegativeButton(
                    R.string.no
                ) { paramDialogInterface, paramInt ->
                    gpsCheck()
                }
                .show()
        } else {
            checkLocationPermission()
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            val task = fusedLocationProviderClient.lastLocation
            task.addOnSuccessListener { location ->
                if (location != null) {
                    mapFragment.getMapAsync(OnMapReadyCallback {
                        val latLng = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
                        firstLat = latLng.latitude
                        firstLng = latLng.longitude
                        Log.e(
                            "getCurrentLocation",
                            latLng.latitude.toString() + "-" + latLng.longitude
                        )
                        // polygonsListApi(LatLongModel(firstLat.toString(), firstLng.toString()))

                        var str_firstLat = firstLat.toString()
                        var str_firstLng = firstLng.toString()

                        getRadiusPolygon(str_firstLat, str_firstLng)
                    })
                }

            }
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Log.e("else", "if_Small")
                AlertDialog.Builder(this)
                    .setTitle("Location Required")
                    .setMessage("The app needs to access current location, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            } else {
                Log.e("else", "else_small")
                AlertDialog.Builder(this)
                    .setTitle("Location Required")
                    .setMessage("The app needs to access current location, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            }
        } else {
            Log.e("else", "else_big")
            getCurrentLocation()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    private fun getRadiusPolygon(firstLat: String, firstLng: String) {

        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.checking_data)
        progress.setCancelable(false)
        progress.show()

        one.clear()

        Log.d("userresponse", "userdata   $firstLat  $firstLng")

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.polygonNearby("Bearer $token", unique_id, firstLat, firstLng)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
//                    Log.d("NEW_TEST", "near by Polygons >>>> ${response.code()}")
//                    val test = JSONArray(response.body()?.string())
//                    Log.d("NEW_TEST", "near by Polygons >>>> ${test}")

                    if (response.code() == 200) {

                        if (response.body() != null) {

                            progress.dismiss()

                           // Log.d("userresponse","userdata"+response.body()!!.string())

                            val jsonArray = JSONArray(response.body()!!.string())

                            for (i in 0 until jsonArray.length()) {

                                var innerArray = jsonArray.getJSONArray(i)
                                for (k in 0 until innerArray.length()) {

                                    val innerObject = innerArray.getJSONObject(k)
                                    val rangeArray = innerObject.getJSONArray("ranges")
                                    for (j in 0 until rangeArray.length()) {
                                        val jsonObject1 = rangeArray.getJSONObject(j)

                                        val latitude = jsonObject1.optString("lat").toDouble()
                                        val longitude = jsonObject1.optString("lng").toDouble()

                                        val latLng = LatLng(latitude, longitude)
                                        nearbyPolygonList.add(latLng)
                                        one.add(latLng)
                                    }

                                    Log.e("NearbyPolygonList", nearbyPolygonList.toString())
                                    insideNearbyPolygonList.add(nearbyPolygonList)
                                    Log.e(
                                        "insideNearbyPolygonList",
                                        insideNearbyPolygonList.toString()
                                    )

                                    val polygonOptions = PolygonOptions()

                                    for (j in 0 until nearbyPolygonList.size) {

                                        val latitude = nearbyPolygonList[j].latitude
                                        val longitude = nearbyPolygonList[j].longitude
                                        overlapsList.add(nearbyPolygonList[j])
                                        polygonOptions.add(LatLng(latitude, longitude))
                                        polygonOptions.strokeColor(Color.WHITE)
                                        polygonOptions.strokeWidth(5f)
                                        //  polygonOptions.fillColor(Color.argb(50, 79, 240, 228))
                                        val polygon = mMap.addPolygon(polygonOptions)
                                    }
                                    nearbyPolygonList.clear()
                                }
                            }
                        } else if (response.code() == 422) {
                            progress.dismiss()
                        }
                    } else {
                        if (response.body() != null) {
                            val jsonArray = JSONArray(response.body()!!.string())
                            Log.e("NEW_TEST", "JSOn >>> $jsonArray")
                        }
                        progress.dismiss()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progress.dismiss()
//                    Toast.makeText(
//                        this@MapActivity,
//                        "Please Retry",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            })
    }

    private fun checkCoordinates(latLng: LatLng) {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.checkCoordinates(
            "Bearer $token",
            unique_id,
            latLng.latitude.toString(),
            latLng.longitude.toString()
        )
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.e("NEW_TEST", "Modified Id = ${response.code()}")
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            val jsonObject = JSONObject(response.body()!!.string())
                            val status = jsonObject.optBoolean("status")

                            progress.dismiss()

                            pointOverlappingMsg()
                            Toast.makeText(
                                this@CaptureData,
                                "Point overlapping",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            progress.dismiss()
                        }
                    } else if (response.code() == 422) {
                        progress.dismiss()
                        addMarker(latLng)
                    } else {
                        progress.dismiss()
                        Log.e("CheckCoordinaters", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(
                        this@CaptureData,
                        "Please Retry",
                        Toast.LENGTH_SHORT
                    ).show()
                    progress.dismiss()

                }
            })
    }

    private fun pointOverlappingMsg() {
        val WarningDialog = SweetAlertDialog(this@CaptureData, SweetAlertDialog.WARNING_TYPE)

        WarningDialog.titleText = resources.getString(R.string.warning)
        WarningDialog.contentText = resources.getString(R.string.point_overlapping_warning)
        WarningDialog.confirmText = resources.getString(R.string.ok)
        WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun addMarker(latLng: LatLng) {

        val LATLNG = LatLng(firstLat, firstLng)
        val distance = SphericalUtil.computeDistanceBetween(latLng, LATLNG)

        Log.d("maerkeradd one", "data one   " + "adddmarker 1")

        if (latLngArrayListPolygon.size == 0 && distance > 50) {
            Toast.makeText(
                this@CaptureData,
                "Distance is greater than 50 meters",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            var count = 0
            two.add(latLng)

            for (j in 0 until one.size) {
                val validLocation = PolyUtil.containsLocation(one[j], two, false)
                Log.e("Distance_greater", validLocation.toString())
                if (validLocation) {
                    Log.e("Distance_greater", validLocation.toString())
                    Toast.makeText(this@CaptureData, "Point overlapping", Toast.LENGTH_SHORT).show()
                    two.removeAt(two.size - 1)
                    count = 1

                    val WarningDialog =
                        SweetAlertDialog(this@CaptureData, SweetAlertDialog.WARNING_TYPE)
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText =
                        resources.getString(R.string.point_overlapping_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

                    break
                }
            }

            if (count == 0) {

                Log.d("maerkeradd one", "data two   " + "adddmarkerc 2")

                val pickupMarkerDrawable =
                    resources.getDrawable(R.drawable.location, null)
                mCurrLocationMarker = mMap.addMarker(
                    MarkerOptions().anchor(0.5f, 0.5f).position(latLng)
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                pickupMarkerDrawable.toBitmap(
                                    pickupMarkerDrawable.intrinsicWidth,
                                    pickupMarkerDrawable.intrinsicHeight, null
                                )
                            )
                        )
                )
                mCurrLocationMarker?.tag = latLng

                val c: Calendar = Calendar.getInstance()
                val dfi = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val formattedDate: String = dfi.format(c.time)
                polygon_date_time = formattedDate


                //adjustPolygonWithRespectTo(latLng)

                latLngArrayListPolygon.add(latLng)

                currentLat = latLng.latitude
                currentLng = latLng.longitude

                if (polygon != null) {
                    polygon!!.remove()
                }

                Log.d("maerkeradd one", "data four   " + "adddmarker 4")

                //polygonOptions?.add(latLngArrayListPolygon[i])
                val polygonOptions = PolygonOptions().addAll(latLngArrayListPolygon)
                polygon = polygonOptions?.let { mMap.addPolygon(it) }
                polygon!!.fillColor = ContextCompat.getColor(this@CaptureData, R.color.polygonfill)
                polygon!!.strokeColor =
                    ContextCompat.getColor(this@CaptureData, R.color.polygonstock)
                polygon!!.strokeWidth = 4f

                mCurrLocationMarker?.let {
                    markerList.add(it)
                }

                calculateDistance(latLngArrayListPolygon)

                if (latLngArrayListPolygon.size >= 2) {

                    for (i in 0 until latLngArrayListPolygon.size - 1) {

                        val pointdiastance = calculateDistance1(
                            latLngArrayListPolygon[i],
                            latLngArrayListPolygon[i + 1]
                        )
                        var distancevalue =
                            "${latLngArrayListPolygon[i]} ${latLngArrayListPolygon[i + 1]}"

                        Log.d("userdistance", "$distancevalue" + pointdiastance.toString())
                        val df = DecimalFormat("####0.00")
                        var distance = df.format(pointdiastance)
                        val distamce_m = distance.toDouble() * 1000
                        val showdistance = "$distamce_m  m"
                        val midpoint = SphericalUtil.interpolate(
                            latLngArrayListPolygon[i],
                            latLngArrayListPolygon[i + 1],
                            0.5
                        )
                        val marker =
                            addText(applicationContext, mMap, midpoint, showdistance, 2, 18)
                        if (marker != null) {
                            markerList.add(marker)
                        }
                    }

                }

// Getting the marker Lat & Lng and storing it in variable. It is accessible from "latLng".
                KO = latLng.toString()

// Replacing or trimming all the text that are unnecessary and only keeping the , . & numbers.
                KO = KO.replace("[^0-9,.]".toRegex(), "").trim { it <= ' ' }
                Polygon_lat_lng.add(KO)
                Log.e("Polygon", Polygon_lat_lng.toString())

//                txtPolygon.text = ""
            } else {
                count = 0
            }
        }
    }

    private fun calculateDistance(latLngArrayListPolygon: ArrayList<LatLng>) {
// Calculating meters from polygon list
        val m = SphericalUtil.computeArea(latLngArrayListPolygon)
        Log.e("m", "computeArea $m")

// converting meters to acers
        val df = DecimalFormat("#.#####")
        polygon_area = df.format(m * 0.0001).toDouble()
        Log.e("a", "computeArea $polygon_area")
        binding.polygonArea.text = "$polygon_area  hectare"
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
        textView.text = ""
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
        textView.text = ""
        textView.text = text
        textView.textSize = fontSize.toFloat()
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

    override fun onBackPressed() {
        runnable?.let { handler.removeCallbacks(it) } //stop handler when activity not visible
        super.onBackPressed()
        finish()
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocationAccuracy() {
        val startingNumber = 20
        var currentNumber = startingNumber

        lifecycleScope.launch {
            while (currentNumber >= 3) {
                println(currentNumber)
                currentNumber--

                delay(2000) // Pause for 2 seconds
                binding.polygonAccuracy.text = "Accuracy : $currentNumber meters"
            }
        }

    }

    fun convertLatLngListToStringList(latlngList: ArrayList<LatLng>): ArrayList<String> {
        val stringList = ArrayList<String>()

        // Map each LatLng object to its string representation
        for (latlng in latlngList) {
            val lat = latlng.latitude
            val lng = latlng.longitude
            val latlngString = "$lat,$lng"
            stringList.add(latlngString)
        }

        return stringList
    }
}