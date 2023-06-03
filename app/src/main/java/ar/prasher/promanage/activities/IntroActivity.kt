package ar.prasher.promanage.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import ar.prasher.promanage.R
import ar.prasher.promanage.firebase.FirestoreClass
import ar.prasher.promanage.models.User
import ar.prasher.promanage.utils.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider

class IntroActivity : BaseActivity() {

    private var tvSignUp : TextView? = null
    private var etPassword : EditText? = null
    private var etEmail : EditText? = null
    private var tvLogin : TextView? = null
    private var googleLogin : CardView? = null
    private var twitterLogin : CardView? = null

    private lateinit var auth : FirebaseAuth
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private val provider = OAuthProvider.newBuilder("twitter.com")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        auth = FirebaseAuth.getInstance()

        tvSignUp = findViewById(R.id.tv_sign_up)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        tvLogin = findViewById(R.id.tv_login)
        googleLogin = findViewById(R.id.google_login)
        twitterLogin = findViewById(R.id.twitter_login)

        tvLogin?.setOnClickListener {
            signInUserWithEmailAndPassword()
        }

        googleLogin?.setOnClickListener {
            signInGoogle()
        }

        twitterLogin?.setOnClickListener {
            signInTwitter()
        }

        tvSignUp?.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))//from the credentials page
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.GOOGLE_SIGN_IN_REQ_CODE){
            val task: Task<GoogleSignInAccount> = GoogleSignIn
                .getSignedInAccountFromIntent(data)

            handleResult(task)
        }
    }

    //here we will mak changes in UI after login
    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account : GoogleSignInAccount? = completedTask
                .getResult(ApiException::class.java)

            if (account!=null){
                updateUI(account)
            }
        }catch (e: ApiException){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show()
            Log.e("Sign In API Exception",e.toString())
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider
            .getCredential(account.idToken,null)

        showProgressDialog("Please Wait")

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task->
                hideProgressDialog()

                if (task.isSuccessful){
                    val firebaseUser : FirebaseUser = task.result!!.user!!
                    val registeredEmail = account.email!!
                    val registeredName = account.displayName!!

                    val user = User(firebaseUser.uid,registeredName, registeredEmail,account.photoUrl.toString())
                    FirestoreClass().registerUser(this,user)

                    FirestoreClass().loadUserData(this)
                }
                else{//if wasn't successful
                    Toast.makeText(
                        this,
                        "Sign In Failed",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
    }

    private fun signInUserWithEmailAndPassword(){
        val email = etEmail?.text.toString().trim{ it <= ' '}
        val password = etPassword?.text.toString().trim{ it <= ' '}

        if (validateForm(email,password)){
            showProgressDialog("Please Wait")

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    hideProgressDialog()
                    if(task.isSuccessful){
                        //Sign in Success, update your ui with the same user
                        Log.d("Sign in with Email","Successful")
                        FirestoreClass().loadUserData(this)
                    }
                    else{
                    //Sign in failed display some msg to user
                        Log.w("Sign in","Failure",task.exception)

                        Toast.makeText(
                            baseContext,
                            "Authentication Failed",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
        }
    }

    private fun validateForm(email : String, password : String) : Boolean{
        return when {
            TextUtils.isEmpty(email)->{
                showErrorSnackBar("Please Enter an Email")
                false
            }
            TextUtils.isEmpty(password)->{
                showErrorSnackBar("Please Enter a Password")
                false
            }
            else -> true
        }
    }

    fun signInSuccess() {
        hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }


    private fun signInGoogle(){
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, Constants.GOOGLE_SIGN_IN_REQ_CODE)
    }

    private fun signInTwitter() {
        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null){
            showProgressDialog("Please Wait")
            //there is something already here Finish the sign in
            pendingResultTask
                .addOnSuccessListener {
                    FirestoreClass().loadUserData(this)
                }
                .addOnFailureListener {

                }
        } else{
            startTwitterSignIn()
        }
    }

    private fun startTwitterSignIn() {
        auth.startActivityForSignInWithProvider(this,provider.build())
            .addOnSuccessListener {authResult ->

                showProgressDialog("Please Wait")

                val credential = authResult.credential
                auth.signInWithCredential(credential!!)
                    .addOnCompleteListener {task ->
                        hideProgressDialog()

                        if (task.isSuccessful){

                            val firebaseUser : FirebaseUser = task.result!!.user!!
                            val profileImage = authResult.user?.photoUrl!!
                            val registeredName = authResult.user?.displayName!!

                            val user = User(firebaseUser.uid,registeredName, "",profileImage.toString())
                            FirestoreClass().registerUser(this,user)

                            FirestoreClass().loadUserData(this)

                        }else {
                            Toast.makeText(
                                this,
                                "Sign in Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
            .addOnFailureListener {e ->
                Log.e("Error Twitter Sign In",e.toString())
            }
    }


}