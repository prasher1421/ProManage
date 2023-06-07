package ar.prasher.promanage.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ar.prasher.promanage.R
import ar.prasher.promanage.adapters.CardMemberListItemsAdapter
import ar.prasher.promanage.adapters.MemberListItemsAdapter
import ar.prasher.promanage.dialogs.LabelColorListDialog
import ar.prasher.promanage.dialogs.MemberListDialog
import ar.prasher.promanage.firebase.FirestoreClass
import ar.prasher.promanage.models.*
import ar.prasher.promanage.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {

    private var toolbar : Toolbar? = null
    private var etNameCardDetails :AppCompatEditText? = null
    private var btnUpdateCardDetails :TextView? = null
    private var tvSelectLabelColor :TextView? = null
    private var tvSelectMembers :TextView? = null
    private var tvSelectDueDate :TextView? = null
    private var rvSelectedMemberList :RecyclerView? = null

    private lateinit var mBoardDetails : Board
    private lateinit var mAssignedMemberDetailsList : ArrayList<User>
    private var mSelectedDueDateMilliseconds : Long = 0
    private var mTaskListPosition = -1
    private var mCardPosition = -1
    private var mSelectedColor = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

        toolbar = findViewById(R.id.toolbar_card_details_activity)
        etNameCardDetails = findViewById(R.id.et_name_card_details)
        btnUpdateCardDetails = findViewById(R.id.btn_update_card_details)
        tvSelectLabelColor = findViewById(R.id.tv_select_label_color)
        tvSelectMembers = findViewById(R.id.tv_select_members)
        tvSelectDueDate = findViewById(R.id.tv_select_due_date)

        getIntentData()
        setupActionBar()

        mSelectedColor = mBoardDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition].labelColor

        etNameCardDetails?.setText(mBoardDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition].name)
        etNameCardDetails?.setSelection(etNameCardDetails?.text.toString().length)


        mSelectedColor = mBoardDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition]
            .labelColor

        if (mSelectedColor.isNotEmpty()){
            setColor()
        }

        btnUpdateCardDetails?.setOnClickListener {
            if (etNameCardDetails?.text.toString().isNotEmpty()){
                updateCardDetails()
            }else{
                Toast.makeText(
                    this,
                    "Please Enter a Card Name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        tvSelectLabelColor?.setOnClickListener {
            labelColorsListDialog()
        }
        tvSelectMembers?.setOnClickListener {
            membersListDialog()
        }

        setupSelectedMembersList()

        mSelectedDueDateMilliseconds = mBoardDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition]
            .dueDate

        if (mSelectedDueDateMilliseconds > 0){
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val date = sdf.format(Date(mSelectedDueDateMilliseconds))
            tvSelectDueDate?.text = date
        }

        tvSelectDueDate?.setOnClickListener {
            showDatePicker()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.action_delete_card ->{
                alertDialogForDeleteCard(
                    mBoardDetails
                    .taskList[mTaskListPosition]
                    .cards[mCardPosition]
                    .name
                )
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
            actionBar.title = mBoardDetails
                .taskList[mTaskListPosition]
                .cards[mCardPosition].name
        }
        toolbar?.setNavigationOnClickListener { onBackPressed() }
    }

    //extracting everything out of intent
    private fun getIntentData(){
        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails =  intent.getParcelableExtra(
                Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition =  intent.getIntExtra(
                Constants.TASK_LIST_ITEM_POSITION,-1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition =  intent.getIntExtra(
                Constants.CARD_LIST_ITEM_POSITION,-1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mAssignedMemberDetailsList =  intent.getParcelableArrayListExtra(
                Constants.BOARD_MEMBERS_LIST)!!
        }

    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun membersListDialog(){
        val cardAssignedMembersList = mBoardDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition]
            .assignedTo

        if (cardAssignedMembersList.size > 0){
            for( i in mAssignedMemberDetailsList.indices){
                for (j in cardAssignedMembersList){
                    if (mAssignedMemberDetailsList[i].id == j){
                        mAssignedMemberDetailsList[i].selected = true
                    }
                }
            }
        }else{
            for(i in mAssignedMemberDetailsList.indices){
                mAssignedMemberDetailsList[i].selected = false
            }
        }

        val  listDialog = object : MemberListDialog(
            this,
            mAssignedMemberDetailsList,
            resources.getString(R.string.select_members)
        ){
            override fun onItemSelected(user: User, action: String) {

                if (action == Constants.SELECT){
                    if (!mBoardDetails
                            .taskList[mTaskListPosition]
                            .cards[mCardPosition]
                            .assignedTo
                            .contains(user.id)
                    ){//if doesn't contain then add
                        mBoardDetails
                            .taskList[mTaskListPosition]
                            .cards[mCardPosition]
                            .assignedTo
                            .add(user.id)
                    }
                }else{//get rid of the member
                    mBoardDetails
                        .taskList[mTaskListPosition]
                        .cards[mCardPosition]
                        .assignedTo
                        .remove(user.id)

                    for (i in mAssignedMemberDetailsList.indices){
                        if(mAssignedMemberDetailsList[i].id == user.id){
                            mAssignedMemberDetailsList[i].selected = false
                        }
                    }
                }
                setupSelectedMembersList()

            }
        }
        listDialog.show()
    }

    private fun updateCardDetails(){
        val card = Card(
            etNameCardDetails?.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliseconds
        )

        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog("Please Wait")
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    private fun deleteCard(){
        val cardsList = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardPosition)

        val taskList = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        taskList[mTaskListPosition].cards = cardsList

        mBoardDetails.taskList = taskList
        showProgressDialog("Please Wait")
        FirestoreClass().addUpdateTaskList(this,mBoardDetails)
    }

    private fun alertDialogForDeleteCard(cardName: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $cardName")
        builder.setIcon(R.drawable.ic_delete)
        builder.setPositiveButton("Yes"){
                dialogInterface,_ ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton("No"){
                dialogInterface,_ ->
            dialogInterface.dismiss()
        }
        val alertDialog : AlertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()
    }

    private fun colorsList(): ArrayList<String>{
        val colorsList : ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    private fun setColor(){
        tvSelectLabelColor?.text = ""
        tvSelectLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun labelColorsListDialog(){
        val colorsList = colorsList()
        val listDialog = object : LabelColorListDialog(
            this,
            colorsList,
            resources.getString(R.string.select_label_color),
            mSelectedColor
        ){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    private fun setupSelectedMembersList(){
        val cardAssignedMembersList =
            mBoardDetails
                .taskList[mTaskListPosition]
                .cards[mCardPosition]
                .assignedTo

        val selectedMembersList : ArrayList<SelectedMembers> = ArrayList()

        for( i in mAssignedMemberDetailsList.indices){
            for (j in cardAssignedMembersList){
                if (mAssignedMemberDetailsList[i].id == j){
                    val selectedMember = SelectedMembers(
                        mAssignedMemberDetailsList[i].id,
                        mAssignedMemberDetailsList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0){
            selectedMembersList.add(SelectedMembers("",""))
            rvSelectedMemberList = findViewById(R.id.rv_selected_members_list)
            tvSelectMembers?.visibility = View.GONE
            rvSelectedMemberList?.visibility = View.VISIBLE

            rvSelectedMemberList?.layoutManager = GridLayoutManager(
                this,6
            )
            val adapter = CardMemberListItemsAdapter(
                this,
                selectedMembersList,
                true
            )
            rvSelectedMemberList?.adapter = adapter
            adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                override fun onClick() {
                    membersListDialog()
                }
            })

        }else{
            tvSelectMembers?.visibility = View.VISIBLE
            rvSelectedMemberList?.visibility = View.GONE
        }
    }

    private fun showDatePicker(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                val sMonthOfYear =
                    if ((monthOfYear + 1)<10) "0${monthOfYear+1}" else "${monthOfYear + 1}"
                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                tvSelectDueDate?.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                mSelectedDueDateMilliseconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show()

    }

}