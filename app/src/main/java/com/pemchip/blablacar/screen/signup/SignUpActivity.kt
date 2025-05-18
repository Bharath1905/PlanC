package com.pemchip.blablacar.screen.signup

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.pemchip.blablacar.common.BaseActivity
import com.pemchip.blablacar.common.Constants
import com.pemchip.blablacar.common.CustomLog
import com.pemchip.blablacar.common.PreferenceKey
import com.pemchip.blablacar.databinding.ActivitySignUpBinding
import com.pemchip.blablacar.model.UserModel
import java.util.UUID


class SignUpActivity : BaseActivity(){

    private var mContext: Context? = null
    private lateinit var mBinding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var mRole: String? = PreferenceKey.CUSTOMER_ROLE
    private var pDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this;
        mBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        initViews()
        checkPermission()
    }

    private fun initViews() {

        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)

        mBinding.registerBtn.setOnClickListener(){
            register()
        }

        mBinding.adminRadioButton.setOnClickListener(){
            mRole = PreferenceKey.CUSTOMER_ROLE
        }

        mBinding.employeeRadioButton.setOnClickListener(){
            mRole = PreferenceKey.CAR_OWNER_ROLE
        }
    }

    private fun validate(fullName: String?, phoneNumber: String?, email: String?, password: String?, confirmPassword: String?): Boolean {
        if (fullName == null || fullName.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please input your full name", Toast.LENGTH_LONG).show();
            return false;
        }
        if (phoneNumber == null || phoneNumber.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please input your phone number", Toast.LENGTH_LONG).show();
            return false;
        }
        if (email == null || email.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please input your email", Toast.LENGTH_LONG).show();
            return false;
        }

        if (password == null || password.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please input your password", Toast.LENGTH_LONG).show();
            return false;
        }
        if (confirmPassword == null || confirmPassword.equals(EMPTY_STRING)) {
            Toast.makeText(this@SignUpActivity, "Please input your confirm password", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this@SignUpActivity, "Your password and confirm password must be matched", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private fun insertFirebaseDatabase(userId: String?, fullname: String?, phoneNumber: String?, email: String?) {
        val userModel = UserModel()
        userModel.uid = userId
        userModel.name = fullname
        userModel.phone = phoneNumber
        userModel.email = email
        if(mBinding.maleRadioButton.isChecked){
            userModel.gender =  "Male"
        }else{
            userModel.gender =  "Female"
        }
        userModel.role = mRole
        userModel.securityPin = ""
        database = Firebase.database.reference;
        database.child(Constants.FIREBASE_USERS).child(userId!!).setValue(userModel).addOnCompleteListener(){
            pDialog!!.dismiss()
            CustomLog.trace("userModel: "+userModel)
            toast(mContext,"Signup successfully")
            finish()
        }.addOnFailureListener {
            pDialog!!.dismiss()
            toast(mContext,"Please try again...")
        }
    }

    private fun createFirebaseAccount(fullname: String?, phoneNumber: String?, email: String?, password: String?) {
        if (email != null && password != null) {
            auth = Firebase.auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = UUID.randomUUID()
                        insertFirebaseDatabase(userId.toString(), fullname, phoneNumber, email)
                    } else {
                        CustomLog.trace(task.result.toString())
                        CustomLog.trace(task.exception!!.cause!!.message)
                        pDialog!!.dismiss()
                        Toast.makeText(this@SignUpActivity, "Cannot create your account, please try again: ", Toast.LENGTH_LONG).show();
                    }
                }
        } else {
            pDialog!!.dismiss()
            Toast.makeText(this@SignUpActivity, "Please provide your email and password", Toast.LENGTH_LONG).show();
        }
    }

    private fun register() {
        val fullName = mBinding.fullnameEdt.text.toString().trim()
        val phoneNumber = mBinding.phoneNumberEdt.text.toString().trim()
        val email = mBinding.emailEdt.text.toString().trim()
        val password = mBinding.passwordEdt.text.toString().trim()
        val confirmPassword = mBinding.confirmPasswordEdt.text.toString().trim()
        if (validate(fullName, phoneNumber, email, password, confirmPassword)) {
            pDialog!!.show()
            createFirebaseAccount(fullName, phoneNumber, email, password)
        }
    }

    companion object {
        const val EMPTY_STRING = ""
    }
}