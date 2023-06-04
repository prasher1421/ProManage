package ar.prasher.promanage.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R
import ar.prasher.promanage.adapters.BoardItemsAdapter
import ar.prasher.promanage.firebase.FirestoreClass
import ar.prasher.promanage.models.Board
import ar.prasher.promanage.models.User
import ar.prasher.promanage.utils.Constants
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var toolbar : Toolbar? = null
    private var drawerLayout : DrawerLayout? = null
    private var navView : NavigationView? = null
    private var ivUserImage : ImageView? = null
    private  var tvUserName : TextView? = null
    private  var tvNoBoardsAvailable : TextView? = null
    private  var fabCreateBoard : FloatingActionButton? = null
    private  var rvBoardsList : RecyclerView? = null

    private lateinit var mUserName : String

    // declare the GoogleSignInClient
    lateinit var mGoogleSignInClient: GoogleSignInClient

    companion object{
        const val PROFILE_UPDATE_REQUEST_CODE = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar_main_activity)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        ivUserImage = findViewById(R.id.iv_user_image)
        tvUserName = findViewById(R.id.tv_user_name)
        fabCreateBoard = findViewById(R.id.fab_create_board)


        setupActionBar()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)
        // pass the same server client ID used while implementing the LogIn feature earlier.

        navView?.setNavigationItemSelectedListener(this)

        FirestoreClass().loadUserData(this,true)

        fabCreateBoard?.setOnClickListener {
            val intent = Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME,mUserName)
            startActivityForResult(intent,Constants.CREATE_BOARD_REQUEST_CODE)
        }

    }

    fun populateListToUI(boardsList : ArrayList<Board>){
        hideProgressDialog()
        rvBoardsList = findViewById(R.id.rv_boards_list)
        tvNoBoardsAvailable = findViewById(R.id.tv_no_boards_available)

        if (boardsList.size > 0){
            rvBoardsList?.visibility = View.VISIBLE
            tvNoBoardsAvailable?.visibility = View.GONE

            rvBoardsList?.layoutManager = LinearLayoutManager(this)
            rvBoardsList?.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this,boardsList)
            rvBoardsList?.adapter = adapter

            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity,TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID,model.documentID)
                    startActivity(intent)
                }
            })

        }else{
            rvBoardsList?.visibility = View.GONE
            tvNoBoardsAvailable?.visibility = View.VISIBLE
        }

    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            //Toggle Drawer
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if (drawerLayout?.isDrawerOpen(GravityCompat.START)!!){
            drawerLayout?.closeDrawer(GravityCompat.START)
        }else{
            drawerLayout?.openDrawer(GravityCompat.START)
        }

    }

    override fun onBackPressed() {
        if (drawerLayout?.isDrawerOpen(GravityCompat.START)!!){
            drawerLayout?.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PROFILE_UPDATE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }

        if (resultCode == RESULT_OK  && requestCode == Constants.CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getBoardsList(this)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this,MyProfileActivity::class.java),
                    PROFILE_UPDATE_REQUEST_CODE)
            }
            R.id.nav_sign_out -> {

                FirebaseAuth.getInstance()
                    .signOut()
                mGoogleSignInClient.signOut().addOnCompleteListener{
                    val intent= Intent(this, IntroActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }
        }
        drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User,readBoardsList : Boolean) {

        mUserName = user.name

        ivUserImage = findViewById(R.id.iv_user_image)
        Log.i("User Image URL",user.image)
        if (user.image.isNotEmpty())
            Glide
                .with(this)
                .load(user.image)
                .centerCrop()
                .into(ivUserImage!!)

        tvUserName = findViewById(R.id.tv_user_name)
        tvUserName?.text = user.name
        Log.i("Username set",user.name)

        if (readBoardsList){
            showProgressDialog("Please Wait")
            FirestoreClass().getBoardsList(this)
        }

    }

    //another approach but not clean
//    override fun onResume() {
//        super.onResume()
//        FirestoreClass().loadUserData(this)
//    }
}