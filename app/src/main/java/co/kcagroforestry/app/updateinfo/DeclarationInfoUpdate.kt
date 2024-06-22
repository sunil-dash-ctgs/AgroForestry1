package co.kcagroforestry.app.updateinfo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.R
import co.kcagroforestry.app.cropintellix.DashBoardCrop
import co.kcagroforestry.app.databinding.ActivityDeclarationInfoUpdateBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import com.bumptech.glide.Glide
import com.kosherclimate.userapp.TimerData
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeclarationInfoUpdate : AppCompatActivity() {

    lateinit var binding: ActivityDeclarationInfoUpdateBinding

    private lateinit var progress: SweetAlertDialog
    lateinit var timerData: TimerData
    var StartTime = 0;
    var StartTime1 = 0;
    var unique_id = ""
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_declaration_info_update)

        binding = ActivityDeclarationInfoUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@DeclarationInfoUpdate, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("farmerUniqueId")!!
            StartTime1 = bundle.getInt("StartTime")

            showOnBoardingData(unique_id,"6")
        }

        timerData = TimerData(this@DeclarationInfoUpdate, binding.textTimer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

        binding.declaraNext.setOnClickListener {
            nextScreen()
        }
    }
    private fun nextScreen() {

        val SuccessDialog = SweetAlertDialog(this@DeclarationInfoUpdate, SweetAlertDialog.SUCCESS_TYPE)

        SuccessDialog.titleText = resources.getString(R.string.success)
        SuccessDialog.contentText = resources.getString(R.string.submitted_successfully)
        SuccessDialog.confirmText = resources.getString(R.string.ok)
        SuccessDialog.showCancelButton(false)
        SuccessDialog.setCancelable(false)
        SuccessDialog.setConfirmClickListener {

            SuccessDialog.cancel()
            val intent = Intent(this, DashBoardCrop::class.java)
            startActivity(intent)
            finish()

        }.show()
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
                                 val teramcondition = framerDetails.getString("teramcondition")
                                val farmer_signature = framerDetails.getString("farmer_signature")
                                val enroller = framerDetails.getString("enroller")
                                val document1_photo = framerDetails.getString("document1_photo")
                                val document2_photo = framerDetails.getString("document2_photo")
                                val document3_photo = framerDetails.getString("document3_photo")


                                Log.d("NEW_TEST", "Farmer Unique ID: $farmeruniquid")


                                Glide.with(this@DeclarationInfoUpdate).load(enroller)
                                    .into(binding.enrollerSign)
                                Glide.with(this@DeclarationInfoUpdate).load(farmer_signature)
                                    .into(binding.farmerSign)
                                Glide.with(this@DeclarationInfoUpdate).load(document1_photo)
                                    .into(binding.farmerCamera)
                                Glide.with(this@DeclarationInfoUpdate).load(document2_photo)
                                    .into(binding.declarationCamera)
                                Glide.with(this@DeclarationInfoUpdate).load(document3_photo)
                                    .into(binding.otherCamera)

                                if (teramcondition.equals("1")){
                                    binding.creditCheckBox.isChecked = true
                                }

                            }

                            423 -> {

                                progress.dismiss()

                                val WarningDialog = SweetAlertDialog(
                                    this@DeclarationInfoUpdate, SweetAlertDialog.WARNING_TYPE
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
                                    this@DeclarationInfoUpdate,
                                    "Failed to get Search Types. Please try again later.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@DeclarationInfoUpdate, "" + t, Toast.LENGTH_SHORT)
                        .show()
                }

            })


    }

    private fun backScreen() {
        super.onBackPressed()
        finish()
    }

}