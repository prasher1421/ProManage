package ar.prasher.promanage.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ar.prasher.promanage.R
import ar.prasher.promanage.firebase.FirestoreClass
import ar.prasher.promanage.models.User
import ar.prasher.promanage.utils.Constants
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var llEdit : LinearLayout? = null
    private var profileImg : ImageView? = null
    private var nameTxt : TextView? = null
    private var mobileTxt : TextView? = null
    private var emailTxt : TextView? = null
    private var toolbar : Toolbar? = null

    private var mSelectedImageFileUri : Uri? = null
    private var mProfileImageUrl : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        llEdit = findViewById(R.id.ll_edit)
        toolbar = findViewById(R.id.toolbar_my_profile)
        profileImg = findViewById(R.id.profileImg)

        llEdit?.setOnClickListener {
            startActivityForResult(Intent(this,EditProfileActivity::class.java),
                Constants.PROFILE_UPDATE_REQUEST_CODE)
        }

        setupActionBar()

        profileImg?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                //SHOW IMAGE CHOOSER
                Constants.showImageChooser(this)
            }else{
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        FirestoreClass().loadUserData(this)



    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_24)
            actionBar.title = getString(R.string.my_profile)
        }
        toolbar?.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Show Image Chooser
                Constants.showImageChooser(this)

            }else{
                Toast.makeText(
                    this,
                    "Oops, you have denied the permissions for storage",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null){

            mSelectedImageFileUri = data.data

            Log.i("Glide uploaded Image",mSelectedImageFileUri.toString())
            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .into(profileImg!!)

                uploadUserImage()

            }catch (e : IOException){
                Log.e("Loading Image From Storage Error",e.toString())
            }
        }

        if (resultCode == RESULT_OK && requestCode == Constants.PROFILE_UPDATE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
            setResult(Activity.RESULT_OK)
        }else{
            Log.i("NO UPDATE MADE","Cancelled")
        }
    }

    fun updateUserProfileDetails(user: User) {
        profileImg = findViewById(R.id.profileImg)

        Log.i("User Image URL",user.image)
        if (user.image.isNotEmpty())
            Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .into(profileImg!!)

        nameTxt = findViewById(R.id.nameTxt)
        nameTxt?.text = user.name
        Log.i("Username set",user.name)

        mobileTxt = findViewById(R.id.mobileTxt)
        if (user.mobile.toInt() != 0)
            mobileTxt?.text = user.mobile.toString()
        Log.i("Mobile set",user.mobile.toString())

        emailTxt = findViewById(R.id.emailTxt)
        if (user.email.isNotEmpty())
            emailTxt?.text = user.email
        Log.i("Email set",user.email)

    }

    private fun uploadUserImage(){
        showProgressDialog("Please Wait")

        Log.i("Uploading Image in Firebase Storage Started","")

        if (mSelectedImageFileUri != null){
            //to store we need storage reference
            val sRef : StorageReference =
                FirebaseStorage.getInstance().reference.child("USER_IMAGE"
                        + System.currentTimeMillis() + "." + Constants.getFileExtension(this,mSelectedImageFileUri)
                )

            //here we store to firebase storage
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
                Log.i("Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                //now using the downloadable Image Url generated
                //we need to update user Profile Data
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.i("Downloadable Image URI", uri.toString())
                    mProfileImageUrl = uri.toString()

                    //In firebase we also need to update
                    //Update User Profile Data
                    updateUserProfileData()
                }
            }.addOnFailureListener {
                e ->
                Toast.makeText(
                    this,
                    e.message.toString(),
                    Toast.LENGTH_LONG
                ).show()

                hideProgressDialog()
            }
        }
    }

    fun updateUserProfileData(){
        val userHashMap = HashMap<String,Any>()

        var changesMade = false
        if (mProfileImageUrl.isNotEmpty()){
            userHashMap[Constants.IMAGE] = mProfileImageUrl
            changesMade=true
        }
        if (changesMade)
            FirestoreClass().updateUserProfileData(this,userHashMap)
    }

    fun imageUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
    }

}