package co.kcagroforestry.app.cropintellix

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.BuildConfig
import co.kcagroforestry.app.onboardingpage.FarmerOnBoarding
import co.kcagroforestry.app.polygon.PolygonMapping
import co.kcagroforestry.app.revisit.RevisitDeatails
import java.util.Locale
import co.kcagroforestry.app.R
import co.kcagroforestry.app.databinding.ActivityDashBoardCropBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import co.kcagroforestry.app.updateinfo.FarmerOnBoardingUpdate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Double
import java.security.Permissions


class DashBoardCrop : AppCompatActivity() {

    private lateinit var locale: Locale
    private lateinit var language: String
    private lateinit var token: String
    private lateinit var state_id: String

    lateinit var farmeronboarding : LinearLayout
    lateinit var polygonmapping : LinearLayout
    lateinit var revistplotdetail : LinearLayout
    lateinit var updateinfo : LinearLayout

    lateinit var farmer_back : Button
    lateinit var binding:ActivityDashBoardCropBinding
    private lateinit var progress: SweetAlertDialog
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var current_latitude: kotlin.Double= 0.0
    private var current_longitude: kotlin.Double= 0.0

    var PERMISSION_ALL = 1
    private var Permissions: Array<String> = arrayOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_dash_board_crop)

        binding = ActivityDashBoardCropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@DashBoardCrop, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token","")!!
        state_id = sharedPreference.getString("state_id","")!!

        Log.d("usertoken",token)

        farmeronboarding = findViewById(R.id.farmeronboarding)
        polygonmapping = findViewById(R.id.polygonmapping)
        revistplotdetail = findViewById(R.id.revistplotdetail)
        updateinfo = findViewById(R.id.updateinfo)
        farmer_back = findViewById(R.id.assam_farmer_back)

        val bundle = intent.extras
        if (bundle != null) {
            language = bundle.getString("language").toString()
        } else {
            language = "en"
            Log.e("data", "No bundle data")
        }

        locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        Permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS
        )

        if (!hasPermissions(this, *Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, PERMISSION_ALL)
        }


        farmeronboarding.setOnClickListener {
            startActivity(Intent(this@DashBoardCrop,FarmerOnBoarding::class.java))
        }
        polygonmapping.setOnClickListener {
            startActivity(Intent(this@DashBoardCrop,PolygonMapping::class.java))
        }
        revistplotdetail.setOnClickListener {
            startActivity(Intent(this@DashBoardCrop,RevisitDeatails::class.java))
        }
        updateinfo.setOnClickListener {
            startActivity(Intent(this@DashBoardCrop,FarmerOnBoardingUpdate::class.java))
        }

       /* binding.assamFarmerBack.setOnClickListener {

            onBackPressed()
        }*/

        farmer_back.setOnClickListener {
           startActivity(Intent(this@DashBoardCrop,LanguageSelectActivity::class.java))
       }

        val versionCode: Int = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME
        val release = Double.parseDouble(java.lang.String(Build.VERSION.RELEASE).replaceAll("(\\d+[.]\\d+)(.*)", "$1"))
        Log.e("version", versionName + versionCode + release.toString())

        val deviceName = Build.MODEL // returns model name
        val deviceManufacturer = Build.MANUFACTURER // returns manufacturer
        Log.e("version", deviceName + deviceManufacturer)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkVersion(versionName, versionCode)

        getCurrentLocation()

        val active = isLocationEnabled(this@DashBoardCrop)
        Log.e("active", active.toString())

        if (!active) {
            AlertDialog.Builder(this@DashBoardCrop)
                .setMessage(R.string.gps_network_not_enabled)
                .setPositiveButton(
                    R.string.yes
                ) { paramDialogInterface, paramInt ->
                    this@DashBoardCrop.startActivity(
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    )
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }
        else{
            getCurrentLocation()
        }


    }

    private fun checkVersion(versionName: String, versionCode: Int) {

        println("Checking app Version Current is $versionCode");
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = "Checking"
        progress.contentText = " Checking for new version"
        progress.setCancelable(false)
        progress.show()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.checkVersion("Bearer $token", versionName).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {

                        //modulesAvailable()
                        //getDateIntervals()

                        progress.dismiss()
                    }
                } else if (response.code() == 500) {
                    progress.dismiss()
                    println("Checking app Version Current is in 500 ${response.body()}");
                    if (response.errorBody() != null) {
                        val stringResponse = JSONObject(response.errorBody()!!.string())
                        val url = stringResponse.optString("url")
                        message(url)
                    }

//                    modulesAvailable()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@DashBoardCrop, "Please Retry", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                current_latitude = location.latitude
                current_longitude = location.longitude
// Use latitude and longitude values
                println("Latitude: $current_latitude, Longitude: $current_longitude")

            } else {
                // Handle the case where location is null
            }
        }
            .addOnFailureListener { exception: Exception ->
                Log.e("Dashboard Location Error", exception.message.toString())
// Handle any errors that occurred while retrieving the location
            }
    }

    private fun hasPermissions(context: Context?, vararg PERMISSIONS: String): Boolean {
        if (context != null) {
            for (permissions in PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(context, permissions) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun message(url: String) {

        val WarningDialog = SweetAlertDialog(this@DashBoardCrop, SweetAlertDialog.WARNING_TYPE)

        WarningDialog.titleText = " Warning "
        WarningDialog.contentText = " Please download new app. "
        WarningDialog.confirmText = " Download Now "
        WarningDialog.showCancelButton(false)
        WarningDialog.setCancelable(false)
        WarningDialog.setConfirmClickListener {
            WarningDialog.cancel()

            val newAppUrl = Uri.parse(url)
            val browserIntent = Intent(Intent.ACTION_VIEW, newAppUrl)
            ContextCompat.startActivity(this, browserIntent, null)

        }.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
       // finishAffinity()
       // finish()
    }
}