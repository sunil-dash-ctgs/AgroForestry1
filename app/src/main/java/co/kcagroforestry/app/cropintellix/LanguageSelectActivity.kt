package co.kcagroforestry.app.cropintellix

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import co.kcagroforestry.app.R
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed

class LanguageSelectActivity : AppCompatActivity() {
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_select)


        val hindi = findViewById<View>(R.id.hindi_layout) as LinearLayout
        val english = findViewById<View>(R.id.english_layout) as LinearLayout
        val telugu = findViewById<View>(R.id.telugu_layout) as LinearLayout
        val marathi = findViewById<View>(R.id.marathi_layout) as LinearLayout
        val gujrati = findViewById<View>(R.id.gujrati_layout) as LinearLayout
        val odia = findViewById<View>(R.id.odia_layout) as LinearLayout
        val llAssam = findViewById<View>(R.id.assami) as LinearLayout
        val llBengali = findViewById<View>(R.id.bengali) as LinearLayout

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
//        checkForUpdate()

        english.setOnClickListener {
            val intent = Intent(this@LanguageSelectActivity, DashBoardCrop::class.java)
            intent.putExtra("language", "en")
            startActivity(intent)
        }


        hindi.setOnClickListener {
            val intent = Intent(this@LanguageSelectActivity, DashBoardCrop::class.java)
            intent.putExtra("language", "hi")
            startActivity(intent)
        }


        telugu.setOnClickListener {
            val intent = Intent(this@LanguageSelectActivity, DashBoardCrop::class.java)
            intent.putExtra("language", "te")
            startActivity(intent)
        }


        marathi.setOnClickListener { }


        gujrati.setOnClickListener { }


        odia.setOnClickListener { }

        llAssam.setOnClickListener {
            val intent = Intent(this@LanguageSelectActivity, DashBoardCrop::class.java)
            intent.putExtra("language", "as")
            startActivity(intent)
        }

        llBengali.setOnClickListener {
            val intent = Intent(this@LanguageSelectActivity, DashBoardCrop::class.java)
            intent.putExtra("language", "bn")
            startActivity(intent)
        }

    }

    private fun checkForUpdate() {
        try{
            Log.e("DEBUG_EXC","Start Fun checkForUpdate in $this")
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val isUpdateAllowed = when (updateType) {
                    AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                    else -> false
                }


                if (isUpdateAllowed && isUpdateAvailable) {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateType,
                        this@LanguageSelectActivity,
                        123
                    )
                }
            }
        }catch (e:Exception){
            Log.e("DEBUG_EXC","Exception ---> $e")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123){
            if(resultCode != RESULT_OK){
                println("Something went wrong !!")
            }
        }
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
        finish()
    }


}