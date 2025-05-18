package com.pemchip.blablacar.screen.customerHome

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.google.common.reflect.TypeToken
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.pemchip.blablacar.R
import com.pemchip.blablacar.common.BaseActivity
import com.pemchip.blablacar.common.Constants
import com.pemchip.blablacar.common.CustomLog
import com.pemchip.blablacar.common.PcpUtils
import com.pemchip.blablacar.common.PreferenceKey
import com.pemchip.blablacar.model.TripModel
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import kotlin.math.round

class TripDetailsActivity: BaseActivity() {

    private var mContext: Context? = null
    private var mDatabase: DatabaseReference? = null
    private var gridLayout: GridLayout? = null
    private var routes_gridLayout: GridLayout? = null
    private var hld_no_bookings: TextView? = null
    private var trackBtn: Button? = null
    private var rideCompletedBtn: Button? = null
    private var actionBtn: LinearLayout? = null
    private var mRoutesMap: HashMap<String, List<String>>? = null
    private var bookedCustomerList = ArrayList<HashMap<String,String>>()
    private var container: LinearLayout? = null
    private var hldTravellerView: RelativeLayout? = null
    private var cancelBtn: Button? = null
    private var confirmBtn: Button? = null
    private var mCustomerRequiredSeat = 0

    private var inputNameTextList = mutableListOf<EditText>()
    private var inputAgeTextList = mutableListOf<EditText>()
    private var inputGenderTextList = mutableListOf<EditText>()

    override fun onDestroy() {
        super.onDestroy()
        mContext = null
    }

    companion object{
        lateinit var mTripModel: TripModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.ride_details_screen)
        container = findViewById(R.id.mainView);
        hldTravellerView = findViewById(R.id.hldTravellerView);
        cancelBtn = findViewById(R.id.cancelBtn);
        confirmBtn = findViewById(R.id.confirmBtn);

        // Reading JSON data from assets
        val mRoutesMapStr = loadJSONFromAsset("data/routesv2.json")
        val gson = Gson()
        val type = object : TypeToken<HashMap<String, List<String>>>() {}.type
        mRoutesMap = gson.fromJson(mRoutesMapStr, type)

        initFirebaseDatabase()
        setUpView()

        cancelBtn!!.setOnClickListener(){
            hldTravellerView!!.visibility = View.GONE
        }

        confirmBtn!!.setOnClickListener(){

            val nameList = inputNameTextList.map { it.text.toString() }
            val ageList = inputAgeTextList.map { it.text.toString() }
            val genderList = inputGenderTextList.map { it.text.toString() }
            CustomLog.trace("nameList: "+nameList)
            CustomLog.trace("ageList: "+ageList)
            CustomLog.trace("genderList: "+genderList)

            var isValid = true;

            for (item in nameList){
                if(item.isEmpty()){
                    isValid = false;
                }
            }

            for (item in ageList){
                if(item.isEmpty()){
                    isValid = false;
                }
            }

            if(isValid){
                bookSeats(mCustomerRequiredSeat);
            }else{
                toast(mContext, "please fill all details")
            }

        }

    }

    private fun initFirebaseDatabase() {
        mDatabase = FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()
    }

    private fun setUpView() {
        if (mContext == null) return

        var mTotalBookedSeats = 0
        val name = findViewById<android.widget.TextView?>(R.id.name)
        val cardDetails = findViewById<android.widget.TextView?>(R.id.cardDetails)
        val amount = findViewById<android.widget.TextView?>(R.id.amount)
        val fromAddress = findViewById<android.widget.TextView?>(R.id.fromAddress)
        val toAddress = findViewById<android.widget.TextView?>(R.id.toAddress)
        val passenger = findViewById<android.widget.TextView?>(R.id.passenger)
        val dateAndTime = findViewById<android.widget.TextView?>(R.id.dateAndTime)
        val bookingStatus = findViewById<android.widget.TextView?>(R.id.bookingStatus)
        val bookingCount = findViewById<android.widget.TextView?>(R.id.bookingCount)
        val bookingBtn = findViewById<android.widget.Button?>(R.id.bookingBtn)
        val cancelRide = findViewById<android.widget.TextView?>(R.id.cancelRide)
        gridLayout = findViewById<android.widget.GridLayout?>(R.id.gridLayout)
        routes_gridLayout = findViewById<android.widget.GridLayout?>(R.id.routes_gridLayout)
        hld_no_bookings = findViewById<android.widget.TextView?>(R.id.hld_no_bookings)
        trackBtn = findViewById<android.widget.Button?>(R.id.trackBtn)
        actionBtn = findViewById<android.widget.LinearLayout?>(R.id.actionBtn)
        rideCompletedBtn = findViewById<android.widget.Button?>(R.id.rideCompletedBtn)



        name.setText(mTripModel.userName)
        cardDetails.setText(mTripModel.carDetails)
        amount.setText("₹ "+mTripModel.amount)
        fromAddress.setText(mTripModel.fromAddress)
        toAddress.setText(mTripModel.toAddress)
        passenger.setText(mTripModel.passenger+" Seats")
        dateAndTime.setText(mTripModel.tripDate+", "+mTripModel.tripTime)

        if(mTripModel.bookedCustomerList!=null){
            bookedCustomerList = (mTripModel.bookedCustomerList as ArrayList<HashMap<String, String>>?)!!

            for (mCustomerHasMap in bookedCustomerList){
                val mCustomerMap = mCustomerHasMap
                if(mCustomerMap.get("customerBookingStatus").equals("CONFIRMED")){
                    val allottedSeats = mCustomerMap.get("customerRequiredSeats")?.toInt()
                    mTotalBookedSeats = mTotalBookedSeats + allottedSeats!!
                }
            }
        }

        if (mPreferencesSession!!.userRole == PreferenceKey.CUSTOMER_ROLE) {
            bookingCount.visibility = View.GONE
            bookingStatus.visibility = View.GONE
            bookingBtn.visibility = View.VISIBLE
            cancelRide.visibility = View.GONE

            if(bookedCustomerList.size > 0){
                for (mCustomerHasMap in bookedCustomerList){
                    val mCustomerMap = mCustomerHasMap
                    if(mCustomerMap.get("customerID").equals(mPreferencesSession?.userId)){
                        bookingBtn.visibility = View.GONE
                        bookingStatus.visibility = View.VISIBLE
                        if(mCustomerMap.get("customerBookingStatus").equals("PENDING")){
                            bookingStatus.setText("Request Sent")
                        }else{
                            bookingStatus.setText("Booked")
                        }
                    }
                }

                renderGridLayout(bookedCustomerList)
            }
        }else{

            cancelRide.visibility = View.VISIBLE
            if(mTripModel.bookingStatus.equals("NEW")){
                bookingCount.visibility = View.VISIBLE
                if(bookedCustomerList.size>0){
                    bookingCount.setText(bookedCustomerList.size.toString()+" Bookings")
                    renderGridLayout(bookedCustomerList)
                    cancelRide.visibility = View.GONE
                }else{
                    bookingCount.setText("No Bookings")
                    hld_no_bookings?.visibility = View.VISIBLE
                }
            }else{
                bookingStatus.setText("Ride Completed")
                if(mTripModel.bookingStatus.equals("CANCELED")){
                    bookingStatus.setText("Ride Canceled")
                }else if(mTripModel.bookingStatus.equals("COMPLETED")){
                    bookingStatus.setText("Ride Completed")
                    renderGridLayout(bookedCustomerList)
                }
                bookingStatus.visibility = View.VISIBLE
                cancelRide.visibility = View.GONE
            }
        }

        val result = compareDates(getCurrentFormattedDate(), mTripModel.tripDate.toString())
        if(result.equals("BEFORE")){
            bookingCount.visibility = View.GONE
            bookingBtn.visibility = View.GONE
            actionBtn!!.visibility = View.GONE
        }


        cancelRide.setOnClickListener(){

            val builder = AlertDialog.Builder(mContext)
            builder.setMessage("Are you sure, want to cancel this ride?")
                .setCancelable(true) // Disable closing the dialog by tapping outside
                .setPositiveButton("Yes") { dialog, id ->
                    dialog.dismiss()

                    val database = FirebaseDatabase.getInstance()
                    val usersRef = database.getReference("trip") // reference to 'users' node
                    val userRef = usersRef.child(mTripModel.tripID.toString())

                    val updates = mapOf<String, String>(
                        "bookingStatus" to "CANCELED"
                    )

                    // Update the user's age in the database
                    userRef.updateChildren(updates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(mContext, "Booking canceled successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(mContext, "Try again!!", Toast.LENGTH_SHORT).show()
                            }
                        }

                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            builder.create().show()
        }

        bookingBtn.setOnClickListener{

            val availAbleSeats = mTripModel.passenger!!.toInt() - mTripModel.bookedSeatCount!!.toInt()
            val bookedSeats = mTotalBookedSeats + 1
            val seatAmount = mTripModel.amount!!.toDouble()
            val seatPrice = (seatAmount/bookedSeats)


            AlertDialog.Builder(mContext)
                .setTitle("Seat Details")
                .setMessage("Available Seat: "+availAbleSeats+", Seat Price: "+ round(seatPrice).toString()+", Note: Seat price may vary at end of the trip based on passengers traveled.")
                .setCancelable(true)
                .setPositiveButton("OK") { dialog, _ ->

                    val options = arrayOf("1","2","3","4","5","6","7","8")

                    var selectedItemIndex = -1
                    AlertDialog.Builder(mContext)
                        .setTitle("Choose an no of seats")
                        .setCancelable(true)
                        .setSingleChoiceItems(options, selectedItemIndex) { dialog, which ->
                            selectedItemIndex = which  // Update the selected item index
                        }
                        .setPositiveButton("OK") { dialog, _ ->

                            if (selectedItemIndex != -1)
                            {
                                val requiredSeats = options[selectedItemIndex].toInt()
                                if(requiredSeats <= availAbleSeats){
                                    mCustomerRequiredSeat = requiredSeats
                                    hldTravellerView!!.visibility = View.VISIBLE
                                    container!!.removeAllViews()
                                    inputNameTextList = mutableListOf<EditText>()
                                    inputAgeTextList = mutableListOf<EditText>()
                                    inputGenderTextList = mutableListOf<EditText>()
                                    for (i in 0 until requiredSeats) {
                                        val fieldView = layoutInflater.inflate(R.layout.dynamic_value, container, false)
                                        val label = fieldView.findViewById<TextView>(R.id.travellerText)
                                        val nameEditText = fieldView.findViewById<EditText>(R.id.fullnameEdt)
                                        val ageEditText = fieldView.findViewById<EditText>(R.id.ageEdt)
                                        val genderEdt = fieldView.findViewById<EditText>(R.id.genderEdt)
                                        genderEdt.setText("Male")
                                        val maleRadioButton = fieldView.findViewById<RadioButton>(R.id.maleRadioButton)
                                        val femaleRadioButton = fieldView.findViewById<RadioButton>(R.id.femaleRadioButton)
                                        label.text = "Traveller "+ (i+1)

                                        maleRadioButton.setOnClickListener(){
                                            genderEdt.setText("Male")
                                        }

                                        femaleRadioButton.setOnClickListener(){
                                            genderEdt.setText("Female")
                                        }

                                        inputNameTextList.add(nameEditText)
                                        inputAgeTextList.add(ageEditText)
                                        inputGenderTextList.add(genderEdt)

                                        container!!.addView(fieldView)
                                    }
                                    //bookSeats(requiredSeats);
                                }else{
                                    Toast.makeText(mContext, "Available seats "+availAbleSeats+" only", Toast.LENGTH_SHORT).show()
                                }
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }.show()

                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }

        renderRoutes()

        setUpListener()
    }

    private fun renderGridLayout(bookedCustomerList: ArrayList<HashMap<String, String>>) {

        var INDEX = -1
        for (customerList in bookedCustomerList) {
            INDEX++
            val localIndex = INDEX
            val mLineItem = LayoutInflater.from(mContext)
                .inflate(R.layout.booking_customer_line_item, gridLayout, false)
            val name = mLineItem.findViewById<TextView>(R.id.name)
            val seats = mLineItem.findViewById<TextView>(R.id.seats)
            val gender = mLineItem.findViewById<TextView>(R.id.gender)
            val status = mLineItem.findViewById<TextView>(R.id.status)
            val confirmBtn = mLineItem.findViewById<Button>(R.id.confirmBtn)
            val seatsDetails = mLineItem.findViewById<TextView>(R.id.seatsDetails)

            var nameList = customerList.get("customerBookingNameList")
            nameList = nameList!!.replace("<@>","\n")
            seatsDetails.setText(nameList)

            name.setText(customerList.get("customerName"))
            gender.setText(customerList.get("customerGender"))
            if(customerList.get("customerGender").equals("Male",true)){
                gender.setBackgroundColor(mContext!!.resources.getColor(R.color.purple_700))
            }else{
                gender.setBackgroundColor(mContext!!.resources.getColor(R.color.pink))
            }
            seats.setText("Required Seats: "+customerList.get("customerRequiredSeats"))

            if(customerList.get("customerBookingStatus").equals("CONFIRMED")){
                seats.setText("Booked Seats: "+customerList.get("customerRequiredSeats"))
                confirmBtn.visibility = View.GONE

                if(mTripModel.bookingStatus.equals("COMPLETED")){
                    status.visibility = View.VISIBLE
                    var totalBookedSeatCount = mTripModel.bookedSeatCount!!.toDouble()
                    var amount = mTripModel.amount!!.toDouble()
                    var finalAmount = amount/totalBookedSeatCount
                    status.setText("Per Seat Rs: "+ round(finalAmount))
                    actionBtn!!.visibility = View.GONE
                }else{
                    status.visibility = View.VISIBLE
                    actionBtn!!.visibility = View.VISIBLE
                }

            }else{
                confirmBtn.visibility = View.VISIBLE
                status.visibility = View.GONE
            }

            confirmBtn.setOnClickListener{

                showProgressDialog()

                val database = FirebaseDatabase.getInstance()
                val usersRef = database.getReference("trip") // reference to 'users' node
                val userRef = usersRef.child(mTripModel.tripID.toString()).child("bookedCustomerList")

                // Update the user's age in the database
                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // Get the current list of booked customers
                            val currentList = dataSnapshot.getValue(object : GenericTypeIndicator<List<HashMap<String,String>>>() {})

                            currentList?.let {

                                val updates = mapOf<String, Any>(
                                    "customerBookingStatus" to "CONFIRMED"
                                )

                                userRef.child(localIndex.toString()).updateChildren(updates)
                                    .addOnSuccessListener {
                                        toast(mContext, "Booking confirmed successfully")
                                        finish()
                                    }
                                    .addOnFailureListener { error ->
                                        hideProgressDialog()
                                    }
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        toast(mContext,"Try again")
                        hideProgressDialog()
                    }
                })

            }

            if (mPreferencesSession!!.userRole == PreferenceKey.CUSTOMER_ROLE) {
                confirmBtn.visibility = View.GONE
                actionBtn!!.visibility = View.GONE
                var customerID = customerList.get("customerID")
                var status = mTripModel.bookingStatus
                if(status.equals("COMPLETED")){
                    if(customerID.equals(mPreferencesSession!!.userId)){
                        gridLayout!!.addView(mLineItem)
                        hld_no_bookings?.visibility = View.GONE
                    }
                }else{
                    gridLayout!!.addView(mLineItem)
                    hld_no_bookings?.visibility = View.GONE
                }
            }else{
                gridLayout!!.addView(mLineItem)
                hld_no_bookings?.visibility = View.GONE
            }
        }

    }

    private fun renderRoutes() {
        if(mRoutesMap==null)return

        val mFromAddress = mTripModel.fromAddress!!.toLowerCase(Locale.ROOT)
        val mToAddress = mTripModel.toAddress!!.toLowerCase(Locale.ROOT)

        CustomLog.trace("mFromAddress: "+mFromAddress)
        CustomLog.trace("mToAddress: "+mToAddress)

        var strightKey = mFromAddress+"<@>"+mToAddress
        var reverseKey = mToAddress+"<@>"+mFromAddress

        CustomLog.trace("strightKey: "+strightKey)
        CustomLog.trace("reverseKey: "+reverseKey)

        var citiesList1: List<String> ?=null
        if(mRoutesMap!!.containsKey(strightKey)){
            citiesList1 = mRoutesMap!![strightKey]
        }

        var citiesList2: List<String> ?=null
        if(mRoutesMap!!.containsKey(reverseKey)){
            citiesList2 = mRoutesMap!![reverseKey]
        }

        CustomLog.trace("citiesList1: "+citiesList1)
        CustomLog.trace("citiesList2: "+citiesList2)

        if(citiesList1!=null && citiesList1!!.size>0){

            for (item in citiesList1){

                val mLineItem = LayoutInflater.from(mContext)
                    .inflate(R.layout.route_line_item, routes_gridLayout, false)
                val route = mLineItem.findViewById<TextView>(R.id.route)
                route.setText(item)
                routes_gridLayout!!.addView(mLineItem)

            }
        }

        if(citiesList2!=null && citiesList2!!.size>0){

            for (item in citiesList2){

                val mLineItem = LayoutInflater.from(mContext)
                    .inflate(R.layout.route_line_item, routes_gridLayout, false)
                val route = mLineItem.findViewById<TextView>(R.id.route)
                route.setText(item)
                routes_gridLayout!!.addView(mLineItem)

            }
        }

    }

    private fun setUpListener() {
        if (mContext == null) return

        trackBtn!!.setOnClickListener {

            val gmmIntentUri = Uri.parse("geo:0,0?q=Chennai")

            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
            }

            // Check if there's an app to handle the intent
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
            }
        }

        rideCompletedBtn!!.setOnClickListener(){

            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("trip") // reference to 'users' node
            val userRef = usersRef.child(mTripModel.tripID.toString())

            val updates = mapOf<String, String>(
                "bookingStatus" to "COMPLETED"
            )

            // Update the user's age in the database
            userRef.updateChildren(updates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(mContext, "Ride completed successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(mContext, "Try again!!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun loadJSONFromAsset(fileName: String): String? {
        var json: String? = null
        try {
            val inputStream: InputStream = assets.open(fileName)
            json = inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return json
    }

    private fun bookSeats(requiredSeats: Int) {

        val nameList = inputNameTextList.map { it.text.toString() }
        val ageList = inputAgeTextList.map { it.text.toString() }
        val genderList = inputGenderTextList.map { it.text.toString() }

        var formattedOutPut = ""
        for (i in nameList.indices) {
            val output = "${i + 1}. ${nameList[i]}(${ageList[i]})(${genderList[i]})"
            if(formattedOutPut.isEmpty()){
                formattedOutPut += output
            }else{
                formattedOutPut += "<@>"+output
            }
        }

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("trip") // reference to 'users' node
        val userRef = usersRef.child(mTripModel.tripID.toString())

        val customerHashMap = mapOf<String, String>(
            "customerID" to mPreferencesSession!!.userId,
            "customerName" to mPreferencesSession!!.userName,
            "customerGender" to mPreferencesSession!!.userGender,
            "customerLatitude" to PcpUtils.latitude.toString(),
            "customerLongitude" to PcpUtils.longitude.toString(),
            "customerRequiredSeats" to requiredSeats.toString(),
            "customerBookingStatus" to "PENDING",
            "customerBookingNameList" to formattedOutPut,
        )

        val arrayList = ArrayList<HashMap<String,String>>()
        arrayList.add(customerHashMap as HashMap<String, String>)
        bookedCustomerList.addAll(arrayList)

        var previousBookedCount = mTripModel.bookedSeatCount!!.toInt()
        var finalBookedCount = previousBookedCount + requiredSeats

        val updates = mapOf<String, Any>(
            "bookedCustomerList" to bookedCustomerList,
            "bookedSeatCount" to finalBookedCount.toString()
        )

        // Update the user's age in the database
        userRef.updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mTripModel.bookedCustomerList = bookedCustomerList
                    Toast.makeText(mContext, "Booked successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // Failed to update
                    println("Failed to update user age: ${task.exception?.message}")
                }
            }
    }
}