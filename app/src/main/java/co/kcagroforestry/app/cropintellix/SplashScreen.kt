package co.kcagroforestry.app.cropintellix

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import co.kcagroforestry.app.R
import co.kcagroforestry.app.MockLocationHelper

class SplashScreen : AppCompatActivity() {

    private val REQUEST_CODE_WRITE_SETTINGS = 123
    var mockLocationHelper : MockLocationHelper = MockLocationHelper()

    lateinit var sharedPreference : SharedPreferences
    var logged : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        sharedPreference =  getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE)
        logged = sharedPreference.getBoolean("isLoggedin", false)

        val devOptions = Settings.Secure.getInt(
            this.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        )

        gonextSystem()
    }

    fun gonextSystem(){

        Handler(Looper.getMainLooper()).postDelayed({
            if (logged){
                val intent = Intent(this@SplashScreen, LanguageSelectActivity::class.java)
                startActivity(intent)
                finish()
            } else{
                val editor = sharedPreference.edit()
                editor.putBoolean("isLoggedin", false)
                editor.commit()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, 3000)
    }
}