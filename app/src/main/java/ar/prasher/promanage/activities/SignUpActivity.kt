package ar.prasher.promanage.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
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

class SignUpActivity : BaseActivity() {

    private var tvLogin : TextView? = null
    private var tvCreateAccount : TextView? = null
    private var etName : EditText? = null
    private var etPassword : EditText? = null
    private var etEmail : EditText? = null
    private var toolbarSignUp : Toolbar? = null
    private var googleSignInButton : CardView? = null
    private var twitterSignInButton : CardView? = null

    private lateinit var auth : FirebaseAuth
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private val provider = OAuthProvider.newBuilder("twitter.com")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        tvLogin = findViewById(R.id.tv_login)
        tvCreateAccount = findViewById(R.id.tv_create_account)
        toolbarSignUp = findViewById(R.id.toolbar_sign_up)
        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        tvProgressText = findViewById(R.id.tv_progress_text)
        googleSignInButton = findViewById(R.id.google_sign_in)
        twitterSignInButton = findViewById(R.id.twitter_sign_in)

        auth = FirebaseAuth.getInstance()

        setupActionBar()

        tvLogin?.setOnClickListener {
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
        }

        tvCreateAccount?.setOnClickListener {
            registerUser()
        }

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))//from the credentials page
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)
        googleSignInButton?.setOnClickListener{
            signInGoogle()
        }

        twitterSignInButton?.setOnClickListener {
            signInTwitter()
        }


    }


    //here we are providing task and data for the google account
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
        }catch (e:ApiException){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    //this is where we specify what UI updation are needed after sign in
    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider
            .getCredential(account.idToken,null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task->

                if (task.isSuccessful){
                    val firebaseUser : FirebaseUser = task.result!!.user!!
                    val registeredEmail = account.email!!
                    val registeredName = account.displayName!!

                    val user = User(firebaseUser.uid,registeredName, registeredEmail)
                    FirestoreClass().registerUser(this,user)

                    startActivity(Intent(this,MainActivity::class.java))
                    finish()

                }
                else{//if wasn't successful
                    Toast.makeText(
                        this,
                        "Registration Failed",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
    }

    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbarSignUp)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_24)
        }
        toolbarSignUp?.setNavigationOnClickListener { onBackPressed() }
    }

    private fun registerUser(){
        val name : String = etName?.text.toString().trim{ it <= ' '}
        val email : String = etEmail?.text.toString().trim{ it <= ' '}
        val password : String = etPassword?.text.toString().trim{ it <= ' '}

        if (validateForm(name, email, password)){
            showProgressDialog("Please Wait")

            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    task ->

                    if (task.isSuccessful){
                        val firebaseUser : FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!

                        val user = User(firebaseUser.uid,name, registeredEmail)

                        FirestoreClass().registerUser(this,user)
                    }
                    else{//if wasn't successful
                        Toast.makeText(
                            this,
                            "Registration Failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

    }


    //just checking all entries
    private fun validateForm(name : String, email : String, password : String) : Boolean{
        return when {
            TextUtils.isEmpty(name)->{
                showErrorSnackBar("Please Enter a Name")
                false
            }
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

    fun userRegisteredSuccess() {
        Toast.makeText(
            this,
            "You have successfully registered",
            Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun signInGoogle(){
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent,Constants.GOOGLE_SIGN_IN_REQ_CODE)
    }


    private fun signInTwitter() {
        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null){
            showProgressDialog("Please Wait")
            //there is something already here Finish the sign in
            pendingResultTask
                .addOnSuccessListener {
                    //FirestoreClass().signInUser(this)
                    //Already signed in
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