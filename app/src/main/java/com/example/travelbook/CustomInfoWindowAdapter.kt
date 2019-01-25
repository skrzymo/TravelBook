package com.example.travelbook

import android.content.*
import android.view.*
import android.widget.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

class CustomInfoWindowAdapter(context : Context) : GoogleMap.InfoWindowAdapter {

    private val mWindow : View = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
    private var mContext : Context = context

    private fun renderWindowText(marker : Marker?, view : View) {
        var title : String = marker!!.title
        var tvTitle : TextView = view.findViewById(R.id.title)

        if(title != "") {
            tvTitle.text = title
        }

        var snippet : String = marker.snippet
        var tvSnippet : TextView = view.findViewById(R.id.snippet)

        if(title != "") {
            tvSnippet.text = snippet
        }
    }

    override fun getInfoContents(p0: Marker?): View {
        renderWindowText(p0, mWindow)
        return mWindow
    }

    override fun getInfoWindow(p0: Marker?): View {
        renderWindowText(p0, mWindow)
        return mWindow
    }

}