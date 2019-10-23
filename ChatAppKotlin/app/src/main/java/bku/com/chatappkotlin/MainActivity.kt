package bku.com.chatappkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button_register.setOnClickListener {
            Register()
        }

        Already_have_account_textview.setOnClickListener {
            Log.d("Activitymain", "Open LoginActivity")
            // Launch login activity

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun Register(){
        val email = email_textbox_register.text.toString()
        val password = password_textbox_register.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter email and pasword again!", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("Activitymain", "Email : $email")
        Log.d("Activitymain", "Password : $password")

        // Fire base author
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener{
                    if(!it.isSuccessful) return@addOnCompleteListener
                    Log.d("Main", "Successful create user with uid: ${it.result.user.uid}")
                }
                .addOnFailureListener{
                    Log.d("Main", "Fail create user ${it.message}")

                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                }
    }
}
