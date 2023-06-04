package ar.prasher.promanage.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ar.prasher.promanage.R
import ar.prasher.promanage.firebase.FirestoreClass
import ar.prasher.promanage.models.Board
import ar.prasher.promanage.utils.Constants
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var toolbar : Toolbar? = null
    private var ivBoardImage : ImageView? = null
    private var etBoardName : EditText? = null
    private var tvCreateBoard : TextView? = null

    private var mSelectedImageFileUri : Uri? = null
    private lateinit var mUserName : String
    private var mBoardImageUrl : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        toolbar = findViewById(R.id.toolbar_create_board)
        ivBoardImage = findViewById(R.id.iv_board_image)
        etBoardName = findViewById(R.id.et_board_name)
        tvCreateBoard = findViewById(R.id.tv_create_board)

        setupActionBar()

        if (intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        ivBoardImage?.setOnClickListener {
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

        tvCreateBoard?.setOnClickListener {
            if (mSelectedImageFileUri != null){
                uploadBoardImage()
                //uploading image and creating board
            }else{
                showProgressDialog("Please Wait")
                createBoard()
                //just creating board
            }
        }
    }

    private fun createBoard(){
        val assignedUsersArrayList : ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        val board = Board(
            etBoardName?.text.toString(),
            mBoardImageUrl,
            mUserName,
            assignedUsersArrayList
        )
        FirestoreClass().createBoard(this,board)
    }

    private fun uploadBoardImage(){
        showProgressDialog("Please Wait")

        val sRef : StorageReference =
            FirebaseStorage.getInstance().reference.child("BOARD_IMAGE"
                    + System.currentTimeMillis() + "." + Constants.getFileExtension(this,mSelectedImageFileUri)
            )

        //here we store to firebase storage
        sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->
            Log.i("Firebase Board Image URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

            //now using the downloadable Image Url generated
            //we need to update user Profile Data
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                Log.i("Downloadable Image URI", uri.toString())
                mBoardImageUrl = uri.toString()

                //In firebase we also need to update
                //create Board here

                createBoard()
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

    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_24)
            actionBar.title = getString(R.string.create_board)
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
                    .into(ivBoardImage!!)

            }catch (e : IOException){
                Log.e("Loading Image From Storage Error",e.toString())
            }
        }

    }


}