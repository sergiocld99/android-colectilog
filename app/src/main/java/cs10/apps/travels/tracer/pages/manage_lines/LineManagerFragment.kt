package cs10.apps.travels.tracer.pages.manage_lines

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cs10.apps.common.android.ui.CS_Fragment
import cs10.apps.travels.tracer.databinding.FragmentManageLinesBinding
import cs10.apps.travels.tracer.model.lines.CustomBusLine
import cs10.apps.travels.tracer.pages.manage_lines.adapter.LineManagerAdapter
import cs10.apps.travels.tracer.pages.manage_lines.viewmodel.LineManagerVM
import cs10.apps.travels.tracer.viewmodel.RootVM

class LineManagerFragment : CS_Fragment() {
    private lateinit var binding: FragmentManageLinesBinding
    private lateinit var rootVM: RootVM
    private lateinit var lineManagerVM: LineManagerVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentManageLinesBinding.inflate(inflater, container, false)

        // instanciar view models
        rootVM = ViewModelProvider(requireActivity())[RootVM::class.java]
        lineManagerVM = ViewModelProvider(requireActivity())[LineManagerVM::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = LineManagerAdapter(listOf()) { line -> onLineClick(line) }
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        lineManagerVM.observe(viewLifecycleOwner) { list ->
            val ogSize = adapter.itemCount
            val newSize = list.size

            adapter.list = list

            if (ogSize == 0) adapter.notifyItemRangeInserted(0, newSize)
            else adapter.notifyDataSetChanged()
        }

        rootVM.loading.observe(viewLifecycleOwner) {
            binding.root.isVisible = !it
        }
    }

    override fun onResume() {
        super.onResume()

        // reload
        lineManagerVM.load(rootVM.database.linesDao(), rootVM)
    }

    private fun onLineClick(customBusLine: CustomBusLine){
        lineManagerVM.selectEditing(customBusLine)

        customBusLine.number?.let { n ->
            val intent: Intent

            if (n >= 0){
                intent = Intent(requireActivity(), LineDetail::class.java)
                intent.putExtra("number", customBusLine.number)
            } else if (customBusLine.id == -1) {
                intent = Intent(requireActivity(), TrainDetail::class.java)
            } else {
                return
            }

            startActivity(intent)
        }


    }

}