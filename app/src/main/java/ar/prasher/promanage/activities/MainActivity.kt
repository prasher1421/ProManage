package ar.prasher.promanage.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import ar.prasher.promanage.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var toolbar : Toolbar? = null
    private var drawerLayout : DrawerLayout? = null
    private var navView : NavigationView? = null


    // declare the GoogleSignInClient
    lateinit var mGoogleSignInClient: GoogleSignInClient
    // val auth is initialized by lazy
//    private val auth by lazy {
//        FirebaseAuth.getInstance()
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar_main_activity)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        setupActionBar()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)
        // pass the same server client ID used while implementing the LogIn feature earlier.

        navView?.setNavigationItemSelectedListener(this)


    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar)
        toolbar?.setNavigationOnClickListener {
            //Toggle Drawer
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile -> {
                Toast.makeText(this@MainActivity,"Profile Section",Toast.LENGTH_SHORT).show()
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
}