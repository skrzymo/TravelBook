package com.example.travelbook

import android.support.v7.app.AppCompatActivity
import android.util.*
import android.view.*
import android.widget.*
import com.facebook.*
import com.facebook.appevents.*
import com.facebook.login.*
import com.facebook.login.widget.*
import com.google.firebase.auth.*
import com.facebook.AccessToken
import android.content.Intent
import android.os.*


class CreateAccountActivity : AppCompatActivity() {

    val TAG = "CreateAccount"

    //Init views
    lateinit var facebookSignInButton: LoginButton

    //Request codes
    val FACEBOOK_LOG_IN_RC = 1

    // Firebase Auth Object.
    var firebaseAuth: FirebaseAuth? = null

    //Facebook Callback manager
    var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createaccount)

        AppEventsLogger.activateApp(application)

        facebookSignInButton = findViewById<View>(R.id.facebook_sign_in_button) as LoginButton

        firebaseAuth = FirebaseAuth.getInstance()

        callbackManager = CallbackManager.Factory.create()
        facebookSignInButton.setReadPermissions("email")
        // Callback registration
        facebookSignInButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
            }
            override fun onCancel() {
            }
            override fun onError(exception: FacebookException) {
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "Got Result code $requestCode.")

        callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken: $token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = firebaseAuth!!.currentUser
                    startActivity(Intent(this@CreateAccountActivity, MainActivity::class.java))
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this@CreateAccountActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
