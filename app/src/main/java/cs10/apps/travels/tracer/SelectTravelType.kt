package cs10.apps.travels.tracer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cs10.apps.travels.tracer.databinding.ActivitySelectTravelTypeBinding
import cs10.apps.travels.tracer.databinding.ButtonSelectTypeBinding

class SelectTravelType : AppCompatActivity() {
    private lateinit var binding: ActivitySelectTravelTypeBinding
    private val busType = 0
    private val trainType = 1
    private val undefined = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelectTravelTypeBinding.inflate(layoutInflater);
        setContentView(binding.root)

        initializeViews()
    }

    private fun initializeViews() {
        renderBusButton(binding.btn1)
        renderTrainButton(binding.btn2)
        renderStopButton(binding.btn3)
        renderZoneButton(binding.btn4)
    }

    private fun renderBusButton(btn : ButtonSelectTypeBinding) {
        btn.buttonDrawing.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bus))
        btn.buttonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bus))
        btn.buttonLabel.text = getString(R.string.bus_travel)
        btn.root.setOnClickListener { returnType(busType) }
    }

    private fun renderTrainButton(btn: ButtonSelectTypeBinding) {
        btn.buttonDrawing.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_train))
        btn.buttonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.train))
        btn.buttonLabel.text = getString(R.string.train_travel)
        btn.root.setOnClickListener { returnType(trainType) }
    }

    private fun renderStopButton(btn: ButtonSelectTypeBinding) {
        btn.buttonDrawing.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit_location))
        btn.buttonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bus_500))
        btn.buttonLabel.text = getString(R.string.stop)
        btn.root.setOnClickListener { returnType(2) }
    }

    private fun renderZoneButton(btn: ButtonSelectTypeBinding) {
        btn.buttonDrawing.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_radar))
        btn.buttonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bus_324))
        btn.buttonLabel.text = getString(R.string.zone)
        btn.root.setOnClickListener { returnType(3) }
    }

    private fun returnType(type : Int) {
        setResult(type)
        finish()
    }

    override fun onBackPressed() {
        setResult(undefined)
        super.onBackPressed()
    }
}