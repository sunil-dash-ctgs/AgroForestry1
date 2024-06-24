package co.kcagroforestry.app.revisit

import android.content.Context
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
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.R
import co.kcagroforestry.app.cropintellix.DashBoardCrop
import co.kcagroforestry.app.databinding.ActivityPolygonMappingBinding
import co.kcagroforestry.app.databinding.ActivityRevisitDeatailsBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import co.kcagroforestry.app.polygon.CaptureData
import com.google.android.gms.maps.model.LatLng
import com.kosherclimate.userapp.TimerData
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RevisitDeatails : AppCompatActivity() {

    lateinit var farmer_Next: Button
    lateinit var farmer_back: Button

    lateinit var binding: ActivityRevisitDeatailsBinding

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
    var polygonid = ""
    private var Polygon_lat_lng = ArrayList<String>()
    var revisitnoArrayList = java.util.ArrayList<String>()

    lateinit var timerData: TimerData
    var StartTime = 0;

    private var selectedSearchTypeId = 0
    private var searchTypeIDList = arrayListOf<Int>()
    private var searchTypeList = arrayListOf<String>()

    private var serachId: Int = 0
    private var searchName: Int = 0

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_revisit_deatails)

        binding = ActivityRevisitDeatailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@RevisitDeatails, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        timerData = TimerData(this@RevisitDeatails, binding.textTimer)
        StartTime = timerData.startTime(0).toInt()

        farmer_back = findViewById(R.id.assam_farmer_back)

        fetchSearchType()

        farmer_back.setOnClickListener {
            super.onBackPressed()
        }

        binding.assamFarmerNext.setOnClickListener {

            if (binding.assamFarmerName.text.toString().equals("")) {

                val WarningDialog =
                    SweetAlertDialog(this@RevisitDeatails, SweetAlertDialog.WARNING_TYPE)
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Search Framer Details"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else if (binding.firstRevisitNo.text.toString().equals("--Select--")) {

                val WarningDialog =
                    SweetAlertDialog(this@RevisitDeatails, SweetAlertDialog.WARNING_TYPE)
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Your Revisit Number"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else if (binding.firstRevisitNo.text.toString().equals("")) {

                val WarningDialog =
                    SweetAlertDialog(this@RevisitDeatails, SweetAlertDialog.WARNING_TYPE)
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Your Revisit Number"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else {

                checkvisitnumber()

                //nextScreen()
            }

        }

        binding.pipeSearch.setOnClickListener {

            if (binding.searchData.text.toString() == "") {

                val WarningDialog =
                    SweetAlertDialog(this@RevisitDeatails, SweetAlertDialog.WARNING_TYPE)
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
        apiInterface.searchFramer("Bearer $token", searchNumber,serachId.toString()).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

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
                                this@RevisitDeatails,
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
                }else if(response.code() == 422){

                    progress.dismiss()

                    val warningDialog = SweetAlertDialog(this@RevisitDeatails, SweetAlertDialog.WARNING_TYPE)
                    warningDialog.titleText = resources.getString(R.string.warning)
                    warningDialog.contentText = "Data is not available"
                    warningDialog.confirmText = " OK "
                    warningDialog.showCancelButton(false)
                    warningDialog.setCancelable(false)
                    warningDialog.setConfirmClickListener {
                        warningDialog.cancel()
                        //  backScreen()
                    }.show()
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

                                polygonid = polygonlist.getString("id")
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

                            } else {

                                val WarningDialog = SweetAlertDialog(
                                    this@RevisitDeatails,
                                    SweetAlertDialog.WARNING_TYPE
                                )
                                WarningDialog.titleText = resources.getString(R.string.warning)
                                WarningDialog.contentText = "Fill Polygon First"
                                WarningDialog.confirmText = resources.getString(R.string.ok)
                                WarningDialog.setCancelClickListener {
                                    WarningDialog.cancel()
                                    startActivity(
                                        Intent(
                                            this@RevisitDeatails,
                                            DashBoardCrop::class.java
                                        )
                                    )
                                    finish()
                                }.show()

                            }

                            var locationData = jsonObject.getJSONObject("location")
                            totalarea = locationData.getString("totalarea")
                            plantedarea = locationData.getString("planted_area")

                            binding.assamFarmerAge.text = farmer_age.toEditable()
                            binding.assamFarmerName.text = farmer_name.toEditable()
                            binding.assamGuardianName.text = guardian_name.toEditable()
                            binding.assamAadharno.text = aadharnumber.toEditable()

                            getframseason()
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

        val intent = Intent(this@RevisitDeatails, CaptureDataRevisit::class.java).apply {
            putExtra("farmer_id", farmeruniquid)
            putStringArrayListExtra("polygon_lat_lng", Polygon_lat_lng)
            putExtra("farmer_name", farmer_name)
            putExtra("plot_area", plot_area)
            putExtra("totalarea", totalarea)
            putExtra("plantedarea", plantedarea)
            putExtra("polygonid", polygonid)
            putExtra("revisitNO", binding.firstRevisitNo.text.toString())
            putExtra("StartTime", StartTime)

        }
        startActivity(intent)

        binding.assamFarmerAge.text = "".toEditable()
        binding.assamFarmerName.text = "".toEditable()
        binding.assamGuardianName.text = "".toEditable()
        binding.assamAadharno.text = "".toEditable()
        binding.searchData.text = "".toEditable()
        binding.farmerUniqueId.text = "".toEditable()
        binding.firstRevisitNo.text = "".toEditable()

    }

    fun getframseason() {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.farmervisitno("Bearer $token")
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {

                    revisitnoArrayList.clear()

                    if (response.body() != null) {

                        revisitnoArrayList.add("--Select--")

                        if (response.code() == 200) {

                            val stringResponse = JSONObject(response.body()!!.string())
                            val number_array = stringResponse.optJSONArray("numbers")

                            if (number_array != null) {
                                for (i in 0 until number_array.length()) {
                                    revisitnoArrayList.add(number_array.get(i).toString())

                                }
                            }

                            val adapter9: ArrayAdapter<String> =
                                ArrayAdapter<String>(
                                    this@RevisitDeatails,
                                    android.R.layout.select_dialog_item,
                                    revisitnoArrayList
                                )
                            binding.firstRevisitNo.threshold =
                                1 //will start working from first character
                            binding.firstRevisitNo.setAdapter(adapter9) //setting the adapter data into the AutoCompleteTextView
                            binding.firstRevisitNo.setTextColor(Color.BLACK)
                        }
                    }


                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.message?.let { Log.e("NEW_TEST", it) }
                    progress.dismiss()
                }

            })

    }

    fun checkvisitnumber() {

        Log.d("userdata", farmeruniquid + "  " + binding.firstRevisitNo.text.toString())

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.checkvisitno(
            "Bearer $token",
            farmeruniquid,
            binding.firstRevisitNo.text.toString()
        )
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {

                    if (response.code() == 200) {

                        val stringResponse = JSONObject(response.body()!!.string())

                        Log.d("responsedetails", response.body()!!.string())

                        val WarningDialog =
                            SweetAlertDialog(this@RevisitDeatails, SweetAlertDialog.WARNING_TYPE)
                        WarningDialog.titleText = resources.getString(R.string.warning)
                        WarningDialog.contentText = "Already Done Please \n Select Another No"
                        WarningDialog.confirmText = resources.getString(R.string.ok)
                        WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

                    } else if (response.code() == 423) {

                        val WarningDialog =
                            SweetAlertDialog(this@RevisitDeatails, SweetAlertDialog.WARNING_TYPE)
                        WarningDialog.titleText = resources.getString(R.string.warning)
                        WarningDialog.contentText = "Please fill first previous revisit no"
                        WarningDialog.confirmText = resources.getString(R.string.ok)
                        WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

                    } else {
                        nextScreen()
                    }


                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    t.message?.let { Log.e("NEW_TEST", it) }
                    progress.dismiss()
                }

            })

    }

    private fun fetchSearchType() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.searchType("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progress.dismiss()
                when (response.code()) {
                    200 -> {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val jsonArray = stringResponse.optJSONArray("type")
                        if (jsonArray != null) {
                            if (jsonArray.length() > 0) {
                                searchTypeIDList.add(0)
                                searchTypeList.add("Select Search Type")
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject = jsonArray.getJSONObject(i)
                                    val searchTypeId = jsonObject.optString("id").toInt()
                                    val searchTypeName = jsonObject.optString("name")
                                    searchTypeIDList.add(searchTypeId)
                                    searchTypeList.add(searchTypeName)
                                }
                                searchTypeSpinner()
                            }
                        }
                    }
                    else ->{
                        Toast.makeText(this@RevisitDeatails, "Failed to get Search Types. Please try again later.", Toast.LENGTH_SHORT).show()
                    }
                }

            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@RevisitDeatails, "Error occurred while getting Search Types.", Toast.LENGTH_SHORT).show()
                progress.dismiss()
            }
        })
    }

    private fun searchTypeSpinner(){
        val adapter = ArrayAdapter(this, R.layout.dropdown_list_layout, searchTypeList)
        binding.searchTypeSpinner.setText(adapter.getItem(0));
        binding.searchTypeSpinner.setAdapter(adapter)

        binding.searchTypeSpinner.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, position: Int,
                id: Long
            ) {
                selectedSearchTypeId = position

                serachId = searchTypeIDList[selectedSearchTypeId]

                var searchName = searchTypeList[position]
                Log.d("userdetailsposition", "Data   $serachId")
                Log.d("userdetailsposition", "Data1   $searchName")
            }
        }
    }
}