package com.example.vega_visual

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vega_visual.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val PAGE_SIZE = 20


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolBar = binding.toolbar
        setSupportActionBar(toolBar)

        val products = mutableListOf<Product>()
        var page = 0


        val retrofit = Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val layoutManager = GridLayoutManager(baseContext, 1)
        binding.recyclerView.layoutManager = layoutManager
        val adapter = ProductsAdapter(products)
        binding.recyclerView.adapter = adapter

        val ab = supportActionBar


        val productsApi = retrofit.create(ProductApi::class.java)


        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    loadPage(productsApi, ++page, adapter)
                }
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val currentPage = (firstVisibleItemPosition / PAGE_SIZE) + 1
                ab?.title = "Страница $currentPage"

            }
        })

        loadPage(productsApi, page, adapter)


    }

    private fun loadPage(productsApi: ProductApi, page: Int, adapter: ProductsAdapter) {
        val call = productsApi.getProducts(PAGE_SIZE, page * PAGE_SIZE)





        call.enqueue(object : Callback<ProductsResponse> {
            override fun onResponse(
                call: Call<ProductsResponse>,
                response: Response<ProductsResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.products?.let {
                        adapter.addProducts(it)
                    }
                } else {
                    Log.d("RRR", response.code().toString())
                    Log.d("RRR", "${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ProductsResponse>, t: Throwable) {
                Log.d("RRR", "46531253715371652731")
            }


        })
    }
}