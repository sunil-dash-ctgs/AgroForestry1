package co.kcagroforestry.app.onboardingpage

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
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
import co.kcagroforestry.app.databinding.ActivityPlantationInfoBinding
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import co.kcagroforestry.app.utils.CommonData
import com.kosherclimate.userapp.TimerData
import com.whiteelephant.monthpicker.MonthPickerDialog
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class PlantationInfo : AppCompatActivity() {

    lateinit var farmer_back: Button

    lateinit var binding: ActivityPlantationInfoBinding

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_plantation_info)

        binding = ActivityPlantationInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = SweetAlertDialog(this@PlantationInfo, SweetAlertDialog.PROGRESS_TYPE)
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

         timerData = TimerData(this@PlantationInfo, binding.textTimer)
         StartTime = timerData.startTime(StartTime1.toLong()).toInt()

        getplanttype()

        getmixplanttype()

        plantationName()

        binding.document1Camera.setOnClickListener {

            if (image1.isEmpty()) {

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

            } else {

                imageAlertDialog(image1)
            }

        }

        binding.document2Camera.setOnClickListener {

            if (image2.isEmpty()) {

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

            } else {
                imageAlertDialog(image2)
            }

        }

        binding.document3Camera.setOnClickListener {

            if (image3.isEmpty()) {

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

        binding.dateofPlantation.setOnClickListener {

            // Get Current Date
            // Get Current Date
            val c = Calendar.getInstance()
            val mYear = c[Calendar.YEAR]
            val mMonth = c[Calendar.MONTH]
            val mDay = c[Calendar.DAY_OF_MONTH]


            val datePickerDialog = DatePickerDialog(this,
                { view, year, monthOfYear, dayOfMonth ->

                    Log.d("monthofyear",monthOfYear.toString())

                    val month = monthOfYear + 1
                    var fm = "" + month
                    var fd = "" + dayOfMonth
                    if (month < 10) {
                        fm = "0$month"
                    }
                    if (dayOfMonth < 10) {
                        fd = "0$dayOfMonth"
                    }
                    val date = "$year-$fm-$fd"
                    val date1 = "$fd / $fm / $year"
                    Log.d("tag", "" + date)

                    binding.dateofPlantation.setText(date1)
                },
                mYear,
                mMonth,
                mDay
            )
            datePickerDialog.show()

        }

//        binding.YearofPlantation.setOnClickListener {
//
//            val calendar = Calendar.getInstance()
//            val year = calendar[Calendar.YEAR]
//
//            var choosenYear = year
//
//            val builder = MonthPickerDialog.Builder(this@PlantationInfo,
//                { selectedMonth, selectedYear ->
//                    binding.YearofPlantation.setText(Integer.toString(selectedYear))
//                    choosenYear = selectedYear
//                }, choosenYear, 0
//            )
//
//            builder.showYearOnly()
//                .setYearRange(1990, 2030)
//                .build()
//                .show()
//        }

        binding.plantNext.setOnClickListener {

            val WarningDialog = SweetAlertDialog(this@PlantationInfo, SweetAlertDialog.WARNING_TYPE)

            if (binding.plantationName.text.toString().equals("")) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Enter Your Plantation Name"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else if (planttypePosition == 0) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Your Type of Plantation"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

            } else if (mixplanttypePosition == 0) {

                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Your Mixed Plantation"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()

//            } else if (binding.NoOfPlants.text.toString().equals("")) {
//
//                WarningDialog.titleText = resources.getString(R.string.warning)
//                WarningDialog.contentText = "Enter Your No Of Plants"
//                WarningDialog.confirmText = resources.getString(R.string.ok)
//                WarningDialog.setCancelClickListener { WarningDialog.cancel() }.show()
//
//            }  else if (binding.girthofPlant.text.toString().equals("")) {

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

                submitPlantInfo()
            }

        }

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

        val dialog = Dialog(this@PlantationInfo)
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
                Toast.makeText(this@PlantationInfo, "Please Retry", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@PlantationInfo, "Please Retry", Toast.LENGTH_SHORT).show()
            }


        })

    }

    private fun plantTypeSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, planttypeNmaeList)
        binding.typeplant.setText(adapter.getItem(0));
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
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mixplanttypeNmaeList)
        binding.mixtypeplant.setText(adapter.getItem(0));
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


        val file1 = File(image1)
        val file2 = File(image2)
        val file3 = File(image3)

        val PlantationName: RequestBody = plantationId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val planttype_Id: RequestBody = planttypeId.toRequestBody("text/plain".toMediaTypeOrNull())
        val mixplanttype_Id: RequestBody = mixplanttypeId.toRequestBody("text/plain".toMediaTypeOrNull())

        val standingtrees: RequestBody = binding.standingtrees.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val plantspacing: RequestBody = binding.plantspacing.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val rowspacing: RequestBody = binding.rowspacing.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val hightofPlant: RequestBody = binding.hightofPlant.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val girthofPlant: RequestBody = binding.girthofPlant.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val dateofPlantation: RequestBody = binding.dateofPlantation.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val uniqueid: RequestBody = unique_id.toRequestBody("text/plain".toMediaTypeOrNull())

        val requestFileImage1: RequestBody = file1.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage2: RequestBody = file2.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val requestFileImage3: RequestBody = file3.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        val farmeruniquid: MultipartBody.Part = MultipartBody.Part.createFormData("farmeruniquid", null, uniqueid)
        val plantation_name: MultipartBody.Part = MultipartBody.Part.createFormData("plantation_id", null, PlantationName)
        val type_of_plantation: MultipartBody.Part = MultipartBody.Part.createFormData("type_of_plantation", null, planttype_Id)
        val mixed_plantation: MultipartBody.Part = MultipartBody.Part.createFormData("mixed_plantation", null, mixplanttype_Id)

        val standing_trees: MultipartBody.Part = MultipartBody.Part.createFormData("Current_Standing_Trees", null, standingtrees)
        val plant_spacing: MultipartBody.Part = MultipartBody.Part.createFormData("planttoplantspacing", null, plantspacing)
        val row_spacing: MultipartBody.Part = MultipartBody.Part.createFormData("rowtorowspacing", null, rowspacing)
        val height_Plant: MultipartBody.Part = MultipartBody.Part.createFormData("hightofPlant", null, hightofPlant)
        val girth_Plant: MultipartBody.Part = MultipartBody.Part.createFormData("girthofPlant", null, girthofPlant)
        val date_Plantation: MultipartBody.Part = MultipartBody.Part.createFormData("dateyearofPlantation", null, dateofPlantation)

        val ImageBody1: MultipartBody.Part = MultipartBody.Part.createFormData("document1_photo", file1.name, requestFileImage1)
        val ImageBody2: MultipartBody.Part = MultipartBody.Part.createFormData("document2_photo", file2.name, requestFileImage2)
        val ImageBody3: MultipartBody.Part = MultipartBody.Part.createFormData("document3_photo", file3.name, requestFileImage3)

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        val submitplantinfo = apiInterface.submitPlantationinfo(
            "Bearer $token",
            farmeruniquid,
            plantation_name,
            type_of_plantation,
            mixed_plantation,
            ImageBody1,
            ImageBody2,
            ImageBody3,
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
                        var message = stringResponse.getString("message")
                        var FarmerId = stringResponse.getString("FarmerId")
                        var farmeruniquid = stringResponse.getString("farmeruniquid")

                        val intent =
                            Intent(this@PlantationInfo, CultivationInfo::class.java).apply {
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

    fun plantationName(){

        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
        apiInterface.plantationname("Bearer $token").enqueue(object: Callback<ResponseBody>{
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                plantationNmaeList.clear()
                plantationIdList.clear()

               if (response.body() != null){

                   if (response.code() == 200){

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
                Toast.makeText(this@PlantationInfo, "Please Retry", Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun plantationSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, plantationNmaeList)
        binding.plantationName.setText(adapter.getItem(0));
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
}