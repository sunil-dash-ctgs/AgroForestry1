package co.kcagroforestry.app.onboardingpage

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.BuildConfig
import co.kcagroforestry.app.R
import co.kcagroforestry.app.cropintellix.DashBoardCrop
import co.kcagroforestry.app.databinding.ActivityFarmerOnBoardingBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import co.kcagroforestry.app.utils.CommonData
import com.kosherclimate.userapp.TimerData
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

import android.Manifest
import android.content.pm.PackageManager
import android.widget.RadioButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class FarmerOnBoarding : AppCompatActivity() {

    lateinit var farmer_back : Button

    lateinit var binding : ActivityFarmerOnBoardingBinding
    val watermark1: CommonData = CommonData()

    private lateinit var progress: SweetAlertDialog
    lateinit var timerData: TimerData
    var StartTime = 0;
    lateinit var token: String
    var imageFileName: String = ""
    private var image1: String = ""
    private var image2: String = ""
    private var image3: String = ""
    lateinit var uri: Uri
    lateinit var currentPhotoPath: String
    lateinit var photoPath: File
    var rotate = 0
    var realtionshipIDList = java.util.ArrayList<Int>()
    var relationshipNameList = java.util.ArrayList<String>()
    var relationship: String = ""
    val access = arrayOf("--Select--", "Own Number", "Relatives Number")
    var mobile_access: String = ""


    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_farmer_on_boarding)

        binding = ActivityFarmerOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@FarmerOnBoarding, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        farmer_back = findViewById(R.id.assam_farmer_back)

        val versionCode: Int = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME
        val release = java.lang.Double.parseDouble(
            java.lang.String(Build.VERSION.RELEASE).replaceAll("(\\d+[.]\\d+)(.*)", "$1")
        )
        Log.e("version", versionName + versionCode + release.toString())

        getUniqueId(versionName)

        farmer_back.setOnClickListener {
            super.onBackPressed()
        }

        timerData = TimerData(this@FarmerOnBoarding, binding.textTimer)
        StartTime = timerData.startTime(0).toInt()

        binding.document1Camera.setOnClickListener {

            if (image1.isEmpty()){

                if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    0
                )
            } else {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    // Create the File where the photo should go
                    try {
                        photoPath = createImageFile()
                        // Continue only if the File was successfully created
                        if (photoPath != null) {
                            uri = FileProvider.getUriForFile(
                                this,
                               BuildConfig.APPLICATION_ID + ".provider",
                                photoPath
                            )
                            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                            resultLauncher1.launch(takePictureIntent)
                        }
                    } catch (ex: Exception) {
                        // Error occurred while creating the File
                        displayMessage(baseContext, ex.message.toString())
                    }

                } else {
                    displayMessage(baseContext, "Null")
                }
            }

            }else{

                imageAlertDialog(image1)
            }

        }

        binding.document2Camera.setOnClickListener {

            if (image2.isEmpty()){

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        0
                    )
                } else {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePictureIntent.resolveActivity(packageManager) != null) {
                        // Create the File where the photo should go
                        try {
                            photoPath = createImageFile()
                            // Continue only if the File was successfully created
                            if (photoPath != null) {
                                uri = FileProvider.getUriForFile(
                                    this,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    photoPath
                                )
                                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                                resultLauncher2.launch(takePictureIntent)
                            }
                        } catch (ex: Exception) {
                            // Error occurred while creating the File
                            displayMessage(baseContext, ex.message.toString())
                        }

                    } else {
                        displayMessage(baseContext, "Null")
                    }
                }

            }else{
                imageAlertDialog(image2)
            }

        }

        binding.document3Camera.setOnClickListener {

           if (image3.isEmpty()){

               if (ContextCompat.checkSelfPermission(
                       this,
                       Manifest.permission.CAMERA
                   ) != PackageManager.PERMISSION_GRANTED
               ) {
                   ActivityCompat.requestPermissions(
                       this,
                       arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                       0
                   )
               } else {
                   val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                   if (takePictureIntent.resolveActivity(packageManager) != null) {
                       // Create the File where the photo should go
                       try {
                           photoPath = createImageFile()
                           // Continue only if the File was successfully created
                           if (photoPath != null) {
                               uri = FileProvider.getUriForFile(
                                   this,
                                   BuildConfig.APPLICATION_ID + ".provider",
                                   photoPath
                               )
                               takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                               takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                               resultLauncher3.launch(takePictureIntent)
                           }
                       } catch (ex: Exception) {
                           // Error occurred while creating the File
                           displayMessage(baseContext, ex.message.toString())
                       }

                   } else {
                       displayMessage(baseContext, "Null")
                   }
               }

           }else{

               imageAlertDialog(image3)
           }

        }

        binding.assamFarmerNext.setOnClickListener {

            val WarningDialog = SweetAlertDialog(this@FarmerOnBoarding, SweetAlertDialog.WARNING_TYPE)

            if (binding.assaFarmerName.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = resources.getString(R.string.farmer_name_warning)
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.assamFarmerAge.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Age"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.assamGuardianName.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Fill Your Guardian Name"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.assamMobileAccess.text.toString().equals("--Select--")){

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

            }else if (binding.farmeruniqueId.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Fill the UniqueId"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (image1.equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Document One Image"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (image2.equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Document Two Image"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (image3.equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Document Three Image"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else{

                submitOnBording()
            }


        }

        if (binding.assamMobileAccess != null) {

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, access)
            binding.assamMobileAccess.setAdapter(adapter)

            binding.assamMobileAccess.onItemClickListener = object : AdapterView.OnItemClickListener {
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

        binding.document1CameraCancel.setOnClickListener(View.OnClickListener {
            if (image1 != null) {
                binding.document1Camera.setImageBitmap(null)
                image1 = ""
            }
        })

        binding.document2CameraCancel.setOnClickListener(View.OnClickListener {
            if (image2 != null) {
                binding.document2Camera.setImageBitmap(null)
                image2 = ""
            }
        })

        binding.document3CameraCancel.setOnClickListener(View.OnClickListener {
            if (image3 != null) {
                binding.document3Camera.setImageBitmap(null)
                image3 = ""
            }
        })

    }

    private var resultLauncher1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val timeStamp = SimpleDateFormat("dd/MM/yy HH:mm").format(Date())
                val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

                val exif = ExifInterface(photoPath.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    6 -> 90
                    8 -> -90
                    else -> 0
                }
                Log.e("rotate", rotate.toString())
                val year = "Year"
                val season = "Season"
                val nameImage = " Farmer Document 1 Image "
                var unique_id = binding.farmeruniqueId.text.toString()
                val water_mark = "#$unique_id - $timeStamp \n $nameImage"
                //val water_mark = "#$unique_id - P$plot_number - $timeStamp "
                // imgCamera3.setImageBitmap(watermark.addWatermark(application.applicationContext, image, water_mark))
                binding.document1Camera.setImageBitmap(watermark1.drawTextToBitmap(application.applicationContext, image, water_mark))
                binding.document1Camera.rotation = rotate.toFloat()

                try {
                    val draw = binding.document1Camera.drawable
                    val bitmap = draw.toBitmap()

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")
                    val outStream = FileOutputStream(outFile)
                    val resized = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true)
                    resized.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    image1 = outFile.absolutePath

                    val file = File(image1)
                    var length = file.length()
                    length = length / 1024
                    println("File Path : " + file.path + ", File size : " + length + " KB")
                    Log.d("FileImagePath","File Path3 : " + file.path + ", File size3 : " + length + " KB")

                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var resultLauncher2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val timeStamp = SimpleDateFormat("dd/MM/yy HH:mm").format(Date())
                val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

                val exif = ExifInterface(photoPath.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    6 -> 90
                    8 -> -90
                    else -> 0
                }
                Log.e("rotate", rotate.toString())
                val year = "Year"
                val season = "Season"
                val nameImage = " Farmer Document 2 Image "
                var unique_id = binding.farmeruniqueId.text.toString()
                val water_mark = "#$unique_id - $timeStamp \n $nameImage"
                //val water_mark = "#$unique_id - P$plot_number - $timeStamp "
                // imgCamera3.setImageBitmap(watermark.addWatermark(application.applicationContext, image, water_mark))
                binding.document2Camera.setImageBitmap(watermark1.drawTextToBitmap(application.applicationContext, image, water_mark))
                binding.document2Camera.rotation = rotate.toFloat()

                try {
                    val draw = binding.document2Camera.drawable
                    val bitmap = draw.toBitmap()

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")
                    val outStream = FileOutputStream(outFile)
                    val resized = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true)
                    resized.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    image2 = outFile.absolutePath

                    val file = File(image2)
                    var length = file.length()
                    length = length / 1024
                    println("File Path : " + file.path + ", File size : " + length + " KB")
                    Log.d("FileImagePath","File Path3 : " + file.path + ", File size3 : " + length + " KB")

                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var resultLauncher3 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val timeStamp = SimpleDateFormat("dd/MM/yy HH:mm").format(Date())
                val image = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

                val exif = ExifInterface(photoPath.absolutePath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    6 -> 90
                    8 -> -90
                    else -> 0
                }
                Log.e("rotate", rotate.toString())
                val year = "Year"
                val season = "Season"
                val nameImage = " Farmer Document 3 Image "
                var unique_id = binding.farmeruniqueId.text.toString()
                val water_mark = "#$unique_id - $timeStamp \n $nameImage"
                //val water_mark = "#$unique_id - P$plot_number - $timeStamp "
                // imgCamera3.setImageBitmap(watermark.addWatermark(application.applicationContext, image, water_mark))
                binding.document3Camera.setImageBitmap(watermark1.drawTextToBitmap(application.applicationContext, image, water_mark))
                binding.document3Camera.rotation = rotate.toFloat()

                try {
                    val draw = binding.document3Camera.drawable
                    val bitmap = draw.toBitmap()

                    val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    val outFile = File(storageDir, "$imageFileName.jpg")
                    val outStream = FileOutputStream(outFile)
                    val resized = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true)
                    resized.compress(Bitmap.CompressFormat.JPEG, 50, outStream)
                    outStream.flush()
                    outStream.close()

                    image3 = outFile.absolutePath

                    val file = File(image3)
                    var length = file.length()
                    length = length / 1024
                    println("File Path : " + file.path + ", File size : " + length + " KB")
                    Log.d("FileImagePath","File Path3 : " + file.path + ", File size3 : " + length + " KB")

                } catch (e: FileNotFoundException) {
                    Log.d("TAG", "Error Occurred" + e.message)
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

        val file1 = File(image1)
        val file2 = File(image2)
        val file3 = File(image3)

        val farmername: RequestBody = binding.assaFarmerName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val farmerage: RequestBody = binding.assamFarmerAge.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val radiogender: RequestBody = radioButton1.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val radiocast: RequestBody = radioButton2.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val guardianname: RequestBody = binding.assamGuardianName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val mobileaccess: RequestBody = binding.assamMobileAccess.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val mobilenumber: RequestBody = binding.assamMobile.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val whatesnumber: RequestBody = binding.assamWhatesappno.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val aadhar_number: RequestBody = binding.assamAadharno.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val farmeruniqueid: RequestBody = binding.farmeruniqueId.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val relationship: RequestBody = relationship.toRequestBody("text/plain".toMediaTypeOrNull())
        val nomineename: RequestBody = binding.nomineName.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())


        val requestFileImage1: RequestBody = file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage2: RequestBody = file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage3: RequestBody = file3.asRequestBody("multipart/form-data".toMediaTypeOrNull())

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

        val ImageBody1 : MultipartBody.Part = MultipartBody.Part.createFormData("document1_photo", file1.name, requestFileImage1)
        val ImageBody2: MultipartBody.Part = MultipartBody.Part.createFormData("document2_photo", file2.name, requestFileImage2)
        val ImageBody3: MultipartBody.Part = MultipartBody.Part.createFormData("document3_photo", file3.name, requestFileImage3)

        val submit_onboarding = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        var sumitdata = submit_onboarding.submitOnboarding("Bearer $token",farmer_name,farmeruniquid,farmer_age,gender,cast,guardian_name,
            nominee_name,relationwith,mobileno,whats_no,aadharnumber,ImageBody1,ImageBody2,ImageBody3)

        sumitdata.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                progress.dismiss()

                if (response.body() != null){

                    if (response.code() == 200){

                        val stringResponse = JSONObject(response.body()!!.string())
                        var message = stringResponse.getString("message")
                        var FarmerId = stringResponse.getString("FarmerId")
                        var farmeruniquid = stringResponse.getString("farmeruniquid")

                        val intent = Intent(this@FarmerOnBoarding,LocationInfo::class.java).apply {
                            putExtra("farmeruniquid", farmeruniquid)
                            putExtra("FarmerId", FarmerId)
                            putExtra("StartTime", StartTime)
                        }
                        startActivity(intent)


                    }else if(response.code() == 422){

                        val stringResponse = JSONObject(response.body()!!.string())
                        var error = stringResponse.getString("error")
                        var message = stringResponse.getString("message")


                        val WarningDialog =
                            SweetAlertDialog(this@FarmerOnBoarding, SweetAlertDialog.WARNING_TYPE)

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

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun imageAlertDialog(image: String) {

        val dialog = Dialog(this@FarmerOnBoarding)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.condition_logout)
        val btn_Yes = dialog.findViewById<Button>(R.id.yes)
        val showdatainimage = dialog.findViewById<ImageView>(R.id.showdatainimage)
        val imgBitmap = BitmapFactory.decodeFile(image)
        // on below line we are setting bitmap to our image view.
        showdatainimage.setImageBitmap(imgBitmap)

        btn_Yes.setOnClickListener {
            dialog.dismiss()
            //finish();
            //System.exit(1);
            // File file1 = takescreenShort();
            //screenShortLayout(file1);
        }
        dialog.show()
        val window = dialog.window
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        //window.setBackgroundDrawableResource(R.drawable.homecard_back1);
    }

    private fun getUniqueId(versionCode: String) {

        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_load)
        progress.setCancelable(false)
        progress.show()

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.uniqueID("Bearer $token", versionCode.toString()).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        val stringResponse = JSONObject(response.body()!!.string())
                        val id = stringResponse.getString("UniqueId")
                        Log.e("UniqueId", id)

                        binding.farmeruniqueId.text = id.toEditable()
                        relationshipAPI()
                    } else {
                        progress.dismiss()
                    }
                } else {
                    progress.dismiss()
                    relationshipAPI()

                    Log.d("responsecode",response.code().toString())

                    errorDialog()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    this@FarmerOnBoarding,
                    "Please Retry",
                    Toast.LENGTH_SHORT
                ).show()
                progress.dismiss()
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
                    this@FarmerOnBoarding,
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

    private fun errorDialog() {
        val WarningDialog =
            SweetAlertDialog(this@FarmerOnBoarding, SweetAlertDialog.WARNING_TYPE)

        WarningDialog.titleText = " Warning "
        WarningDialog.contentText = " Please download new app. "
        WarningDialog.confirmText = " Download Now "
        WarningDialog.showCancelButton(false)
        WarningDialog.setCancelable(false)
        WarningDialog.setConfirmClickListener {

            val intent = Intent(this, DashBoardCrop::class.java)
            startActivity(intent)
            finish()

        }.show()
    }

    private fun displayMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}