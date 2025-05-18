package com.pemchip.blablacar.common

import android.Manifest
import android.R
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

abstract class BaseActivity : AppCompatActivity() {

    protected var mFirestoreDB: FirebaseFirestore? = null
    protected var mPreferencesSession: PreferencesSession? = null
    protected var mProcessLoader: ProcessLoaderDialogue? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (mPreferencesSession == null) {
            mPreferencesSession = PreferencesSession.getInstance(this)
        }

        if (mFirestoreDB == null) {
            mFirestoreDB = FirebaseFirestore.getInstance()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    fun toast(mContext: Context?, message: String?) {
        if (mContext == null) return
        val toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.show()
    }

    fun isConnectingToInternet(): Boolean {
        val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity != null) {
            val info = connectivity.allNetworkInfo
            if (info != null) for (i in info.indices) if (info[i].state == NetworkInfo.State.CONNECTED) {
                return true
            }
        }
        return false
    }

    fun noInternetAlertDialog() {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_dialog_alert)
            .setTitle("No internet!")
            .setMessage("Please check your network connection.")
            .setCancelable(true)
            .show()
    }

    fun showProgressDialog() {
        if (mProcessLoader == null) {
            mProcessLoader = ProcessLoaderDialogue(this)
        }
        mProcessLoader!!.setCancelable(true)
        mProcessLoader!!.show()
    }

    fun hideProgressDialog() {
        try {
            if (mProcessLoader != null) {
                if (mProcessLoader!!.isShowing) {
                    mProcessLoader!!.dismiss()
                }
            }
        } catch (e: Exception) {
            CustomLog.trace("hideProgressDialog: Error: " + e.message)
        }
    }

    fun showAlertDialogue(message: String) {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_dialog_alert)
            .setTitle("Alert!")
            .setMessage(message)
            .setCancelable(true)
            .show()
    }

    fun getCurrentFormattedDate(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return currentDate.format(formatter)
    }

    fun compareDates(currentDate: String, emiStartDate: String): String{
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        val mCurrentDate = LocalDate.parse(currentDate, formatter)
        val mEMIDate = LocalDate.parse(emiStartDate, formatter)

        // Compare the two dates
        if (mEMIDate.isBefore(mCurrentDate)) {
            return "BEFORE"
        } else if (mEMIDate.isAfter(mCurrentDate)) {
            return "AFTER"
        } else {
            return "SAME"
        }
    }

    public fun checkPermission() {

        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            AlertDialog.Builder(this)
                .setTitle("Location Permission Needed")
                .setMessage("This app needs the Location permission, please accept to use location functionality")
                .setPositiveButton(
                    "OK"
                ) { _, _ ->
                    //Prompt the user once explanation has been shown
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ),
                        MY_PERMISSIONS_REQUEST_LOCATION
                    )
                }
                .create()
                .show()
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        getLastLocation()
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()

                    // Check if we are in a state where the user has denied the permission and
                    // selected Don't ask again
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.packageName, null),
                            ),
                        )
                    }
                }
                return
            }
        }
    }

    public fun getLastLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return       }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                PcpUtils.latitude = location.latitude
                PcpUtils.longitude = location.longitude
            } else {
                Toast.makeText(this, "Location is null", Toast.LENGTH_SHORT).show()
            }
        }
    }

}