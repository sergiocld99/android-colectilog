package cs10.apps.travels.tracer.common.components

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class Dropdown<T>(
    private val sorter: Spinner,
    private val listOptions: List<T>,
    private val onSelectItem: (index: Int) -> Unit = {}
) {
    private var selectedIndex = -1

    init {
        val adapter = ArrayAdapter(sorter.context, android.R.layout.simple_spinner_item, listOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sorter.adapter = adapter
        sorter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, index: Int, p3: Long) {
                selectedIndex = index
                onSelectItem(index)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }

    private fun isValidIndex(index: Int) = listOptions.isNotEmpty() && index >= 0 && index < listOptions.size

    fun isValidSelection() = isValidIndex(selectedIndex)
    fun getSelectedItem() = listOptions[selectedIndex]

    fun select(index: Int) {
        if (!isValidIndex(index)) return
        sorter.setSelection(index, true)
    }
}