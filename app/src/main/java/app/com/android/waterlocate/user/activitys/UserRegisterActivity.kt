package app.com.android.waterlocate.user.activitys

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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserRegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var progressBar: ProgressBar? = null
    private var btnSignUp: Button? = null
    private var signupForm: LinearLayout? = null
    private var inputName: EditText? = null
    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_register)

        val toolbar: Toolbar = findViewById(R.id.tbSignUp)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        inputName = findViewById(R.id.etSignUpName)
        inputEmail = findViewById(R.id.etUserSignUpEmail)
        inputPassword = findViewById(R.id.etUserSignUpPassword)

        val tvGoToLogin: TextView = findViewById(R.id.tvGoToLogin)
        btnSignUp = findViewById(R.id.btnUserSignUp)

        progressBar = findViewById(R.id.pBarSignUp)
        signupForm = findViewById(R.id.signupForm)

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

            if(inputEmail?.text.toString() != "" && inputPassword?.text.toString() != "" &&
                inputName?.text.toString() != "") {
                signup(inputName?.text.toString(), inputEmail?.text.toString(), inputPassword?.text.toString())
            }
            else {
                Snackbar.make(it, "All fields are required. Pls try again", Snackbar.LENGTH_LONG)
                    .setAction("Close", null).show()
            }
        }

        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, UserLoginActivity::class.java)
            startActivity(intent)
        }
    }


    private fun signup(name: String, email: String, password: String) {

        auth = Firebase.auth

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                val curUser = auth.currentUser

                if(curUser != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    curUser.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            Snackbar.make(signupForm!!, "User successfully created", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Continue", View.OnClickListener {
                                    val intent = Intent(this, UserLoginActivity::class.java)
                                    startActivity(intent)
                                }).show()
                            progressBar!!.visibility = View.GONE
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            inputName?.setText("")
                            inputEmail?.setText("")
                            inputPassword?.setText("")
                            /*btnSignUp?.isEnabled = true
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                ViewCompat.setBackgroundTintList(btnSignUp!!, ColorStateList.valueOf(resources.getColor(R.color.primary, theme)))
                            }
                            else {
                                ViewCompat.setBackgroundTintList(btnSignUp!!, ColorStateList.valueOf(resources.getColor(R.color.primary)))
                            }*/
                        }
                    }
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