package co.kcagroforestry.app.updateinfo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.R
import co.kcagroforestry.app.databinding.ActivityCultivationInfoUpdateBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import co.kcagroforestry.app.onboardingpage.BankAccountDetails
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

class CultivationInfoUpdate : AppCompatActivity() {

    lateinit var binding: ActivityCultivationInfoUpdateBinding

    private lateinit var progress: SweetAlertDialog
    lateinit var timerData: TimerData
    var StartTime = 0;
    var StartTime1 = 0;
    lateinit var token: String
    var imageFileName: String = ""
    private var unique_id: String = ""
    lateinit var strIrrigation: String

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_cultivation_info_update)

        binding = ActivityCultivationInfoUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@CultivationInfoUpdate, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("farmeruniquid")!!
            StartTime1 = bundle.getInt("StartTime")

            showOnBoardingData(unique_id, "4")
        }

        timerData = TimerData(this@CultivationInfoUpdate, binding.textTimer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

        binding.cultivationNext.setOnClickListener {

            val WarningDialog =
                SweetAlertDialog(this@CultivationInfoUpdate, SweetAlertDialog.WARNING_TYPE)

            if (binding.SoilType.text.toString().equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Soil Type"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else if (binding.remark.text.toString().equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your remark"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else {

                submitcultivationinfo()
            }
        }

    }

    fun submitcultivationinfo() {

        Log.e("NEW_TEST", "Entered sendData")
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_send)
        progress.setCancelable(false)
        progress.show()

        val intSelectButton1: Int = binding.plowwingRadioGroup.checkedRadioButtonId
        var radioButton1: RadioButton = findViewById(intSelectButton1)

        val intSelectButton2: Int = binding.fertigationRadioGroup1.checkedRadioButtonId
        var radioButton2: RadioButton = findViewById(intSelectButton2)

        val intSelectButton3: Int = binding.irrigationradioGroup3.checkedRadioButtonId
        var radioButton3: RadioButton = findViewById(intSelectButton3)
        strIrrigation = radioButton3.text.toString()

//        val intSelectButton: Int = binding.irrigationradioGroup.checkedRadioButtonId

//        if (intSelectButton3 != -1){
//            var radioButton3 : RadioButton = findViewById(intSelectButton3)
//            strIrrigation = radioButton3.text.toString()
//        }else{
//            var radioButton : RadioButton = findViewById(intSelectButton)
//            strIrrigation = radioButton.text.toString()
//        }


        val intSelectButton4: Int = binding.managementRadioGroup4.checkedRadioButtonId
        var radioButton4: RadioButton = findViewById(intSelectButton4)

        Log.d("radiodetail1", radioButton1.text.toString())
        Log.d("radiodetail2", radioButton2.text.toString())

//        val file1 = File(image1)
//        val file2 = File(image2)
//        val file3 = File(image3)

        val uniqueid: RequestBody = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val farmername: RequestBody =
            binding.SoilType.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val plowwing_radio: RequestBody =
            radioButton1.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val fertigation_radio: RequestBody =
            radioButton2.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val irrigation_radio: RequestBody =
            strIrrigation.toRequestBody("text/plain".toMediaTypeOrNull())
        val management_radio: RequestBody =
            radioButton4.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val remerks: RequestBody =
            binding.remark.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())

//        val requestFileImage1: RequestBody = file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//        val requestFileImage2: RequestBody = file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//        val requestFileImage3: RequestBody = file3.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val farmeruniquid: MultipartBody.Part =
            MultipartBody.Part.createFormData("farmeruniquid", null, uniqueid)
        val soil_types: MultipartBody.Part =
            MultipartBody.Part.createFormData("soil_types", null, farmername)
        val plowwing_of_land: MultipartBody.Part =
            MultipartBody.Part.createFormData("plowwing_of_land", null, plowwing_radio)
        val fertigation_management: MultipartBody.Part =
            MultipartBody.Part.createFormData("fertigation_management", null, fertigation_radio)
        val irrigation_type: MultipartBody.Part =
            MultipartBody.Part.createFormData("irrigation_type", null, irrigation_radio)
        val water_management: MultipartBody.Part =
            MultipartBody.Part.createFormData("water_management", null, management_radio)
        val did_you_notice: MultipartBody.Part =
            MultipartBody.Part.createFormData("did_you_notice", null, remerks)

//        val ImageBody1 : MultipartBody.Part = MultipartBody.Part.createFormData("document1_photo", file1.name, requestFileImage1)
//        val ImageBody2: MultipartBody.Part = MultipartBody.Part.createFormData("document2_photo", file2.name, requestFileImage2)
//        val ImageBody3: MultipartBody.Part = MultipartBody.Part.createFormData("document3_photo", file3.name, requestFileImage3)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val submitculivation = apiInterface.updatefarmerulativation(
            "Bearer $token", farmeruniquid, soil_types, plowwing_of_land, fertigation_management,
            irrigation_type, water_management, did_you_notice
        )

        submitculivation.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progress.dismiss()

                if (response.body() != null) {

                    if (response.code() == 200) {

                        val stringResponse = JSONObject(response.body()!!.string())
                        var farmerId = stringResponse.getString("farmerId")
                        var farmerUniqueId = stringResponse.getString("farmerUniqueId")

                        val intent = Intent(
                            this@CultivationInfoUpdate,
                            BankAccountUpdate::class.java
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

                    if (response.body() != null) {

                        progress.dismiss()

                        when (response.code()) {

                            200 -> {

                                val stringResponse = JSONObject(response.body()!!.string())
                                val framerDetails = stringResponse.optJSONObject("farmer")

                                val farmeruniquid = framerDetails.getString("farmeruniquid")
                                val soil_types = framerDetails.getString("soil_types")
                                val plowwing_of_land = framerDetails.getString("plowwing_of_land")
                                val fertigation_management = framerDetails.getString("fertigation_management")
                                val irrigation_type = framerDetails.getString("irrigation_type")
                                val water_management = framerDetails.getString("water_management")
                                val did_you_notice = framerDetails.getString("did_you_notice")
                                val document1_photo = framerDetails.getString("document1_photo")
                                val document2_photo = framerDetails.getString("document2_photo")
                                val document3_photo = framerDetails.getString("document3_photo")

                                Log.d("NEW_TEST", "Farmer Unique ID: $farmeruniquid")
                                Log.d("NEW_TEST", "soil_types: $soil_types")
                                Log.d("NEW_TEST", "plowwing_of_land: $plowwing_of_land")
                                Log.d("NEW_TEST", "fertigation_management: $fertigation_management")
                                Log.d("NEW_TEST", "irrigation_type: $irrigation_type")
                                Log.d("NEW_TEST", "water_management: $water_management")
                                Log.d("NEW_TEST", "did_you_notice: $did_you_notice")

                                binding.SoilType.text = soil_types.toEditable()
                                binding.remark.text = did_you_notice.toEditable()

                                Glide.with(this@CultivationInfoUpdate).load(document1_photo)
                                    .into(binding.document1Camera)
                                Glide.with(this@CultivationInfoUpdate).load(document2_photo)
                                    .into(binding.document2Camera)
                                Glide.with(this@CultivationInfoUpdate).load(document3_photo)
                                    .into(binding.document3Camera)

                                when (plowwing_of_land) {

                                    "Nagali" -> binding.plowwingRadioGroup.check(binding.assamRadioNagali.id)
                                    "Machinary" -> binding.plowwingRadioGroup.check(binding.assamRadioMachinary.id)
                                    "None" -> binding.plowwingRadioGroup.check(binding.assamRadioNone.id)
                                    else -> binding.plowwingRadioGroup.check(binding.assamRadioNagali.id)
                                }

                                when (fertigation_management) {

                                    "Organic" -> binding.fertigationRadioGroup1.check(binding.assamRadioOrganic.id)
                                    "Chemical" -> binding.fertigationRadioGroup1.check(binding.assamRadioChemical.id)
                                    "Both" -> binding.fertigationRadioGroup1.check(binding.assamRadioBoth.id)
                                    else -> binding.plowwingRadioGroup.check(binding.assamRadioOrganic.id)
                                }

                                when (irrigation_type) {

                                    "Drip" -> binding.irrigationradioGroup3.check(binding.assamRadioDrip.id)
                                    "Flood" -> binding.irrigationradioGroup3.check(binding.assamRadioFlood.id)
                                    "Rainy" -> binding.irrigationradioGroup3.check(binding.assamRadioRainy.id)
                                    "Well" -> binding.irrigationradioGroup3.check(binding.radioWell.id)
                                    "Canal" -> binding.irrigationradioGroup3.check(binding.radioCanal.id)
                                    "Tube well" -> binding.irrigationradioGroup3.check(binding.radioTubewell.id)
                                    else -> binding.irrigationradioGroup3.check(binding.assamRadioDrip.id)
                                }

                                when (water_management) {

                                    "Electricity" -> binding.managementRadioGroup4.check(binding.assamRadioElectricity.id)
                                    "Diesel" -> binding.managementRadioGroup4.check(binding.assamRadioDiesel.id)
                                    "Solar" -> binding.managementRadioGroup4.check(binding.assamRadioSolar.id)
                                    else -> binding.irrigationradioGroup3.check(binding.assamRadioDrip.id)
                                }

                            }

                            423 -> {

                                progress.dismiss()

                                val WarningDialog = SweetAlertDialog(
                                    this@CultivationInfoUpdate, SweetAlertDialog.WARNING_TYPE
                                )

                                WarningDialog.titleText = resources.getString(R.string.warning)
                                WarningDialog.contentText = "Farmer Onboarding not \n Completed"
                                WarningDialog.confirmText = " OK "
                                WarningDialog.showCancelButton(false)
                                WarningDialog.setCancelable(false)
                                WarningDialog.setConfirmClickListener {
                                    WarningDialog.cancel()

                                    backScreen()

                                }.show()
                            }

                            else -> {
                                Toast.makeText(
                                    this@CultivationInfoUpdate,
                                    "Failed to get Search Types. Please try again later.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@CultivationInfoUpdate, "" + t, Toast.LENGTH_SHORT)
                        .show()
                }

            })


    }

    private fun backScreen() {
        super.onBackPressed()
        finish()
    }
}