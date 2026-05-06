package com.example.smartspend.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.smartspend.data.entity.Category

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category): Long

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?
}
