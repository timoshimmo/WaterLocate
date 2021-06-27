package app.com.android.waterlocate.driver.activitys

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import app.com.android.waterlocate.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DriverRegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var progressBar: ProgressBar? = null
    private var btnSignUp: Button? = null
    private var signupForm: LinearLayout? = null
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_registration)

        val toolbar: Toolbar = findViewById(R.id.tbDriverSignUp)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val inputName: EditText = findViewById(R.id.etDriverSignUpName)
        val inputPhone: EditText = findViewById(R.id.etDriverPhoneNumber)
        val inputPassword: EditText = findViewById(R.id.etDriverSignUpPassword)

        val tvGoToLogin: TextView = findViewById(R.id.tvDriverGoToLogin)
        btnSignUp = findViewById(R.id.btnDriverSignUp)

        progressBar = findViewById(R.id.pBarDriverSignUp)
        signupForm = findViewById(R.id.signupDriverForm)

        btnSignUp?.setOnClickListener {

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

            if(inputPhone.text.toString() != "" && inputPassword.text.toString() != "" &&
                inputName.text.toString() != "") {
                signup(inputName.text.toString(), inputPhone.text.toString(), inputPassword.text.toString())
            }
            else {
                Snackbar.make(it, "All fields are required. Pls try again", Snackbar.LENGTH_LONG)
                    .setAction("Close", null).show()
            }
        }

        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, DriverLoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signup(name: String, phone: String, password: String) {

        auth = Firebase.auth
        database = Firebase.database.reference
        val fakeEmail = "$phone@waterlocate.com"

        auth.createUserWithEmailAndPassword(fakeEmail, password).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                val curUser = auth.currentUser

                if(curUser != null) {

                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    val data = hashMapOf(
                        "name" to name,
                        "email" to fakeEmail,
                        "mobileNo" to phone,
                        "status" to false
                    )

                    database.child("drivers").child(curUser.uid).setValue(data)

                    Snackbar.make(signupForm!!, "Driver successfully created", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Continue", View.OnClickListener {
                            val intent = Intent(this, DriverLoginActivity::class.java)
                            startActivity(intent)
                        }).show()
                    progressBar!!.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                   /* btnSignUp?.isEnabled = true
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        ViewCompat.setBackgroundTintList(btnSignUp!!, ColorStateList.valueOf(resources.getColor(R.color.primary, theme)))
                    }
                    else {
                        ViewCompat.setBackgroundTintList(btnSignUp!!, ColorStateList.valueOf(resources.getColor(R.color.primary)))
                    } */
                }
            }
            else {
                progressBar!!.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                btnSignUp?.isEnabled = true
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    ViewCompat.setBackgroundTintList(btnSignUp!!, ColorStateList.valueOf(resources.getColor(R.color.primary, theme)))
                }
                else {
                    ViewCompat.setBackgroundTintList(btnSignUp!!, ColorStateList.valueOf(resources.getColor(R.color.primary)))
                }
                Snackbar.make(btnSignUp!!, "Authentication Error: " + task.exception?.localizedMessage, Snackbar.LENGTH_LONG)
                    .setAction("Close", null).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}