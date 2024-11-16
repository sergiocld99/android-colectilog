package cs10.apps.travels.tracer.pages.fab

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import cs10.apps.common.android.ui.CSActivity
import cs10.apps.travels.tracer.R
import cs10.apps.travels.tracer.common.enums.SelectOption
import cs10.apps.travels.tracer.databinding.ActivitySelectTravelTypeBinding
import cs10.apps.travels.tracer.databinding.ButtonSelectTypeBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Point
import cs10.apps.travels.tracer.model.lines.CustomBusLine
import cs10.apps.travels.tracer.pages.fab.db.LikelyBusFinder
import cs10.apps.travels.tracer.viewmodel.LocationVM

class SelectTravelType : CSActivity() {
    private lateinit var binding: ActivitySelectTravelTypeBinding
    private lateinit var locationVM: LocationVM
    private val undefined = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelectTravelTypeBinding.inflate(layoutInflater);
        locationVM = ViewModelProvider(this)[LocationVM::class.java]
        setContentView(binding.root)

        initializeViews()
    }

    private fun initializeViews() {
        initializeLines()
        renderBusButton(binding.btn2)
        renderTrainButton(binding.btn1)
        renderCarButton(binding.btn3)
        renderSubwayButton(binding.btn5)
        renderStopButton(binding.btn6)
        renderZoneButton(binding.btn4)
    }

    private fun initializeLines() {
        val location = Point(intent.getDoubleExtra("x", 0.0), intent.getDoubleExtra("y", 0.0))
        if (location.getX() == 0.0) {
            showLongToast("Unable to get location")
            return
        }

        doInBackground {
            val lines = LikelyBusFinder(location, MiDB.getInstance(this)).predict()
            doInForeground { renderLines(lines) }
        }
    }

    private fun renderLines(lines: List<CustomBusLine>) {
        val targetViews = mutableListOf(binding.line1, binding.line2, binding.line3)
        targetViews.forEach {
            it.root.setOnClickListener { }
            it.root.isVisible = false
        }

        lines.forEachIndexed { index, customBusLine ->
            targetViews[index].root.isVisible = true
            val view = targetViews[index].lineIndicator
            view.textLineNumber.text = customBusLine.number.toString()
            view.root.setCardBackgroundColor(customBusLine.color)
            view.root.setOnClickListener { returnBusCreation(customBusLine.number!!) }
        }
    }

    private fun renderBusButton(btn : ButtonSelectTypeBinding) {
        btn.buttonDrawing.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_bus))
        btn.buttonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bus))
        btn.root.setOnClickListener { returnType(SelectOption.BUS_TRAVEL) }
    }

    private fun renderTrainButton(btn: ButtonSelectTypeBinding) {
        btn.buttonDrawing.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_train))
        btn.buttonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.train))
        btn.root.setOnClickListener { returnType(SelectOption.TRAIN_TRAVEL) }
    }

    private fun renderCarButton(btn: ButtonSelectTypeBinding) {
        btn.buttonDrawing.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_car))
        btn.buttonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bus_159))
        btn.root.setOnClickListener { returnType(SelectOption.CAR_TRAVEL) }
    }

    private fun renderSubwayButton(btn: ButtonSelectTypeBinding) {
        btn.buttonDrawing.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_railway))
        btn.buttonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bus_148))
        btn.root.setOnClickListener { returnType(SelectOption.METRO_TRAVEL) }
    }

    private fun renderStopButton(btn: ButtonSelectTypeBinding) {
        btn.buttonDrawing.setImageDrawable(ContextCompat.getDrawable(this,
            R.drawable.ic_edit_location
        ))
        btn.buttonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bus_500))
        btn.root.setOnClickListener { returnType(SelectOption.STOP) }
    }

    private fun renderZoneButton(btn: ButtonSelectTypeBinding) {
        btn.buttonDrawing.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_radar))
        btn.buttonCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.bus_324))
        btn.root.setOnClickListener { returnType(SelectOption.ZONE) }
    }

    private fun returnType(option: SelectOption) {
        setResult(option.ordinal)
        finish()
    }

    private fun returnBusCreation(line: Int) {
        setResult(-line)
        finish()
    }

    override fun onBackPressed() {
        setResult(undefined)
        super.onBackPressed()
    }
}