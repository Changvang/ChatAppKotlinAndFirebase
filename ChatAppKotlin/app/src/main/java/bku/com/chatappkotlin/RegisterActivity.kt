package bku.com.chatappkotlin

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.os.Parcel




class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Already_have_account_textview.setOnClickListener {
            Log.d("RegisterActivity", "Open LoginActivity")
            // Launch login activity

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        button_register.setOnClickListener {
            Register()
        }

        upload_photo_button.setOnClickListener{
            Log.d("RegisterActivity", "Try to upload photo")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("RegisterActivity", "upload success")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            Image_circle_border.setImageBitmap(bitmap)
            upload_photo_button.alpha = 0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            upload_photo_button.setBackgroundDrawable(bitmapDrawable)

        }
    }

    private fun Register(){
        val email = email_textbox_register.text.toString()
        val password = password_textbox_register.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter email and pasword again!", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email : $email")
        Log.d("RegisterActivity", "Password : $password")

        // Fire base author
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener{
                    if(!it.isSuccessful) return@addOnCompleteListener
                    Log.d("RegisterActivity", "Successful create user with uid: ${it.result.user.uid}")
                    upLoadPhotoToFireBase()
                }
                .addOnFailureListener{
                    Log.d("RegisterActivity", "Fail create user ${it.message}")

                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                }

    }

    private fun upLoadPhotoToFireBase(){
        val image_name = UUID.randomUUID()
        val ref = FirebaseStorage.getInstance().getReference("/images/" + image_name.toString())
        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Upload image successfull ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("RegisterActivity", "Link : $it")

                        StoreInfoUserToDatabase(it.toString())
                    }
                }
                .addOnFailureListener{
                    Log.d("RegisterActivity", "Upload image fail ${it}")
                }
    }

    private fun StoreInfoUserToDatabase(Url_image: String){
        val uid =FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid.toString(), Url_image, name_textbox_register.text.toString())
        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("Register", "Successful to save image to database")
                    // Chuyen qua MessageActivity
                    val intent = Intent(this, MessageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)


                }
                .addOnFailureListener{
                    Log.d("Register", "Fail to save image to database")
                }
    }

}


class User(val uid: String, val url_image : String, val userName: String ):Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    constructor(): this("","","")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uid)
        parcel.writeString(url_image)
        parcel.writeString(userName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

}