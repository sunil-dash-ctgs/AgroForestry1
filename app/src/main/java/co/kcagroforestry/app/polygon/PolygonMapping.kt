package co.kcagroforestry.app.polygon

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.R
import co.kcagroforestry.app.databinding.ActivityPolygonMappingBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import com.google.android.gms.maps.model.LatLng
import com.kosherclimate.userapp.TimerData
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PolygonMapping : AppCompatActivity() {

    lateinit var farmer_Next: Button
    lateinit var farmer_back: Button

    lateinit var binding: ActivityPolygonMappingBinding
    private lateinit var progress: SweetAlertDialog
    lateinit var token: String
    lateinit var userId: String
    var IDList = ArrayList<Int>()
    var FarmerUniqueList = ArrayList<String>()
    var SubPlotList = ArrayList<String>()
    private var farmerUniquePosition: Int = 0
    private var subPlotUniquePosition: Int = 0
    var UNIQURID: String = ""
    var id = ""
    var farmeruniquid = ""
    var farmer_name = ""
    var guardian_name = ""
    var farmer_age = ""
    var aadharnumber = ""
    var plot_area = ""
    var status = ""
    var totalarea = ""
    var plantedarea = ""
    private var Polygon_lat_lng = ArrayList<String>()

    lateinit var timerData: TimerData
    var StartTime = 0;

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //  setContentView(R.layout.activity_polygon_mapping)

        binding = ActivityPolygonMappingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@PolygonMapping, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!
        userId = sharedPreference.getString("user_id", "")!!

        farmer_back = findViewById(R.id.assam_farmer_back)

        farmer_back.setOnClickListener {
            super.onBackPressed()
        }

        timerData = TimerData(this@PolygonMapping, binding.textTimer)
        StartTime = timerData.startTime(0).toInt()

        binding.assamFarmerNext.setOnClickListener {

            if (binding.assamFarmerName.text.toString().equals("")) {

                val WarningDialog =
                    SweetAlertDialog(this@PolygonMapping, SweetAlertDialog.WARNING_TYPE)
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Search Framer Details"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else {
                if (status.equals("0")) {
                    nextScreen()
                } else {
                    nextScreen1()
                }
            }

        }

        binding.pipeSearch.setOnClickListener {

            if (binding.searchData.text.toString() == "") {

                val WarningDialog =
                    SweetAlertDialog(this@PolygonMapping, SweetAlertDialog.WARNING_TYPE)
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter For Search"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else {
                searchData(binding.searchData.text.toString())
            }
        }
    }

    fun searchData(searchNumber: String) {

        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.searchFramer("Bearer $token", searchNumber)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {

                    progress.dismiss()

                    if (response.code() == 200) {
                        FarmerUniqueList.clear()
                        IDList.clear()

                        if (response.body() != null) {

                            IDList.add(0)
                            FarmerUniqueList.add("--Select--")

                            val stringResponse = JSONObject(response.body()!!.string())
                            val jsonArray = stringResponse.optJSONArray("list")
                            Log.e("NEW_TEST", jsonArray.length().toString())

                            if (jsonArray.length() != 0) {

                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject = jsonArray.getJSONObject(i)
                                    val id = jsonObject.optInt("id")
                                    val farmer_uniqueId = jsonObject.optString("farmeruniquid")

                                    IDList.add(id)
                                    FarmerUniqueList.add(farmer_uniqueId)
                                }
                                farmerUniqueIdSpinner()
                                progress.dismiss()

                            } else {

                                Log.e("NEW_TEST", "got Farmer Lists")
                                Log.e("NEW_TEST", jsonArray.toString())
                                IDList.clear()
                                FarmerUniqueList.clear()

                                progress.dismiss()
                                val WarningDialog = SweetAlertDialog(
                                    this@PolygonMapping,
                                    SweetAlertDialog.WARNING_TYPE
                                )

                                WarningDialog.titleText = resources.getString(R.string.warning)
                                WarningDialog.contentText = "No data for given \n number"
                                WarningDialog.confirmText = " OK "
                                WarningDialog.showCancelButton(false)
                                WarningDialog.setCancelable(false)
                                WarningDialog.setConfirmClickListener {
                                    WarningDialog.cancel()
                                }.show()
                            }


                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.message?.let { Log.e("access", it) }
                    progress.dismiss()
                }


            })

    }

    private fun farmerUniqueIdSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, FarmerUniqueList)
        binding.farmerUniqueId.setText(adapter.getItem(0))
        binding.farmerUniqueId.setAdapter(adapter)

        binding.farmerUniqueId.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, position: Int,
                id: Long
            ) {
                farmerUniquePosition = position
                UNIQURID = FarmerUniqueList[farmerUniquePosition]
                if (position > 0) {
                    getFarmerDetails(UNIQURID)
                }
            }
        }
    }

    fun getFarmerDetails(uniqueId: String) {

        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.farmerDetails("Bearer $token", uniqueId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {

                    progress.dismiss()
                    Polygon_lat_lng.clear()

                    if (response.body() != null) {

                        if (response.code() == 200) {

                            val stringResponse = JSONObject(response.body()!!.string())
                            val jsonObject = stringResponse.getJSONObject("list")
                            Log.e("NEW_TEST", jsonObject.length().toString())

                            id = jsonObject.getString("id")
                            farmeruniquid = jsonObject.getString("farmeruniquid")
                            farmer_name = jsonObject.getString("farmer_name")
                            guardian_name = jsonObject.getString("guardian_name")
                            farmer_age = jsonObject.getString("farmer_age")
                            aadharnumber = jsonObject.getString("aadharnumber")
                            status = jsonObject.getString("status")
                            val str_polygon = jsonObject.getString("polygon").toString()
                            //val json_polygon = JSONObject(str_polygon)

                            Log.d("polygondata", str_polygon)

                            if (!str_polygon.equals("null")) {

                                var polygonlist = jsonObject.getJSONObject("polygon")

                                val id = polygonlist.getString("id")
                                val farmeruniquid = polygonlist.getString("farmeruniquid")
                                plot_area = polygonlist.getString("plot_area")
                                val locationArray = polygonlist.getJSONArray("ranges")

                                for (i in 0 until locationArray.length()) {
                                    val jsonObject = locationArray.getJSONObject(i)
                                    val lat = jsonObject.optString("lat")
                                    val lng = jsonObject.optString("lng")

                                    val latLng = LatLng(lat.toDouble(), lng.toDouble())
                                    Polygon_lat_lng.add(latLng.toString())
                                }

                            }

                            var locationData = jsonObject.getJSONObject("location")
                            totalarea = locationData.getString("totalarea")
                            plantedarea = locationData.getString("planted_area")

                            binding.assamFarmerAge.text = farmer_age.toEditable()
                            binding.assamFarmerName.text = farmer_name.toEditable()
                            binding.assamGuardianName.text = guardian_name.toEditable()
                            binding.assamAadharno.text = aadharnumber.toEditable()

                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.message?.let { Log.e("access", it) }
                    progress.dismiss()
                }


            })


    }

    fun nextScreen() {

        val intent = Intent(this@PolygonMapping, CaptureData::class.java).apply {
            putExtra("farmer_id", farmeruniquid)
            //  putStringArrayListExtra("polygon_lat_lng", Polygon_lat_lng)
            putExtra("farmer_name", farmer_name)
            putExtra("plot_area", plot_area)
            putExtra("totalarea", totalarea)
            putExtra("plantedarea", plantedarea)
            putExtra("StartTime", StartTime)

        }
        startActivity(intent)

        binding.assamFarmerAge.text = "".toEditable()
        binding.assamFarmerName.text = "".toEditable()
        binding.assamGuardianName.text = "".toEditable()
        binding.assamAadharno.text = "".toEditable()
        binding.searchData.text = "".toEditable()
        binding.farmerUniqueId.text = "".toEditable()
    }

    fun nextScreen1() {

        val intent = Intent(this@PolygonMapping, PolygonAlreadySubmited::class.java).apply {
            putExtra("farmer_id", farmeruniquid)
            putStringArrayListExtra("polygon_lat_lng", Polygon_lat_lng)
            putExtra("farmer_name", farmer_name)
            putExtra("plot_area", plot_area)
            putExtra("totalarea", totalarea)
            putExtra("plantedarea", plantedarea)
            putExtra("StartTime", StartTime)

        }
        startActivity(intent)

        binding.assamFarmerAge.text = "".toEditable()
        binding.assamFarmerName.text = "".toEditable()
        binding.assamGuardianName.text = "".toEditable()
        binding.assamAadharno.text = "".toEditable()
        binding.searchData.text = "".toEditable()
        binding.farmerUniqueId.text = "".toEditable()
    }

    override fun onStart() {
        super.onStart()

        timerData = TimerData(this@PolygonMapping, binding.textTimer)
        StartTime = timerData.startTime(0).toInt()
    }
}