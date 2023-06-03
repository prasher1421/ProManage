package ar.prasher.promanage.activities

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import ar.prasher.promanage.R
import ar.prasher.promanage.firebase.FirestoreClass
import ar.prasher.promanage.models.User
import ar.prasher.promanage.utils.Constants
import com.bumptech.glide.Glide

class EditProfileActivity : BaseActivity() {

    private var toolbar : Toolbar? = null
    private var etName: EditText? = null
    private var etEmail: EditText? = null
    private var etMobile: EditText? = null
    private var tvSaveProfile: TextView? = null

    private lateinit var mUserDetails : User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        toolbar = findViewById(R.id.toolbar)
        tvSaveProfile = findViewById(R.id.tv_save_profile)

        setupActionBar()

        FirestoreClass().loadUserData(this)

        tvSaveProfile?.setOnClickListener {
            showProgressDialog("Please Wait")
            updateUserProfileData()
        }

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_24)
        }
        toolbar?.setNavigationOnClickListener { onBackPressed() }
    }

    fun updateUserProfileDetails(user: User) {
//        profileImg = findViewById(R.id.profileImg)

//        Log.i("User Image URL",user.image)
//        if (user.image.isNotEmpty())
//            Glide
//                .with(this)
//                .load(user.image)
//                .centerCrop()
//                .into(profileImg!!)

        mUserDetails = user

        etName = findViewById(R.id.et_name)
        etName?.setText(user.name)
        Log.i("Username set",user.name)

        etMobile = findViewById(R.id.et_mobile)
        if (user.mobile.toInt() != 0)
            etMobile?.setText(user.mobile.toString())
        Log.i("Mobile set",user.mobile.toString())

        etEmail = findViewById(R.id.et_email)
        if (user.email.isNotEmpty())
            etEmail?.setText(user.email)
        Log.i("Email set",user.email)
    }

    private fun updateUserProfileData(){

        val userHashMap = HashMap<String,Any>()

        etName = findViewById(R.id.et_name)
        etMobile = findViewById(R.id.et_mobile)
        etEmail = findViewById(R.id.et_email)

        var changesMade = false

        if (etName?.text.toString() != mUserDetails.name && etName?.text.toString().isNotEmpty()){
            userHashMap[Constants.NAME] = etName?.text.toString()
            changesMade = true
        }

        if (etEmail?.text.toString() != mUserDetails.email && etEmail?.text.toString().isNotEmpty()){
            userHashMap[Constants.EMAIL] = etEmail?.text.toString()
            changesMade = true
        }

        if (etMobile?.text.toString() != mUserDetails.mobile.toString() && etMobile?.text.toString().isNotEmpty()){
            userHashMap[Constants.MOBILE] = etMobile?.text.toString().toLong()
            changesMade = true
        }

        if (changesMade)
            FirestoreClass().updateUserProfileData(this,userHashMap)


    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}