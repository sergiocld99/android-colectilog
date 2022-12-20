package cs10.apps.common.android.ui

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar

open class FormActivity : CSActivity() {

    override fun setSupportActionBar(toolbar: Toolbar?) {
        super.setSupportActionBar(toolbar)

        // Back arrow
        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}