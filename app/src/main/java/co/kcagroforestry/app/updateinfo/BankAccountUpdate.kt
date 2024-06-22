package co.kcagroforestry.app.updateinfo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.R
import co.kcagroforestry.app.databinding.ActivityBankAccountUpdateBinding
import co.kcagroforestry.app.databinding.ActivityPolygonAlreadySubmitedBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import co.kcagroforestry.app.onboardingpage.DeclarationForm
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

class BankAccountUpdate : AppCompatActivity() {

    lateinit var binding: ActivityBankAccountUpdateBinding
    private lateinit var progress: SweetAlertDialog
    lateinit var timerData: TimerData
    var StartTime = 0;
    var StartTime1 = 0;
    lateinit var token: String
    private var unique_id: String = ""

    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_bank_account_update)

        binding = ActivityBankAccountUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@BankAccountUpdate, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("farmeruniquid")!!
            StartTime1 = bundle.getInt("StartTime")

            showOnBoardingData(unique_id,"5")
        }

        timerData = TimerData(this@BankAccountUpdate, binding.textTimer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

        binding.bankNext.setOnClickListener {

            val WarningDialog = SweetAlertDialog(this@BankAccountUpdate, SweetAlertDialog.WARNING_TYPE)

            if (binding.AccountHolderName.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Account Holder Name"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.AccountNumber.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Account Number"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.NameOfTheBank.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Name of the Bank"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.IFSCCode.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your IFSC Code"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.Branch.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Branch Name"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else{

                submitBankDetails()
            }

        }

    }

    fun submitBankDetails(){

        Log.e("NEW_TEST", "Entered sendData")
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_send)
        progress.setCancelable(false)
        progress.show()


//        val file1 = File(image1)
//        val file2 = File(image2)
//        val file3 = File(image3)

        val accholdname: RequestBody = binding.AccountHolderName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val accnumber: RequestBody = binding.AccountNumber.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val namebank: RequestBody = binding.NameOfTheBank.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val ifccode: RequestBody = binding.IFSCCode.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val branch_name: RequestBody = binding.Branch.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val uniqueid: RequestBody = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())

//        val requestFileImage1: RequestBody = file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//        val requestFileImage2: RequestBody = file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
//        val requestFileImage3: RequestBody = file3.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val farmeruniquid: MultipartBody.Part = MultipartBody.Part.createFormData("farmeruniquid", null, uniqueid)
        val account_holder_name: MultipartBody.Part = MultipartBody.Part.createFormData("account_holder_name", null, accholdname)
        val account_number: MultipartBody.Part = MultipartBody.Part.createFormData("account_number", null, accnumber)
        val bank_name: MultipartBody.Part = MultipartBody.Part.createFormData("bank_name", null, namebank)
        val ifsc_code: MultipartBody.Part = MultipartBody.Part.createFormData("ifsc_code", null, ifccode)
        val branch: MultipartBody.Part = MultipartBody.Part.createFormData("branch", null, branch_name)

//        val ImageBody1 : MultipartBody.Part = MultipartBody.Part.createFormData("document_photo1", file1.name, requestFileImage1)
//        val ImageBody2: MultipartBody.Part = MultipartBody.Part.createFormData("document_photo2", file2.name, requestFileImage2)
//        val ImageBody3: MultipartBody.Part = MultipartBody.Part.createFormData("document_photo3", file3.name, requestFileImage3)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        val submitbank = apiInterface.updatefarmerbankdetails("Bearer $token",farmeruniquid,account_holder_name,account_number,bank_name,
            ifsc_code,branch)

        submitbank.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                progress.dismiss()

                if (response.body() != null){

                    if (response.code() == 200){

                        val stringResponse = JSONObject(response.body()!!.string())
                        var farmerId = stringResponse.getString("farmerId")
                        var farmerUniqueId = stringResponse.getString("farmerUniqueId")

                        val intent = Intent(this@BankAccountUpdate, DeclarationInfoUpdate::class.java).apply {
                             putExtra("farmerUniqueId", farmerUniqueId)
                             putExtra("FarmerId", farmerId)
                             putExtra("StartTime", StartTime)
                        }
                        startActivity(intent)
                    }
                }else{

                    Log.d("userresponsecode",response.code().toString())
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
                                val account_holder_name = framerDetails.getString("account_holder_name")
                                val account_number = framerDetails.getString("account_number")
                                val bank_name = framerDetails.getString("bank_name")
                                val ifsc_code = framerDetails.getString("ifsc_code")
                                val branch = framerDetails.getString("branch")
                                val document_photo1 = framerDetails.getString("document_photo1")
                                val document_photo2 = framerDetails.getString("document_photo2")
                                val document_photo3 = framerDetails.getString("document_photo3")

                                Log.d("NEW_TEST", "Farmer Unique ID: $farmeruniquid")
                                Log.d("NEW_TEST", "account_holder_name: $account_holder_name")
                                Log.d("NEW_TEST", "account_number: $account_number")
                                Log.d("NEW_TEST", "bank_name: $bank_name")
                                Log.d("NEW_TEST", "ifsc_code: $ifsc_code")
                                Log.d("NEW_TEST", "branch: $branch")

                                Glide.with(this@BankAccountUpdate).load(document_photo1)
                                    .into(binding.document1Camera)
                                Glide.with(this@BankAccountUpdate).load(document_photo2)
                                    .into(binding.document2Camera)
                                Glide.with(this@BankAccountUpdate).load(document_photo3)
                                    .into(binding.document3Camera)

                                binding.AccountHolderName.text = account_holder_name.toEditable()
                                binding.AccountNumber.text = account_number.toEditable()
                                binding.IFSCCode.text = ifsc_code.toEditable()
                                binding.Branch.text = branch.toEditable()
                                binding.NameOfTheBank.text = bank_name.toEditable()

                            }

                            423 -> {

                                progress.dismiss()

                                val WarningDialog = SweetAlertDialog(
                                    this@BankAccountUpdate, SweetAlertDialog.WARNING_TYPE
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
                                    this@BankAccountUpdate,
                                    "Failed to get Search Types. Please try again later.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@BankAccountUpdate, "" + t, Toast.LENGTH_SHORT)
                        .show()
                }

            })


    }

    private fun backScreen() {
        super.onBackPressed()
        finish()
    }
}