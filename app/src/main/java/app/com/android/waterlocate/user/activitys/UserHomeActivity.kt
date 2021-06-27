package app.com.android.waterlocate.user.activitys

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import app.com.android.waterlocate.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserHomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home)
        val toolbar: Toolbar = findViewById(R.id.tbUserHome)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fabUserHomeToolbar)
        val btnSignOut: TextView = findViewById(R.id.tvUserHomeSignOut)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Open Settings", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        auth = Firebase.auth

        btnSignOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, UserLoginActivity::class.java)
            startActivity(intent)
        }

    }

    /*   override fun onBackPressed() {
          return
      }

     override fun onCreateOptionsMenu(menu: Menu): Boolean {
          // Inflate the menu; this adds items to the action bar if it is present.
          menuInflater.inflate(R.menu.user_home, menu)
          return true
      }

      override fun onSupportNavigateUp(): Boolean {
          val navController = findNavController(R.id.nav_user_host_fragment)
          return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
      } */
}