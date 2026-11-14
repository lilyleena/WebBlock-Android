package com.kshrd.starter.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.kshrd.features.auth.data.google.GoogleSignIn
import com.kshrd.starter.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Receives Keycloak's redirect to `com.webblock.app:/oauth2redirect` after "Continue with Google", hands it to
 * [GoogleSignIn] (which accepts only the answer to the sign-in this app started) and brings the app back to the front.
 * It has no screen of its own.
 */
@AndroidEntryPoint
class GoogleRedirectActivity : ComponentActivity() {
    @Inject
    lateinit var googleSignIn: GoogleSignIn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.let { googleSignIn.onRedirect(it.toString()) }
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        )
        finish()
    }
}
