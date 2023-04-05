package cs10.apps.travels.tracer.ui.zones

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import cs10.apps.common.android.NumberUtils
import cs10.apps.common.android.ui.FormActivity
import cs10.apps.travels.tracer.Utils
import cs10.apps.travels.tracer.databinding.ActivityZoneCreatorBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.enums.StatusCode
import cs10.apps.travels.tracer.model.Zone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ZoneCreator : FormActivity() {

    private lateinit var binding: ActivityZoneCreatorBinding
    private lateinit var client: FusedLocationProviderClient
    private var details: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityZoneCreatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        Utils.loadBusBanner(binding.appbarImage)
        binding.toolbarLayout.title = "Nueva zona"

        // Location
        client = LocationServices.getFusedLocationProviderClient(this)
        getLocation()

        // Save Button
        binding.fab.setOnClickListener { onFabClicked() }

        // Other fields
        binding.etRadix.setText("0.8")
    }

    private fun onFabClicked() {

        lifecycleScope.launch(Dispatchers.IO){
            val status = checkEntries()
            doInForeground { checkStatus(status) }
        }

        /*
        doInBackground {
            val status = checkEntries()
            doInForeground { checkStatus(status) }
        }

         */
    }

    private fun checkStatus(status: StatusCode) {
        when (status) {
            StatusCode.OK -> {
                showLongToast("Zona guardada con éxito")
                finish()
            }
            StatusCode.EMPTY_FIELDS -> showShortToast("Por favor, complete todos los campos")
            StatusCode.RADIX_ZERO -> binding.etRadix.error = "Ingrese un número mayor a 0"

            StatusCode.OVERLAP_CENTER -> {
                showShortToast("Atención: centro en $details")
                finish()
            }

            StatusCode.OVERLAP_RADIX -> {
                showShortToast("Atención: superposición con $details")
                finish()
            }
        }
    }

    @Throws(SecurityException::class)
    private fun getLocation() {
        if (Utils.checkPermissions(this)) client.lastLocation.addOnSuccessListener {
            if (it != null){
                binding.etLatitude.setText(it.latitude.toString())
                binding.etLongitude.setText(it.longitude.toString())
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

        // N°03: overlap (can save in creation)
        val db = MiDB.getInstance(this).zonesDao()
        val overlaps = db.findZonesIn(latitude, longitude)
        var statusCode = StatusCode.OK

        if (overlaps.isNotEmpty()) {
            details = overlaps[0].name
            statusCode = StatusCode.OVERLAP_CENTER
        }

        // passed all checks, then save to database
        val radixAbsoluteValue = NumberUtils.kmToCoordsDistance(radixValue)
        val x0 = latitude - radixAbsoluteValue
        val x1 = latitude + radixAbsoluteValue
        val y0 = longitude - radixAbsoluteValue
        val y1 = longitude + radixAbsoluteValue

        // N°04: partial overlap (can save on creation)
        val partialOverlaps = db.findPartialOverlapsIn(x0, x1, y0, y1)
        if (partialOverlaps.isNotEmpty()){
            details = partialOverlaps[0].name
            statusCode = StatusCode.OVERLAP_RADIX
        }

        val zone = Zone(name, x0, x1, y0, y1)
        db.insert(zone)

        return statusCode
    }

}