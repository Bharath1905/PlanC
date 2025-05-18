package com.pemchip.blablacar.screen.customerHome

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.pemchip.blablacar.R
import com.pemchip.blablacar.common.BaseActivity
import com.pemchip.blablacar.common.PcpUtils
import com.pemchip.blablacar.screen.history.RideHistoryScreen
import com.pemchip.blablacar.screen.profile.UserProfileActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomerHomeActivity: BaseActivity(){
    private var mContext: Context? = null
    private var fromAddress: TextView? = null
    private var toAddress: TextView? = null
    private var date: TextView? = null
    private var searchBtn: Button? = null
    private var btn_history: LinearLayout? = null
    private var btn_profile: LinearLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this;
        setContentView(R.layout.customer_home_screen)

        fromAddress = findViewById(R.id.fromAddress)
        toAddress = findViewById(R.id.toAddress)
        date = findViewById(R.id.date)

        searchBtn = findViewById(R.id.searchBtn)
        btn_history = findViewById(R.id.btn_history)
        btn_profile = findViewById(R.id.btn_profile)

        searchBtn?.setOnClickListener{
            if(isValidForm()){
                TripListActivity.fromAddress = fromAddress!!.text.toString()
                TripListActivity.toAddress = toAddress!!.text.toString()
                TripListActivity.tripDate = date!!.text.toString()
                val intent = Intent(this@CustomerHomeActivity, TripListActivity::class.java)
                startActivity(intent)
            }
        }

        btn_profile?.setOnClickListener(){
            val intent = Intent(this@CustomerHomeActivity, UserProfileActivity::class.java)
            UserProfileActivity.fallbackScreen = "HOME"
            startActivity(intent)
        }

        btn_history ?. setOnClickListener (){
            val intent = Intent(this@CustomerHomeActivity, RideHistoryScreen::class.java)
            UserProfileActivity.fallbackScreen = "HISTORY"
            startActivity(intent)
        }

        fromAddress?.setOnClickListener(){
            routeSelection("FROM")
        }

        toAddress?.setOnClickListener(){
            routeSelection("TO")
        }

        date?.setOnClickListener(){
            showDatePickerDialog()
        }
    }

    private fun isValidForm(): Boolean {

        var fromAddress = fromAddress!!.getText().toString()
        var toAddress = toAddress!!.getText().toString()
        var mDate = date!!.getText().toString()

        if (fromAddress.equals("Leaving From")) {
            toast(mContext, "Please enter a leaving from address");
        }else if (toAddress.equals("Going To")) {
            toast(mContext, "Please enter a going to address");
        }else if (mDate.equals("Select date")) {
            toast(mContext, "Please enter a journy date");
        } else {
            return true;
        }

        return false;
    }

    private fun routeSelection(type: String) {

        val options = arrayOf("Chennai", "Coimbatore", "Madurai", "Tiruchirappalli", "Salem", "Tirunelveli",
            "Erode", "Vellore", "Thoothukudi", "Dindigul", "Karur", "Kanchipuram", "Cuddalore",
            "Villupuram", "Karaikal", "Nagercoil", "Chidambaram", "Pollachi", "Sivakasi", "Ramanathapuram")

        var selectedItemIndex = -1
        AlertDialog.Builder(this)
            .setTitle("Choose an Option")
            .setCancelable(true)
            .setSingleChoiceItems(options, selectedItemIndex) { dialog, which ->
                selectedItemIndex = which  // Update the selected item index
            }
            .setPositiveButton("OK") { dialog, _ ->
                if (selectedItemIndex != -1) {
                    if(type.equals("FROM")){
                        fromAddress?.setText(options[selectedItemIndex])
                    }else{
                        toAddress?.setText(options[selectedItemIndex])
                    }

                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showDatePickerDialog() {
        // Get the current date
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog instance
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                // Format the selected date
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                calendar.set(year, monthOfYear, dayOfMonth)
                date?.setText(dateFormat.format(calendar.time))
            },
            currentYear,
            currentMonth,
            currentDay
        )

        // Disable past dates
        val calendarForMinDate = Calendar.getInstance()
        datePickerDialog.datePicker.minDate = calendarForMinDate.timeInMillis

        // Show the DatePickerDialog
        datePickerDialog.setTitle("Select date")
        datePickerDialog.setCancelable(true)
        datePickerDialog.show()
    }
}