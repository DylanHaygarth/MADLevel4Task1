package com.example.madlevel4task1.repository

import android.content.Context
import com.example.madlevel4task1.database.ShoppingListRoomDatabase
import com.example.madlevel4task1.model.ShoppingProduct
import com.example.madlevel4task1.dao.ProductDao

class ProductRepository(context: Context) {
    private val productDao: ProductDao

    init {
        val database =
            ShoppingListRoomDatabase.getDatabase(
                context
            )
        productDao = database!!.productDao()
    }

    suspend fun getAllProducts(): List<ShoppingProduct> = productDao.getAllProducts()

    suspend fun insertProduct(product: ShoppingProduct) = productDao.insertProduct(product)

    suspend fun deleteProduct(product: ShoppingProduct) = productDao.deleteProduct(product)

    suspend fun deleteAllProducts() = productDao.deleteAllProducts()

}