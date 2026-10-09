package com.example.mda_mobile_app.debug

import android.util.Log
import com.example.mda_mobile_app.data.model.TestRequest
import com.example.mda_mobile_app.data.remote.RetrofitClient

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
}