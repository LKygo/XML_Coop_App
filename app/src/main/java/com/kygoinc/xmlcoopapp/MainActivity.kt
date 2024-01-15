package com.kygoinc.xmlcoopapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kygoinc.xmlcoopapp.ApiCall.OkHttpHelper
import com.kygoinc.xmlcoopapp.databinding.ActivityMainBinding
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val okHttpHelper = OkHttpHelper()
    private val apiUrl = "https://dummyjson.com/auth/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        binding.btnLogin.setOnClickListener {
            val username = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {

                this@MainActivity.lifecycleScope.launch {
                    try {
                        checkCredentials(username, password, this@MainActivity)
                    } catch (e: Exception) {
                        // Handle exceptions gracefully within the coroutine
                        Toast.makeText(this@MainActivity, "An error occurred", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private suspend fun checkCredentials(username: String, password: String, context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val jsonBody = JSONObject().apply {
                    put("username", username)
                    put("password", password)
                }
                Log.d("MainActivity", "$jsonBody")

                val requestBody =
                    jsonBody.toString().toRequestBody("application/json".toMediaType())
                val postResponse = okHttpHelper.sendRequestWithCredentials(
                    apiUrl, username, password, "POST", requestBody
                )

                if (postResponse != null) {
                    if (postResponse.isSuccessful) {
                        // Handle successful response
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Successful login", Toast.LENGTH_SHORT).show()
                            // Navigate to welcome activity
                            val intent = Intent(context, WelcomeActivity::class.java)
                            intent.putExtra("USERNAME_EXTRA", username)
                            startActivity(intent)
                        }
                    } else {
                        // Handle error response
                        withContext(Dispatchers.Main) {
                            if (postResponse != null) {
                                when (postResponse.code) {
                                    401 -> Toast.makeText(
                                        context, "Invalid credentials", Toast.LENGTH_SHORT
                                    ).show()

                                    404 -> Toast.makeText(
                                        context, "Server not found", Toast.LENGTH_SHORT
                                    ).show()

                                    else -> {
                                        Toast.makeText(
                                            context, "Something went wrong", Toast.LENGTH_SHORT
                                        ).show()

                                        Log.d("MainActivity", postResponse.code.toString())
                                    }
                                }
                            }
                        }
                    }


                } else {
                    // Handle null response
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                // Handle exceptions (e.g., network errors)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show()
                    Log.d("MainActivity", e.toString())
                }
            }
        }
    }
}