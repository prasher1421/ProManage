package ar.prasher.promanage.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import ar.prasher.promanage.activities.MyProfileActivity

object Constants {

    const val USERS : String = "users"
    const val BOARDS : String = "boards"
    const val GOOGLE_SIGN_IN_REQ_CODE = 1
    const val IMAGE : String = "image"
    const val NAME : String = "name"
    const val EMAIL : String = "email"
    const val MOBILE : String = "mobile"
    const val READ_STORAGE_PERMISSION_CODE = 11
    const val PICK_IMAGE_REQUEST_CODE = 22
    const val PROFILE_UPDATE_REQUEST_CODE = 21

    fun showImageChooser(activity: Activity){
        var galleryIntent = Intent(
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