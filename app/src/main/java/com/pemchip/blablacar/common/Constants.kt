package com.pemchip.blablacar.common

interface Constants {
    companion object {
        const val FIREBASE_REALTIME_DATABASE_URL = "https://blablacar-34bf2-default-rtdb.asia-southeast1.firebasedatabase.app"
        const val FIREBASE_USERS = "users" // this is not a secret value, it is just a constant variable that will be accessed from different places of the application.
        const val FIREBASE_EMAIL_KEY = "email"
    }
}