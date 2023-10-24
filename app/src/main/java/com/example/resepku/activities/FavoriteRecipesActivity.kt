package com.example.resepku.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.resepku.R
import com.example.resepku.adapter.FavoritesAdapter
import com.example.resepku.databinding.ActivityDetailRecipesBinding
import com.example.resepku.model.ModelRecipes
import com.example.resepku.realm.RealmHelper
import com.example.resepku.databinding.ActivityFavoriteRecipesBinding
import java.util.*

class FavoriteRecipesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteRecipesBinding
    var modelRecipes: MutableList<ModelRecipes> = ArrayList()
    var helper: RealmHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoriteRecipesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        helper = RealmHelper(this)

        setSupportActionBar(binding.toolbar)
        assert(supportActionBar != null)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.rvListFavorite.setLayoutManager(LinearLayoutManager(this))
        binding.rvListFavorite.setAdapter(FavoritesAdapter(this, modelRecipes))
        binding.rvListFavorite.setHasFixedSize(true)

        //menampilkan data favorite
        getFavorite()
    }

    private fun getFavorite() {
            modelRecipes = helper!!.showFavoriteRecipes()
            if (modelRecipes.size == 0) {
                binding.tvNoData.visibility = View.VISIBLE
                binding.rvListFavorite.visibility = View.GONE
            } else {
                binding.tvNoData.visibility = View.GONE
                binding.rvListFavorite.visibility = View.VISIBLE
                binding.rvListFavorite.adapter = FavoritesAdapter(this, modelRecipes)
            }
        }

    public override fun onResume() {
        super.onResume()
        getFavorite()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}