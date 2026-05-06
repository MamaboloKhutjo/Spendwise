package com.example.spendwise

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private var isExpense = true
    private var selectedDate: Calendar = Calendar.getInstance()
    private var startTime: Calendar = Calendar.getInstance()
    private var endTime: Calendar = Calendar.getInstance()
    private var photoFile: File? = null

    private lateinit var tvDate: TextView
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var ivPhoto: ImageView
    private lateinit var spinnerCategory: Spinner

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            dispatchTakePictureIntent()
        } else {
            Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            photoFile?.let {
                ivPhoto.setImageURI(Uri.fromFile(it))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnExpense = findViewById<Button>(R.id.btnExpense)
        val btnIncome = findViewById<Button>(R.id.btnIncome)
        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etNote = findViewById<EditText>(R.id.etNote)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        val btnAddCategory = findViewById<ImageButton>(R.id.btnAddCategory)
        val btnSave = findViewById<Button>(R.id.btnSave)
        tvDate = findViewById(R.id.tvDate)
        tvStartTime = findViewById(R.id.tvStartTime)
        tvEndTime = findViewById(R.id.tvEndTime)
        ivPhoto = findViewById(R.id.ivPhoto)

        btnBack.setOnClickListener { finish() }

        updateDateLabel()
        updateStartTimeLabel()
        updateEndTimeLabel()
        updateCategorySpinner()

        tvDate.setOnClickListener { showDatePicker() }
        tvStartTime.setOnClickListener { showTimePicker(startTime, true) }
        tvEndTime.setOnClickListener { showTimePicker(endTime, false) }
        
        ivPhoto.setOnClickListener {
            if (photoFile != null && photoFile!!.exists()) {
                showPhotoOptions()
            } else {
                checkPermissionAndTakeTable()
            }
        }

        btnAddCategory.setOnClickListener { showAddCategoryDialog() }

        btnExpense.setOnClickListener {
            isExpense = true
            btnExpense.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#C62828")
            )
            btnExpense.setTextColor(android.graphics.Color.WHITE)
            btnIncome.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#2C2C2E")
            )
            btnIncome.setTextColor(android.graphics.Color.parseColor("#888888"))
        }

        btnIncome.setOnClickListener {
            isExpense = false
            btnIncome.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#2E7D32")
            )
            btnIncome.setTextColor(android.graphics.Color.WHITE)
            btnExpense.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor("#2C2C2E")
            )
            btnExpense.setTextColor(android.graphics.Color.parseColor("#888888"))
        }

        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString().trim()
            if (amountStr.isEmpty() || amountStr.toDoubleOrNull() == null) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val amount = amountStr.toDouble()
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = spinnerCategory.selectedItem?.toString() ?: "Other"
            val note = etNote.text.toString().trim()
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)
            val startTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(startTime.time)
            val endTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(endTime.time)
            val type = if (isExpense) "expense" else "income"

            val transaction = Transaction(
                id = System.currentTimeMillis(),
                type = type,
                category = category,
                amount = amount,
                note = note,
                date = dateStr,
                startTime = startTimeStr,
                endTime = endTimeStr,
                imagePath = photoFile?.absolutePath
            )

            AppData.addTransaction(transaction)
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("View Photo", "Retake Photo", "Remove Photo")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Photograph Options")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> showFullPhoto()
                1 -> checkPermissionAndTakeTable()
                2 -> {
                    photoFile = null
                    ivPhoto.setImageResource(android.R.drawable.ic_menu_camera)
                }
            }
        }
        builder.show()
    }

    private fun showFullPhoto() {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        val imageView = ImageView(this)
        imageView.setImageURI(Uri.fromFile(photoFile))
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        builder.setView(imageView)
        builder.setPositiveButton("Close", null)
        builder.show()
    }

    private fun checkPermissionAndTakeTable() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            dispatchTakePictureIntent()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun updateCategorySpinner() {
        val categories = AppData.getCategories()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun showAddCategoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add New Category")
        val input = EditText(this)
        input.hint = "Category Name"
        builder.setView(input)
        builder.setPositiveButton("Add") { _, _ ->
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) {
                AppData.addCategory(name)
                updateCategorySpinner()
                val index = AppData.getCategories().indexOf(name)
                if (index != -1) spinnerCategory.setSelection(index)
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun showDatePicker() {
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            selectedDate.set(Calendar.YEAR, year)
            selectedDate.set(Calendar.MONTH, month)
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateLabel()
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker(calendar: Calendar, isStart: Boolean) {
        TimePickerDialog(this, { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            if (isStart) updateStartTimeLabel() else updateEndTimeLabel()
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun updateDateLabel() {
        val format = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        tvDate.text = format.format(selectedDate.time)
    }

    private fun updateStartTimeLabel() {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        tvStartTime.text = format.format(startTime.time)
    }

    private fun updateEndTimeLabel() {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        tvEndTime.text = format.format(endTime.time)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: Exception) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "com.example.spendwise.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureLauncher.launch(takePictureIntent)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            photoFile = this
        }
    }
}
