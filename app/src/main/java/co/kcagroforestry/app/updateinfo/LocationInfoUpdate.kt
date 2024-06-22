package co.kcagroforestry.app.updateinfo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.R
import co.kcagroforestry.app.databinding.ActivityLocationInfoUpdateBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import co.kcagroforestry.app.onboardingpage.PlantationInfo
import com.bumptech.glide.Glide
import com.kosherclimate.userapp.TimerData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class LocationInfoUpdate : AppCompatActivity() {

    lateinit var binding: ActivityLocationInfoUpdateBinding

    private lateinit var progress: SweetAlertDialog
    lateinit var timerData: TimerData
    var StartTime = 0;
    var StartTime1 = 0;
    lateinit var token: String
    var imageFileName: String = ""
    var unique_id: String = ""

    var stateNmaeList = ArrayList<String>()
    var stateIdList = ArrayList<Int>()

    var districtNmaeList = ArrayList<String>()
    var districtIdList = ArrayList<Int>()

    var talukaNmaeList = ArrayList<String>()
    var talukaIdList = ArrayList<Int>()

    var panchayatNmaeList = ArrayList<String>()
    var panchayatIdList = ArrayList<Int>()

    var villegeNmaeList = ArrayList<String>()
    var villegeIdList = ArrayList<Int>()

    var statePosition = 0
    lateinit var stateId: String

    var districtsPosition = 0
    lateinit var districtsId: String

    var talukaPosition = 0
    lateinit var talukaId: String

    var panchayatPosition = 0
    lateinit var panchayatId: String

    var villagePosition = 0
    lateinit var villageId: String

    lateinit var stateid: String
    lateinit var districtid: String
    lateinit var talukaid: String
    lateinit var villageid: String
    lateinit var panchayatid: String
    var statename: String = ""

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_location_info_update)

        binding = ActivityLocationInfoUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@LocationInfoUpdate, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("farmeruniquid")!!
            StartTime1 = bundle.getInt("StartTime")

            getState()
            showOnBoardingData(unique_id, "2")
        }

        timerData = TimerData(this@LocationInfoUpdate, binding.textTimer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()



        binding.locationNext.setOnClickListener {

            if (statePosition == 0) {

                submitLocationInfo()

            } else {

                val WarningDialog =
                    SweetAlertDialog(this@LocationInfoUpdate, SweetAlertDialog.WARNING_TYPE)

                if (stateNmaeList.isEmpty() || statePosition == 0) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.state_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (districtNmaeList.isEmpty() || districtsPosition == 0) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.district_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (talukaNmaeList.isEmpty() || talukaPosition == 0) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.taluka_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (panchayatNmaeList.isEmpty() || panchayatPosition == 0) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.panchayat_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (villegeNmaeList.isEmpty() || villagePosition == 0) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = resources.getString(R.string.village_warning)
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (binding.pincode.text.toString().equals("")) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = "Enter Your Pincode"
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                }/*else if (binding.remark.text.toString().equals("")){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Villege Remarks"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }*/ else if (binding.RegestrationNo.text.toString().equals("")) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = "Enter Your Patta Number"
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (binding.SurveyNo.text.toString().equals("")) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = "Enter Your SurveyNo Number"
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (binding.TotalAcers.text.toString().equals("")) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = "Enter Your Total Acers Number"
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else if (binding.PlantedAcers.text.toString().equals("")) {
                    WarningDialog.titleText = resources.getString(R.string.warning)
                    WarningDialog.contentText = "Enter Your Planted Acers Number"
                    WarningDialog.confirmText = resources.getString(R.string.ok)
                    WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
                } else {

                    submitLocationInfo()
                }
            }


        }
    }

    fun showOnBoardingData(framerunique: String, screennumber: String) {

        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.getfarmerdata("Bearer $token", framerunique, screennumber)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>, response: Response<ResponseBody>
                ) {

                  //  if (response.body() != null) {

                        progress.dismiss()

                        when (response.code()) {

                            200 -> {

                                val stringResponse = JSONObject(response.body()!!.string())
                                val framerDetails = stringResponse.optJSONObject("farmer")

                                val farmeruniquid = framerDetails.getString("farmeruniquid")
                                val pincode = framerDetails.getString("pincode")
                                val remark = framerDetails.getString("remark")
                                val pattanumber = framerDetails.getString("pattanumber")
                                val survey_no = framerDetails.getString("survey_no")
                                val totalarea = framerDetails.getString("totalarea")
                                val planted_area = framerDetails.getString("planted_area")
                                val document1_photo = framerDetails.getString("document1_photo")
                                val document2_photo = framerDetails.getString("document2_photo")
                                val document3_photo = framerDetails.getString("document3_photo")
                                val panchayat = framerDetails.getString("panchayat")

                                val state = framerDetails.getJSONObject("state")
                                stateid = state.getString("id")
                                statename = state.getString("name")

                                val district = framerDetails.getJSONObject("district")
                                districtid = district.getString("id")
                                val districtname = district.getString("district")

                                val taluka = framerDetails.getJSONObject("taluka")
                                talukaid = taluka.getString("id")
                                val talukaname = taluka.getString("taluka")

                                val village = framerDetails.getJSONObject("village")
                                villageid = village.getString("id")
                                val villagename = village.getString("village")


                                var panchayatname = ""
                                if (!panchayat.equals("null")) {
                                    val panchayat1 = JSONObject(panchayat)
                                    panchayatid = panchayat1.getString("id")
                                    panchayatname = panchayat1.getString("panchayat")
                                }else{
                                    panchayatid = "1"
                                }

                                Log.d("NEW_TEST", "Farmer Unique ID: $farmeruniquid")
                                Log.d("NEW_TEST", "pincode: $pincode")
                                Log.d("NEW_TEST", "remark: $remark")
                                Log.d("NEW_TEST", "pattanumber: $pattanumber")
                                Log.d("NEW_TEST", "survey_no: $survey_no")
                                Log.d("NEW_TEST", "totalarea: $totalarea")
                                Log.d("NEW_TEST", "planted_area: $planted_area")
                                Log.d("NEW_TEST", "state: $state")
                                Log.d("NEW_TEST", "district: $district")
                                Log.d("NEW_TEST", "taluka: $taluka")
                                Log.d("NEW_TEST", "village: $village")
                                Log.d("NEW_TEST", "panchayat: $panchayat")

                                binding.state.text = statename.toEditable()
                                binding.district.text = districtname.toEditable()
                                binding.taluka.text = talukaname.toEditable()
                                binding.village.text = villagename.toEditable()
                                binding.panchayat.text = panchayatname.toEditable()
                                binding.SurveyNo.text = survey_no.toEditable()
                                binding.pincode.text = pincode.toEditable()
                                binding.remark.text = remark.toEditable()
                                binding.RegestrationNo.text = pattanumber.toEditable()
                                binding.TotalAcers.text = totalarea.toEditable()
                                binding.PlantedAcers.text = planted_area.toEditable()

                                Glide.with(this@LocationInfoUpdate).load(document1_photo)
                                    .into(binding.document1Camera)
                                Glide.with(this@LocationInfoUpdate).load(document2_photo)
                                    .into(binding.document2Camera)
                                Glide.with(this@LocationInfoUpdate).load(document3_photo)
                                    .into(binding.document3Camera)

                                stateSpinner()

                            }

                            423 -> {

                                progress.dismiss()

                                val WarningDialog = SweetAlertDialog(
                                    this@LocationInfoUpdate, SweetAlertDialog.WARNING_TYPE
                                )

                                WarningDialog.titleText = resources.getString(R.string.warning)
                                WarningDialog.contentText = "Farmer Onboarding not \n Completed"
                                WarningDialog.confirmText = " OK "
                                WarningDialog.showCancelButton(false)
                                WarningDialog.setCancelable(false)
                                WarningDialog.setConfirmClickListener {
                                    WarningDialog.cancel()
                                }.show()
                            }

                            else -> {
                                Toast.makeText(
                                    this@LocationInfoUpdate,
                                    "Failed to get Search Types. Please try again later.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                //    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@LocationInfoUpdate, "" + t, Toast.LENGTH_SHORT)
                        .show()
                }

            })


    }

    fun getState() {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.state().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                stateNmaeList.clear()
                stateIdList.clear()

                if (response.body() != null) {

                    if (response.code() == 200) {

                        stateNmaeList.add("--Select--")
                        stateIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("state")

                        for (i in 0 until stateArray.length()) {

                            val jsonState = stateArray.getJSONObject(i)
                            val id = jsonState.optInt("id")
                            val name = jsonState.optString("name")
                            val lm_units = jsonState.optString("lm_units")
                            val base_value = jsonState.optString("base_value")
                            val max_base_value = jsonState.optString("max_base_value")

                            stateNmaeList.add(name)
                            stateIdList.add(id)
                        }

                        stateSpinner()

                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LocationInfoUpdate, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })

    }

    fun getDistricts(id: String) {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.districts(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                districtNmaeList.clear()
                districtIdList.clear()

                if (response.body() != null) {

                    if (response.code() == 200) {

                        districtNmaeList.add("--Select--")
                        districtIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("district")

                        for (i in 0 until stateArray.length()) {

                            val jsonState = stateArray.getJSONObject(i)
                            val id = jsonState.optInt("id")
                            val district = jsonState.optString("district")

                            districtNmaeList.add(district)
                            districtIdList.add(id)
                        }

                        districtsSpinner()

                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LocationInfoUpdate, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })


    }

    fun getTaluka(id: String) {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.taluka(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                talukaNmaeList.clear()
                talukaIdList.clear()

                if (response.body() != null) {

                    if (response.code() == 200) {

                        talukaNmaeList.add("--Select--")
                        talukaIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("Taluka")

                        for (i in 0 until stateArray.length()) {

                            val jsonState = stateArray.getJSONObject(i)
                            val id = jsonState.optInt("id")
                            val taluka = jsonState.optString("taluka")

                            talukaNmaeList.add(taluka)
                            talukaIdList.add(id)
                        }

                        talukaSpinner()

                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LocationInfoUpdate, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })


    }

    fun getvillagepanchayat(id: String) {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.villagepanchayat(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                panchayatNmaeList.clear()
                panchayatIdList.clear()

                if (response.body() != null) {

                    if (response.code() == 200) {

                        panchayatNmaeList.add("--Select--")
                        panchayatIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("panchayat")

                        for (i in 0 until stateArray.length()) {

                            val jsonState = stateArray.getJSONObject(i)
                            val id = jsonState.optInt("id")
                            val panchayat = jsonState.optString("panchayat")

                            panchayatNmaeList.add(panchayat)
                            panchayatIdList.add(id)
                        }

                        panchayatSpinner()

                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LocationInfoUpdate, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })


    }

    fun getVillage(id: String) {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.Village(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                villegeNmaeList.clear()
                villegeIdList.clear()

                if (response.body() != null) {

                    if (response.code() == 200) {

                        villegeNmaeList.add("--Select--")
                        villegeIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("Village")

                        for (i in 0 until stateArray.length()) {

                            val jsonState = stateArray.getJSONObject(i)
                            val id = jsonState.optInt("id")
                            val Village = jsonState.optString("village")

                            villegeNmaeList.add(Village)
                            villegeIdList.add(id)
                        }

                        villageSpinner()

                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LocationInfoUpdate, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })


    }

    private fun stateSpinner() {

        binding.state.text = statename.toEditable()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stateNmaeList)
        //binding.state.setText(adapter.getItem(0));
        binding.state.setAdapter(adapter)

        binding.state.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, position: Int,
                id: Long
            ) {
                statePosition = position

                Log.e("stateposition", statePosition.toString())
                if (position != 0) {
                    stateId = stateIdList[statePosition].toString()
                    getDistricts(stateId)

                    //binding.state.text = "".toEditable()
                    binding.district.text = "".toEditable()
                    binding.taluka.text = "".toEditable()
                    binding.village.text = "".toEditable()
                    binding.panchayat.text = "".toEditable()


                }
            }
        }
    }

    private fun districtsSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, districtNmaeList)
        binding.district.setText(adapter.getItem(0));
        binding.district.setAdapter(adapter)
        binding.district.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, position: Int,
                id: Long
            ) {
                districtsPosition = position
                Log.e("stateposition", districtsPosition.toString())
                if (position != 0) {
                    districtsId = districtIdList[districtsPosition].toString()
                    getTaluka(districtsId)
                }
            }
        }
    }

    private fun talukaSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, talukaNmaeList)
        binding.taluka.setText(adapter.getItem(0));
        binding.taluka.setAdapter(adapter)
        binding.taluka.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, position: Int,
                id: Long
            ) {
                talukaPosition = position
                Log.e("stateposition", talukaPosition.toString())
                if (position != 0) {
                    talukaId = talukaIdList[talukaPosition].toString()
                    getvillagepanchayat(talukaId)
                }
            }
        }
    }

    private fun panchayatSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, panchayatNmaeList)
        binding.panchayat.setText(adapter.getItem(0));
        binding.panchayat.setAdapter(adapter)
        binding.panchayat.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, position: Int,
                id: Long
            ) {
                panchayatPosition = position
                Log.e("stateposition", panchayatPosition.toString())
                if (position != 0) {
                    panchayatId = panchayatIdList[panchayatPosition].toString()
                    getVillage(panchayatId)
                }
            }
        }
    }

    private fun villageSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, villegeNmaeList)
        binding.village.setText(adapter.getItem(0));
        binding.village.setAdapter(adapter)
        binding.village.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, position: Int,
                id: Long
            ) {
                villagePosition = position
                Log.e("stateposition", villagePosition.toString())
                if (position != 0) {
                    villageId = villegeIdList[villagePosition].toString()
                }
            }
        }
    }

    fun submitLocationInfo() {

        if (statePosition == 0 && districtsPosition == 0 && talukaPosition == 0
            && panchayatPosition == 0 && villagePosition == 0
        ) {

            stateId = stateid
            districtsId = districtid
            talukaId = talukaid
            panchayatId = panchayatid
            villageId = villageid

        }

        Log.e("NEW_TEST", "Entered sendData")
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_send)
        progress.setCancelable(false)
        progress.show()


//        val file1 = File(image1)
//        val file2 = File(image2)
//        val file3 = File(image3)

        val stateid: RequestBody = stateId.toRequestBody("text/plain".toMediaTypeOrNull())
        val uniqueid: RequestBody = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val districtsid: RequestBody = districtsId.toRequestBody("text/plain".toMediaTypeOrNull())
        val talukaid: RequestBody = talukaId.toRequestBody("text/plain".toMediaTypeOrNull())
        val panchayatid: RequestBody = panchayatId.toRequestBody("text/plain".toMediaTypeOrNull())
        val villageid: RequestBody = villageId.toRequestBody("text/plain".toMediaTypeOrNull())
        val pincode_no: RequestBody =
            binding.pincode.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val remark_data: RequestBody =
            binding.remark.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val regestraction: RequestBody =
            binding.RegestrationNo.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val survery_no: RequestBody =
            binding.SurveyNo.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val totalacers: RequestBody =
            binding.TotalAcers.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val planted_acers: RequestBody =
            binding.PlantedAcers.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())


//        val requestFileImage1: RequestBody = file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//        val requestFileImage2: RequestBody = file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//        val requestFileImage3: RequestBody = file3.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val farmeruniquid: MultipartBody.Part =
            MultipartBody.Part.createFormData("farmeruniquid", null, uniqueid)
        val state_id: MultipartBody.Part =
            MultipartBody.Part.createFormData("state_id", null, stateid)
        val district_id: MultipartBody.Part =
            MultipartBody.Part.createFormData("district_id", null, districtsid)
        val taluka_id: MultipartBody.Part =
            MultipartBody.Part.createFormData("taluka_id", null, talukaid)
        val village_id: MultipartBody.Part =
            MultipartBody.Part.createFormData("village_id", null, villageid)
        val pincode: MultipartBody.Part =
            MultipartBody.Part.createFormData("pincode", null, pincode_no)
        val remark: MultipartBody.Part =
            MultipartBody.Part.createFormData("remark", null, remark_data)
        val pattanumber: MultipartBody.Part =
            MultipartBody.Part.createFormData("pattanumber", null, regestraction)
        val survey_no: MultipartBody.Part =
            MultipartBody.Part.createFormData("survey_no", null, survery_no)
        val totalarea: MultipartBody.Part =
            MultipartBody.Part.createFormData("totalarea", null, totalacers)
        val planted_area: MultipartBody.Part =
            MultipartBody.Part.createFormData("planted_area", null, planted_acers)

//        val ImageBody1 : MultipartBody.Part = MultipartBody.Part.createFormData("document1_photo", file1.name, requestFileImage1)
//        val ImageBody2: MultipartBody.Part = MultipartBody.Part.createFormData("document2_photo", file2.name, requestFileImage2)
//        val ImageBody3: MultipartBody.Part = MultipartBody.Part.createFormData("document3_photo", file3.name, requestFileImage3)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        val submitLocation = apiInterface.updatefarmerlocation(
            "Bearer $token", farmeruniquid, state_id, district_id, taluka_id,
            village_id, pincode, remark, pattanumber, survey_no, totalarea, planted_area,
        )

        submitLocation.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                progress.dismiss()

                if (response.body() != null) {

                    if (response.code() == 200) {

                        val stringResponse = JSONObject(response.body()!!.string())
                        var farmerId = stringResponse.getString("farmerId")
                        var farmerUniqueId = stringResponse.getString("farmerUniqueId")

                        val intent = Intent(
                            this@LocationInfoUpdate,
                            PlantationInfoUpdate::class.java
                        ).apply {
                            putExtra("farmeruniquid", farmerUniqueId)
                            putExtra("FarmerId", farmerId)
                            putExtra("StartTime", StartTime)
                        }
                        startActivity(intent)


                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("NEW_TEST", it) }
                progress.dismiss()
            }


        })
    }

    private fun backScreen() {
        super.onBackPressed()
        finish()
    }

}