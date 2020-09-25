package com.example.madlevel4task1.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.madlevel4task1.model.ShoppingProduct

@Dao
interface ProductDao {
    @Query("SELECT * FROM reminderTable")
    suspend fun getAllProducts(): List<ShoppingProduct>

    @Insert
    suspend fun insertProduct(product: ShoppingProduct)

    @Delete
    suspend fun deleteProduct(product: ShoppingProduct)

    @Query("DELETE FROM reminderTable")
    suspend fun deleteAllProducts()
}