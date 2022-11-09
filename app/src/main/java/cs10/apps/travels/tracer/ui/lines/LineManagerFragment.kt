package cs10.apps.travels.tracer.ui.lines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cs10.apps.common.android.CS_Fragment
import cs10.apps.travels.tracer.adapter.LineManagerAdapter
import cs10.apps.travels.tracer.databinding.FragmentManageLinesBinding
import cs10.apps.travels.tracer.viewmodel.LineManagerVM
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

        val adapter = LineManagerAdapter(listOf())
        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        lineManagerVM.observe(viewLifecycleOwner) { list ->
            adapter.list = list
            adapter.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()

        // reload
        lineManagerVM.load(rootVM.database.linesDao())
    }


}