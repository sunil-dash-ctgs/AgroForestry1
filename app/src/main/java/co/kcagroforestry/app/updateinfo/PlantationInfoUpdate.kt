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
import co.kcagroforestry.app.databinding.ActivityPlantationInfoUpdateBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import co.kcagroforestry.app.onboardingpage.CultivationInfo
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

class PlantationInfoUpdate : AppCompatActivity() {

    lateinit var binding: ActivityPlantationInfoUpdateBinding

    private lateinit var progress: SweetAlertDialog
    lateinit var timerData: TimerData
    var StartTime = 0;
    var StartTime1 = 0;
    lateinit var token: String
    var imageFileName: String = ""
    private var unique_id: String = ""
    lateinit var uri: Uri
    lateinit var currentPhotoPath: String


    var planttypeNmaeList = ArrayList<String>()
    var planttypeIdList = ArrayList<Int>()

    var mixplanttypeNmaeList = ArrayList<String>()
    var mixplanttypIdList = ArrayList<Int>()

    var plantationNmaeList = ArrayList<String>()
    var plantationIdList = ArrayList<Int>()

    var planttypePosition = 0
    lateinit var planttypeId: String

    var mixplanttypePosition = 0
    lateinit var mixplanttypeId: String

    var plantationPosition = 0
    lateinit var plantationId: String

    lateinit var planttypeid: String
    lateinit var mixplanttypeid: String
    lateinit var plantationnameid: String

    var planttypename = ""
    var mixplanttypename = ""
    var plantationname = ""

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_plantation_info_update)

        binding = ActivityPlantationInfoUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@PlantationInfoUpdate, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("farmeruniquid")!!
            StartTime1 = bundle.getInt("StartTime")

            showOnBoardingData(unique_id,"3")
        }

        timerData = TimerData(this@PlantationInfoUpdate, binding.textTimer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

        getplanttype()

        getmixplanttype()

        plantationName()

        binding.plantNext.setOnClickListener {


            val WarningDialog = SweetAlertDialog(this@PlantationInfoUpdate, SweetAlertDialog.WARNING_TYPE)

            if (binding.plantationName.text.toString().equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Plantation Name"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }

//            else if (planttypePosition == 0) {
//
//                WarningDialog.titleText = resources.getString(R.string.warning)
//                WarningDialog.contentText = "Select Your Type of Plantation"
//                WarningDialog.confirmText = resources.getString(R.string.ok)
//                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
//
//            }
//
//            else if (mixplanttypePosition == 0) {
//
//                WarningDialog.titleText = resources.getString(R.string.warning)
//                WarningDialog.contentText = "Select Your Mixed Plantation"
//                WarningDialog.confirmText = resources.getString(R.string.ok)
//                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
//
//            }
//            else if (binding.NoOfPlants.text.toString().equals("")) {
//
//                WarningDialog.titleText = resources.getString(R.string.warning)
//                WarningDialog.contentText = "Enter Your No Of Plants"
//                WarningDialog.confirmText = resources.getString(R.string.ok)
//                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
//
//            }

            else if (binding.girthofPlant.text.toString().equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Girth of Plant"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else if (binding.plantspacing.text.toString().equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Plant to Plant Spacing"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else if (binding.rowspacing.text.toString().equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Row to Row Spacing"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else if (binding.dateofPlantation.text.toString().equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Date of Plantation"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else if (binding.standingtrees.text.toString().equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Current Standing Trees"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else{

                submitPlantInfo()
            }

        }

    }

    fun getplanttype() {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.plantationtype("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                planttypeNmaeList.clear()
                planttypeIdList.clear()

                if (response.body() != null) {

                    if (response.code() == 200) {

                        planttypeNmaeList.add("--Select--")
                        planttypeIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("data")

                        for (i in 0 until stateArray.length()) {

                            val jsonState = stateArray.getJSONObject(i)
                            val id = jsonState.optInt("id")
                            val type = jsonState.optString("type")

                            planttypeNmaeList.add(type)
                            planttypeIdList.add(id)
                        }

                        plantTypeSpinner()

                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@PlantationInfoUpdate, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })

    }

    fun getmixplanttype() {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.mixplantationtype("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                mixplanttypeNmaeList.clear()
                mixplanttypIdList.clear()

                if (response.body() != null) {

                    if (response.code() == 200) {

                        mixplanttypeNmaeList.add("--Select--")
                        mixplanttypIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("data")

                        for (i in 0 until stateArray.length()) {

                            val jsonState = stateArray.getJSONObject(i)
                            val id = jsonState.optInt("id")
                            val type = jsonState.optString("type")

                            mixplanttypeNmaeList.add(type)
                            mixplanttypIdList.add(id)
                        }

                        mixPlantTypeSpinner()

                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@PlantationInfoUpdate, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })

    }

    private fun plantTypeSpinner() {

        binding.typeplant.text = planttypename.toEditable()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, planttypeNmaeList)
       // binding.typeplant.setText(adapter.getItem(0));
        binding.typeplant.setAdapter(adapter)
        binding.typeplant.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, position: Int,
                id: Long
            ) {
                planttypePosition = position
                Log.e("stateposition", planttypePosition.toString())
                if (position != 0) {
                    planttypeId = planttypeIdList[planttypePosition].toString()
                }
            }
        }
    }

    private fun mixPlantTypeSpinner() {

        binding.mixtypeplant.text = mixplanttypename.toEditable()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mixplanttypeNmaeList)
        //binding.mixtypeplant.setText(adapter.getItem(0));
        binding.mixtypeplant.setAdapter(adapter)
        binding.mixtypeplant.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, position: Int,
                id: Long
            ) {
                mixplanttypePosition = position
                Log.e("stateposition", mixplanttypePosition.toString())
                if (position != 0) {
                    mixplanttypeId = mixplanttypIdList[mixplanttypePosition].toString()
                }
            }
        }
    }

    fun submitPlantInfo() {

        Log.e("NEW_TEST", "Entered sendData")
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_send)
        progress.setCancelable(false)
        progress.show()


//        val file1 = File(image1)
//        val file2 = File(image2)
//        val file3 = File(image3)

        if (planttypePosition == 0){
            planttypeId = planttypeid
        }

        if (mixplanttypePosition == 0){
            mixplanttypeId = mixplanttypeid
        }

        if (plantationPosition == 0){
            plantationId = plantationnameid
        }

        val PlantationName: RequestBody =
            plantationId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val planttype_Id: RequestBody = planttypeId.toRequestBody("text/plain".toMediaTypeOrNull())
        val mixplanttype_Id: RequestBody =
            mixplanttypeId.toRequestBody("text/plain".toMediaTypeOrNull())

        val standingtrees: RequestBody =
            binding.standingtrees.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val plantspacing: RequestBody =
            binding.plantspacing.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val rowspacing: RequestBody =
            binding.rowspacing.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val hightofPlant: RequestBody =
            binding.hightofPlant.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val girthofPlant: RequestBody =
            binding.girthofPlant.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val dateofPlantation: RequestBody =
            binding.dateofPlantation.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val uniqueid: RequestBody = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())

//        val requestFileImage1: RequestBody = file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//        val requestFileImage2: RequestBody = file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//        val requestFileImage3: RequestBody = file3.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val farmeruniquid: MultipartBody.Part =
            MultipartBody.Part.createFormData("farmeruniquid", null, uniqueid)
        val plantation_name: MultipartBody.Part =
            MultipartBody.Part.createFormData("plantation_id", null, PlantationName)
        val type_of_plantation: MultipartBody.Part =
            MultipartBody.Part.createFormData("type_of_plantation", null, planttype_Id)
        val mixed_plantation: MultipartBody.Part =
            MultipartBody.Part.createFormData("mixed_plantation", null, mixplanttype_Id)

        val standing_trees: MultipartBody.Part =
            MultipartBody.Part.createFormData("Current_Standing_Trees", null, standingtrees)
        val plant_spacing: MultipartBody.Part =
            MultipartBody.Part.createFormData("planttoplantspacing", null, plantspacing)
        val row_spacing: MultipartBody.Part =
            MultipartBody.Part.createFormData("rowtorowspacing", null, rowspacing)
        val height_Plant: MultipartBody.Part =
            MultipartBody.Part.createFormData("hightofPlant", null, hightofPlant)
        val girth_Plant: MultipartBody.Part =
            MultipartBody.Part.createFormData("girthofPlant", null, girthofPlant)
        val date_Plantation: MultipartBody.Part =
            MultipartBody.Part.createFormData("dateyearofPlantation", null, dateofPlantation)

//        val ImageBody1: MultipartBody.Part = MultipartBody.Part.createFormData("document1_photo", file1.name, requestFileImage1)
//        val ImageBody2: MultipartBody.Part = MultipartBody.Part.createFormData("document2_photo", file2.name, requestFileImage2)
//        val ImageBody3: MultipartBody.Part = MultipartBody.Part.createFormData("document3_photo", file3.name, requestFileImage3)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val submitplantinfo = apiInterface.updatefarmerplantation(
            "Bearer $token",
            farmeruniquid,
            plantation_name,
            type_of_plantation,
            mixed_plantation,
            standing_trees,
            plant_spacing,
            row_spacing,
            height_Plant,
            girth_Plant,
            date_Plantation
        )

        submitplantinfo.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                progress.dismiss()
                if (response.body() != null) {

                    if (response.code() == 200) {

                        val stringResponse = JSONObject(response.body()!!.string())
                        var farmerId = stringResponse.getString("farmerId")
                        var farmerUniqueId = stringResponse.getString("farmerUniqueId")

                        val intent =
                            Intent(this@PlantationInfoUpdate, CultivationInfoUpdate::class.java).apply {
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

    fun plantationName() {

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.plantationname("Bearer $token").enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                plantationNmaeList.clear()
                plantationIdList.clear()

                if (response.body() != null) {

                    if (response.code() == 200) {

                        plantationNmaeList.add("--Select--")
                        plantationIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("list")

                        for (i in 0 until stateArray.length()) {

                            val jsonState = stateArray.getJSONObject(i)
                            val id = jsonState.optInt("id")
                            val plantation_name = jsonState.optString("plantation_name")

                            plantationNmaeList.add(plantation_name)
                            plantationIdList.add(id)
                        }

                        plantationSpinner()

                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@PlantationInfoUpdate, "Please Retry", Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun plantationSpinner() {

        binding.plantationName.text = plantationname.toEditable()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, plantationNmaeList)
       // binding.plantationName.setText(adapter.getItem(0));
        binding.plantationName.setAdapter(adapter)
        binding.plantationName.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, position: Int,
                id: Long
            ) {
                plantationPosition = position
                Log.e("stateposition", plantationPosition.toString())
                if (position != 0) {
                    plantationId = plantationIdList[plantationPosition].toString()
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
                                val girth_of_plant = framerDetails.getString("girth_of_plant")
                                val current_standing = framerDetails.getString("current_standing")
                                val plant_to_plant_spacing = framerDetails.getString("plant_to_plant_spacing")
                                val row_to_row_spacing = framerDetails.getString("row_to_row_spacing")
                                val height_of_plant = framerDetails.getString("height_of_plant")
                                val plantation_date_time = framerDetails.getString("plantation_date_time")
                                val document1_photo = framerDetails.getString("document1_photo")
                                val document2_photo = framerDetails.getString("document2_photo")
                                val document3_photo = framerDetails.getString("document3_photo")


                                Log.d("NEW_TEST", "Farmer Unique ID: $farmeruniquid")
                                Log.d("NEW_TEST", "girth_of_plant: $girth_of_plant")
                                Log.d("NEW_TEST", "current_standing: $current_standing")
                                Log.d("NEW_TEST", "plant_to_plant_spacing: $plant_to_plant_spacing")
                                Log.d("NEW_TEST", "row_to_row_spacing: $row_to_row_spacing")
                                Log.d("NEW_TEST", "height_of_plant: $height_of_plant")
                                Log.d("NEW_TEST", "plantation_date_time: $plantation_date_time")

                                Glide.with(this@PlantationInfoUpdate).load(document1_photo)
                                    .into(binding.document1Camera)
                                Glide.with(this@PlantationInfoUpdate).load(document2_photo)
                                    .into(binding.document2Camera)
                                Glide.with(this@PlantationInfoUpdate).load(document3_photo)
                                    .into(binding.document3Camera)

                                val plant_type = framerDetails.getJSONObject("plant_type")
                                planttypeid = plant_type.getString("id")
                                planttypename = plant_type.getString("type")

                                val mix_plant_type = framerDetails.getJSONObject("mix_plant_type")
                                mixplanttypeid = mix_plant_type.getString("id")
                                mixplanttypename = mix_plant_type.getString("type")

                                val plantationname1 = framerDetails.getString("plantation_name")

                                if (!plantationname1.equals("null")){
                                    val plantation_name = framerDetails.getJSONObject("plantation_name")
                                    plantationnameid = plantation_name.getString("id")
                                    plantationname = plantation_name.getString("plantation_name")
                                }

                                binding.plantationName.text = plantationname.toEditable()
                                binding.typeplant.text = planttypename.toEditable()
                                binding.mixtypeplant.text = mixplanttypename.toEditable()
                                binding.plantspacing.text = plant_to_plant_spacing.toEditable()
                                binding.rowspacing.text = row_to_row_spacing.toEditable()
                                binding.hightofPlant.text = height_of_plant.toEditable()

                                binding.standingtrees.text = current_standing.toEditable()
                                binding.dateofPlantation.text = plantation_date_time.toEditable()

                                if (girth_of_plant.equals("null")){
                                    binding.girthofPlant.text = "".toEditable()
                                }else{
                                    binding.girthofPlant.text = girth_of_plant.toEditable()
                                }

                            }

                            423 -> {

                                progress.dismiss()

                                val WarningDialog = SweetAlertDialog(
                                    this@PlantationInfoUpdate, SweetAlertDialog.WARNING_TYPE
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
                                    this@PlantationInfoUpdate,
                                    "Failed to get Search Types. Please try again later.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                //    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@PlantationInfoUpdate, "" + t, Toast.LENGTH_SHORT)
                        .show()
                }

            })


    }

    private fun backScreen() {
        super.onBackPressed()
        finish()
    }

}