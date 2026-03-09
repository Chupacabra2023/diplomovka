package com.example.diplomovka_kotlin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.diplomovka_kotlin.ui.auth.AuthActivity
import com.example.diplomovka_kotlin.ui.map.MapActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: re-enable auth before release
        // val user = FirebaseAuth.getInstance().currentUser
        // if (user != null) {
        //     startActivity(Intent(this, MapActivity::class.java))
        // } else {
        //     startActivity(Intent(this, AuthActivity::class.java))
        // }
        startActivity(Intent(this, MapActivity::class.java))

        finish()
    }
}
