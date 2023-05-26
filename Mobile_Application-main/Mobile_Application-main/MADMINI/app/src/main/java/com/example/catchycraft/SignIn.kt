package com.example.catchycraft

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignIn : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private var email: EditText?=null
    private var password: EditText?=null
    private var signUpTextButton: TextView?=null
    private var signInButton: Button?=null

    override fun onBackPressed() {
        return
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        firebaseAuth=FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        email=findViewById(R.id.signIn_email_field)
        password=findViewById(R.id.signIn_password_field)
        signUpTextButton=findViewById(R.id.signUp_text_button)
        signInButton=findViewById(R.id.signIn_button)

        signUpTextButton?.setOnClickListener{
            val indent= Intent(this@SignIn,SignUp::class.java)
            startActivity(indent)
        }
        signInButton?.setOnClickListener{
            if (isValidate()){
                signUp(email?.text.toString(),password?.text.toString())
            }

        }
    }


    private fun isValidate(): Boolean {

        if (email?.text?.length == 0) {
            email?.error="Email is required"
            return false
        }
        if (password?.text?.length == 0) {
            password?.error="Password is required"
            return false
        }
        return true
    }
    private fun signUp( email:String,password:String){
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(this,"Successfully Login", Toast.LENGTH_SHORT).show()

                val indent=Intent(this@SignIn,Navigation::class.java)
                startActivity(indent)
            }
        }.addOnFailureListener {
            Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
        }

    }
    private fun isHasUser(user: FirebaseUser?){
        if (user!=null){
            val indent=Intent(this@SignIn,MainActivity::class.java)
            startActivity(indent)
        }else{
            val indent=Intent(this@SignIn,SignUp::class.java)
            startActivity(indent)
        }

    }
}