package ar.prasher.promanage.activities

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R
import ar.prasher.promanage.adapters.TaskListItemsAdapter
import ar.prasher.promanage.firebase.FirestoreClass
import ar.prasher.promanage.models.Board
import ar.prasher.promanage.models.Task
import ar.prasher.promanage.utils.Constants

class TaskListActivity : BaseActivity() {

    private var toolbar : Toolbar? = null
    private var rvTaskList : RecyclerView? = null

    private var boardDocumentId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        toolbar = findViewById(R.id.toolbar_task_list_activity)

        if (intent.hasExtra(Constants.DOCUMENT_ID)){
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)
        }

        showProgressDialog("Please Wait")
        FirestoreClass().getBoardDetails(this,boardDocumentId)

    }

    private fun setupActionBar(title : String){
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.arrow_back_24)
            actionBar.title = title
        }
        toolbar?.setNavigationOnClickListener { onBackPressed() }
    }

    fun boardDetails(board: Board) {
        hideProgressDialog()
        setupActionBar(board.name)

        val addTaskList = Task(getString(R.string.action_add_list))
        board.taskList.add(addTaskList)

        rvTaskList = findViewById(R.id.rv_task_list)
        rvTaskList?.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true)
        rvTaskList?.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this,board.taskList)
        rvTaskList?.adapter = adapter
    }
}