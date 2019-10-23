package bku.com.chatappkotlin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

/**
 * Created by Admin on 10/22/2019.
 */
class LoginActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_login.setOnClickListener {
            val email = email_login_text.text.toString()
            val password = password_login_text.text.toString()

            Log.d("LoginActivity", "Email : $email")
            Log.d("LoginActivity", "Password : $password")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        Log.d("Main", "Successful login with $email")
                    }
                    .addOnFailureListener {
                        Log.d("Main", "Login fail ${it.message}")

                        Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                    }
        }

        create_new_account_textview.setOnClickListener {
            finish()
        }
    }
}