package com.example.vega_visual

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductsAdapter(private val products: MutableList<Product>) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {


    inner class ProductViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.title)
        val descTextView: TextView = view.findViewById(R.id.description)
        val productImageView: ImageView = view.findViewById(R.id.image)
        val priceTextView: TextView = view.findViewById(R.id.price)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_card, parent, false)
        return ProductViewHolder(view)
    }


    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentItem = products[position]

        holder.titleTextView.text = currentItem.title
        holder.descTextView.text = currentItem.description
        holder.priceTextView.text = "${currentItem.price.toString()} Rub."
        Glide.with(holder.itemView.context).load(currentItem.thumbnail).into(holder.productImageView)
    }


    override fun getItemCount() = products.size

    fun addProducts(newProducts: List<Product>) {
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}