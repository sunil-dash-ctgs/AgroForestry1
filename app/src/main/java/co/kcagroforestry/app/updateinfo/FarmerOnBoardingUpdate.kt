package co.kcagroforestry.app.updateinfo

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
import android.widget.RadioButton
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.R
import co.kcagroforestry.app.databinding.ActivityFarmerOnBoardingUpdateBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import co.kcagroforestry.app.onboardingpage.LocationInfo
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
import java.util.Locale

class FarmerOnBoardingUpdate : AppCompatActivity() {

    lateinit var binding: ActivityFarmerOnBoardingUpdateBinding
    private lateinit var progress: SweetAlertDialog
    lateinit var timerData: TimerData
    var StartTime = 0;
    lateinit var token: String
    var IDList = ArrayList<Int>()
    var FarmerUniqueList = ArrayList<String>()
    private var farmerUniquePosition: Int = 0
    var UNIQURID: String = ""
    var id = ""
    var farmeruniquid = ""
    var farmer_name = ""
    var guardian_name = ""
    var plot_area = ""
    var status = ""
    var totalarea = ""
    var plantedarea = ""

    private var selectedSearchTypeId = 0
    private var searchTypeIDList = arrayListOf<Int>()
    private var searchTypeList = arrayListOf<String>()

    private var serachId: Int = 0
    private var searchName: Int = 0
    val access = arrayOf("--Select--", "Own Number", "Relatives Number")
    var realtionshipIDList = java.util.ArrayList<Int>()
    var relationshipNameList = java.util.ArrayList<String>()
    var relationship: String = ""
    var mobile_access: String = ""
    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_farmer_on_boarding_update)

        binding = ActivityFarmerOnBoardingUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@FarmerOnBoardingUpdate, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        timerData = TimerData(this@FarmerOnBoardingUpdate, binding.textTimer)
        StartTime = timerData.startTime(0).toInt()

        fetchSearchType()
        relationshipAPI()

        if (binding.mobileAccess != null) {

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, access)
            binding.mobileAccess.setAdapter(adapter)

            binding.mobileAccess.onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(
                    parent: AdapterView<*>?, arg1: View?, position: Int,
                    id: Long
                ) {
                    mobile_access = access[position]
                    Log.e("access", access[position])
                    binding.owenRelationship.isEnabled = position == 2

                    if (position != 2) {

                        binding.owenRelationship.isEnabled = false
                        binding.owenRelationship.isClickable = false

                        binding.relationshipdata.isClickable = false
                        binding.relationshipdata.isEnabled = false

                        binding.owenRelationship.text = "".toEditable()

                    }else{

                        binding.owenRelationship.isEnabled = true
                        binding.owenRelationship.isClickable = true

                        binding.relationshipdata.isClickable = true
                        binding.relationshipdata.isEnabled = true

                    }
                }
            }
        }

        binding.pipeSearch.setOnClickListener {

            if (binding.searchData.text.toString() == "") {

                val WarningDialog =
                    SweetAlertDialog(this@FarmerOnBoardingUpdate, SweetAlertDialog.WARNING_TYPE)
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter For Search"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else {

                searchData(binding.searchData.text.toString())
            }
        }

        binding.assamFarmerNext.setOnClickListener {

            val WarningDialog = SweetAlertDialog(this@FarmerOnBoardingUpdate, SweetAlertDialog.WARNING_TYPE)

            if (binding.farmerName.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.farmer_name_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.farmerAge.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Age"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.guardianName.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Fill Your Guardian Name"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.mobileAccess.text.toString().equals("--Select--")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select You Nominee_name"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.assamMobile.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Mobile No"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.assamMobile.text.toString().length < 10) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.mobile_length_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.nomineName.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Nominee Name"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.assamWhatesappno.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your WastesApp No"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.assamAadharno.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Aadhaar No"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.assamAadharno.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Aadhaar No"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.farmerUniqueId.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Fill the UniqueId"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else{

                submitOnBording()
            }


        }
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

                    else -> {
                        Toast.makeText(
                            this@FarmerOnBoardingUpdate,
                            "Failed to get Search Types. Please try again later.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    this@FarmerOnBoardingUpdate,
                    "Error occurred while getting Search Types.",
                    Toast.LENGTH_SHORT
                ).show()
                progress.dismiss()
            }
        })
    }
    private fun searchTypeSpinner() {
        val adapter = ArrayAdapter(this, R.layout.dropdown_list_layout, searchTypeList)
        binding.searchTypeSpinner.setText(adapter.getItem(0));
        binding.searchTypeSpinner.setAdapter(adapter)

        binding.searchTypeSpinner.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, position: Int, id: Long
            ) {
                selectedSearchTypeId = position

                serachId = searchTypeIDList[selectedSearchTypeId]

                var searchName = searchTypeList[position]
                Log.d("userdetailsposition", "Data   $serachId")
                Log.d("userdetailsposition", "Data1   $searchName")
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
        apiInterface.searchFramer("Bearer $token", searchNumber, serachId.toString())
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>, response: Response<ResponseBody>
                ) {

                    progress.dismiss()

                    Log.d("responsedeta", response.toString())

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
                                    this@FarmerOnBoardingUpdate, SweetAlertDialog.WARNING_TYPE
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
                    } else if (response.code() == 422) {

                        progress.dismiss()

                        val warningDialog = SweetAlertDialog(
                            this@FarmerOnBoardingUpdate, SweetAlertDialog.WARNING_TYPE
                        )
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
                parent: AdapterView<*>?, arg1: View?, position: Int, id: Long
            ) {
                farmerUniquePosition = position
                UNIQURID = FarmerUniqueList[farmerUniquePosition]
                if (position > 0) {
                    showOnBoardingData(UNIQURID, "1")
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

                 //   if (response.body() != null) {

                        progress.dismiss()

                        when (response.code()) {

                            200 -> {

                                val stringResponse = JSONObject(response.body()!!.string())
                                val framerDetails = stringResponse.optJSONObject("farmer")

                                val farmeruniquid = framerDetails.getString("farmeruniquid")
                                val farmer_name = framerDetails.optString("farmer_name")
                                val farmer_age = framerDetails.optString("farmer_age")
                                val gender = framerDetails.optString("gender")
                                val cast = framerDetails.optString("cast")
                                val guardian_name = framerDetails.optString("guardian_name")
                                val nominee_name = framerDetails.optString("nominee_name")
                                val relationwith = framerDetails.optString("relationwith")
                                val mobileno = framerDetails.optString("mobileno")
                                val whats_no = framerDetails.optString("whats_no")
                                val aadharnumber = framerDetails.optString("aadharnumber")
                                val document1_photo = framerDetails.optString("document1_photo")
                                val document2_photo = framerDetails.optString("document2_photo")
                                val document3_photo = framerDetails.optString("document3_photo")
                                val status = framerDetails.optString("status")
                                val mobile_relation_owner = framerDetails.optString("mobile_relation_owner")

                                Log.d("NEW_TEST", "Farmer Unique ID: $farmeruniquid")
                                Log.d("NEW_TEST", "farmer_name: $farmer_name")
                                Log.d("NEW_TEST", "farmer_age: $farmer_age")
                                Log.d("NEW_TEST", "gender: $gender")
                                Log.d("NEW_TEST", "cast: $cast")
                                Log.d("NEW_TEST", "guardian_name: $guardian_name")
                                Log.d("NEW_TEST", "nominee_name: $nominee_name")
                                Log.d("NEW_TEST", "relationwith: $relationwith")
                                Log.d("NEW_TEST", "mobileno: $mobileno")
                                Log.d("NEW_TEST", "whats_no: $whats_no")
                                Log.d("NEW_TEST", "aadharnumber: $aadharnumber")
                                Log.d("NEW_TEST", "mobile_relation_owner: $mobile_relation_owner")

                                if (!relationwith.isNullOrEmpty()){
                                    binding.owenRelationship.text = relationwith.toEditable()
                                }

                                if (!mobile_relation_owner.isNullOrEmpty()){
                                    binding.mobileAccess.text = mobile_relation_owner.toEditable()
                                }

                                if (relationwith.equals("null")){
                                    binding.owenRelationship.text = "".toEditable()
                                }

                                if (mobile_relation_owner.equals("null")){
                                    binding.mobileAccess.text = "".toEditable()
                                }

                                binding.farmerName.text = farmer_name.toEditable()
                                binding.farmerAge.text = farmer_age.toEditable()
                                binding.guardianName.text = guardian_name.toEditable()
                                binding.nomineName.text = nominee_name.toEditable()
                                binding.assamMobile.text = mobileno.toEditable()
                                binding.assamWhatesappno.text = whats_no.toEditable()
                                binding.assamAadharno.text = aadharnumber.toEditable()

                                Glide.with(this@FarmerOnBoardingUpdate).load(document1_photo)
                                    .into(binding.document1Camera)
                                Glide.with(this@FarmerOnBoardingUpdate).load(document2_photo)
                                    .into(binding.document2Camera)
                                Glide.with(this@FarmerOnBoardingUpdate).load(document3_photo)
                                    .into(binding.document3Camera)

                                when (gender) {

                                    "Male" -> binding.assamRadioGroup.check(binding.assamRadioMale.id)
                                    "Female" -> binding.assamRadioGroup.check(binding.assamRadioFemale.id)
                                    "Other" -> binding.assamRadioGroup.check(binding.assamRadioOther.id)
                                    else -> binding.assamRadioGroup.check(binding.assamRadioMale.id)
                                }

                                when (cast) {

                                    "ST" -> binding.assamRadioGroup1.check(binding.assamRadioST.id)
                                    "SC" -> binding.assamRadioGroup1.check(binding.assamRadioSC.id)
                                    "BC" -> binding.assamRadioGroup1.check(binding.assamRadioBC.id)
                                    "OBC" -> binding.assamRadioGroup1.check(binding.assamRadioOBC.id)
                                    "General" -> binding.assamRadioGroup1.check(binding.assamRadioGeneral.id)
                                    else -> binding.assamRadioGroup1.check(binding.assamRadioGeneral.id)
                                }

                                val adapter = ArrayAdapter(this@FarmerOnBoardingUpdate, android.R.layout.simple_list_item_1, access)
                                binding.mobileAccess.setAdapter(adapter)

                                binding.relationshipdata.isClickable = false
                                binding.relationshipdata.isEnabled = false

                            }

                            423 -> {

                                progress.dismiss()

                                val WarningDialog = SweetAlertDialog(
                                    this@FarmerOnBoardingUpdate, SweetAlertDialog.WARNING_TYPE
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
                                    this@FarmerOnBoardingUpdate,
                                    "Failed to get Search Types. Please try again later.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                  //  }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@FarmerOnBoardingUpdate, "" + t, Toast.LENGTH_SHORT)
                        .show()
                }

            })


    }
    private fun relationshipAPI() {
        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.relationship("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.body() != null) {
                    val stringResponse = JSONObject(response.body()!!.string())
                    val jsonArray = stringResponse.optJSONArray("relationshipowner")

                    realtionshipIDList.clear()
                    relationshipNameList.clear()

                    relationshipNameList.add("--Select--")

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id").toInt()
                        val name = jsonObject.optString("name")

                        realtionshipIDList.add(id)
                        relationshipNameList.add(name)
                    }

                    relationshipSpinner()
                    progress.dismiss()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progress.dismiss()
                Toast.makeText(
                    this@FarmerOnBoardingUpdate,
                    "Please Retry",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    private fun relationshipSpinner() {
        if (binding.owenRelationship != null) {
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, relationshipNameList)
            binding.owenRelationship.setAdapter(adapter)

            binding.owenRelationship.onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(
                    parent: AdapterView<*>?, arg1: View?, position: Int,
                    id: Long
                ) {
                    relationship = relationshipNameList[position]
                    Log.e("owner_spinner", relationshipNameList[position])
                }
            }
        }
    }
    private fun backScreen() {
        super.onBackPressed()
        finish()
    }

    fun submitOnBording(){

        Log.e("NEW_TEST", "Entered sendData")
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_send)
        progress.setCancelable(false)
        progress.show()

        val intSelectButton1: Int = binding.assamRadioGroup.checkedRadioButtonId
        var radioButton1 : RadioButton = findViewById(intSelectButton1)

        val intSelectButton2: Int = binding.assamRadioGroup1.checkedRadioButtonId
        var radioButton2 : RadioButton = findViewById(intSelectButton2)

        Log.d("radiodetail1",radioButton1.text.toString())
        Log.d("radiodetail2",radioButton2.text.toString())

        if (relationship == "--Select--") {
            relationship = "NA"
        }
        Log.e("relationship", relationship)

//        val file1 = File(image1)
//        val file2 = File(image2)
//        val file3 = File(image3)

        val farmername: RequestBody = binding.farmerName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val farmerage: RequestBody = binding.farmerAge.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val radiogender: RequestBody = radioButton1.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val radiocast: RequestBody = radioButton2.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val guardianname: RequestBody = binding.guardianName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val mobileaccess: RequestBody = binding.mobileAccess.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val mobilenumber: RequestBody = binding.assamMobile.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val whatesnumber: RequestBody = binding.assamWhatesappno.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val aadhar_number: RequestBody = binding.assamAadharno.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val farmeruniqueid: RequestBody = binding.farmerUniqueId.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val relationship: RequestBody = relationship.toRequestBody("text/plain".toMediaTypeOrNull())
        val nomineename: RequestBody = binding.nomineName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())


//        val requestFileImage1: RequestBody = file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//        val requestFileImage2: RequestBody = file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//        val requestFileImage3: RequestBody = file3.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val farmer_name: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_name", null, farmername)
        val farmeruniquid: MultipartBody.Part = MultipartBody.Part.createFormData("farmeruniquid", null, farmeruniqueid)
        val farmer_age: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_age", null, farmerage)
        val gender: MultipartBody.Part = MultipartBody.Part.createFormData("gender", null, radiogender)
        val cast: MultipartBody.Part = MultipartBody.Part.createFormData("cast", null, radiocast)
        val guardian_name: MultipartBody.Part = MultipartBody.Part.createFormData("guardian_name", null, guardianname)
        val nominee_name: MultipartBody.Part = MultipartBody.Part.createFormData("nominee_name", null, nomineename)
        val relationwith: MultipartBody.Part = MultipartBody.Part.createFormData("relationwith", null, relationship)
        val mobileno: MultipartBody.Part = MultipartBody.Part.createFormData("mobileno", null, mobilenumber)
        val whats_no: MultipartBody.Part = MultipartBody.Part.createFormData("whats_no", null, whatesnumber)
        val aadharnumber: MultipartBody.Part = MultipartBody.Part.createFormData("aadharnumber", null, aadhar_number)
        val mobile_relation_owner: MultipartBody.Part = MultipartBody.Part.createFormData("mobile_relation_owner", null, mobileaccess)

//        val ImageBody1 : MultipartBody.Part = MultipartBody.Part.createFormData("document1_photo", file1.name, requestFileImage1)
//        val ImageBody2: MultipartBody.Part = MultipartBody.Part.createFormData("document2_photo", file2.name, requestFileImage2)
//        val ImageBody3: MultipartBody.Part = MultipartBody.Part.createFormData("document3_photo", file3.name, requestFileImage3)

        val submit_onboarding = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        var sumitdata = submit_onboarding.farmeronboardinginfoupdate("Bearer $token",farmeruniquid,farmer_name,farmer_age,gender,cast,guardian_name,
            nominee_name,relationwith,mobileno,whats_no,aadharnumber,mobile_relation_owner)

        sumitdata.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                progress.dismiss()

                Log.d("userresponse",response.toString())

                if (response.body() != null){

                    if (response.code() == 200){

                        val stringResponse = JSONObject(response.body()!!.string())
                        var message = stringResponse.getString("message")
                        var FarmerId = stringResponse.getString("FarmerId")
                        var FarmerUniqueID = stringResponse.getString("FarmerUniqueID")

                        Toast.makeText(this@FarmerOnBoardingUpdate, message,Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@FarmerOnBoardingUpdate, LocationInfoUpdate::class.java).apply {
                            putExtra("farmeruniquid", FarmerUniqueID)
                            putExtra("FarmerId", FarmerId)
                            putExtra("StartTime", StartTime)
                        }
                        startActivity(intent)


                    }else if(response.code() == 422){

                        val stringResponse = JSONObject(response.body()!!.string())
                        var error = stringResponse.getString("error")
                        var message = stringResponse.getString("message")


                        val WarningDialog =
                            SweetAlertDialog(this@FarmerOnBoardingUpdate, SweetAlertDialog.WARNING_TYPE)

                        WarningDialog.titleText = resources.getString(R.string.warning)
                        WarningDialog.contentText = message
                        WarningDialog.confirmText = resources.getString(R.string.ok)
                        WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

                    }else{
                        Log.d("userdata",response.code().toString())
                    }
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("NEW_TEST", it) }
                progress.dismiss()
            }

        })

    }

}