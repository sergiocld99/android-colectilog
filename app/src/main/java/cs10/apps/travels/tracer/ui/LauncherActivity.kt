package cs10.apps.travels.tracer.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cs10.apps.travels.tracer.DrawerActivity

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        //setTheme(R.style.SplashScreen)
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, DrawerActivity::class.java))
        finish()
    }
}