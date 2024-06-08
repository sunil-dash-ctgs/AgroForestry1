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
import co.kcagroforestry.app.databinding.ActivityCultivationInfoBinding
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

class CultivationInfo : AppCompatActivity() {

    lateinit var farmer_back : Button
    lateinit var binding: ActivityCultivationInfoBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_cultivation_info)

        binding = ActivityCultivationInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        farmer_back = findViewById(R.id.location_back)

        progress = SweetAlertDialog(this@CultivationInfo, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        farmer_back.setOnClickListener {
            super.onBackPressed()
        }

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("farmeruniquid")!!
            StartTime1 = bundle.getInt("StartTime")
        }

        timerData = TimerData(this@CultivationInfo, binding.textTimer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

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

        binding.cultivationNext.setOnClickListener {

            val WarningDialog = SweetAlertDialog(this@CultivationInfo, SweetAlertDialog.WARNING_TYPE)

            if (binding.SoilType.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Soil Type"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.remark.text.toString().equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your remark"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else{

                submitcultivationinfo()
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
                val nameImage = " Farmer Document 1 Image "
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

        val dialog = Dialog(this@CultivationInfo)
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

    fun submitcultivationinfo(){

        Log.e("NEW_TEST", "Entered sendData")
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_send)
        progress.setCancelable(false)
        progress.show()

        val intSelectButton1: Int = binding.plowwingRadioGroup.checkedRadioButtonId
        var radioButton1 : RadioButton = findViewById(intSelectButton1)

        val intSelectButton2: Int = binding.fertigationRadioGroup1.checkedRadioButtonId
        var radioButton2 : RadioButton = findViewById(intSelectButton2)

        val intSelectButton3: Int = binding.irrigationradioGroup3.checkedRadioButtonId
        var radioButton3 : RadioButton = findViewById(intSelectButton3)

        val intSelectButton4: Int = binding.managementRadioGroup4.checkedRadioButtonId
        var radioButton4 : RadioButton = findViewById(intSelectButton4)

        Log.d("radiodetail1",radioButton1.text.toString())
        Log.d("radiodetail2",radioButton2.text.toString())

        val file1 = File(image1)
        val file2 = File(image2)
        val file3 = File(image3)

        val uniqueid: RequestBody = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val farmername: RequestBody = binding.SoilType.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val plowwing_radio: RequestBody = radioButton1.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val fertigation_radio: RequestBody = radioButton2.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val irrigation_radio: RequestBody = radioButton3.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val management_radio: RequestBody = radioButton4.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val remerks: RequestBody = binding.remark.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFileImage1: RequestBody = file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage2: RequestBody = file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage3: RequestBody = file3.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val farmeruniquid: MultipartBody.Part = MultipartBody.Part.createFormData("farmeruniquid", null, uniqueid)
        val soil_types: MultipartBody.Part = MultipartBody.Part.createFormData("soil_types", null, farmername)
        val plowwing_of_land: MultipartBody.Part = MultipartBody.Part.createFormData("plowwing_of_land", null, plowwing_radio)
        val fertigation_management: MultipartBody.Part = MultipartBody.Part.createFormData("fertigation_management", null, fertigation_radio)
        val irrigation_type: MultipartBody.Part = MultipartBody.Part.createFormData("irrigation_type", null, irrigation_radio)
        val water_management: MultipartBody.Part = MultipartBody.Part.createFormData("water_management", null, management_radio)
        val did_you_notice: MultipartBody.Part = MultipartBody.Part.createFormData("did_you_notice", null, remerks)

        val ImageBody1 : MultipartBody.Part = MultipartBody.Part.createFormData("document1_photo", file1.name, requestFileImage1)
        val ImageBody2: MultipartBody.Part = MultipartBody.Part.createFormData("document2_photo", file2.name, requestFileImage2)
        val ImageBody3: MultipartBody.Part = MultipartBody.Part.createFormData("document3_photo", file3.name, requestFileImage3)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val submitculivation = apiInterface.submitCultivationinfo("Bearer $token",farmeruniquid,soil_types,plowwing_of_land,fertigation_management,
            irrigation_type,water_management,did_you_notice,ImageBody1,ImageBody2,ImageBody3)

        submitculivation.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progress.dismiss()

                if (response.body() != null){

                    if (response.code() == 200){

                        val stringResponse = JSONObject(response.body()!!.string())
                        var message = stringResponse.getString("message")
                        var FarmerId = stringResponse.getString("FarmerId")
                        var farmeruniquid = stringResponse.getString("farmeruniquid")

                        val intent = Intent(this@CultivationInfo, BankAccountDetails::class.java).apply {
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