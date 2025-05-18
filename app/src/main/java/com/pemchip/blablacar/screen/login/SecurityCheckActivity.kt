package com.pemchip.blablacar.screen.login

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.pemchip.blablacar.common.BaseActivity
import com.pemchip.blablacar.common.CustomLog
import com.pemchip.blablacar.common.PreferenceKey
import com.pemchip.blablacar.databinding.SecurityPinScreenBinding
import com.pemchip.blablacar.screen.customerHome.CustomerHomeActivity
import com.pemchip.blablacar.screen.ownerHome.OwnerHomeActivity

class SecurityCheckActivity : BaseActivity() {

    private val mHandler = Handler(Looper.getMainLooper())
    private var mContext: Context? = null
    private lateinit var mBinding: SecurityPinScreenBinding
    private var pDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        mBinding = SecurityPinScreenBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)

        val oldPin = mPreferencesSession!!.getStringData(PreferenceKey.SECURITY_PIN)
        if(oldPin.equals("")){
            mBinding.securityCodeEdt.hint = "Create a new pin"
            mBinding.confirmBtn.text = "Confirm"
        }else{
            mBinding.securityCodeEdt.hint = "Pin"
            mBinding.confirmBtn.text = "Verify"
        }

        mBinding.confirmBtn.setOnClickListener() {

            val pin = mBinding.securityCodeEdt.text.toString()
            if (pin.equals("")) {
                toast(mContext, "Please enter a pin number")
            } else {

                val oldPin = mPreferencesSession!!.getStringData(PreferenceKey.SECURITY_PIN)

                if(oldPin.equals("")){
                    pDialog!!.show()
                    toast(mContext, "Security pin created successfully")
                    mPreferencesSession!!.saveStringData(PreferenceKey.SECURITY_PIN,pin)
                    excuteTargetActivity()
                }else{
                    if(pin.equals(oldPin)){
                        pDialog!!.show()
                        toast(mContext, "Security pin verified successfully")
                        excuteTargetActivity()
                    }else{
                        toast(mContext, "Please enter a valid pin number")
                    }
                }
            }
        }
    }

    private fun excuteTargetActivity() {
        mHandler.postDelayed({
        if (mPreferencesSession!!.isLoggedIn) {
            CustomLog.trace("LoggedIn User")
            if (mPreferencesSession!!.userRole == PreferenceKey.CUSTOMER_ROLE) {
                CustomLog.trace("Customer")
                val intent = Intent(this@SecurityCheckActivity, CustomerHomeActivity::class.java)
                startActivity(intent)
            } else {
                CustomLog.trace("Owner")
                val intent = Intent(this@SecurityCheckActivity, OwnerHomeActivity::class.java)
                startActivity(intent)
            }
        } else {
            CustomLog.trace("Guest User")
            intent = Intent(this@SecurityCheckActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        finish()
        }, 2000)
    }
}