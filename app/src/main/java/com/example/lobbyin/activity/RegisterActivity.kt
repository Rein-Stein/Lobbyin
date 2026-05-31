package com.example.lobbyin.activity

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lobbyin.R
import com.example.lobbyin.database.AppDatabase
import com.example.lobbyin.model.User
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etName)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val spRole = findViewById<Spinner>(R.id.spRole)
        val etRoleCode = findViewById<EditText>(R.id.etRoleCode)
        val btnRegister = findViewById<View>(R.id.btnRegister)
        val btnBack = findViewById<View>(R.id.btnBack)
        val btnBackToLogin = findViewById<TextView>(R.id.btnBackToLogin)

        val db = AppDatabase.getDatabase(this)

        // Show/Hide Role Code field based on Spinner selection
        spRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRole = parent?.getItemAtPosition(position).toString()
                if (selectedRole == "Mahasiswa") {
                    etRoleCode.visibility = View.GONE
                } else {
                    etRoleCode.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString()
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            val role = spRole.selectedItem.toString()
            val roleCode = etRoleCode.text.toString()

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Data tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi Kode Khusus
            if (role == "Admin") {
                if (roleCode != "norero") {
                    Toast.makeText(this, "Kode Admin salah!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else if (role == "Dosen") {
                if (roleCode != "naan1") {
                    Toast.makeText(this, "Kode Dosen salah!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            lifecycleScope.launch {
                val existingUser = db.userDao().getUserByUsername(username)
                if (existingUser != null) {
                    Toast.makeText(this@RegisterActivity, "Username sudah digunakan", Toast.LENGTH_SHORT).show()
                } else {
                    // Pastikan model User mendukung field name dan role
                    val newUser = User(
                        username = username,
                        password = password,
                        role = role
                    )
                    db.userDao().insertUser(newUser)
                    Toast.makeText(this@RegisterActivity, "Register Berhasil sebagai $role!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        btnBack.setOnClickListener { finish() }
        btnBackToLogin.setOnClickListener { finish() }
    }
}
