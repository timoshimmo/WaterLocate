package app.com.android.waterlocate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.cardview.widget.CardView
import app.com.android.waterlocate.driver.activitys.DriverLoginActivity
import app.com.android.waterlocate.user.activitys.UserLoginActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val btnOpenUser: CardView = findViewById(R.id.btnGoToUserApp)
        val btnOpenDriver: CardView = findViewById(R.id.btnGoToDriverApp)


        btnOpenUser.setOnClickListener {
            val intent = Intent(this, UserLoginActivity::class.java)
            startActivity(intent)
        }

        btnOpenDriver.setOnClickListener {
            val intent = Intent(this, DriverLoginActivity::class.java)
            startActivity(intent)
        }
    }
}