package com.example.doglist

import android.os.Bundle
import android.os.Parcel
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doglist.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import android.widget.SearchView.OnQueryTextListener as AndroidWidgetSearchViewOnQueryTextListener

private lateinit var binding:ActivityMainBinding
private lateinit var adapter: DogAdapter
private val itemsAdd = mutableListOf<ArrayList<String>>()
private val dogImages = mutableListOf<String>()


class MainActivity : AppCompatActivity(), AndroidWidgetSearchViewOnQueryTextListener,
    SearchView.OnQueryTextListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.svDogs.setOnQueryTextListener(this)
        initReciclerView()
    }


    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/breed/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    private fun initReciclerView() {
        val recyclerV = binding.rvDogs
        adapter = DogAdapter(dogImages)
        binding.rvDogs.layoutManager = LinearLayoutManager(this)
        binding.rvDogs.adapter = adapter


    }

    class ItemAdapter(
        private val context: Parcel,
        private val elementSearch: MutableList<ArrayList<String>>,
        val itemClickListener: MainActivity
    )



    @DelicateCoroutinesApi
    private fun searchByName(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val call: Response<DogResponse> =
                getRetrofit().create(APIService::class.java).getDogsbyBreed("$query/images")
            val puppies: DogResponse? = call.body()
            runOnUiThread {
                if (call.isSuccessful) {
                    val images = puppies?.images ?: emptyList()
                    dogImages.clear()
                    dogImages.addAll(images)
                    adapter.notifyDataSetChanged()
                } else {
                    showError()
                }
                hideKeyboard()
            }

        }
    }

    private fun showError() {
        Toast.makeText(this, "ha ocurrido un error", Toast.LENGTH_SHORT).show()
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (!query.isNullOrEmpty()) {
            searchByName(query.lowercase(Locale.getDefault()))
        }
        return true
    }

    private fun hideKeyboard() {
        val imn = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imn.hideSoftInputFromWindow(binding.viewRoot.windowToken, 0)
    }
}
