package co.kcagroforestry.app.onboardingpage

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.BuildConfig
import co.kcagroforestry.app.R
import co.kcagroforestry.app.databinding.ActivityLocationInfoBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import co.kcagroforestry.app.utils.CommonData
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
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

class LocationInfo : AppCompatActivity() {

    lateinit var farmer_back : Button

    lateinit var binding: ActivityLocationInfoBinding
    val watermark1: CommonData = CommonData()

    private lateinit var progress: SweetAlertDialog
    lateinit var timerData: TimerData
    var StartTime = 0;
    var StartTime1 = 0;
    lateinit var token: String
    var imageFileName: String = ""
    private var image1: String = ""
    private var image2: String = ""
    private var image3: String = ""
    private var unique_id: String = ""
    lateinit var uri: Uri
    lateinit var currentPhotoPath: String
    lateinit var photoPath: File
    var rotate = 0

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

    var statePosition  = 0
    lateinit var stateId : String

    var districtsPosition  = 0
    lateinit var districtsId : String

    var talukaPosition  = 0
    lateinit var talukaId : String

    var panchayatPosition  = 0
    lateinit var panchayatId : String

    var villagePosition  = 0
    lateinit var villageId : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // setContentView(R.layout.activity_location_info)

        binding = ActivityLocationInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@LocationInfo, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        farmer_back = findViewById(R.id.location_back)

        farmer_back.setOnClickListener {
            super.onBackPressed()
        }

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("farmeruniquid")!!
            StartTime1 = bundle.getInt("StartTime")
        }

        timerData = TimerData(this@LocationInfo, binding.textTimer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

        getState()

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

        binding.locationNext.setOnClickListener {

            val WarningDialog = SweetAlertDialog(this@LocationInfo, SweetAlertDialog.WARNING_TYPE)

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
            }else if (talukaNmaeList.isEmpty() || talukaPosition == 0) {
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
            }else if (binding.pincode.text.toString().equals("")){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Pincode"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }/*else if (binding.remark.text.toString().equals("")){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Villege Remarks"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }*/else if (binding.RegestrationNo.text.toString().equals("")){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Patta Number"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }else if (binding.SurveyNo.text.toString().equals("")){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your SurveyNo Number"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }else if (binding.TotalAcers.text.toString().equals("")){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Total Acers Number"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
            }else if (binding.PlantedAcers.text.toString().equals("")){
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Planted Acers Number"
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

                submitLocationInfo()
            }


        }
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
                val nameImage = " Location Document 1 Image "
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
                val nameImage = " Location Document 2 Image "
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
                val nameImage = " Location Document 3 Image "
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

        val dialog = Dialog(this@LocationInfo)
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

    private fun displayMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun getState(){

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.state().enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                stateNmaeList.clear()
                stateIdList.clear()

                if (response.body() != null){

                    if (response.code() == 200){

                        stateNmaeList.add("--Select--")
                        stateIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("state")

                        for (i in 0 until stateArray.length()){

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
                Toast.makeText(this@LocationInfo, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })

    }

    fun getDistricts(id : String){

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.districts(id).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                districtNmaeList.clear()
                districtIdList.clear()

                if (response.body() != null){

                    if (response.code() == 200){

                        districtNmaeList.add("--Select--")
                        districtIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("district")

                        for (i in 0 until stateArray.length()){

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
                Toast.makeText(this@LocationInfo, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })


    }

    fun getTaluka(id : String){

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.taluka(id).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                talukaNmaeList.clear()
                talukaIdList.clear()

                if (response.body() != null){

                    if (response.code() == 200){

                        talukaNmaeList.add("--Select--")
                        talukaIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("Taluka")

                        for (i in 0 until stateArray.length()){

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
                Toast.makeText(this@LocationInfo, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })


    }

    fun getvillagepanchayat(id : String){

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.villagepanchayat(id).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                panchayatNmaeList.clear()
                panchayatIdList.clear()

                if (response.body() != null){

                    if (response.code() == 200){

                        panchayatNmaeList.add("--Select--")
                        panchayatIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("panchayat")

                        for (i in 0 until stateArray.length()){

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
                Toast.makeText(this@LocationInfo, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })


    }

    fun getVillage(id : String){

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.Village(id).enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                villegeNmaeList.clear()
                villegeIdList.clear()

                if (response.body() != null){

                    if (response.code() == 200){

                        villegeNmaeList.add("--Select--")
                        villegeIdList.add(0)

                        val jsonResponse = JSONObject(response.body()!!.string())
                        val stateArray = jsonResponse.optJSONArray("Village")

                        for (i in 0 until stateArray.length()){

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
                Toast.makeText(this@LocationInfo, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })


    }

    private fun stateSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stateNmaeList)
        binding.state.setText(adapter.getItem(0));
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

    fun submitLocationInfo(){

        Log.e("NEW_TEST", "Entered sendData")
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_send)
        progress.setCancelable(false)
        progress.show()


        val file1 = File(image1)
        val file2 = File(image2)
        val file3 = File(image3)

        val stateid: RequestBody = stateId.toRequestBody("text/plain".toMediaTypeOrNull())
        val uniqueid: RequestBody = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val districtsid: RequestBody = districtsId.toRequestBody("text/plain".toMediaTypeOrNull())
        val talukaid: RequestBody = talukaId.toRequestBody("text/plain".toMediaTypeOrNull())
        val panchayatid: RequestBody = panchayatId.toRequestBody("text/plain".toMediaTypeOrNull())
        val villageid: RequestBody = villageId.toRequestBody("text/plain".toMediaTypeOrNull())
        val pincode_no: RequestBody = binding.pincode.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val remark_data: RequestBody = binding.remark.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val regestraction: RequestBody = binding.RegestrationNo.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val survery_no: RequestBody = binding.SurveyNo.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val totalacers: RequestBody = binding.TotalAcers.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val planted_acers: RequestBody = binding.PlantedAcers.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())


        val requestFileImage1: RequestBody = file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage2: RequestBody = file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage3: RequestBody = file3.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val farmeruniquid: MultipartBody.Part = MultipartBody.Part.createFormData("farmeruniquid", null, uniqueid)
        val state_id: MultipartBody.Part = MultipartBody.Part.createFormData("state_id", null, stateid)
        val district_id: MultipartBody.Part = MultipartBody.Part.createFormData("district_id", null, districtsid)
        val taluka_id: MultipartBody.Part = MultipartBody.Part.createFormData("taluka_id", null, talukaid)
        val village_id: MultipartBody.Part = MultipartBody.Part.createFormData("village_id", null, villageid)
        val pincode: MultipartBody.Part = MultipartBody.Part.createFormData("pincode", null, pincode_no)
        val remark: MultipartBody.Part = MultipartBody.Part.createFormData("remark", null, remark_data)
        val pattanumber: MultipartBody.Part = MultipartBody.Part.createFormData("pattanumber", null, regestraction)
        val survey_no: MultipartBody.Part = MultipartBody.Part.createFormData("survey_no", null, survery_no)
        val totalarea: MultipartBody.Part = MultipartBody.Part.createFormData("totalarea", null, totalacers)
        val planted_area: MultipartBody.Part = MultipartBody.Part.createFormData("planted_area", null, planted_acers)

        val ImageBody1 : MultipartBody.Part = MultipartBody.Part.createFormData("document1_photo", file1.name, requestFileImage1)
        val ImageBody2: MultipartBody.Part = MultipartBody.Part.createFormData("document2_photo", file2.name, requestFileImage2)
        val ImageBody3: MultipartBody.Part = MultipartBody.Part.createFormData("document3_photo", file3.name, requestFileImage3)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        val submitLocation = apiInterface.submitLocationInfo("Bearer $token",farmeruniquid,state_id,district_id,taluka_id,
            village_id,pincode,remark,pattanumber,survey_no,totalarea,planted_area,ImageBody1,ImageBody2,ImageBody3)

        submitLocation.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                progress.dismiss()

                if (response.body() != null) {

                    if (response.code() == 200) {

                        val stringResponse = JSONObject(response.body()!!.string())
                        var message = stringResponse.getString("message")
                        var FarmerId = stringResponse.getString("FarmerId")
                        var farmeruniquid = stringResponse.getString("farmeruniquid")

                        val intent = Intent(this@LocationInfo, PlantationInfo::class.java).apply {
                            putExtra("farmeruniquid", farmeruniquid)
                            putExtra("FarmerId", FarmerId)
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


}