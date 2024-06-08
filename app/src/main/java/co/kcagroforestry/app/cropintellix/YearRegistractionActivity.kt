package co.kcagroforestry.app.cropintellix

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import co.kcagroforestry.app.R
import co.kcagroforestry.app.model.DataYear
import co.kcagroforestry.app.network.ApiClient
import co.kcagroforestry.app.network.ApiInterface
import com.kosherclimate.userapp.TimerData
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class YearRegistractionActivity : AppCompatActivity() {

    lateinit var showCurrentMonth: TextView
    lateinit var currentMonth: String
    lateinit var currentMonth_Name: String
    val yesr = arrayOf("--Select Year--", "2023-24")
    val session = arrayOf("--Select Season--", "rabi", "kharif")
    lateinit var SelectYear_spinner: AutoCompleteTextView
    lateinit var SelectSession_spinner: AutoCompleteTextView
    lateinit var claseeName: String
    lateinit var token: String
    lateinit var pagename: String
    lateinit var button_next: Button
    lateinit var button_back: Button
    lateinit var text_timer: TextView
    lateinit var setdatayear: TextView

    lateinit var timerData: TimerData
    var StartTime = 0
    var AcresList = ArrayList<String>()
    var update = ArrayList<String>()
    private var end_of_date: Int = 0
    private var preparation_date_interval: Int = 0
    private var transplantation_date_interval: Int = 0
    var str_name = ArrayList<String>()
    var str_year = ArrayList<String>()
    var str_nameid = ArrayList<Int>()
    private var namePosition: Int = 0
    private var namePositionId: Int = 0
    private var yearPosition: Int = 0
    var selectyear: String = ""
    var selectSeason: String = ""
    var sessionid: String = ""

    private val PREF_NAME = "sharedcheckLogin"
    private lateinit var language: String
    private lateinit var locale: Locale
    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_year_registraction)

        val sharedPreference = getSharedPreferences("PREFERENCE_NAME", MODE_PRIVATE)
        token = sharedPreference.getString("token", "")!!


        val bundle = intent.extras
        if (bundle != null) {
            claseeName = bundle.getString("farmer_classname").toString()
            language = bundle.getString("language").toString()
            pagename = bundle.getString("language").toString()

            if (claseeName.equals("farmer_cropinfo")) {
                AcresList = bundle.getStringArrayList("plot_area")!!
                end_of_date = bundle.getInt("cropdata_end_days")
                preparation_date_interval = bundle.getInt("preparation_date_interval")
                transplantation_date_interval = bundle.getInt("cropdata_end_days")
            }
        } else {
            language = "en"
            Log.e("data", "No bundle data")
        }

        locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)


        showCurrentMonth = findViewById(R.id.showCurrentMonth)
        SelectSession_spinner = findViewById(R.id.SelectSession)
        SelectYear_spinner = findViewById(R.id.SelectYear)
        button_next = findViewById(R.id.assam_farmer_Next)
        button_back = findViewById(R.id.assam_farmer_back)
        text_timer = findViewById(R.id.text_timer)
        setdatayear = findViewById(R.id.setdatayear)

        timerData = TimerData(this@YearRegistractionActivity, text_timer)
        StartTime = timerData.startTime(0).toInt()

        val dateFormat: DateFormat = SimpleDateFormat("MM")
        val date = Date()
        currentMonth = dateFormat.format(date)
        Log.d("Month", dateFormat.format(date))

        val cal: Calendar = Calendar.getInstance()
        //  cal.add(Calendar.MONTH, 4);
        val month_date = SimpleDateFormat("MMMM")
        // currentMonth = dateFormat.format(date)
        currentMonth_Name = month_date.format(cal.time)

        showCurrentMonth.text = currentMonth_Name + "   " + "(" + currentMonth + ")"

        //getDataYear(currentMonth)

//        val adapter_year = ArrayAdapter(this, android.R.layout.simple_list_item_1, yesr)
//        SelectYear_spinner.adapter = adapter_year

        SelectYear_spinner.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, pos: Int,
                id: Long
            ) {
                if (SelectYear_spinner.text.toString().equals("--Select Year--")) {
                    yearPosition = 0
                } else {
                    yearPosition = pos
                    selectyear = SelectYear_spinner.text.toString()
                    println(selectSeason.toString())
                    Log.d("selected item1", selectyear.toString())
                }
            }
        }

        SelectSession_spinner.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?, arg1: View?, pos: Int,
                id: Long
            ) {
                if (SelectSession_spinner.text.toString().equals("--Select Season--")) {
                    namePosition = 0
                } else {
                    namePosition = pos
                    namePositionId = pos
                    sessionid = str_nameid[namePositionId].toString()
                    selectSeason = SelectSession_spinner.text.toString()
                    println(selectSeason.toString())

                    Log.d("selecteditem1", sessionid)
                }
            }
        }
//        val adapter_session = ArrayAdapter(this, android.R.layout.simple_list_item_1, session)
//        SelectSession_spinner.adapter = adapter_session


        button_next.setOnClickListener {

            val WarningDialog =
                SweetAlertDialog(this@YearRegistractionActivity, SweetAlertDialog.WARNING_TYPE)

            var sharedprefernce: SharedPreferences = getSharedPreferences("farmer_onboarding", 0)
            var editor: SharedPreferences.Editor = sharedprefernce.edit()
            editor.putString("selectSeason", selectSeason.toString()) // Storing string
            editor.putString("selectyear", selectyear.toString())
            editor.putString("sessionid", sessionid.toString())
            editor.commit() // commit changes// Storing string

            if (yearPosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Your Year"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener {
                    WarningDialog.cancel()
                }.show()

            } else if (namePosition == 0) {
                WarningDialog.titleText = resources.getString(R.string.warning)
                WarningDialog.contentText = "Select Your Season"
                WarningDialog.confirmText = resources.getString(R.string.ok)
                WarningDialog.setCancelClickListener {
                    WarningDialog.cancel()
                }.show()

            }

        }

        button_back.setOnClickListener { super.onBackPressed() }

    }

//    fun getDataYear(currentMonth: String) {
//
//        //var month = DataYear(currentMonth)
//        var month = DataYear(currentMonth)
//        val apiInterface = ApiClient.getRetrofitInstance().create(ApiInterface::class.java)
//        apiInterface.getDataYear1("Bearer $token", month)
//            .enqueue(object : retrofit2.Callback<ResponseBody> {
//                override fun onResponse(
//                    call: Call<ResponseBody>,
//                    response: Response<ResponseBody>
//                ) {
//
//                    if (response.code() == 200) {
//
//                        Log.d("userdtatResponse",response.body().toString())
//
//                        str_name.add("--Select Season--")
//                        str_year.add("--Select Year--")
//                        str_nameid.add(0)
//
//                        val stringResponse = JSONObject(response.body()!!.string())
//                        val jsonarraydata = stringResponse.optJSONArray("data")
//
//                        for (i in 0 until jsonarraydata.length()) {
//
//                            val jsonObject = jsonarraydata.getJSONObject(i)
//
//                            var id = jsonObject.getInt("id")
//                            var string_name = jsonObject.getString("season")
//                            var string_year = jsonObject.getString("year")
//
//                            str_name.add(string_name)
//                            str_year.add(string_year)
//                            str_nameid.add(id)
//                        }
//
//                        Log.d("usersession", str_name.toString())
//
//                        var adapter = ArrayAdapter(
//                            this@YearRegistractionActivity,
//                            android.R.layout.simple_list_item_1,
//                            str_name
//                        )
//                        SelectSession_spinner.setAdapter(adapter)
//
//                        val adapter_year = ArrayAdapter(
//                            this@YearRegistractionActivity,
//                            android.R.layout.simple_list_item_1,
//                            str_year
//                        )
//                        SelectYear_spinner.setAdapter(adapter_year)
//
////                        if (jsonarraydata.length() != 0){
////
////                            for (i in 0 until jsonarraydata.length()) {
////
////                                var jsonObject: JSONObject = jsonarraydata.getJSONObject(i)
////
////                                var string_name = jsonObject.getString("name")
////                                var string_year = jsonObject.getString("year")
////
////                                str_name.add(string_name)
////                                str_year.add(string_year)
////
////                            }
////
////                            Log.d("usersession", str_name.toString())
////
////                            val adapter = ArrayAdapter(
////                                this@YearRegistractionActivity,
////                                android.R.layout.simple_list_item_1,
////                                str_name
////                            )
////                            SelectSession_spinner.adapter = adapter
////
////                            val adapter_year = ArrayAdapter(
////                                this@YearRegistractionActivity,
////                                android.R.layout.simple_list_item_1,
////                                str_year
////                            )
////                            SelectYear_spinner.adapter = adapter_year
////
////                        }
////
////                        else{
////                            val WarningDialog =
////                                SweetAlertDialog(this@YearRegistractionActivity, SweetAlertDialog.WARNING_TYPE)
////                            WarningDialog.titleText = resources.getString(R.string.warning)
////                            WarningDialog.contentText = "Data year Not Avilable"
////                            WarningDialog.confirmText = resources.getString(R.string.ok)
////                            WarningDialog.setCancelClickListener {
////                                WarningDialog.cancel()
////                            }.show()
////                        }
//
//                    }
//
//                }
//
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    Log.e("NEW_TEST", "Get Address Error  $t")
//                    Log.e("NEW_TEST", "Get Address Error  $call")
//                }
//
//            })
//    }
}