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

    fun loadUserData(activity: Activity){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID()) //document for every single user
            .get()//only difference in sign in and signup
            .addOnSuccessListener { document ->
                //now from the document we can have user's data
                val loggedInUser = document.toObject(User::class.java)

                if (loggedInUser != null) {

                    when(activity){
                        is MainActivity ->
                            activity.updateNavigationUserDetails(loggedInUser)
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
                Log.e(activity.javaClass.simpleName,"Error writing document")
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