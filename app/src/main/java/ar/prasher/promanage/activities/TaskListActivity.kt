package ar.prasher.promanage.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R
import ar.prasher.promanage.adapters.TaskListItemsAdapter
import ar.prasher.promanage.firebase.FirestoreClass
import ar.prasher.promanage.models.Board
import ar.prasher.promanage.models.Card
import ar.prasher.promanage.models.Task
import ar.prasher.promanage.utils.Constants

class TaskListActivity : BaseActivity() {

    private var toolbar : Toolbar? = null
    private var rvTaskList : RecyclerView? = null

    private var mBoardDocumentId : String? = null

    private lateinit var mBoardDetails : Board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        toolbar = findViewById(R.id.toolbar_task_list_activity)

        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)
        }

        showProgressDialog("Please Wait")
        FirestoreClass().getBoardDetails(this,mBoardDocumentId)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == MEMBER_REQUEST_CODE){
            showProgressDialog("Please Wait")
            FirestoreClass().getBoardDetails(this,mBoardDocumentId)
        }else{
            Log.e("Cancelled Members Activity", "Cancelled")
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_members ->{
                val intent = Intent(this,MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL,mBoardDetails)
                startActivityForResult(intent, MEMBER_REQUEST_CODE)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar(){
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_24)
            actionBar.title = mBoardDetails.name
        }
        toolbar?.setNavigationOnClickListener { onBackPressed() }
    }

    fun boardDetails(board: Board) {
        mBoardDetails = board

        hideProgressDialog()
        setupActionBar()

        val addTaskList = Task(getString(R.string.action_add_list))
        board.taskList.add(addTaskList)

        rvTaskList = findViewById(R.id.rv_task_list)
        rvTaskList?.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true)
        rvTaskList?.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this,board.taskList)
        rvTaskList?.adapter = adapter
    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        showProgressDialog("Please Wait")
        FirestoreClass().getBoardDetails(this,mBoardDetails.documentID)
    }

    fun createTaskList(taskListName : String){
        val task = Task(taskListName,FirestoreClass().getCurrentUserID())

        mBoardDetails.taskList.add(0,task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size-1)

        showProgressDialog("Please Wait")

        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun updateTaskList(position : Int , listName:String, model: Task){
        val task = Task(listName, model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1)

        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun deleteTaskList(position : Int){
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1)

        showProgressDialog("Please Wait")

        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    fun addCardToTaskList(position : Int, cardName : String){
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size -1)

        val cardAssignedUsersList : ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserID())

        val card = Card(cardName, FirestoreClass().getCurrentUserID(), cardAssignedUsersList)

        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardsList
        )

        mBoardDetails.taskList[position] = task

        showProgressDialog("Please Wait")

        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    companion object {
        const val MEMBER_REQUEST_CODE = 32
    }

}