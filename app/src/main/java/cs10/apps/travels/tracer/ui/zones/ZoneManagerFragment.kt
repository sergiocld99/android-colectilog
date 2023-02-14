package cs10.apps.travels.tracer.ui.zones

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cs10.apps.common.android.ui.CS_Fragment
import cs10.apps.travels.tracer.adapter.ZoneManagerAdapter
import cs10.apps.travels.tracer.databinding.FragmentManageZonesBinding
import cs10.apps.travels.tracer.db.MiDB
import cs10.apps.travels.tracer.model.Zone
import cs10.apps.travels.tracer.viewmodel.RootVM
import cs10.apps.travels.tracer.viewmodel.ZoneManagerVM

class ZoneManagerFragment : CS_Fragment() {
    private lateinit var binding: FragmentManageZonesBinding
    private lateinit var rootVM: RootVM
    private lateinit var zoneManagerVM: ZoneManagerVM
    private lateinit var adapter: ZoneManagerAdapter
    private var autoOpened = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentManageZonesBinding.inflate(inflater, container, false)

        // instanciar view models
        rootVM = ViewModelProvider(requireActivity())[RootVM::class.java]
        zoneManagerVM = ViewModelProvider(requireActivity())[ZoneManagerVM::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ZoneManagerAdapter(
            mutableListOf(),
            { zone -> onZoneClick(zone) },
            { zone, pos -> onZoneLongClick(zone, pos) }
        )

        binding.recycler.adapter = adapter
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())

        zoneManagerVM.observe(viewLifecycleOwner) { list ->
            val ogSize = adapter.itemCount
            val newSize = list.size

            adapter.list = list

            if (ogSize == 0) adapter.notifyItemRangeInserted(0, newSize)
            else adapter.notifyDataSetChanged()

            if (list.isEmpty() && !autoOpened) {
                val intent = Intent(requireActivity(), ZoneCreator::class.java)
                Handler(Looper.getMainLooper()).postDelayed({ startActivity(intent) }, 200)
                autoOpened = true
            }
        }

        rootVM.loading.observe(viewLifecycleOwner) {
            binding.root.isVisible = !it
        }
    }

    override fun onResume() {
        super.onResume()

        // reload
        zoneManagerVM.load(rootVM)
    }

    private fun onZoneClick(item: Zone) {
        zoneManagerVM.selectEditing(item)

        val intent = Intent(requireActivity(), ZoneEditor::class.java)
        intent.putExtra("id", item.id)
        startActivity(intent)
    }

    private fun onZoneLongClick(item: Zone, pos: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(item.name)
        builder.setMessage("¿Quieres eliminar esta zona?")
        builder.setPositiveButton("Si") { dialogInterface, i ->
            doInBackground {
                val dao = MiDB.getInstance(context).zonesDao()
                dao.delete(item)
                doInForeground { adapter.remove(pos) }
            }
        }

        builder.setNeutralButton("Volver") { dialogInterface, i -> dialogInterface.cancel() }
        builder.create().show()
    }

}