package com.pemchip.blablacar.screen.customerHome

import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.pemchip.blablacar.R
import com.pemchip.blablacar.common.BaseActivity
import com.pemchip.blablacar.common.PreferenceKey
import com.pemchip.blablacar.model.TripModel

class TripTrackScreen: BaseActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var driverLocation: LatLng ? = null
    private var customerLocations: ArrayList<LatLng> ? = null

    companion object{
        lateinit var mTripModel: TripModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.track_screen)

        if (mPreferencesSession!!.userRole == PreferenceKey.CUSTOMER_ROLE)
        {
            if(mTripModel.bookedCustomerList!=null){
                customerLocations = ArrayList()
                for (mCustomerHasMap in mTripModel.bookedCustomerList!!){
                    val mCustomerMap = mCustomerHasMap
                    if(mCustomerMap.get("customerID").equals(mPreferencesSession!!.userId)){
                        if(mCustomerMap.get("customerBookingStatus").equals("CONFIRMED")){
                            customerLocations!!.add( LatLng(mCustomerMap.get("customerLatitude")!!.toDouble(), mCustomerMap.get("customerLongitude")!!.toDouble()))
                        }
                    }
                }
            }

            driverLocation = LatLng(mTripModel.latitude!!.toDouble(), mTripModel.longitude!!.toDouble())
        }
        else
        {
            if(mTripModel.bookedCustomerList!=null){
                customerLocations = ArrayList()
                for (mCustomerHasMap in mTripModel.bookedCustomerList!!){
                    val mCustomerMap = mCustomerHasMap
                    if(mCustomerMap.get("customerBookingStatus").equals("CONFIRMED")){
                        customerLocations!!.add( LatLng(mCustomerMap.get("customerLatitude")!!.toDouble(), mCustomerMap.get("customerLongitude")!!.toDouble()))
                    }
                }
            }
            driverLocation = LatLng(mTripModel.latitude!!.toDouble(), mTripModel.longitude!!.toDouble())
        }
        // Get the map fragment and notify the system when the map is ready
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add markers for customer and driver locations
        if (mPreferencesSession!!.userRole == PreferenceKey.CUSTOMER_ROLE) {
            customerLocations!!.forEachIndexed { index, location ->
                mMap.addMarker(MarkerOptions().position(location).title("Your location"))
            }
            mMap.addMarker(MarkerOptions().position(driverLocation!!).title("Car Location"))
        }else{
            customerLocations!!.forEachIndexed { index, location ->
                mMap.addMarker(MarkerOptions().position(location).title("Passenger Location $index"))
            }
            mMap.addMarker(MarkerOptions().position(driverLocation!!).title("Your Location"))
        }

        // Move the camera to the customer location
        // Zoom into the map with the driver location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLocation!!, 12f))

        // Draw routes to each customer
        customerLocations!!.forEach { customerLocation ->
            drawRoute(driverLocation!!, customerLocation)
        }
    }

    // Function to draw a route on the map between the customer and driver
    private fun drawRoute(customerLatLng: LatLng, driverLatLng: LatLng) {
        // Use PolylineOptions to create a line between customer and driver
        val polylineOptions = PolylineOptions()
            .add(customerLatLng) // Start point (customer)
            .add(driverLatLng) // End point (driver)
            .width(5f)
            .color(android.graphics.Color.BLUE)

        // Add polyline to the map
        mMap.addPolyline(polylineOptions)
    }
}