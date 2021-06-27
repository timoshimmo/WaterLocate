package app.com.android.waterlocate.driver.activitys

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import app.com.android.waterlocate.R
import app.com.android.waterlocate.StartActivity
import app.com.android.waterlocate.user.activitys.UserHomeActivity
import app.com.android.waterlocate.user.activitys.UserRegisterActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class DriverLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var progressBar: ProgressBar? = null
    private var btnLogin: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login)

        val toolbar: Toolbar = findViewById(R.id.tbDriverLogin)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val inputPhone: EditText = findViewById(R.id.etDriverLoginMobile)
        val inputPassword: EditText = findViewById(R.id.etDriverLoginPassword)

        progressBar = findViewById<ProgressBar>(R.id.pBarDriverLogin) as ProgressBar

        val tvGoToDriverNewAccount: TextView = findViewById(R.id.tvDriverGoToNewAccount)
        btnLogin = findViewById(R.id.btnDriverSignIn)
        tvGoToDriverNewAccount.setOnClickListener {
            val intent = Intent(this, DriverRegistrationActivity::class.java)
            startActivity(intent)
        }

        btnLogin?.setOnClickListener {
            progressBar!!.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            it?.isEnabled = false
            it.hideKeyboard()

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                ViewCompat.setBackgroundTintList(it, ColorStateList.valueOf(resources.getColor(R.color.light_grey, theme)))
            }
            else {
                ViewCompat.setBackgroundTintList(it, ColorStateList.valueOf(resources.getColor(R.color.light_grey)))
            }

            if(inputPhone.text.toString() != "" || inputPassword.text.toString() != "") {
                login(inputPhone.text.toString(), inputPassword.text.toString())
            }
            else {
                Snackbar.make(it, "All fields are required. Pls try again", Snackbar.LENGTH_LONG)
                    .setAction("Close", null).show()
                // Toast.makeText(this, "All fields are required. Pls try again", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun login(phone: String, password: String) {

        auth = Firebase.auth
        val fakeEmail = "$phone@waterlocate.com"

        auth.signInWithEmailAndPassword(fakeEmail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, DriverHomeActivity::class.java)
                    startActivity(intent)
                }
                else {
                    progressBar!!.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    btnLogin?.isEnabled = true
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        ViewCompat.setBackgroundTintList(btnLogin!!, ColorStateList.valueOf(resources.getColor(R.color.primary, theme)))
                    }
                    else {
                        ViewCompat.setBackgroundTintList(btnLogin!!, ColorStateList.valueOf(resources.getColor(R.color.primary)))
                    }
                    // If sign in fails, display a message to the user.
                    Snackbar.make(btnLogin!!, "Invalid email/password. Pls try again", Snackbar.LENGTH_LONG)
                        .setAction("Close", null).show()
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, StartActivity::class.java)
        startActivity(intent)
        return true
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}