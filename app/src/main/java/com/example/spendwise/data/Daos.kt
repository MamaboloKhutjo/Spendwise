package com.example.spendwise.data

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): User?

    @Insert
    suspend fun register(user: User): Long

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC, id DESC")
    suspend fun getAllTransactions(userId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getTransactionsInPeriod(userId: Long, startDate: String, endDate: String): List<TransactionEntity>

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'income' AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalIncome(userId: Long, startDate: String, endDate: String): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'expense' AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpenses(userId: Long, startDate: String, endDate: String): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'expense' AND category = :category AND date BETWEEN :startDate AND :endDate")
    suspend fun getSpentForCategory(userId: Long, category: String, startDate: String, endDate: String): Double?
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE userId = :userId")
    suspend fun getAllBudgets(userId: Long): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND category = :category LIMIT 1")
    suspend fun getBudgetByCategory(userId: Long, category: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: BudgetEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getCategories(userId: Long): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity)
}
