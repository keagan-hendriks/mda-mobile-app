package com.example.mda_mobile_app.debug

import android.util.Log
import com.example.mda_mobile_app.data.model.TestRequest
import com.example.mda_mobile_app.data.remote.RetrofitClient
import com.example.mda_mobile_app.data.model.preferences.UpdatePreferencesRequest

object ApiTestRunner {

    suspend fun testHealth() {
        try {
            val response = RetrofitClient.api.healthCheck()

            Log.d(
                "API_TEST",
                "HTTP status code: ${response.code()}"
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Log.d(
                        "API_TEST",
                        "Success: ${body.success}"
                    )

                    Log.d(
                        "API_TEST",
                        "Message: ${body.message}"
                    )

                    Log.d(
                        "API_TEST",
                        "Timestamp: ${body.timestamp}"
                    )
                } else {
                    Log.e(
                        "API_TEST",
                        "Response body was empty"
                    )
                }
            } else {
                Log.e(
                    "API_TEST",
                    "Request failed: ${response.code()} ${response.message()}"
                )
            }

        } catch (e: Exception) {
            Log.e(
                "API_TEST",
                "Health request failed",
                e
            )
        }
    }


    suspend fun testPostRequest() {
        try {
            val request = TestRequest(
                message = "Hello from Android"
            )

            val response =
                RetrofitClient.api.testRequest(request)

            Log.d(
                "API_POST_TEST",
                "HTTP status: ${response.code()}"
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Log.d(
                        "API_POST_TEST",
                        "Success: ${body.success}"
                    )

                    Log.d(
                        "API_POST_TEST",
                        "Server received: ${body.received}"
                    )

                    Log.d(
                        "API_POST_TEST",
                        "Timestamp: ${body.timestamp}"
                    )
                } else {
                    Log.e(
                        "API_POST_TEST",
                        "Response body was empty"
                    )
                }
            } else {
                Log.e(
                    "API_POST_TEST",
                    "Request failed: ${response.code()} ${response.message()}"
                )
            }

        } catch (e: Exception) {
            Log.e(
                "API_POST_TEST",
                "POST request failed",
                e
            )
        }
    }

//commented out as it is the old hardcoded version of just testing the API call
//    suspend fun testGetPreferences() {
//
//        try {
//            val response = RetrofitClient.api.getPreferences()
//
//            Log.d(
//                "API_PREF_TEST",
//                "HTTP status: ${response.code()}"
//            )
//
//            if (response.isSuccessful) {
//
//                val result = response.body()
//
//                if (result != null && result.success) {
//
//                    val preferences = result.data
//
//                    if (preferences != null) {
//
//                        Log.d(
//                            "API_PREF_TEST",
//                            "User ID: ${preferences.userId}"
//                        )
//
//                        Log.d(
//                            "API_PREF_TEST",
//                            "Notifications: ${preferences.notificationsEnabled}"
//                        )
//
//                        Log.d(
//                            "API_PREF_TEST",
//                            "Theme: ${preferences.theme}"
//                        )
//
//                    } else {
//                        Log.e(
//                            "API_PREF_TEST",
//                            "Preferences data is missing"
//                        )
//                    }
//
//                } else {
//                    Log.e(
//                        "API_PREF_TEST",
//                        result?.message ?: "Empty response"
//                    )
//                }
//
//            } else {
//                Log.e(
//                    "API_PREF_TEST",
//                    "GET request failed: ${response.code()}"
//                )
//            }
//
//        } catch (e: Exception) {
//
//            Log.e(
//                "API_PREF_TEST",
//                "Could not retrieve preferences",
//                e
//            )
//        }
//    }

    //tests PUT to server from android, uses mock data, commented out to replace mock data with real db call
//    suspend fun testUpdatePreferences() {
//
//        try {
//
//            val request = UpdatePreferencesRequest(
//                notificationsEnabled = false,
//                theme = "light"
//            )
//
//            val response =
//                RetrofitClient.api.updatePreferences(request)
//
//            Log.d(
//                "API_PREF_UPDATE_TEST",
//                "HTTP status: ${response.code()}"
//            )
//
//            if (response.isSuccessful) {
//
//                val result = response.body()
//
//                if (result != null && result.success) {
//
//                    val preferences = result.data
//
//                    if (preferences != null) {
//
//                        Log.d(
//                            "API_PREF_UPDATE_TEST",
//                            "Message: ${result.message}"
//                        )
//
//                        Log.d(
//                            "API_PREF_UPDATE_TEST",
//                            "User ID: ${preferences.userId}"
//                        )
//
//                        Log.d(
//                            "API_PREF_UPDATE_TEST",
//                            "Notifications: ${preferences.notificationsEnabled}"
//                        )
//
//                        Log.d(
//                            "API_PREF_UPDATE_TEST",
//                            "Theme: ${preferences.theme}"
//                        )
//
//                    } else {
//
//                        Log.e(
//                            "API_PREF_UPDATE_TEST",
//                            "Response contained no preference data"
//                        )
//                    }
//
//                } else {
//
//                    Log.e(
//                        "API_PREF_UPDATE_TEST",
//                        result?.message ?: "Empty response"
//                    )
//                }
//
//            } else {
//
//                Log.e(
//                    "API_PREF_UPDATE_TEST",
//                    "PUT failed: ${response.code()} " +
//                            response.errorBody()?.string()
//                )
//            }
//
//        } catch (e: Exception) {
//
//            Log.e(
//                "API_PREF_UPDATE_TEST",
//                "Could not update preferences",
//                e
//            )
//        }
//    }


    suspend fun testGetPreferences() {

        try {
            val userId = 128 //test user not a real user

            val response =
                RetrofitClient.api.getPreferences(userId)

            Log.d("API_PREF_GET", "HTTP status: ${response.code()}")

            if (response.isSuccessful) {
                val result = response.body()
                val preferences = result?.data

                if (result?.success == true && preferences != null) {

                    Log.d("API_PREF_GET", "User ID: ${preferences.userId}")
                    Log.d("API_PREF_GET", "Notifications: ${preferences.notificationsEnabled}")
                    Log.d("API_PREF_GET", "Sound: ${preferences.soundEnabled}")
                    Log.d("API_PREF_GET", "Trusted contact sharing: ${preferences.trustedContactSharing}")

                } else {
                    Log.e(
                        "API_PREF_GET",
                        result?.message ?: "No preferences returned"
                    )
                }

            } else {
                Log.e(
                    "API_PREF_GET",
                    "Request failed: ${response.errorBody()?.string()}"
                )
            }

        } catch (e: Exception) {
            Log.e(
                "API_PREF_GET",
                "Could not get preferences",
                e
            )
        }
    }


    suspend fun testUpdatePreferences() {

        try {
            val userId = 128 //test user not a real user

            val request = UpdatePreferencesRequest(
                userId = userId,
                notificationsEnabled = false,
                soundEnabled = false,
                trustedContactSharing = true
            )

            val response =
                RetrofitClient.api.updatePreferences(request)

            Log.d("API_PREF_PUT", "HTTP status: ${response.code()}")

            if (response.isSuccessful) {
                val result = response.body()
                val preferences = result?.data

                if (result?.success == true && preferences != null) {

                    Log.d("API_PREF_PUT", "Message: ${result.message}")
                    Log.d("API_PREF_PUT", "User ID: ${preferences.userId}")
                    Log.d("API_PREF_PUT", "Notifications: ${preferences.notificationsEnabled}")
                    Log.d("API_PREF_PUT", "Sound: ${preferences.soundEnabled}")
                    Log.d("API_PREF_PUT", "Trusted contact sharing: ${preferences.trustedContactSharing}")

                } else {
                    Log.e(
                        "API_PREF_PUT",
                        result?.message ?: "No preferences returned"
                    )
                }

            } else {
                Log.e(
                    "API_PREF_PUT",
                    "Request failed: ${response.errorBody()?.string()}"
                )
            }

        } catch (e: Exception) {
            Log.e(
                "API_PREF_PUT",
                "Could not update preferences",
                e
            )
        }
    }


}