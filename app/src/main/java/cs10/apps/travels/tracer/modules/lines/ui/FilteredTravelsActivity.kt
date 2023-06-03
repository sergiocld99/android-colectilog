package cs10.apps.travels.tracer.modules.lines.ui

import android.os.Bundle
import android.view.MenuItem
import cs10.apps.common.android.ui.CSActivity
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ActivityFilteredTravelsBinding

class FilteredTravelsActivity : CSActivity() {

    // ViewModel
    private lateinit var binding: ActivityFilteredTravelsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilteredTravelsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Utils.loadBusBanner(binding.appbarImage)

        // UI
        if (intent.hasExtra("ramal")){
            val ramal = intent.getStringExtra("ramal")
            binding.title.text = String.format("Ramal %s", ramal ?: "sin nombre")
        } else if (intent.hasExtra("dest")){
            binding.title.text = String.format("Destino %s", intent.getStringExtra("dest"))
        } else if (intent.hasExtra("wd")){
            binding.title.text = String.format("Día %s", Utils.getWdCompleteString(intent.getIntExtra("wd", -1)))
        }

        // This activity contains a fragment container: MyTravelsFragment

        // back button
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // ------------------------------ TOP MENU ---------------------------

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}