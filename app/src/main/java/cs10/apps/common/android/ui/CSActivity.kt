package cs10.apps.common.android.ui

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

open class CSActivity : AppCompatActivity() {

    open fun doInBackground(runnable: Runnable){
        Thread(runnable).start()
    }

    open fun doInForeground(runnable: Runnable){
        runOnUiThread(runnable)
    }

    fun showLongToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun showShortToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}