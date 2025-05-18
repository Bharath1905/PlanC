package com.pemchip.blablacar.screen.history

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.pemchip.blablacar.R
import com.pemchip.blablacar.common.PcpUtils
import com.pemchip.blablacar.common.PreferenceKey
import com.pemchip.blablacar.common.PreferencesSession
import com.pemchip.blablacar.model.TripModel
import com.pemchip.blablacar.screen.customerHome.TripDetailsActivity
import com.pemchip.blablacar.screen.login.LoginActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RideListAdapter(private val mContext: Context, private var items: List<TripModel>) :
    RecyclerView.Adapter<RideListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.ride_list_adapter, parent, false)
        return ItemViewHolder(view, mContext)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    class ItemViewHolder(itemView: View, private val mContext: Context) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: TripModel) {

            val mPreferencesSession = PreferencesSession.getInstance(mContext)
            var mTotalBookedSeats = 0

            val hld_content = itemView.findViewById<android.widget.LinearLayout?>(R.id.hld_content)
            val name = itemView.findViewById<android.widget.TextView?>(R.id.name)
            val cardDetails = itemView.findViewById<android.widget.TextView?>(R.id.cardDetails)
            val amount = itemView.findViewById<android.widget.TextView?>(R.id.amount)
            val fromAddress = itemView.findViewById<android.widget.TextView?>(R.id.fromAddress)
            val toAddress = itemView.findViewById<android.widget.TextView?>(R.id.toAddress)
            val passenger = itemView.findViewById<android.widget.TextView?>(R.id.passenger)
            val dateAndTime = itemView.findViewById<android.widget.TextView?>(R.id.dateAndTime)
            val bookingStatus = itemView.findViewById<android.widget.TextView?>(R.id.bookingStatus)
            val bookingCount = itemView.findViewById<android.widget.TextView?>(R.id.bookingCount)
            val bookingBtn = itemView.findViewById<android.widget.Button?>(R.id.bookingBtn)
            val cancelRide = itemView.findViewById<android.widget.TextView?>(R.id.cancelRide)

            name.setText(item.userName)
            cardDetails.setText(item.carDetails)
            amount.setText("₹ "+item.amount)
            fromAddress.setText(item.fromAddress)
            toAddress.setText(item.toAddress)
            passenger.setText(item.passenger+" Seats")
            dateAndTime.setText(item.tripDate+", "+item.tripTime)

            var bookedCustomerList = ArrayList<HashMap<String,String>>()
            if(item.bookedCustomerList!=null){
                bookedCustomerList = (item.bookedCustomerList as ArrayList<HashMap<String, String>>?)!!

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
                        if(mCustomerMap.get("customerID").equals(mPreferencesSession.userId)){
                            bookingBtn.visibility = View.GONE
                            bookingStatus.visibility = View.VISIBLE
                            if(mCustomerMap.get("customerBookingStatus").equals("PENDING")){
                                bookingStatus.setText("Request Sent")
                            }else{
                                bookingStatus.setText("Booked")
                            }

                        }
                    }
                }
            }else{

                cancelRide.visibility = View.VISIBLE

                if(item.bookingStatus.equals("NEW")){
                    bookingCount.visibility = View.VISIBLE
                    if(bookedCustomerList.size>0){
                        bookingCount.setText(bookedCustomerList.size.toString()+" Bookings")
                        cancelRide.visibility = View.GONE
                    }else{
                        bookingCount.setText("No Bookings")
                    }
                }else{

                    bookingStatus.setText("Ride Completed")
                    if(item.bookingStatus.equals("CANCELED")){
                        bookingStatus.setText("Ride Canceled")
                    }
                    cancelRide.visibility = View.GONE
                    bookingStatus.visibility = View.VISIBLE
                }
            }

            val result = compareDates(getFormattedCurrentDate(),item.tripDate.toString())
            if(result.equals("BEFORE")){
                bookingCount.visibility = View.GONE
                bookingStatus.visibility = View.GONE
                bookingBtn.visibility = View.GONE
            }

            bookingBtn.setOnClickListener{

                val intent = Intent(mContext, TripDetailsActivity::class.java)
                TripDetailsActivity.mTripModel = item
                mContext.startActivity(intent)

                /*val options = arrayOf("1","2","3","4","5","6","7","8")

                var selectedItemIndex = -1
                AlertDialog.Builder(mContext)
                    .setTitle("Choose an no of seats")
                    .setCancelable(true)
                    .setSingleChoiceItems(options, selectedItemIndex) { dialog, which ->
                        selectedItemIndex = which  // Update the selected item index
                    }
                    .setPositiveButton("OK") { dialog, _ ->

                        val availAbleSeats = item.passenger!!.toInt() - mTotalBookedSeats

                        if (selectedItemIndex != -1)
                        {

                            val requiredSeats = options[selectedItemIndex].toInt()


                            if(requiredSeats <= availAbleSeats){
                                val database = FirebaseDatabase.getInstance()
                                val usersRef = database.getReference("trip") // reference to 'users' node
                                val userRef = usersRef.child(item.tripID.toString())

                                val customerHashMap = mapOf<String, String>(
                                    "customerID" to mPreferencesSession.userId,
                                    "customerName" to mPreferencesSession.userName,
                                    "customerGender" to mPreferencesSession.userGender,
                                    "customerLatitude" to PcpUtils.latitude.toString(),
                                    "customerLongitude" to PcpUtils.longitude.toString(),
                                    "customerRequiredSeats" to requiredSeats.toString(),
                                    "customerBookingStatus" to "PENDING"
                                )

                                val arrayList = ArrayList<HashMap<String,String>>()
                                arrayList.add(customerHashMap as HashMap<String, String>)
                                bookedCustomerList.addAll(arrayList)


                                val updates = mapOf<String, Any>(
                                    "bookedCustomerList" to bookedCustomerList
                                )

                                // Update the user's age in the database
                                userRef.updateChildren(updates)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            bookingStatus.visibility = View.GONE
                                            item.bookedCustomerList = bookedCustomerList
                                            Toast.makeText(mContext, "Booked successfully", Toast.LENGTH_SHORT).show()
                                        } else {
                                            // Failed to update
                                            println("Failed to update user age: ${task.exception?.message}")
                                        }
                                    }
                            }else{
                                Toast.makeText(mContext, "Try again!!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.show()*/
            }

            cancelRide.setOnClickListener(){

                val builder = AlertDialog.Builder(mContext)
                builder.setMessage("Are you sure, want to cancel this ride?")
                    .setCancelable(true) // Disable closing the dialog by tapping outside
                    .setPositiveButton("Yes") { dialog, id ->
                        dialog.dismiss()

                        val database = FirebaseDatabase.getInstance()
                        val usersRef = database.getReference("trip") // reference to 'users' node
                        val userRef = usersRef.child(item.tripID.toString())

                        val updates = mapOf<String, String>(
                            "bookingStatus" to "CANCELED"
                        )

                        // Update the user's age in the database
                        userRef.updateChildren(updates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    bookingStatus.visibility = View.GONE
                                    item.bookedCustomerList = bookedCustomerList
                                    Toast.makeText(mContext, "Booking canceled successfully", Toast.LENGTH_SHORT).show()
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

            hld_content.setOnClickListener{
                val intent = Intent(mContext, TripDetailsActivity::class.java)
                TripDetailsActivity.mTripModel = item
                mContext.startActivity(intent)
            }
        }

        fun getFormattedCurrentDate(): String {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val currentDate = LocalDate.now()
            return currentDate.format(formatter) // Format the date to dd-MM-yyyy
        }

        private fun compareDates(currentDate: String, emiStartDate: String): String{
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
    }


    override fun getItemCount(): Int = items.size
}