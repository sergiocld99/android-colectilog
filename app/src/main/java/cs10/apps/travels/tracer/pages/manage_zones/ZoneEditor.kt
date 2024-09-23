package cs10.apps.travels.tracer.pages.manage_zones

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import cs10.apps.common.android.NumberUtils
import cs10.apps.common.android.ui.FormActivity
import cs10.apps.travels.tracer.utils.Utils
import cs10.apps.travels.tracer.databinding.ActivityZoneCreatorBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.common.enums.StatusCode
import cs10.apps.travels.tracer.model.Zone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ZoneEditor : FormActivity() {

    private lateinit var binding: ActivityZoneCreatorBinding
    private var id: Long = -1
    private var details: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityZoneCreatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Utils.loadBusBanner(binding.appbarImage)
        binding.toolbarLayout.title = "Editando zona"

        // Current location is not used in editor

        // Save Button
        binding.fab.setOnClickListener { onFabClicked() }

        // Complete by saved parameters
        id = intent.getLongExtra("id", -1)
        if (id != -1L) retrieveData()
    }

    private fun retrieveData() {

        lifecycleScope.launch(Dispatchers.IO){
            val db = MiDB.getInstance(this@ZoneEditor)
            db.zonesDao().getZoneWithId(id)?.let {
                binding.etRadix.setText(String.format("%.2f", it.getRadix()))
                binding.etLatitude.setText(it.getCenterX().toString())
                binding.etLongitude.setText(it.getCenterY().toString())
                binding.etName.setText(it.name)
            }
        }
    }


    private fun onFabClicked() {

        lifecycleScope.launch(Dispatchers.IO){
            val status = checkEntries()
            doInForeground { checkStatus(status) }
        }
    }

    private fun checkStatus(status: StatusCode) {
        when (status) {
            StatusCode.OK -> {
                showLongToast("Zona actualizada con éxito")
                finish()
            }
            StatusCode.EMPTY_FIELDS -> showShortToast("Por favor, complete todos los campos")
            StatusCode.RADIX_ZERO -> binding.etRadix.error = "Ingrese un número mayor a 0"
            //StatusCode.OVERLAP_CENTER -> showShortToast("El centro ya se encuentra en $details")
            //StatusCode.OVERLAP_RADIX -> binding.etRadix.error = "Se superpone con zona $details"

            StatusCode.OVERLAP_CENTER -> {
                showLongToast("Atención: centro en $details")
                finish()
            }

            StatusCode.OVERLAP_RADIX -> {
                showLongToast("Atención: superposición con $details")
                finish()
            }
        }
    }

    private suspend fun checkEntries() : StatusCode {
        val name = binding.etName.text.toString().trim()
        val latitude = binding.etLatitude.text.toString().trim().toDoubleOrNull()
        val longitude = binding.etLongitude.text.toString().trim().toDoubleOrNull()
        val radix = binding.etRadix.text.toString().trim()

        // N°01: empty fields
        if (name.isEmpty() || latitude == null || longitude == null || radix.isEmpty()){
            return StatusCode.EMPTY_FIELDS
        }

        // N°02: radix positive
        val radixValue = radix.toDouble()
        if (radixValue <= 0) return StatusCode.RADIX_ZERO

        // N°03: overlap
        val db = MiDB.getInstance(this).zonesDao()
        val overlaps = db.findZonesIn(latitude, longitude)
        var statusCode = StatusCode.OK

        if (overlaps.isNotEmpty()) {
            if (overlaps.first().id == this.id) overlaps.removeAt(0)
            if (overlaps.isNotEmpty()) {
                details = overlaps[0].name
                statusCode = StatusCode.OVERLAP_CENTER
            }
        }

        // passed all checks, then save to database
        val radixAbsoluteValue = NumberUtils.kmToCoordsDistance(radixValue)
        val x0 = latitude - radixAbsoluteValue
        val x1 = latitude + radixAbsoluteValue
        val y0 = longitude - radixAbsoluteValue
        val y1 = longitude + radixAbsoluteValue

        // N°04: partial overlap (but this)
        val partialOverlaps = db.findPartialOverlapsIn(x0, x1, y0, y1)
        if (partialOverlaps.isNotEmpty()){
            if (partialOverlaps.first().id == this.id) partialOverlaps.removeAt(0)

            if (partialOverlaps.isNotEmpty()) {
                details = partialOverlaps[0].name
                statusCode = StatusCode.OVERLAP_RADIX
            }
        }

        val zone = Zone(name, x0, x1, y0, y1)

        // update instead of insert
        zone.id = this.id
        db.update(zone)

        return statusCode
    }

}