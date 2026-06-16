package com.example.spendwise

import android.content.Context
import android.content.SharedPreferences
import com.example.spendwise.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

data class Transaction(
    val id: Long,
    val type: String,
    val category: String,
    val amount: Double,
    val note: String,
    val date: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val imagePath: String? = null
)

object AppData {

    private const val PREF_NAME = "spendwise_prefs"
    private const val KEY_USER_ID = "current_user_id"

    private lateinit var db: AppDatabase
    private lateinit var prefs: SharedPreferences
    var currentUser: User? = null
        private set

    fun init(context: Context) {
        db = AppDatabase.getDatabase(context)
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedUserId = prefs.getLong(KEY_USER_ID, -1L)
        if (savedUserId != -1L) {
            runBlocking {
                currentUser = db.userDao().getUserById(savedUserId)
            }
        }
    }

    private fun checkUser() {
        if (currentUser == null) throw IllegalStateException("No user logged in")
    }

    // ── Authentication ────────────────────────────────────────────────────────
    suspend fun login(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val user = db.userDao().login(email, password)
        if (user != null) {
            setCurrentUserInternal(user)
            return@withContext true
        }
        false
    }

    suspend fun register(name: String, email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        if (db.userDao().getUserByEmail(email) != null) return@withContext false
        val newUser = User(name = name, email = email, password = password)
        val id = db.userDao().register(newUser)
        setCurrentUserInternal(newUser.copy(id = id))
        
        // Add default categories for new user
        val defaults = listOf("Food", "Transport", "Shopping", "Entertainment", "Bills", "Health", "Education", "Gifts", "Other")
        defaults.forEach { 
            db.categoryDao().insertCategory(CategoryEntity(userId = id, name = it))
        }
        true
    }

    fun logout() {
        currentUser = null
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    private fun setCurrentUserInternal(user: User) {
        currentUser = user
        prefs.edit().putLong(KEY_USER_ID, user.id).apply()
    }

    // ── Transactions ──────────────────────────────────────────────────────────

    fun addTransaction(t: Transaction) = runBlocking {
        checkUser()
        db.transactionDao().insertTransaction(
            TransactionEntity(
                userId = currentUser!!.id,
                type = t.type,
                category = t.category,
                amount = t.amount,
                note = t.note,
                date = t.date,
                startTime = t.startTime,
                endTime = t.endTime,
                imagePath = t.imagePath
            )
        )
    }

    fun getTransactions(): List<Transaction> = runBlocking {
        if (currentUser == null) return@runBlocking emptyList<Transaction>()
        db.transactionDao().getAllTransactions(currentUser!!.id).map { it.toDomain() }
    }

    fun getTransactionsInPeriod(startDate: String, endDate: String): List<Transaction> = runBlocking {
        if (currentUser == null) return@runBlocking emptyList<Transaction>()
        db.transactionDao().getTransactionsInPeriod(currentUser!!.id, startDate, endDate).map { it.toDomain() }
    }

    fun getTotalIncome(): Double = runBlocking {
        if (currentUser == null) return@runBlocking 0.0
        db.transactionDao().getTotalIncome(currentUser!!.id, "0000-00-00", "9999-99-99") ?: 0.0
    }

    fun getTotalExpenses(): Double = runBlocking {
        if (currentUser == null) return@runBlocking 0.0
        db.transactionDao().getTotalExpenses(currentUser!!.id, "0000-00-00", "9999-99-99") ?: 0.0
    }

    fun getMoneyLeft(): Double = (currentUser?.monthlyBudget ?: 0.0) - getTotalExpenses()

    fun getSpentForCategory(category: String, startDate: String? = null, endDate: String? = null): Double = runBlocking {
        if (currentUser == null) return@runBlocking 0.0
        db.transactionDao().getSpentForCategory(
            currentUser!!.id, 
            category, 
            startDate ?: "0000-00-00", 
            endDate ?: "9999-99-99"
        ) ?: 0.0
    }

    // ── Budgets ───────────────────────────────────────────────────────────────

    fun setBudget(category: String, limit: Double) = runBlocking {
        checkUser()
        db.budgetDao().setBudget(BudgetEntity(userId = currentUser!!.id, category = category, limitAmount = limit))
    }

    fun getBudgetLimit(category: String): Double = runBlocking {
        if (currentUser == null) return@runBlocking 0.0
        db.budgetDao().getBudgetByCategory(currentUser!!.id, category)?.limitAmount ?: 0.0
    }

    fun getAllBudgets(): Map<String, Double> = runBlocking {
        if (currentUser == null) return@runBlocking emptyMap()
        db.budgetDao().getAllBudgets(currentUser!!.id).associate { it.category to it.limitAmount }
    }

    // ── Categories ────────────────────────────────────────────────────────────

    fun addCategory(category: String) = runBlocking {
        checkUser()
        db.categoryDao().insertCategory(CategoryEntity(userId = currentUser!!.id, name = category))
    }

    fun getCategories(): List<String> = runBlocking {
        if (currentUser == null) return@runBlocking emptyList()
        db.categoryDao().getCategories(currentUser!!.id).map { it.name }
    }

    // ── Goals & Monthly Budget ────────────────────────────────────────────────

    fun setGoals(min: Double, max: Double) = runBlocking {
        checkUser()
        val updatedUser = currentUser!!.copy(minGoal = min, maxGoal = max)
        db.userDao().updateUser(updatedUser)
        currentUser = updatedUser
    }

    fun setMonthlyBudget(amount: Double) = runBlocking {
        checkUser()
        val updatedUser = currentUser!!.copy(monthlyBudget = amount)
        db.userDao().updateUser(updatedUser)
        currentUser = updatedUser
    }

    fun getMinGoal(): Double = currentUser?.minGoal ?: 0.0
    fun getMaxGoal(): Double = currentUser?.maxGoal ?: 0.0
    fun getMonthlyBudget(): Double = currentUser?.monthlyBudget ?: 0.0

    // ── Mapper ───────────────────────────────────────────────────────────────

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        type = type,
        category = category,
        amount = amount,
        note = note,
        date = date,
        startTime = startTime,
        endTime = endTime,
        imagePath = imagePath
    )
}
