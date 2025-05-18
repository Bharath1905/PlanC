package com.pemchip.blablacar.screen.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.pemchip.blablacar.R
import com.pemchip.blablacar.common.BaseActivity
import com.pemchip.blablacar.common.PreferenceKey
import com.pemchip.blablacar.screen.customerHome.CustomerHomeActivity
import com.pemchip.blablacar.screen.history.RideHistoryScreen
import com.pemchip.blablacar.screen.login.LoginActivity
import com.pemchip.blablacar.screen.ownerHome.OwnerHomeActivity

class UserProfileActivity: BaseActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var btn_home: LinearLayout
    private lateinit var btn_history: LinearLayout
    private var mAuth: FirebaseAuth? = null

    companion object{
        var fallbackScreen: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        mAuth = FirebaseAuth.getInstance()

        // Initialize the views
        userNameTextView = findViewById(R.id.userNameTextView)
        userEmailTextView = findViewById(R.id.userEmailTextView)
        logoutButton = findViewById(R.id.logoutButton)
        btn_home = findViewById(R.id.btn_home)
        btn_history = findViewById(R.id.btn_history)

        // For demo purposes, setting some static data. You can get this from your database or API.
        userNameTextView.text = mPreferencesSession!!.getStringData(PreferenceKey.USER_NAME)
        userEmailTextView.text = mPreferencesSession!!.getStringData(PreferenceKey.USER_EMAIL)

        logoutButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure?")
                .setCancelable(true) // Disable closing the dialog by tapping outside
                .setPositiveButton("Yes") { dialog, id ->
                    dialog.dismiss()
                    mPreferencesSession!!.logout()
                    if(mAuth!!.currentUser!=null){
                        mAuth!!.signOut()
                    }
                    val intent = Intent(this@UserProfileActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            builder.create().show()
        }

        btn_home?.setOnClickListener(){
            if(fallbackScreen.equals("HOME")){
                finish()
            }else{
                if (mPreferencesSession!!.userRole == PreferenceKey.CUSTOMER_ROLE) {
                    val intent = Intent(this@UserProfileActivity, CustomerHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@UserProfileActivity, OwnerHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        btn_history?.setOnClickListener(){
            if(fallbackScreen.equals("HISTORY")){
                finish()
            }else{
                val intent = Intent(this@UserProfileActivity, RideHistoryScreen::class.java)
                RideHistoryScreen.fallbackScreen = "PROFILE"
                startActivity(intent)
                finish()
            }
        }
    }
}