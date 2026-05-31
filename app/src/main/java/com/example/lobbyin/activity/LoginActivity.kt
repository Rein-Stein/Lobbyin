package com.example.lobbyin.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lobbyin.R
import com.example.lobbyin.database.AppDatabase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<View>(R.id.btnLogin)
        val btnGoToRegister = findViewById<View>(R.id.btnGoToRegister)
        val rbMahasiswa = findViewById<RadioButton>(R.id.rbMahasiswa)
        val rbDosen = findViewById<RadioButton>(R.id.rbDosen)
        val rbAdmin = findViewById<RadioButton>(R.id.rbAdmin)

        val db = AppDatabase.getDatabase(this)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tentukan role yang dipilih di UI
            val selectedRole = when {
                rbMahasiswa.isChecked -> "Mahasiswa"
                rbDosen.isChecked -> "Dosen"
                rbAdmin.isChecked -> "Admin"
                else -> ""
            }

            lifecycleScope.launch {
                val user = db.userDao().login(username, password)
                if (user != null) {
                    // Validasi apakah role akun sesuai dengan role yang dipilih saat login
                    if (user.role == selectedRole) {
                        val isDosen = user.role == "Dosen"
                        val isAdmin = user.role == "Admin"
                        
                        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                        intent.putExtra("IS_DOSEN", isDosen)
                        intent.putExtra("IS_ADMIN", isAdmin)
                        intent.putExtra("USERNAME", user.username)
                        startActivity(intent)
                        finish()
                    } else {
                        // Jika role tidak sesuai
                        Toast.makeText(
                            this@LoginActivity, 
                            "Login Gagal! Akun Anda terdaftar sebagai ${user.role}", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Login Gagal! Username atau Password salah", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
