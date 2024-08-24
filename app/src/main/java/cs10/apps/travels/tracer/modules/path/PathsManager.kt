package cs10.apps.travels.tracer.modules.path

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cs10.apps.common.android.ui.CS_Fragment
import cs10.apps.travels.tracer.databinding.FragmentPathsManagerBinding
import cs10.apps.travels.tracer.viewmodel.RootVM

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PathsManager.newInstance] factory method to
 * create an instance of this fragment.
 */
class PathsManager : CS_Fragment() {
    private lateinit var binding: FragmentPathsManagerBinding
    private lateinit var rootVM: RootVM
    private lateinit var pathsVM: PathsVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPathsManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootVM = ViewModelProvider(requireActivity())[RootVM::class.java]
        pathsVM = ViewModelProvider(requireActivity())[PathsVM::class.java]

        // test
        pathsVM.searchAllMediumStops(rootVM.database.safeStopsDao())
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PathsManager.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PathsManager().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}