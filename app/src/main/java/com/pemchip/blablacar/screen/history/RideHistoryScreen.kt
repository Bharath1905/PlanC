package com.pemchip.blablacar.screen.history

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.pemchip.blablacar.R
import com.pemchip.blablacar.common.BaseActivity
import com.pemchip.blablacar.common.Constants
import com.pemchip.blablacar.common.PreferenceKey
import com.pemchip.blablacar.model.TripModel
import com.pemchip.blablacar.screen.customerHome.CustomerHomeActivity
import com.pemchip.blablacar.screen.ownerHome.OwnerHomeActivity
import com.pemchip.blablacar.screen.profile.UserProfileActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RideHistoryScreen: BaseActivity() {

    private var mContext: Context? = null
    private var newRideRadioButton: RadioButton? = null
    private var finishedRideRadioButton: RadioButton? = null
    private var newRideRecyclerview: RecyclerView? = null
    private var finishedRideRecyclerview: RecyclerView? = null
    private var hld_no_data: FrameLayout? = null
    private var mDatabase: DatabaseReference? = null
    private var mNewTripModelList: List<TripModel>? = null
    private var mFinishedTripModelList: List<TripModel>? = null

    private var btn_home: LinearLayout? = null
    private var btn_history: LinearLayout? = null
    private var btn_profile: LinearLayout? = null

    companion object{
        var fallbackScreen: String = ""
    }

    override fun onDestroy() {
        super.onDestroy()
        mContext = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.ride_history_screen)

        initFirebaseDatabase()
        setUpView()
    }

    private fun initFirebaseDatabase() {
        mDatabase = FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()
    }

    private fun setUpView() {
        if (mContext == null) return

        newRideRadioButton = findViewById(R.id.newRideRadioButton)
        finishedRideRadioButton = findViewById(R.id.finishedRideRadioButton)
        newRideRecyclerview = findViewById(R.id.newRideRecyclerview)
        finishedRideRecyclerview = findViewById(R.id.finishedRideRecyclerview)
        hld_no_data = findViewById(R.id.hld_no_data)

        btn_home = findViewById(R.id.btn_home)
        btn_history = findViewById(R.id.btn_history)
        btn_profile = findViewById(R.id.btn_profile)

        getRideHistoryList()
        setUpListener()
    }

    private fun setUpListener() {
        if (mContext == null) return

        newRideRadioButton!!.setOnClickListener {
            if (newRideRadioButton!!.isChecked) {
                renderRideList()
            }
        }

        finishedRideRadioButton!!.setOnClickListener {
            if (finishedRideRadioButton!!.isChecked) {
                renderFinishedRideList()
            }
        }

        btn_home?.setOnClickListener(){
            if(fallbackScreen.equals("HOME")){
                finish()
            }else{
                if (mPreferencesSession!!.userRole == PreferenceKey.CUSTOMER_ROLE) {
                    val intent = Intent(this@RideHistoryScreen, CustomerHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@RideHistoryScreen, OwnerHomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        btn_profile?.setOnClickListener{
            if(UserProfileActivity.fallbackScreen.equals("PROFILE")){
                finish()
            }else{
                val intent = Intent(this@RideHistoryScreen, UserProfileActivity::class.java)
                UserProfileActivity.fallbackScreen = "HISTORY"
                startActivity(intent)
            }
        }
    }

    private fun getRideHistoryList() {
        if (mContext == null) return
        if (!isConnectingToInternet()) {
            noInternetAlertDialog()
            return
        }

        showProgressDialog()

        if (mPreferencesSession!!.userRole == PreferenceKey.CUSTOMER_ROLE) {
            mDatabase?.child("trip")?.addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mNewTripModelList = ArrayList()
                    mFinishedTripModelList = ArrayList()
                    if (dataSnapshot.children.count() > 0) {
                        for (userSnapshot in dataSnapshot.getChildren()) {

                            var mTripModel: TripModel? = TripModel()
                            mTripModel!!.tripID = userSnapshot.key
                            mTripModel!!.fromAddress = userSnapshot.child("fromAddress").getValue(String::class.java)
                            mTripModel!!.toAddress = userSnapshot.child("toAddress").getValue(String::class.java)
                            mTripModel!!.tripDate = userSnapshot.child("tripDate").getValue(String::class.java)
                            mTripModel!!.tripTime = userSnapshot.child("tripTime").getValue(String::class.java)
                            mTripModel!!.passenger = userSnapshot.child("passenger").getValue(String::class.java)
                            mTripModel!!.bookedSeatCount = userSnapshot.child("bookedSeatCount").getValue(String::class.java)
                            mTripModel!!.amount = userSnapshot.child("amount").getValue(String::class.java)
                            mTripModel!!.carDetails = userSnapshot.child("carDetails").getValue(String::class.java)
                            mTripModel!!.bookingStatus = userSnapshot.child("bookingStatus").getValue(String::class.java)
                            mTripModel!!.latitude = userSnapshot.child("latitude").getValue(String::class.java)
                            mTripModel!!.longitude = userSnapshot.child("longitude").getValue(String::class.java)
                            mTripModel!!.userID = userSnapshot.child("userID").getValue(String::class.java)
                            mTripModel!!.userName = userSnapshot.child("userName").getValue(String::class.java)
                            mTripModel!!.bookedCustomerList = userSnapshot.child("bookedCustomerList")
                                .getValue(object : GenericTypeIndicator<List<HashMap<String, String>>>() {}) as List<HashMap<String, String>>?

                            if(mTripModel!=null)
                            {
                                var bookedCustomerList = ArrayList<HashMap<String,String>>()
                                if(mTripModel.bookedCustomerList!=null){
                                    bookedCustomerList = (mTripModel.bookedCustomerList as ArrayList<HashMap<String, String>>?)!!
                                }

                                if (bookedCustomerList.size > 0) {
                                    for (mCustomerHasMap in bookedCustomerList) {
                                        val mCustomerMap = mCustomerHasMap
                                        if (mCustomerMap.get("customerID")
                                                .equals(mPreferencesSession?.userId)
                                        ) {
                                            if (mTripModel!!.bookingStatus.equals("CANCELED")) {
                                                (mFinishedTripModelList as ArrayList<TripModel>).add(
                                                    mTripModel!!
                                                )
                                            } else if (mTripModel!!.bookingStatus.equals("COMPLETED")) {
                                                (mFinishedTripModelList as ArrayList<TripModel>).add(
                                                    mTripModel!!
                                                )
                                            } else {
                                                val result = compareDates(
                                                    getFormattedCurrentDate(),
                                                    mTripModel.tripDate.toString()
                                                )
                                                if (result.equals("BEFORE")) {
                                                    (mFinishedTripModelList as ArrayList<TripModel>).add(
                                                        mTripModel!!
                                                    )
                                                } else {
                                                    (mNewTripModelList as ArrayList<TripModel>).add(
                                                        mTripModel!!
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    renderRideList()
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    hideProgressDialog()
                    toast(mContext,"Try again")
                }
            })
        }else{
            mDatabase?.child("trip")?.orderByChild("userID")?.equalTo(mPreferencesSession!!.userId)?.addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    mNewTripModelList = ArrayList()
                    mFinishedTripModelList = ArrayList()
                    for (userSnapshot in dataSnapshot.getChildren()) {

                        var mTripModel: TripModel? = TripModel()
                        mTripModel!!.tripID = userSnapshot.key
                        mTripModel!!.fromAddress = userSnapshot.child("fromAddress").getValue(String::class.java)
                        mTripModel!!.toAddress = userSnapshot.child("toAddress").getValue(String::class.java)
                        mTripModel!!.tripDate = userSnapshot.child("tripDate").getValue(String::class.java)
                        mTripModel!!.tripTime = userSnapshot.child("tripTime").getValue(String::class.java)
                        mTripModel!!.passenger = userSnapshot.child("passenger").getValue(String::class.java)
                        mTripModel!!.bookedSeatCount = userSnapshot.child("bookedSeatCount").getValue(String::class.java)
                        mTripModel!!.amount = userSnapshot.child("amount").getValue(String::class.java)
                        mTripModel!!.carDetails = userSnapshot.child("carDetails").getValue(String::class.java)
                        mTripModel!!.bookingStatus = userSnapshot.child("bookingStatus").getValue(String::class.java)
                        mTripModel!!.latitude = userSnapshot.child("latitude").getValue(String::class.java)
                        mTripModel!!.longitude = userSnapshot.child("longitude").getValue(String::class.java)
                        mTripModel!!.userID = userSnapshot.child("userID").getValue(String::class.java)
                        mTripModel!!.userName = userSnapshot.child("userName").getValue(String::class.java)
                        mTripModel!!.bookedCustomerList = userSnapshot.child("bookedCustomerList")
                            .getValue(object : GenericTypeIndicator<List<HashMap<String, String>>>() {}) as List<HashMap<String, String>>?


                        if(mTripModel!=null)
                        {
                            if(mTripModel!!.bookingStatus.equals("CANCELED")){
                                (mFinishedTripModelList as ArrayList<TripModel>).add(mTripModel!!)
                            }else if(mTripModel!!.bookingStatus.equals("COMPLETED")){
                                (mFinishedTripModelList as ArrayList<TripModel>).add(mTripModel!!)
                            }else{
                                val result = compareDates(getFormattedCurrentDate(),mTripModel.tripDate.toString())
                                if(result.equals("BEFORE")){
                                    (mFinishedTripModelList as ArrayList<TripModel>).add(mTripModel!!)
                                }else{
                                    (mNewTripModelList as ArrayList<TripModel>).add(mTripModel!!)
                                }
                            }
                        }
                    }

                    renderRideList()
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    hideProgressDialog()
                    toast(mContext,"Try again")
                }
            })
        }

    }

    private fun renderRideList() {
        if (mContext == null) return
        if (mNewTripModelList == null) return
        if (mNewTripModelList!!.size <= 0){
            hld_no_data?.visibility = View.VISIBLE
            hideProgressDialog()
        }

        hld_no_data?.visibility = View.GONE
        finishedRideRecyclerview?.visibility = View.GONE
        newRideRecyclerview?.visibility = View.VISIBLE
        newRideRecyclerview?.layoutManager =
            LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        newRideRecyclerview?.itemAnimator = DefaultItemAnimator()
        newRideRecyclerview?.setHasFixedSize(true)
        val mRideListAdapter = RideListAdapter(mContext!!,mNewTripModelList!!)
        newRideRecyclerview?.adapter = mRideListAdapter

        hideProgressDialog()
    }

    private fun renderFinishedRideList() {
        if (mContext == null) return
        if (mFinishedTripModelList == null) return
        if (mFinishedTripModelList!!.size <= 0){
            hld_no_data?.visibility = View.VISIBLE
        }

        hld_no_data?.visibility = View.GONE
        newRideRecyclerview?.visibility = View.GONE
        finishedRideRecyclerview?.visibility = View.VISIBLE
        finishedRideRecyclerview?.layoutManager =
            LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        finishedRideRecyclerview?.itemAnimator = DefaultItemAnimator()
        finishedRideRecyclerview?.setHasFixedSize(true)
        val mRideListAdapter = RideListAdapter(mContext!!,mFinishedTripModelList!!)
        finishedRideRecyclerview?.adapter = mRideListAdapter
    }

    fun getFormattedCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val currentDate = LocalDate.now()
        return currentDate.format(formatter) // Format the date to dd-MM-yyyy
    }

}