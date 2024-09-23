package cs10.apps.travels.tracer.pages.month_summary

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import cs10.apps.travels.tracer.utils.Utils
import cs10.apps.travels.tracer.databinding.ActivityEditBalanceBinding
import cs10.apps.travels.tracer.pages.month_summary.viewmodel.BalanceVM

class EditBalanceActivity : AppCompatActivity() {
    private lateinit var binding : ActivityEditBalanceBinding
    private lateinit var balanceVM: BalanceVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditBalanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // title
        binding.appbar.toolbarLayout.title = "Actualizar saldo"
        Utils.loadSubeBanner(binding.appbar.appbarImage)

        // vm
        balanceVM = ViewModelProvider(this)[BalanceVM::class.java]
        balanceVM.savedBalance.observe(this){binding.etBalance.hint = "$ $it"}

        // fab behavior
        binding.fab.setOnClickListener {
            val input = binding.etBalance.text.toString()

            try {
                val balance = input.toFloat()
                balanceVM.save(balance) { finish() }
            } catch (e : NumberFormatException){
                Toast.makeText(this, "Entrada inválida", Toast.LENGTH_SHORT).show()
            }
        }
    }
}