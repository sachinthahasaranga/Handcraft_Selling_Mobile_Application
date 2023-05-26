package com.example.catchycraft

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var firstName: EditText?=null
    var lastName: EditText?=null
    private var email: EditText?=null
    private var password: EditText?=null
    private var confirmPassword: EditText?=null
    private var registerButton: Button?=null
    private var signInTextButton: TextView?=null
    private lateinit var spinner: Spinner

    private  lateinit var roles:ArrayList<String>
    private   var selectRole:String?=null
    var selectPosition:Int=0
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        firebaseAuth=FirebaseAuth.getInstance()
        firebaseFirestore= FirebaseFirestore.getInstance()
        roles= arrayListOf()
        roles.add("Select Role")
        roles.add("Seller")
        roles.add("Buyer")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        firstName=findViewById(R.id.first_name_field)
        lastName=findViewById(R.id.last_name_field)
        email=findViewById(R.id.signUp_email_field)
        password=findViewById(R.id.signUp_password_field)
        confirmPassword=findViewById(R.id.signUp_confirm_password_field)
        registerButton=findViewById(R.id.register_button)
        signInTextButton=findViewById(R.id.signIn_text_button)
        spinner=findViewById(R.id.spinner)

        if (spinner != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, roles)

            spinner.adapter = adapter
            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
                    selectPosition=position
                  if (selectPosition!=0){
                      selectRole=roles[position]
                      println(roles[position])
                  }



                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }


        registerButton?.setOnClickListener {

       if (selectPosition!=0){
           if (isValidate()){
               if (password?.text.toString().equals(confirmPassword?.text.toString())){
                   println("ddd")
                   signUp(email?.text.toString(),password?.text.toString(),firstName?.text.toString(),lastName?.text.toString())
               }else{
                   Toast.makeText(this,"Confirm Password not Correct", Toast.LENGTH_SHORT).show()
               }
           }else{
               Toast.makeText(this,"Not validate", Toast.LENGTH_SHORT).show()
           }
       }
            else{
           Toast.makeText(this,"Please select role", Toast.LENGTH_SHORT).show()
            }
        }

        signInTextButton?.setOnClickListener {
            val indent= Intent(this@SignUp,SignIn::class.java)
            startActivity(indent)
        }
    }
    private fun signUp( email:String,password:String,firstName: String,lastName: String){
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful){
                updateUser(firebaseAuth.currentUser,firstName,lastName)
                setProfile(selectRole!!,firstName+lastName,email)
                val indent=Intent(this@SignUp,SignIn::class.java)
                startActivity(indent)
            }
        }.addOnFailureListener {
            Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
        }

    }
    private fun updateUser(user: FirebaseUser?, firstName: String, lastName: String) {
        val profileUpdates = UserProfileChangeRequest.Builder().apply {
            displayName = firstName+lastName
        }.build()
        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("Successfully added")
            }
        }?.addOnFailureListener {
            Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
        }
    }
    private fun setProfile(role:String,name:String,email: String){

        val myProfile=
            firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("Profile").collection("Profile")
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "role" to role,
        )

        myProfile.add(user).addOnSuccessListener {
            Toast.makeText(this,"Successful Added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidate(): Boolean {
        if (firstName?.text?.length==0) {
            firstName?.error = "First name is required"
            return false
        }
        if (lastName?.text?.length == 0) {
            lastName?.error="Last name is required"
            return false
        }
        if (email?.text?.length == 0) {
            email?.error="Email is required"
            return false
        }
        if (password?.text?.length == 0) {
            password?.error="Password is required"
            return false
        } else if (confirmPassword?.text?.length == 0) {
            confirmPassword?.error=" Confirm Password is required"
            return false
        }
        return true
    }
}