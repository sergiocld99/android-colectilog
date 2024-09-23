package cs10.apps.travels.tracer.pages.home.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import cs10.apps.travels.tracer.model.Parada
import cs10.apps.travels.tracer.pages.home.components.StopArrivalsFragment
import cs10.apps.travels.tracer.pages.home.constants.HomeArgs

class HomeAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    val favourites = mutableListOf<Parada>()

    override fun createFragment(position: Int): Fragment {
        val fragment =
            StopArrivalsFragment()
        val args = Bundle()
        args.putInt(HomeArgs.POSITION, position)
        fragment.arguments = args
        return fragment
    }

    override fun getItemCount() = favourites.size

    fun setData(favourites: List<Parada>) {
        this.favourites.clear()
        this.favourites.addAll(favourites)
        notifyDataSetChanged()
    }
}