package com.pemchip.blablacar.screen.login;

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pemchip.blablacar.R
import com.pemchip.blablacar.common.BaseActivity
import com.pemchip.blablacar.common.Constants
import com.pemchip.blablacar.common.CustomLog
import com.pemchip.blablacar.common.PcpUtils
import com.pemchip.blablacar.common.PreferenceKey
import com.pemchip.blablacar.databinding.ActivityLoginBinding
import com.pemchip.blablacar.databinding.ActivitySignUpBinding
import com.pemchip.blablacar.model.UserModel
import com.pemchip.blablacar.screen.customerHome.CustomerHomeActivity
import com.pemchip.blablacar.screen.ownerHome.OwnerHomeActivity
import com.pemchip.blablacar.screen.signup.SignUpActivity
import java.util.concurrent.TimeUnit

class LoginActivity : BaseActivity(), View.OnClickListener {

    private var mContext: Context? = null
    private lateinit var mBinding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private var pDialog: ProgressDialog? = null
    private var loggedInUser: UserModel? = null
    private var mRole: String? = PreferenceKey.CUSTOMER_ROLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initViews()
        initEvents()
        initFirebase()
        initFirebaseDatabase()
        checkPermission()
    }

    private fun initViews() {

        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)
    }

    private fun initEvents() {
        mBinding.loginBtn.setOnClickListener(this)
        mBinding.registerTxt.setOnClickListener(this)

        mBinding.adminRadioButton.setOnClickListener(){
            mRole = PreferenceKey.CUSTOMER_ROLE
        }

        mBinding.employeeRadioButton.setOnClickListener(){
            mRole = PreferenceKey.CAR_OWNER_ROLE
        }
    }

    private fun initFirebase() {
        mAuth = FirebaseAuth.getInstance()
    }

    private fun initFirebaseDatabase() {
        mDatabase = FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()
    }

    private fun getUserDetails(email: String?) {
        if (email == null) {
            pDialog!!.dismiss()
            return
        }
        mDatabase?.child(Constants.FIREBASE_USERS)?.orderByChild(Constants.FIREBASE_EMAIL_KEY)?.equalTo(email)?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.getChildren()) {
                        loggedInUser = userSnapshot.getValue(UserModel::class.java)
                        if (loggedInUser != null) {
                            mPreferencesSession!!.saveStringData(PreferenceKey.USER_ID,loggedInUser!!.uid)
                            mPreferencesSession!!.saveStringData(PreferenceKey.USER_NAME,loggedInUser!!.name)
                            mPreferencesSession!!.saveStringData(PreferenceKey.USER_EMAIL,loggedInUser!!.email)
                            mPreferencesSession!!.saveStringData(PreferenceKey.USER_ROLE,loggedInUser!!.role)
                            mPreferencesSession!!.saveStringData(PreferenceKey.USER_GENDER,loggedInUser!!.gender)
                            pDialog!!.dismiss()

                            toast(mContext,"Login successfully")
                            val intent = Intent(this@LoginActivity, SecurityCheckActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()

                        } else {
                            pDialog!!.dismiss()
                        }
                    }
                }
            override fun onCancelled(databaseError: DatabaseError) {
                    pDialog!!.dismiss()
                    Toast.makeText(
                        this@LoginActivity,
                        "Cannot fetch user details information",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun callFirebaseAuthService(email: String, password: String) {
        mAuth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                getUserDetails(email)
            } else {
                pDialog!!.dismiss()
                Toast.makeText(
                    this@LoginActivity,
                    "Incorrect email or password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validateUserCredentials(email: String?, password: String?): Boolean {
        if (email != null && email == EMPTY_STRING) {
            Toast.makeText(this@LoginActivity, "Please input your email", Toast.LENGTH_LONG).show()
            return false
        }
        if (password != null && password == EMPTY_STRING) {
            Toast.makeText(this@LoginActivity, "Please input your password", Toast.LENGTH_LONG)
                .show()
            return false
        }
        return true
    }

    private fun login() {
        val email = mBinding.emailEdt.text.toString().trim { it <= ' ' }
        val password = mBinding.passwordEdt.text.toString().trim { it <= ' ' }
        if (validateUserCredentials(email, password)) {
            pDialog!!.show()
            callFirebaseAuthService(email, password)
        }
    }

    private fun goToSignUpScreen() {
        val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
        startActivity(intent)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.loginBtn -> login()
            R.id.registerTxt -> goToSignUpScreen()
            else -> {
            }
        }
    }

    companion object {
        private const val EMPTY_STRING = ""
    }
}