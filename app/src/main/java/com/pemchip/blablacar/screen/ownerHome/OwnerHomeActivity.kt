package com.pemchip.blablacar.screen.ownerHome

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.pemchip.blablacar.R
import com.pemchip.blablacar.common.BaseActivity
import com.pemchip.blablacar.common.PcpUtils
import com.pemchip.blablacar.common.PreferenceKey
import com.pemchip.blablacar.model.TripModel
import com.pemchip.blablacar.screen.history.RideHistoryScreen
import com.pemchip.blablacar.screen.profile.UserProfileActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OwnerHomeActivity: BaseActivity(){
    private var mContext: Context? = null
    private var fromAddress: TextView? = null
    private var toAddress: TextView? = null
    private var date: TextView? = null
    private var time: TextView? = null
    private var amount: EditText? = null
    private var passenger: TextView? = null
    private var carNumber: EditText? = null
    private var searchBtn: Button? = null
    private var btn_history: LinearLayout? = null
    private var btn_profile: LinearLayout? = null
    private var hld_success_screen: RelativeLayout? = null
    private lateinit var database: DatabaseReference
    private var pDialog: ProgressDialog? = null
    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this;
        setContentView(R.layout.owner_home_screen)

        fromAddress = findViewById(R.id.fromAddress)
        toAddress = findViewById(R.id.toAddress)
        date = findViewById(R.id.date)
        time = findViewById(R.id.time)
        amount = findViewById(R.id.amount)
        passenger = findViewById(R.id.passenger)
        carNumber = findViewById(R.id.carNumber)
        searchBtn = findViewById(R.id.searchBtn)
        btn_history = findViewById(R.id.btn_history)
        btn_profile = findViewById(R.id.btn_profile)
        hld_success_screen = findViewById(R.id.hld_success_screen)

        pDialog = ProgressDialog(this)
        pDialog!!.setMessage("Loading")
        pDialog!!.setCanceledOnTouchOutside(false)

        checkPermission()

        searchBtn?.setOnClickListener{
            getLastLocation()
            if(isValidForm()){
                pDialog?.show()

                getLastLocation()
                var fromAddress = fromAddress!!.getText().toString()
                var toAddress = toAddress!!.getText().toString()
                var date = date!!.getText().toString()
                var time = time!!.getText().toString()
                var amount = amount!!.getText().toString()
                var passenger = passenger!!.getText().toString()
                var carNumber = carNumber!!.getText().toString()

                val mTripModel = TripModel()
                mTripModel.fromAddress = fromAddress
                mTripModel.toAddress = toAddress
                mTripModel.tripDate = date
                mTripModel.tripTime = time
                mTripModel.amount = amount
                mTripModel.passenger = passenger
                mTripModel.bookedSeatCount = "0"
                mTripModel.carDetails = carNumber
                mTripModel.bookingStatus = "NEW"
                mTripModel.userID = mPreferencesSession!!.userId
                mTripModel.userName = mPreferencesSession!!.getStringData(PreferenceKey.USER_NAME);
                mTripModel.latitude = PcpUtils.latitude.toString()
                mTripModel.longitude = PcpUtils.longitude.toString()
                mTripModel.bookedCustomerList = ArrayList<HashMap<String,String>>()

                database = Firebase.database.reference
                database.child("trip").child(System.currentTimeMillis().toString()).setValue(mTripModel).addOnCompleteListener(){
                    pDialog!!.dismiss()
                    hld_success_screen!!.visibility = View.VISIBLE
                    toast(mContext,"Ride posted successfully")
                    invalidateView()
                }.addOnFailureListener {
                    pDialog!!.dismiss()
                    toast(mContext,"Please try again...")
                }

            }
        }

        btn_profile?.setOnClickListener(){
            val intent = Intent(this@OwnerHomeActivity, UserProfileActivity::class.java)
            UserProfileActivity.fallbackScreen = "HOME"
            startActivity(intent)
        }

        btn_history ?. setOnClickListener (){
            val intent = Intent(this@OwnerHomeActivity, RideHistoryScreen::class.java)
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

        time?.setOnClickListener(){
            openTimePickerDialog()
        }

        passenger?.setOnClickListener(){
            selectPassengerDialog()
        }
    }

    private fun invalidateView() {
        fromAddress!!.setText("Leaving From")
        toAddress!!.setText("Going To")
        date!!.setText("Journey Date")
        time!!.setText("Journey Time")
        amount!!.getText().clear()
        passenger!!.setText("Passenger")
        carNumber!!.getText().clear()

        Thread {
            // Simulating some background work
            Thread.sleep(2000)  // Sleep for 2 seconds

            // Now post the UI update to the main thread
            handler.post {
                hld_success_screen?.visibility = View.GONE
            }
        }.start()
    }

    private fun isValidForm(): Boolean {

        var fromAddress = fromAddress!!.getText().toString()
        var toAddress = toAddress!!.getText().toString()
        var date = date!!.getText().toString()
        var time = time!!.getText().toString()
        var amount = amount!!.getText().toString()
        var passenger = passenger!!.getText().toString()
        var carNumber = carNumber!!.getText().toString()

        if (fromAddress.equals("Leaving From")) {
            toast(mContext, "Please enter a leaving from address");
        }else if (toAddress.equals("Going To")) {
            toast(mContext, "Please enter a going to address");
        } else if (date.equals("Journey Date")) {
            toast(mContext, "Please enter a travel date");
        } else if (time.equals("Journey Time")) {
            toast(mContext, "Please enter a travel time");
        } else if (passenger.equals("Passenger")) {
            toast(mContext, "Please enter a passenger count");
        } else if (PcpUtils.isEmptyStr(amount)) {
            toast(mContext, "Please enter a amount");
        }else if (PcpUtils.isEmptyStr(carNumber)) {
            toast(mContext, "Please enter a car details");
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

    private fun openTimePickerDialog() {
        // Get the current time (you can set a specific time if needed)
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // Create the TimePickerDialog
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                // This method will be called when the user selects a time
                val formattedTime = formatTime(hourOfDay, minute)
                time?.setText(formattedTime)
            },
            currentHour, currentMinute, false // true for 24-hour format
        )

        // Show the TimePickerDialog
        timePickerDialog.show()
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val hourIn12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val minuteFormatted = String.format("%02d", minute)
        return "$hourIn12:$minuteFormatted $amPm"
    }

    private fun selectPassengerDialog(){
        val options = arrayOf("1","2","3","4","5","6","7","8")

        var selectedItemIndex = -1
        AlertDialog.Builder(this)
            .setTitle("Choose an no of passenger")
            .setCancelable(true)
            .setSingleChoiceItems(options, selectedItemIndex) { dialog, which ->
                selectedItemIndex = which  // Update the selected item index
            }
            .setPositiveButton("OK") { dialog, _ ->
                if (selectedItemIndex != -1) {
                    passenger?.setText(options[selectedItemIndex])
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    fun convertDateToTimestamp(dateString: String): Long {
        // Define the date format of the input string (DD-MM-YYYY)
        val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        try {
            // Parse the string into a Date object
            val date = format.parse(dateString)

            // Convert the Date to a Unix timestamp (milliseconds since epoch)
            return date?.time ?: 0
        } catch (e: Exception) {
            // Handle parsing exception if the date string is invalid
            println("Error parsing date: ${e.message}")
            return 0
        }
    }
}