package com.example.travelbook

import android.content.*
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.SparseBooleanArray
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import android.widget.ListView
import android.widget.Toast
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.*
import java.lang.Exception


class DeleteActivity : AppCompatActivity() {

    var firebaseAuth: FirebaseAuth? = null
    private lateinit var listView: ListView
    private val LOG_TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete)

        Log.d(LOG_TAG, "-------")
        Log.d(LOG_TAG, "onCreate")


        listView = findViewById(R.id.to_delete_list_view)

        val adapter = ListAdapter(this@DeleteActivity, countriesArray, titlesArray)
        listView.adapter = adapter
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL

        listView.setMultiChoiceModeListener(object: AbsListView.MultiChoiceModeListener {

            override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: android.view.ActionMode?) {

            }

            override fun onCreateActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
                mode?.menuInflater?.inflate(R.menu.menu_delete_layout, menu)
                return true
            }

            override fun onActionItemClicked(mode: android.view.ActionMode?, item: MenuItem?): Boolean {

                Log.d(LOG_TAG, "-------")
                Log.d(LOG_TAG, "onActionItemClicked")

                when (item?.itemId) {
                    R.id.selectAll -> {
                        val checkedCount: Int = countriesArray.size
                        adapter.removeSelection()
                        for (i in 0 until checkedCount) {
                            listView.setItemChecked(i, true)
                        }
                        mode?.title = "Zaznaczone: $checkedCount"
                        return true
                    }

                    R.id.delete -> {
                        var checkedCount = 0
                        val builder = AlertDialog.Builder(this@DeleteActivity)
                        builder.setMessage("Czy chcesz usunąć zaznaczone elementy?")
                        builder.setNegativeButton("Nie") { dialog, which ->
                        }
                        builder.setPositiveButton("Tak") { dialog, which ->
                            var selected: SparseBooleanArray = adapter.getSelectedIds()
                            for (i in (selected.size() - 1) downTo 0) {
                                if (selected.valueAt(i)) {
                                    firebaseAuth = FirebaseAuth.getInstance()
                                    val user = firebaseAuth?.currentUser
                                    val name : String = user?.displayName!!
                                    var selectedCountry: String = adapter.getItem(selected.keyAt(i), countriesArray)
                                    var selectedTitle: String = adapter.getItem(selected.keyAt(i), titlesArray)
                                    val database = openOrCreateDatabase("Places", Context.MODE_PRIVATE, null)
                                    database.execSQL("DELETE FROM placesNew WHERE country = \"$selectedCountry\" AND title = \"$selectedTitle\" AND userName = \"$name\"")
                                    //adapter.remove(selectedItem)
                                    checkedCount ++
                                }
                                mode?.finish()
                                selected.clear()
                            }
                            Toast.makeText(applicationContext, "Usunięto $checkedCount elementów",Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@DeleteActivity, MainActivity::class.java))
                        }
                        val alert: AlertDialog = builder.create()
                        alert.setIcon(R.drawable.question)
                        alert.setTitle("Potwierdzenie")
                        alert.show()
                        return true
                    }

                    else -> {
                        return false
                    }
                }
            }

            override fun onItemCheckedStateChanged(mode: android.view.ActionMode?, position: Int, id: Long, checked: Boolean) {

                Log.d(LOG_TAG, "-------")
                Log.d(LOG_TAG, "onItemCheckedStateChanged")

                val checkedCount: Int = listView.checkedItemCount
                mode?.title = "$checkedCount Selected"
                adapter.toggleSelection(position)
            }

        })
    }
}
