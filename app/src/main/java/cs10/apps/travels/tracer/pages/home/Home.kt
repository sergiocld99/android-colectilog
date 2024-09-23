package cs10.apps.travels.tracer.pages.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cs10.apps.common.android.TimedLocation
import cs10.apps.common.android.ui.CS_Fragment
import cs10.apps.travels.tracer.databinding.FragmentHomeBinding
import cs10.apps.travels.tracer.pages.home.adapter.HomeAdapter
import cs10.apps.travels.tracer.pages.home.viewmodel.HomeVM
import cs10.apps.travels.tracer.viewmodel.LocationVM
import cs10.apps.travels.tracer.viewmodel.RootVM

class Home : CS_Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: HomeAdapter
    private lateinit var homeVM: HomeVM
    private lateinit var locationVM: LocationVM
    private lateinit var rootVM: RootVM
    private lateinit var firstLocationObserver: Observer<TimedLocation>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeVM = ViewModelProvider(requireActivity())[HomeVM::class.java]
        locationVM = ViewModelProvider(requireActivity())[LocationVM::class.java]
        rootVM = ViewModelProvider(requireActivity())[RootVM::class.java]

        adapter = HomeAdapter(this)
        binding.viewPager.adapter = adapter

        homeVM.favoriteStops.observe(viewLifecycleOwner) {
            adapter.setData(it)
            rootVM.disableLoading()
        }

        firstLocationObserver = Observer {
            rootVM.enableLoading()
            locationVM.getLiveData().removeObserver(firstLocationObserver)
            homeVM.buildFavList(it.location)
        }
        locationVM.getLiveData().observe(viewLifecycleOwner, firstLocationObserver)
    }
}