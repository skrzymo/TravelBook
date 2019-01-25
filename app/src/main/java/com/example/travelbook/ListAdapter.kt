package com.example.travelbook

import android.content.Context
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ListAdapter(private val context: Context,
                  private val dataSourceCountries: ArrayList<String>, private val dataSourceTitles: ArrayList<String> ) : BaseAdapter() {

    override fun getCount(): Int {
        return dataSourceCountries.size
    }

    override fun getItem(position: Int): Any {
        return dataSourceCountries[position]
    }

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var mSelectedItemsIds: SparseBooleanArray = SparseBooleanArray()

    fun getCount(list: ArrayList<String>): Int {
        return list.size
    }

    fun getItem(position: Int, list: ArrayList<String>): String {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_item, parent, false)

            holder = ViewHolder()
            holder.countryTextView = view.findViewById(R.id.list_country) as TextView
            holder.titleTextView = view.findViewById(R.id.list_title) as TextView

            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val titleTextView = holder.titleTextView
        val countryTextView = holder.countryTextView

        val country = getItem(position, dataSourceCountries)
        val title = getItem(position, dataSourceTitles)

        countryTextView.text = country
        titleTextView.text = title

        return view
    }

    private class ViewHolder {
        lateinit var titleTextView: TextView
        lateinit var countryTextView: TextView
    }

    /*fun remove(obj: String) {
        dataSource.remove(obj)
        notifyDataSetChanged()
    }*/

    fun toggleSelection(position: Int) {
        selectView(position, !mSelectedItemsIds.get(position))
    }

    fun removeSelection() {
        mSelectedItemsIds = SparseBooleanArray()
        notifyDataSetChanged()
    }

    fun selectView(position: Int, value: Boolean) {
        if(value) {
            mSelectedItemsIds.put(position, value)
        } else {
            mSelectedItemsIds.delete(position)
        }
        notifyDataSetChanged()
    }


    fun getSelectedIds(): SparseBooleanArray {
        return mSelectedItemsIds
    }
}