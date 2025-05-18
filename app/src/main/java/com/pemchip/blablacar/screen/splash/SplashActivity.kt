package com.pemchip.blablacar.screen.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.pemchip.blablacar.R
import com.pemchip.blablacar.common.BaseActivity
import com.pemchip.blablacar.common.CustomLog
import com.pemchip.blablacar.common.PreferenceKey
import com.pemchip.blablacar.screen.customerHome.CustomerHomeActivity
import com.pemchip.blablacar.screen.login.LoginActivity
import com.pemchip.blablacar.screen.login.SecurityCheckActivity
import com.pemchip.blablacar.screen.ownerHome.OwnerHomeActivity


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private val mHandler = Handler(Looper.getMainLooper())
    private var mContext: Context? = null

    override fun onDestroy() {
        super.onDestroy()
        mContext = null
        mHandler.removeCallbacksAndMessages(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.splash_activity)
        checkPermission()
        setUpView()
    }

    private fun setUpView() {
        if (mContext == null) return
        setupListeners()
    }

    private fun setupListeners() {
        if (mContext == null) return
        if (mHandler == null) return

        mHandler.postDelayed({
            if (mPreferencesSession!!.isLoggedIn) {
                val intent = Intent(this@SplashActivity, SecurityCheckActivity::class.java)
                startActivity(intent)
            } else {
                CustomLog.trace("Guest User")
                intent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            finish()
        }, 2000)
    }
}