package com.example.your_app_name

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vega_visual.Product
import com.example.vega_visual.ProductApi
import com.example.vega_visual.ProductsAdapter
import com.example.vega_visual.ProductsResponse
import com.example.your_app_name.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val PAGE_SIZE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val toolBar = binding.toolbar
        setSupportActionBar(toolBar)

        val products = mutableListOf<Product>()
        var page = 0
        val layoutManager = GridLayoutManager(baseContext, 1)
        binding.recyclerView.layoutManager = layoutManager
        val adapter = ProductsAdapter(products)
        binding.recyclerView.adapter = adapter


        val ab = supportActionBar


        val retrofit = Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val productsApi = retrofit.create(ProductApi::class.java)


        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    loadPage(productsApi, ++page, adapter)
                }
                // Обновление заголовка ActionBar
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val currentPage = (firstVisibleItemPosition / PAGE_SIZE) + 1
                ab?.title = "Страница $currentPage"
            }
        })


        loadPage(productsApi, page, adapter)

        // подключаюсь к postgreSQL
        val connection = connectToDatabase()
        if (connection != null) {
            createPricesTable(connection)
            insertPrices(connection)
            fetchProducts(connection)

            connection.close()
        } else {
            Log.e("Database", "Не удалось подключиться к базе данных")
        }
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

                        it.forEach { product ->
                            insertPrice(connectToDatabase(), product)
                        }
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


    private fun connectToDatabase(): Connection? {
        try {
            val url = "jdbc:postgresql://localhost:5432/bb"
            val user = "1234"
            val password = "1234"
            return DriverManager.getConnection(url, user, password)
        } catch (e: SQLException) {
            e.printStackTrace()
            return null
        }
    }


    private fun insertPrice(connection: Connection?, product: Product) {
        if (connection == null) {
            return
        }
        try {
            val preparedStatement = connection.prepareStatement("""
                INSERT INTO Prices (Product_ID, Value, DateTime)
            """)
            preparedStatement.setInt(1, product.id)
            preparedStatement.setDouble(2, product.price)
            preparedStatement.setTimestamp(3, Timestamp.valueOf(product.updatedAt))
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
    private fun fetchProducts(connection: Connection): List<Product> {
        val products = mutableListOf<Product>()
        try {
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT * FROM Products")
            while (resultSet.next()) {
                val id = resultSet.getInt("ID")
                val name = resultSet.getString("Name")
                val description = resultSet.getString("Description")
                val url = resultSet.getString("URL")
                val category = resultSet.getString("Category")
                val brand = resultSet.getString("Brand")
                val image = resultSet.getString("Image")
                products.add(Product(id, name, description, url, category, brand, image))
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return products
    }

    fun createPricesTable(connection: Connection) {
        try {
            val statement = connection.createStatement()
            statement.execute("""
            CREATE TABLE Prices (
                ID INT AUTO_INCREMENT PRIMARY KEY,
                Product_ID INT,
                Value DECIMAL(10,2),
                DateTime DATETIME,
                FOREIGN KEY (Product_ID) REFERENCES Products(ID)
            )
        """)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }
}