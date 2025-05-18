package com.pemchip.blablacar.model

class TripModel{
    var tripID: String? = null
    var fromAddress: String? = null
    var toAddress: String? = null
    var tripDate: String? = null
    var tripTime: String? = null
    var passenger: String? = null
    var bookedSeatCount: String? = null
    var amount: String? = null
    var carDetails: String? = null
    var bookingStatus: String? = null
    var latitude: String? = null
    var longitude: String? = null
    var userID: String? = null
    var userName: String? = null
    var bookedCustomerList: List<HashMap<String,String>> ? = null
}