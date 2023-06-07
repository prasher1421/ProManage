package ar.prasher.promanage.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R
import ar.prasher.promanage.adapters.MemberListItemsAdapter
import ar.prasher.promanage.firebase.FirestoreClass
import ar.prasher.promanage.models.Board
import ar.prasher.promanage.models.User
import ar.prasher.promanage.utils.Constants
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MembersActivity : BaseActivity() {

    private var toolbar : Toolbar? = null
    private var rvMembersList : RecyclerView? = null

    private lateinit var mBoardDetails : Board
    private lateinit var mAssignedMemberList : ArrayList<User>

    private var anyChangesMade :Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        toolbar = findViewById(R.id.toolbar_members_activity)

        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        setupActionBar()

        showProgressDialog("Please Wait")
        FirestoreClass().getAssignedMembersListDetails(this,mBoardDetails.assignedTo)
    }

    override fun onBackPressed() {
        if (anyChangesMade){
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_add_member->{
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        val tvAdd : TextView? = dialog.findViewById(R.id.tv_add)
        val tvCancel : TextView? = dialog.findViewById(R.id.tv_cancel)
        val etEmailSearchMember : AppCompatEditText? = dialog.findViewById(R.id.et_email_search_member)

        tvAdd?.setOnClickListener {
            Log.i("Dialog Add clicked","Once")
            val email = etEmailSearchMember?.text.toString()
            if (email.isNotEmpty()){
                dialog.dismiss()

                showProgressDialog("Please Wait")
                FirestoreClass().getMemberDetails(this,email)


            }else{
                Toast.makeText(
                    this,
                    "Please Enter Email Address",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        tvCancel?.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun memberDetails(user: User){
        mBoardDetails.assignedTo.add(user.id)
        FirestoreClass().assignMemberToBoard(this,mBoardDetails,user)
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_24)
            actionBar.title = getString(R.string.members)
        }
        toolbar?.setNavigationOnClickListener { onBackPressed() }
    }

    fun setupMembersList(list : ArrayList<User>){
        mAssignedMemberList = list

        hideProgressDialog()
        rvMembersList = findViewById(R.id.rv_members_list)
        rvMembersList?.layoutManager = LinearLayoutManager(this)
        rvMembersList?.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this, list)
        rvMembersList?.adapter = adapter
    }

    fun memberAssignSuccess(user : User){
        hideProgressDialog()
        mAssignedMemberList.add(user)
        anyChangesMade =true
        setupMembersList(mAssignedMemberList)
        SendNotificationToUserAsyncTask(mBoardDetails.name,user.fcmToken)
            .execute()
    }

    private inner class SendNotificationToUserAsyncTask(boardName : String, token : String)
        : AsyncTask<Any, Void, String>() {

        val boardName = boardName
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog("Please Wait")
        }
        override fun doInBackground(vararg params: Any?): String {
            var result : String
            var connection : HttpURLConnection? = null

            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"

                connection.setRequestProperty("Content-type","application/json")
                connection.setRequestProperty("charset","utf-8")
                connection.setRequestProperty("accept","application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION,
                    "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )
                connection.useCaches = false

                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE,"Board Assigned $boardName")
                dataObject.put(Constants.FCM_KEY_MESSAGE,
                    "You have been assigned to the Board by ${mAssignedMemberList[0].name}")

                jsonRequest.put(Constants.FCM_KEY_DATA,dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO,dataObject)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK){
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var line : String?

                    try {
                        while (reader.readLine().also { line = it } != null){
                            sb.append(line+"\n")
                        }
                    }catch (e : IOException){
                        e.printStackTrace()
                    }finally {
                        try{
                            inputStream.close()
                        }catch (e : IOException){
                            e.printStackTrace()
                        }
                    }

                    result = sb.toString()

                }else {
                    result = connection.responseMessage
                }

            }catch (e : SocketTimeoutException){
                result = "Connection Timeout"
            }catch (e : Exception){
                result = "Error : " + e.message
            }finally {
                connection?.disconnect()
            }


            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            hideProgressDialog()
        }
    }
}