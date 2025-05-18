package com.pemchip.blablacar.screen.customerHome

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
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
import com.pemchip.blablacar.common.CustomLog
import com.pemchip.blablacar.model.TripModel
import com.pemchip.blablacar.screen.history.RideListAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TripListActivity: BaseActivity() {

    private var mContext: Context? = null
    private var newRideRecyclerview: RecyclerView? = null
    private var hld_no_data: FrameLayout? = null
    private var mDatabase: DatabaseReference? = null
    private var mNewTripModelList: List<TripModel>? = null

    companion object{
        var fromAddress = ""
        var toAddress = ""
        var tripDate = ""
    }

    override fun onDestroy() {
        super.onDestroy()
        mContext = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        setContentView(R.layout.ride_screen)

        initFirebaseDatabase()
        setUpView()
        checkPermission()
    }

    private fun initFirebaseDatabase() {
        mDatabase = FirebaseDatabase.getInstance(Constants.FIREBASE_REALTIME_DATABASE_URL).getReference()
    }

    private fun setUpView() {
        if (mContext == null) return

        hld_no_data = findViewById(R.id.hld_no_data)
        newRideRecyclerview = findViewById(R.id.newRideRecyclerview)

        getRideHistoryList()
        setUpListener()
    }

    private fun setUpListener() {
        if (mContext == null) return

    }

    private fun getRideHistoryList() {
        if (mContext == null) return
        if (!isConnectingToInternet()) {
            noInternetAlertDialog()
            return
        }

        showProgressDialog()

        mDatabase?.child("trip")?.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mNewTripModelList = ArrayList()
                for (userSnapshot in dataSnapshot.getChildren()) {
                    var mTripModel: TripModel? = TripModel()

                    mTripModel!!.tripID = userSnapshot.key
                    mTripModel!!.fromAddress =
                        userSnapshot.child("fromAddress").getValue(String::class.java)
                    mTripModel!!.toAddress =
                        userSnapshot.child("toAddress").getValue(String::class.java)
                    mTripModel!!.tripDate =
                        userSnapshot.child("tripDate").getValue(String::class.java)
                    mTripModel!!.tripTime =
                        userSnapshot.child("tripTime").getValue(String::class.java)
                    mTripModel!!.passenger =
                        userSnapshot.child("passenger").getValue(String::class.java)
                    mTripModel!!.bookedSeatCount =
                        userSnapshot.child("bookedSeatCount").getValue(String::class.java)
                    mTripModel!!.amount = userSnapshot.child("amount").getValue(String::class.java)
                    mTripModel!!.carDetails =
                        userSnapshot.child("carDetails").getValue(String::class.java)
                    mTripModel!!.bookingStatus =
                        userSnapshot.child("bookingStatus").getValue(String::class.java)
                    mTripModel!!.latitude =
                        userSnapshot.child("latitude").getValue(String::class.java)
                    mTripModel!!.longitude =
                        userSnapshot.child("longitude").getValue(String::class.java)
                    mTripModel!!.userID = userSnapshot.child("userID").getValue(String::class.java)
                    mTripModel!!.userName =
                        userSnapshot.child("userName").getValue(String::class.java)
                    mTripModel!!.bookedCustomerList = userSnapshot.child("bookedCustomerList")
                        .getValue(object : GenericTypeIndicator<List<HashMap<String, String>>>() {})


                    if (mTripModel!!.fromAddress.equals(fromAddress, true)) {
                        if (mTripModel!!.toAddress.equals(toAddress, true)) {
                            if (mTripModel!!.tripDate.equals(tripDate, true)) {
                                if (mTripModel!!.bookingStatus.equals("NEW", true)) {
                                    if (mTripModel != null) {
                                        val result =
                                            compareDates(
                                                getFormattedCurrentDate(),
                                                mTripModel.tripDate.toString()
                                            )
                                        if (!result.equals("BEFORE")) {
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

                renderRideList()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                hideProgressDialog()
                toast(mContext,"Try again")
            }
        })
    }

    private fun renderRideList() {
        if (mContext == null) return
        if (mNewTripModelList == null){
            hld_no_data?.visibility = View.VISIBLE
            hideProgressDialog()
            return
        }
        if (mNewTripModelList!!.size <= 0){
            hld_no_data?.visibility = View.VISIBLE
            hideProgressDialog()
            return
        }

        hld_no_data?.visibility = View.GONE
        newRideRecyclerview?.visibility = View.VISIBLE
        newRideRecyclerview?.layoutManager =
            LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        newRideRecyclerview?.itemAnimator = DefaultItemAnimator()
        newRideRecyclerview?.setHasFixedSize(true)
        val mRideListAdapter = RideListAdapter(mContext!!,mNewTripModelList!!)
        newRideRecyclerview?.adapter = mRideListAdapter

        getLastLocation()
        hideProgressDialog()
    }

    fun getFormattedCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val currentDate = LocalDate.now()
        return currentDate.format(formatter) // Format the date to dd-MM-yyyy
    }
}