package com.example.resepku.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.resepku.R
import com.example.resepku.adapter.CategoriesAdapter
import com.example.resepku.adapter.NewRecipesAdapter
import com.example.resepku.databinding.ActivityDetailRecipesBinding
import com.example.resepku.model.ModelCategories
import com.example.resepku.model.ModelRecipes
import com.example.resepku.networking.ApiEndpoint
import com.example.resepku.databinding.ActivityNewRecipesBinding
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class NewRecipesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewRecipesBinding
    var modelRecipesList: MutableList<ModelRecipes> = ArrayList()
    var modelCategoriesList: MutableList<ModelCategories> = ArrayList()
    var newRecipesAdapter: NewRecipesAdapter? = null
    var categoriesAdapter: CategoriesAdapter? = null
    var page = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        binding.pbListNewResep.setVisibility(View.GONE)

        binding.searchRecipe.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                getSearchRecipe(query)
                return false
            }
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText == "") {
                    getListNewResep(page)
                }
                return false
            }
        })

        val searchPlateId = binding.searchRecipe.getContext().resources.getIdentifier("android:id/search_plate",
                null, null)
        val searchPlate = binding.searchRecipe.findViewById<View>(searchPlateId)
        searchPlate?.setBackgroundColor(Color.TRANSPARENT)

        categoriesAdapter = CategoriesAdapter(this, modelCategoriesList)
        binding.rvCategories.setLayoutManager(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
        binding.rvCategories.setHasFixedSize(true)
        binding.rvCategories.setAdapter(categoriesAdapter)

        //pagination
        binding.nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener {
            v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                binding.pbListNewResep.setVisibility(View.VISIBLE)
                getListNewResep(page)
            }
        })

        binding.fabFavorite.setOnClickListener {
            val intent = Intent(this@NewRecipesActivity, FavoriteRecipesActivity::class.java)
            startActivity(intent)
        }

        showRecyclerRecipe()

        //method get data
        getCategories()
        getListNewResep(page)
    }

    private fun showRecyclerRecipe() {
        newRecipesAdapter = NewRecipesAdapter(this@NewRecipesActivity, modelRecipesList)
        binding.rvListNewResep.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvListNewResep.setHasFixedSize(true)
        binding.rvListNewResep.adapter = newRecipesAdapter
    }

    private fun getSearchRecipe(query: String) {
        binding.rvListNewResep.showShimmerAdapter()
        val apiUrl = ApiEndpoint.BASEURL + "/api/search/?q=$query"
        Log.d("NetworkRequest", "Search Sending GET request to: $apiUrl")

        AndroidNetworking.get(apiUrl)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d("NetworkResponse", "Search Received response: $response")
                    try {
                        modelRecipesList = ArrayList()
                        val jsonArray = response.getJSONArray("results")
                        for (i in 0 until jsonArray.length()) {
                            val jsonObjectList = jsonArray.getJSONObject(i)
                            val dataApi = ModelRecipes()
                            dataApi.strTitleResep = jsonObjectList.getString("title")
                            dataApi.strThumbnail = jsonObjectList.getString("thumb")
                            dataApi.strKeyResep = jsonObjectList.getString("key")
                            dataApi.strTimes = jsonObjectList.getString("times")
                            dataApi.strPortion = jsonObjectList.getString("serving")
                            dataApi.strDificulty = jsonObjectList.getString("difficulty")
                            modelRecipesList.add(dataApi)
                        }
                        showRecyclerRecipe()
                        newRecipesAdapter?.notifyDataSetChanged()
                        binding.rvListNewResep.hideShimmerAdapter()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Toast.makeText(this@NewRecipesActivity, "Oops, gagal menampilkan resep makanan.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError) {
                    binding.pbListNewResep.visibility = View.GONE
                    Log.e("NetworkError", "Search Error in network request: $anError")
                    Toast.makeText(this@NewRecipesActivity, "Oops! Sepertinya ada masalah dengan koneksi internet kamu.", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun getCategories() {
        val apiurl = ApiEndpoint.BASEURL + ApiEndpoint.CATEGORIES
        Log.d("NetworkRequest", "kategori Sending GET request to: $apiurl")
            AndroidNetworking.get(apiurl)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            binding.pbCategories.visibility = View.GONE
                            Log.d("NetworkResponse", "kategori Received response: $response")
                            try {
                                val jsonArray = response.getJSONArray("results")
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObjectList = jsonArray.getJSONObject(i)
                                    val dataApi = ModelCategories()
                                    dataApi.strKategori = jsonObjectList.getString("category")
                                    dataApi.strKategoriKey = jsonObjectList.getString("key")
                                    modelCategoriesList.add(dataApi)
                                }
                                categoriesAdapter?.notifyDataSetChanged()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                Toast.makeText(this@NewRecipesActivity,
                                        "Oops, gagal menampilkan kategori resep masakan.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onError(anError: ANError) {
                            binding.pbCategories.visibility = View.GONE
                            Log.e("NetworkError", "kategori Error in network request: $anError")
                            // Log the specific error details
                            Log.e("NetworkError", "kategori Error in network request: ${anError.errorBody}")
                            Log.e("NetworkError", "kategori Error code: ${anError.errorCode}")
                            Log.e("NetworkError", "kategori Error message: ${anError.errorDetail}")
                            Toast.makeText(this@NewRecipesActivity,
                                    "Oops! Sepertinya ada masalah dengan koneksi internet kamu.", Toast.LENGTH_SHORT).show()
                        }
                    })
        }

    private fun getListNewResep(page: Int) {
        binding.rvListNewResep.showShimmerAdapter()
        val arpiurl = ApiEndpoint.BASEURL + "/api/recipes/$page"
        Log.d("NetworkRequest", "resep Sending GET request to: $arpiurl")
        AndroidNetworking.get(arpiurl)
                .addQueryParameter("page", page.toString())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        binding.pbListNewResep.visibility = View.GONE
                        Log.d("NetworkResponse", "new Received response: $response")
                        try {
                            modelRecipesList = ArrayList()
                            val jsonArray = response.getJSONArray("results")
                            for (i in 0 until jsonArray.length()) {
                                val jsonObjectList = jsonArray.getJSONObject(i)
                                val dataApi = ModelRecipes()
                                dataApi.strTitleResep = jsonObjectList.getString("title")
                                dataApi.strThumbnail = jsonObjectList.getString("thumb")
                                dataApi.strKeyResep = jsonObjectList.getString("key")
                                dataApi.strTimes = jsonObjectList.getString("times")
                                dataApi.strPortion = jsonObjectList.getString("serving")
                                dataApi.strDificulty = jsonObjectList.getString("difficulty")
                                modelRecipesList.add(dataApi)
                            }
                            showRecyclerRecipe()
                            newRecipesAdapter?.notifyDataSetChanged()
                            binding.rvListNewResep.hideShimmerAdapter()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Toast.makeText(this@NewRecipesActivity,
                                    "Oops, gagal menampilkan resep makanan.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(anError: ANError) {
                        binding.pbListNewResep.visibility = View.GONE

                        // Log the specific error details
                        Log.e("NetworkError", "New Error in network request: ${anError.errorBody}")
                        Log.e("NetworkError", "New Error code: ${anError.errorCode}")
                        Log.e("NetworkError", "New Error message: ${anError.errorDetail}")

                        Toast.makeText(this@NewRecipesActivity, "Oops! Sepertinya ada masalah dengan koneksi internet kamu.", Toast.LENGTH_SHORT).show()
                    }

                })
    }

    companion object {
        fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
            val window = activity.window
            val layoutParams = window.attributes
            if (on) {
                layoutParams.flags = layoutParams.flags or bits
            } else {
                layoutParams.flags = layoutParams.flags and bits.inv()
            }
            window.attributes = layoutParams
        }
    }

}
