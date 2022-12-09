package cs10.apps.common.android

import androidx.appcompat.app.AppCompatActivity

open class CSActivity : AppCompatActivity() {

    open fun doInBackground(runnable: Runnable){
        Thread(runnable).start()
    }

    open fun doInForeground(runnable: Runnable){
        runOnUiThread(runnable)
    }
}