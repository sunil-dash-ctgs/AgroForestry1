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
import co.kcagroforestry.app.cropintellix.DashBoardCrop
import co.kcagroforestry.app.databinding.ActivityDeclarationFormBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import co.kcagroforestry.app.utils.CommonData
import co.kcagroforestry.app.utils.SignatureActivity
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

class DeclarationForm : AppCompatActivity() {

    lateinit var farmer_back : Button
    lateinit var binding : ActivityDeclarationFormBinding
    var carbonCreditPath: String = ""
    var plotOwnerPath: String = ""
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
    private var checkboxdata: String = ""
    lateinit var uri: Uri
    lateinit var currentPhotoPath: String
    lateinit var photoPath: File
    var rotate = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_declaration_form)

        binding = ActivityDeclarationFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@DeclarationForm, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        farmer_back = findViewById(R.id.location_back)

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("farmeruniquid")!!
            StartTime1 = bundle.getInt("StartTime")
        }

        timerData = TimerData(this@DeclarationForm, binding.textTimer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

        farmer_back.setOnClickListener {
            super.onBackPressed()
        }
        binding.farmerSign.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SignatureActivity::class.java)
            startForResult1.launch(intent)
        })

        binding.enrollerSign.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, SignatureActivity::class.java)
            startForResult2.launch(intent)
        })

        binding.farmerCamera.setOnClickListener {

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

        binding.declarationCamera.setOnClickListener {

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

        binding.otherCamera.setOnClickListener {

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

        binding.farmerCameraCancel.setOnClickListener(View.OnClickListener {
            if (image1 != null) {
                binding.farmerCamera.setImageBitmap(null)
                image1 = ""
            }
        })

        binding.declarationCameraCancel.setOnClickListener(View.OnClickListener {
            if (image2 != null) {
                binding.declarationCamera.setImageBitmap(null)
                image2 = ""
            }
        })

        binding.otherCameraCancel.setOnClickListener(View.OnClickListener {
            if (image3 != null) {
                binding.otherCamera.setImageBitmap(null)
                image3 = ""
            }
        })

        binding.checkRelative.setOnClickListener {

            if (binding.creditCheckBox.isChecked){
                binding.creditCheckBox.isChecked = false
                checkboxdata = "0"
            }else{
                binding.creditCheckBox.isChecked = true
                checkboxdata = "1"
            }

        }

        binding.declaraNext.setOnClickListener {

            val WarningDialog = SweetAlertDialog(this@DeclarationForm, SweetAlertDialog.WARNING_TYPE)

            if (checkboxdata.equals("0")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Terms & Conditions"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (carbonCreditPath.equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Add Farmer Signature"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (plotOwnerPath.equals("")){

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Add Enroller Signature"
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

                submitDeclaration()
            }
        }

    }

    val startForResult1 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        if (it.resultCode == Activity.RESULT_OK){

            if (it.data != null) {
                carbonCreditPath = it.data!!.getStringExtra("path").toString()
                val exif = ExifInterface(carbonCreditPath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    6 -> 90
                    8 -> -90
                    else -> 0
                }

                binding.farmerSign.setImageURI(Uri.parse(File(carbonCreditPath).toString()))
                binding.farmerSign.rotation = rotate.toFloat()
                Log.e("carbonCreditPath", carbonCreditPath)
            }

        }
    }
    val startForResult2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        if (it.resultCode == Activity.RESULT_OK){

            if(it.data !=null){
                plotOwnerPath = it.data!!.getStringExtra("path").toString()

                val exif = ExifInterface(plotOwnerPath)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    6 -> 90
                    8 -> -90
                    else -> 0
                }

                binding.enrollerSign.setImageURI(Uri.parse(File(plotOwnerPath).toString()))
                binding.enrollerSign.rotation = rotate.toFloat()
                Log.e("plotOwnerPath", plotOwnerPath)
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
                binding.farmerCamera.setImageBitmap(watermark1.drawTextToBitmap(application.applicationContext, image, water_mark))
                binding.farmerCamera.rotation = rotate.toFloat()

                try {
                    val draw = binding.farmerCamera.drawable
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
                binding.declarationCamera.setImageBitmap(watermark1.drawTextToBitmap(application.applicationContext, image, water_mark))
                binding.declarationCamera.rotation = rotate.toFloat()

                try {
                    val draw = binding.declarationCamera.drawable
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
                binding.otherCamera.setImageBitmap(watermark1.drawTextToBitmap(application.applicationContext, image, water_mark))
                binding.otherCamera.rotation = rotate.toFloat()

                try {
                    val draw = binding.otherCamera.drawable
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

        val dialog = Dialog(this@DeclarationForm)
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

    fun submitDeclaration(){

        Log.e("NEW_TEST", "Entered sendData")
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_send)
        progress.setCancelable(false)
        progress.show()


        val file1 = File(image1)
        val file2 = File(image2)
        val file3 = File(image3)
        val file4 = File(carbonCreditPath)
        val file5 = File(plotOwnerPath)

        val termcondcheckbox: RequestBody = checkboxdata.toRequestBody("text/plain".toMediaTypeOrNull())
        val uniqueid: RequestBody = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFileImage1: RequestBody = file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage2: RequestBody = file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage3: RequestBody = file3.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage4: RequestBody = file4.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage5: RequestBody = file5.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val farmeruniquid: MultipartBody.Part = MultipartBody.Part.createFormData("farmeruniquid", null, uniqueid)
        val teramcondition: MultipartBody.Part = MultipartBody.Part.createFormData("teramcondition", null, termcondcheckbox)

        val ImageBody1 : MultipartBody.Part = MultipartBody.Part.createFormData("document1_photo", file1.name, requestFileImage1)
        val ImageBody2: MultipartBody.Part = MultipartBody.Part.createFormData("document2_photo", file2.name, requestFileImage2)
        val ImageBody3: MultipartBody.Part = MultipartBody.Part.createFormData("document3_photo", file3.name, requestFileImage3)
        val ImageBody4: MultipartBody.Part = MultipartBody.Part.createFormData("farmer_signature", file3.name, requestFileImage4)
        val ImageBody5: MultipartBody.Part = MultipartBody.Part.createFormData("enroller", file3.name, requestFileImage5)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val submitDeclara = apiInterface.submitDeclaration("Bearer $token", farmeruniquid,teramcondition,ImageBody4,ImageBody5,
            ImageBody1,ImageBody2,ImageBody3)

        submitDeclara.enqueue(object : Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                progress.dismiss()

                if (response.body() != null){

                    if (response.code() == 200){

                        val stringResponse = JSONObject(response.body()!!.string())
                        var message = stringResponse.getString("message")
                        var FarmerId = stringResponse.getString("FarmerId")
                        var farmeruniquid = stringResponse.getString("farmeruniquid")

                        nextScreen()

                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("NEW_TEST", it) }
                progress.dismiss()
            }

        })

    }

    private fun nextScreen() {

        val SuccessDialog = SweetAlertDialog(this@DeclarationForm, SweetAlertDialog.SUCCESS_TYPE)

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
}