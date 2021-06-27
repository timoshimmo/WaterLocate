package app.com.android.waterlocate.driver.activitys

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import app.com.android.waterlocate.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class DriverHomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_home)

        val toolbar: Toolbar = findViewById(R.id.tbDriverHome)
        setSupportActionBar(toolbar)

        //val btnProfile: FloatingActionButton = findViewById(R.id.fabDriverHomeToolbar)
        val btnSignOut: TextView = findViewById(R.id.tvDriverHomeSignOut)

        auth = Firebase.auth

        btnSignOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, DriverLoginActivity::class.java)
            startActivity(intent)
        }
    }
}