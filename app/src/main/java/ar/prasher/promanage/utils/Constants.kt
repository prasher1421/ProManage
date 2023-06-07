package ar.prasher.promanage.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {

    const val DOCUMENT_ID = "documentId"
    const val CREATE_BOARD_REQUEST_CODE = 30
    const val USERS : String = "users"
    const val BOARDS : String = "boards"
    const val GOOGLE_SIGN_IN_REQ_CODE = 1
    const val IMAGE : String = "image"
    const val NAME : String = "name"
    const val EMAIL : String = "email"
    const val MOBILE : String = "mobile"
    const val ASSIGNED_TO : String = "assignedTo"
    const val TASK_LIST : String = "taskList"
    const val BOARD_DETAIL : String = "boardDetail"
    const val ID : String = "id"
    const val TASK_LIST_ITEM_POSITION : String = "taskListItemPosition"
    const val CARD_LIST_ITEM_POSITION : String = "cardListItemPosition"
    const val BOARD_MEMBERS_LIST : String = "boardMembersList"
    const val SELECT : String = "Select"
    const val UN_SELECT : String = "UnSelect"

    const val PROMANAGE_PREFERENCES = "ProManagePreferences"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"


    const val READ_STORAGE_PERMISSION_CODE = 11
    const val PICK_IMAGE_REQUEST_CODE = 22
    const val PROFILE_UPDATE_REQUEST_CODE = 21

    const val FCM_BASE_URL = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION = "authorization"
    const val FCM_KEY = "key"
    const val FCM_SERVER_KEY = "AAAA8OP-Vt8:APA91bGyQd0FahnOABlzrhx5Z5jO6yU-kqxrq_-zlyJRtuhyXfR1wVLlbtaQBvf-OgfvPOhsf2KQcn9t4fwRuDfqnRBsS_H2qD5XXbttRQokqwmK_nC949Z6ZQ1nrsQVpqyR1upkyYao"
    const val FCM_SENDER_ID = "1034617247455"
    const val FCM_KEY_MESSAGE = "message"
    const val FCM_KEY_TITLE = "title"
    const val FCM_KEY_DATA = "data"
    const val FCM_KEY_TO = "to"

    fun showImageChooser(activity: Activity){
        val galleryIntent = Intent(
            Intent.ACTION_PICK
            , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    //we need to understand the image type too
    fun getFileExtension(activity: Activity,uri : Uri?) : String?{
        return  MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}