package co.kcagroforestry.app.revisit

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.BuildConfig
import co.kcagroforestry.app.R
import co.kcagroforestry.app.cropintellix.DashBoardCrop
import co.kcagroforestry.app.databinding.ActivityRevisitePlotBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import co.kcagroforestry.app.utils.CommonData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
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
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

class RevisitePlotPhoto : AppCompatActivity() {

    lateinit var end_Submit: Button
    lateinit var binding: ActivityRevisitePlotBinding
    private var Polygon_lat_lng = ArrayList<String>()

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
    private var farmername: String = ""
    private var polygonid: String = ""
    private var revisitNO: String = ""
    private var latitude: String = ""
    private var longitude: String = ""
    lateinit var uri: Uri
    lateinit var currentPhotoPath: String
    lateinit var photoPath: File
    var rotate = 0
    var imageLat: String = ""
    var imageLng: String = ""
    var PERMISSION_ALL = 1
    private var Permissions: Array<String> = arrayOf<String>()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var poly_list_LATLNG = ArrayList<LatLng>()
    private var imageNumber = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_revisite_plot)

        binding = ActivityRevisitePlotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@RevisitePlotPhoto, SweetAlertDialog.PROGRESS_TYPE)
        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!

        val bundle = intent.extras
        if (bundle != null) {
            unique_id = bundle.getString("farmer_id")!!
            Polygon_lat_lng = bundle.getStringArrayList("polygon_lat_lng")!!
            farmername = bundle.getString("farmer_name")!!
            polygonid = bundle.getString("polygonid")!!
            revisitNO = bundle.getString("revisitNO")!!
            latitude = bundle.getString("latitude")!!
            longitude = bundle.getString("longitude")!!
            StartTime1 = bundle.getInt("StartTime")

        } else {
            Log.e("area", "Nope")
        }

        timerData = TimerData(this@RevisitePlotPhoto, binding.textTimer)
        StartTime = timerData.startTime(StartTime1.toLong()).toInt()

        for(i in 0 until Polygon_lat_lng.size){
            val dfgdg: String =  Polygon_lat_lng[i].replace("[^0-9,.]".toRegex(), "")
            val lat: Double = dfgdg.split(",").first().toDouble()
            val lng: Double = dfgdg.split(",").last().toDouble()

            poly_list_LATLNG.add(LatLng(lat, lng))
        }

        binding.document1Camera.setOnClickListener {

            if (image1.isEmpty()) {
                imageNumber = 1
                checkData()
            }
            else {

                imageAlertDialog(image1)
            }

        }

        binding.document2Camera.setOnClickListener {

            if (image2.isEmpty()) {

//                if (ContextCompat.checkSelfPermission(
//                        this,
//                        Manifest.permission.CAMERA
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    ActivityCompat.requestPermissions(
//                        this,
//                        arrayOf(
//                            Manifest.permission.CAMERA,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                        ),
//                        0
//                    )
//                } else {
//                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                    if (takePictureIntent.resolveActivity(packageManager) != null) {
//                        // Create the File where the photo should go
//                        try {
//                            photoPath = createImageFile()
//                            // Continue only if the File was successfully created
//                            if (photoPath != null) {
//                                uri = FileProvider.getUriForFile(
//                                    this,
//                                    BuildConfig.APPLICATION_ID + ".provider",
//                                    photoPath
//                                )
//                                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//                                resultLauncher2.launch(takePictureIntent)
//                            }
//                        } catch (ex: Exception) {
//                            // Error occurred while creating the File
//                            displayMessage(baseContext, ex.message.toString())
//                        }
//
//                    } else {
//                        displayMessage(baseContext, "Null")
//                    }
//                }

                imageNumber = 2
                checkData()

            } else {
                imageAlertDialog(image2)
            }

        }

        binding.document3Camera.setOnClickListener {

            if (image3.isEmpty()) {

//                if (ContextCompat.checkSelfPermission(
//                        this,
//                        Manifest.permission.CAMERA
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    ActivityCompat.requestPermissions(
//                        this,
//                        arrayOf(
//                            Manifest.permission.CAMERA,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                        ),
//                        0
//                    )
//                } else {
//                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                    if (takePictureIntent.resolveActivity(packageManager) != null) {
//                        // Create the File where the photo should go
//                        try {
//                            photoPath = createImageFile()
//                            // Continue only if the File was successfully created
//                            if (photoPath != null) {
//                                uri = FileProvider.getUriForFile(
//                                    this,
//                                    BuildConfig.APPLICATION_ID + ".provider",
//                                    photoPath
//                                )
//                                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//                                resultLauncher3.launch(takePictureIntent)
//                            }
//                        } catch (ex: Exception) {
//                            // Error occurred while creating the File
//                            displayMessage(baseContext, ex.message.toString())
//                        }
//
//                    } else {
//                        displayMessage(baseContext, "Null")
//                    }
//                }

                imageNumber = 3
                checkData()

            } else {

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

        binding.endSubmit.setOnClickListener {

            val WarningDialog =
                SweetAlertDialog(this@RevisitePlotPhoto, SweetAlertDialog.WARNING_TYPE)

            if (image1.equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Document One Image"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else if (image2.equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Document Two Image"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else if (image3.equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Document Three Image"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.hightofPlant.text.toString().equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Height of Plant"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else if (binding.girthofPlant.text.toString().equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Girth of Plant"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else if (binding.standingtrees.text.toString().equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Current Standing Trees"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            }else {

                submitPolygonDetails()
            }

        }

        Permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (!hasPermissions(this, *Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, PERMISSION_ALL)
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private var resultLauncher1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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
                    binding.document1Camera.setImageBitmap(
                        watermark1.drawTextToBitmap(
                            application.applicationContext,
                            image,
                            water_mark
                        )
                    )
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
                        Log.d(
                            "FileImagePath",
                            "File Path3 : " + file.path + ", File size3 : " + length + " KB"
                        )

                    } catch (e: FileNotFoundException) {
                        Log.d("TAG", "Error Occurred" + e.message)
                        e.printStackTrace()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    private var resultLauncher2 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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
                    binding.document2Camera.setImageBitmap(
                        watermark1.drawTextToBitmap(
                            application.applicationContext,
                            image,
                            water_mark
                        )
                    )
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
                        Log.d(
                            "FileImagePath",
                            "File Path3 : " + file.path + ", File size3 : " + length + " KB"
                        )

                    } catch (e: FileNotFoundException) {
                        Log.d("TAG", "Error Occurred" + e.message)
                        e.printStackTrace()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    private var resultLauncher3 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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
                    binding.document3Camera.setImageBitmap(
                        watermark1.drawTextToBitmap(
                            application.applicationContext,
                            image,
                            water_mark
                        )
                    )
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
                        Log.d(
                            "FileImagePath",
                            "File Path3 : " + file.path + ", File size3 : " + length + " KB"
                        )

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

        val dialog = Dialog(this@RevisitePlotPhoto)
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

    fun submitPolygonDetails() {

        Log.e("NEW_TEST", "Entered sendData")
        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.data_send)
        progress.setCancelable(false)
        progress.show()


        val file1 = File(image1)
        val file2 = File(image2)
        val file3 = File(image3)

        val unique_id: RequestBody = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())
        val plotnumber: RequestBody = polygonid.toRequestBody("text/plain".toMediaTypeOrNull())
        val status_no: RequestBody = "2".toRequestBody("text/plain".toMediaTypeOrNull())
        val visit_no: RequestBody = revisitNO.toRequestBody("text/plain".toMediaTypeOrNull())
        val latitude: RequestBody = latitude.toRequestBody("text/plain".toMediaTypeOrNull())
        val longitude: RequestBody = longitude.toRequestBody("text/plain".toMediaTypeOrNull())
        val plant_height: RequestBody = binding.hightofPlant.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val plant_girth: RequestBody = binding.girthofPlant.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val current_standing: RequestBody = binding.standingtrees.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFileImage1: RequestBody =
            file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage2: RequestBody =
            file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage3: RequestBody =
            file3.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val farmeruniquid: MultipartBody.Part =
            MultipartBody.Part.createFormData("farmeruniquid", null, unique_id)
        val holder_name: MultipartBody.Part =
            MultipartBody.Part.createFormData("polygon_id", null, plotnumber)
        val status: MultipartBody.Part =
            MultipartBody.Part.createFormData("status", null, status_no)
        val visitno: MultipartBody.Part =
            MultipartBody.Part.createFormData("visit_no", null, visit_no)
        val latitude1: MultipartBody.Part =
            MultipartBody.Part.createFormData("latitude", null, latitude)
        val longitude1: MultipartBody.Part =
            MultipartBody.Part.createFormData("longitude", null, longitude)

        val plantheight: MultipartBody.Part =
            MultipartBody.Part.createFormData("plant_height", null, plant_height)
        val plantgirth: MultipartBody.Part =
            MultipartBody.Part.createFormData("plant_girth", null, plant_girth)
        val currentstanding: MultipartBody.Part =
            MultipartBody.Part.createFormData("current_standing", null, current_standing)

        val ImageBody1: MultipartBody.Part =
            MultipartBody.Part.createFormData("document_1", file1.name, requestFileImage1)
        val ImageBody2: MultipartBody.Part =
            MultipartBody.Part.createFormData("document_2", file2.name, requestFileImage2)
        val ImageBody3: MultipartBody.Part =
            MultipartBody.Part.createFormData("document_3", file3.name, requestFileImage3)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)

        val submitbank = apiInterface.polygonImageSubmit(
            "Bearer $token", farmeruniquid, holder_name, visitno, latitude1, longitude1,
            ImageBody1, ImageBody2, ImageBody3, status, plantheight, plantgirth, currentstanding
        )

        submitbank.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                progress.dismiss()

                if (response.body() != null) {

                    if (response.code() == 200) {

                        val stringResponse = JSONObject(response.body()!!.string())

                        Log.d("userdataresponse", response.body()!!.string())

                        nextScreen()

                    }
                } else {

                    Log.d("userresponsecode", response.code().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.message?.let { Log.e("NEW_TEST", it) }
                progress.dismiss()
            }

        })
    }

    private fun nextScreen() {

        val SuccessDialog = SweetAlertDialog(this@RevisitePlotPhoto, SweetAlertDialog.SUCCESS_TYPE)

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

    private fun checkData() {


        progress.progressHelper.barColor = Color.parseColor("#06c238")
        progress.titleText = resources.getString(R.string.loading)
        progress.contentText = resources.getString(R.string.location)
        progress.setCancelable(false)
        progress.show()

        getActualLocation()
    }

    private fun getActualLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),  101)
            return
        }

        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            val location = task.result
            requestNewLocationData()
        }
    }

    private fun requestNewLocationData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        var mlocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(10)
            .setMaxUpdateDelayMillis(5000)
            .build()


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(mlocationRequest, mLocationCallback, Looper.myLooper())
    }


    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            Log.e("mLastLocation.latitude", mLastLocation?.latitude.toString())
            Log.e("mLastLocation.longitude", mLastLocation?.longitude.toString())
            Log.e("NEW_TEST",  " >> Location new"+ mLastLocation?.longitude.toString())

            imageLat = mLastLocation?.latitude.toString()
            imageLng = mLastLocation?.longitude.toString()

            stopAgain(mLastLocation)
        }
    }

    private fun stopAgain(it: Location?) {

        Log.e("Stopped", "Location Update Stopped")
        fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)

        val df = DecimalFormat("#.#####")
        imageLat = df.format(it?.latitude).toString()
        imageLng = df.format(it?.longitude).toString()

        val latLng = LatLng(it!!.latitude, it.longitude)

        progress.dismiss()

        val valid = PolyUtil.containsLocation(latLng, poly_list_LATLNG, false)
        Log.e("validLocation ", valid.toString())

       if(valid){
           openCamera()
       }

    }

    private fun hasPermissions(context: Context?, vararg PERMISSIONS: String): Boolean {
        if (context != null) {
            for (permissions in PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permissions
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    fun openCamera(){

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
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
                        if (imageNumber == 1){
                            resultLauncher1.launch(takePictureIntent)
                        }else if (imageNumber == 2){
                            resultLauncher2.launch(takePictureIntent)
                        }else if (imageNumber == 3){
                            resultLauncher3.launch(takePictureIntent)
                        }

                    }
                } catch (ex: Exception) {
                    // Error occurred while creating the File
                    displayMessage(baseContext, ex.message.toString())
                }

            } else {
                displayMessage(baseContext, "Null")
            }
        }
    }

}