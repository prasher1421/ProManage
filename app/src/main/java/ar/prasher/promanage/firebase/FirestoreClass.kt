package ar.prasher.promanage.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import ar.prasher.promanage.activities.*
import ar.prasher.promanage.models.Board
import ar.prasher.promanage.models.User
import ar.prasher.promanage.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    //we are registering user on two levels one for auth
    fun registerUser(activity: Activity, userInfo : User){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID()) //document for every single user
            .get()
            .addOnCompleteListener {task->

                if (task.isSuccessful){
                    val document = task.result

                    //if user is registered already and tries to register again

                    if (document.exists()){
                        val loggedInUser = document.toObject(User::class.java)

                        if (loggedInUser != null) {

                            when(activity){
                                is IntroActivity -> activity.signInSuccess()
                                is SignUpActivity -> activity.userRegisteredSuccess()
                            }
                        }
                    }else{
                        document.reference
                        .set(userInfo, SetOptions.merge())//merges user info that is passed to us
                            .addOnSuccessListener {
                                when (activity){
                                    is SignUpActivity -> activity.userRegisteredSuccess()
                                    is IntroActivity -> activity.signInSuccess()
                                }

                            }
                            .addOnFailureListener {
                                when(activity){
                                    is SignUpActivity ->
                                        activity.hideProgressDialog()
                                    is IntroActivity ->
                                        activity.hideProgressDialog()
                                }
                            }
                    }
                }
            }.addOnFailureListener {
                Log.e("Error Creating Document",it.toString())
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.i("Board Created Successfully",activity.javaClass.simpleName)

                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { e->
                Log.e("Board Creation Error",e.toString())
            }
    }

    fun addUpdateTaskList(activity: TaskListActivity, board: Board){
        val taskListHashMap = HashMap<String,Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentID)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.i("Task List Successfully",activity.javaClass.simpleName)

                activity.addUpdateTaskListSuccess()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.i("Task List Updating Error",it.toString())
            }
    }

    //also assign the added member to firestore database (UPDATING)
    fun assignMemberToBoard(activity: MembersActivity, board : Board, user : User){

        val assignedToHashMap = HashMap<String,Any>()

        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo
        mFireStore.collection(Constants.BOARDS)
            .document(board.documentID)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error updating assigned Member Board Details")
            }

    }

    fun updateUserProfileData(activity: Activity,
                              userHashMap: HashMap<String,Any>){

        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID()) //document for every single user
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName, "Profile Data Update")
                Toast.makeText(activity,"Profile Update",Toast.LENGTH_LONG).show()

                when(activity){
                    is MyProfileActivity -> activity.imageUpdateSuccess()
                    is EditProfileActivity -> activity.profileUpdateSuccess()
                }
            }.addOnFailureListener {
                when(activity){
                    is MyProfileActivity ->  activity.hideProgressDialog()
                    is EditProfileActivity -> activity.hideProgressDialog()
                }
                Log.e("Error while uploading",it.toString())
            }
    }

    fun getBoardDetails(activity: TaskListActivity, boardDocumentId: String?) {
        mFireStore.collection(Constants.BOARDS)
            .document(boardDocumentId!!)
            .get()
            .addOnSuccessListener { document ->

                val boardData = document.toObject(Board::class.java)

                if (boardData != null){
                    boardData.documentID = document.id
                    activity.boardDetails(boardData)
                }

            }
            .addOnFailureListener {
                Log.e(activity.javaClass.simpleName,"Error loading board document")
            }
    }

    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())//where assigned to current user
            .get()//only difference in sign in and signup
            .addOnSuccessListener { document ->
                //now from the document we can have user's data
                val boardsList : ArrayList<Board> = ArrayList()

                //this document is a snapshot which contains all documents containing assigned ones
                for (i in document.documents){

                    val board = i.toObject(Board::class.java)
                    //whatever object you have convert it tp Board object
                    board?.documentID = i.id
                    boardsList.add(board!!)
                }

                activity.populateListToUI(boardsList)

            }
            .addOnFailureListener {
                Log.e(activity.javaClass.simpleName,"Error loading boards")
            }
    }

    fun loadUserData(activity: Activity, readBoardsList : Boolean = false){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID()) //document for every single user
            .get()//only difference in sign in and signup
            .addOnSuccessListener { document ->
                //now from the document we can have user's data
                val loggedInUser = document.toObject(User::class.java)

                if (loggedInUser != null) {

                    when(activity){
                        is MainActivity ->
                            activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                        is IntroActivity ->
                            activity.signInSuccess()
                        is MyProfileActivity ->
                            activity.updateUserProfileDetails(loggedInUser)
                        is EditProfileActivity ->
                            activity.updateUserProfileDetails(loggedInUser)

                    }
                }
            }
            .addOnFailureListener {
                Log.e(activity.javaClass.simpleName,"Error loading document")
            }
    }

    fun getAssignedMembersListDetails(
        activity: MembersActivity ,
        assignedTo : ArrayList<String>
    ) {
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID,assignedTo)
            .get()
            .addOnSuccessListener {
                listDocuments ->
                Log.i("Assigned Members List",listDocuments.toString())

                val usersList : ArrayList<User> = ArrayList()

                for(i in listDocuments.documents){
                    val user = i.toObject(User::class.java)
                    usersList.add(user!!)
                }

                activity.setupMembersList(usersList)
            }
            .addOnFailureListener {
                Log.e("Error Assigned Member get()",it.toString())
            }
    }


    fun getMemberDetails(activity: MembersActivity, email: String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL,email)
            .get()
            .addOnSuccessListener {
                document ->
                if (document.documents.size>0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No Such Member Found")
                }
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e("Error while Member get()",it.toString())
            }
    }

    fun getCurrentUserID() : String{

        var currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID
    }
}