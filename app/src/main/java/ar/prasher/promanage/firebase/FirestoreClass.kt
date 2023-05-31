package ar.prasher.promanage.firebase

import android.app.Activity
import android.util.Log
import ar.prasher.promanage.activities.IntroActivity
import ar.prasher.promanage.activities.SignUpActivity
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


    fun signInUser(activity: IntroActivity){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID()) //document for every single user
            .get()//only difference in sign in and signup
            .addOnSuccessListener { document ->
                //now from the document we can have user's data
                val loggedInUser = document.toObject(User::class.java)

                if (loggedInUser != null) {
                    activity.signInSuccess()
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