package com.example.catchycraft

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class BuyresAccount : Fragment() {
    private lateinit var firebaseDatabase: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firebaseAuth=FirebaseAuth.getInstance()
        firebaseDatabase= FirebaseFirestore.getInstance()

        println(firebaseAuth.currentUser?.email)
        println(firebaseAuth.currentUser?.displayName)
        val view= inflater.inflate(R.layout.fragment_buyres_account, container, false)

        val logOutButton=view.findViewById<Button>(R.id.logOut_button_buyer)
        val updateButton=view.findViewById<Button>(R.id.update_button_user_profile_buyer)
        val email=view.findViewById<TextView>(R.id.email_text_buyer)
        val name=view.findViewById<TextView>(R.id.name_text_buyer)
        name.text = firebaseAuth.currentUser?.displayName
        email.text = firebaseAuth.currentUser?.email


        updateButton.setOnClickListener{
            var dialog= BottomSheetDialog(view.context)
            val view = layoutInflater.inflate(R.layout.update_user_name_sheet, null)
            val nameField=view.findViewById<EditText>(R.id.name_edit_field)
            nameField.setText(firebaseAuth.currentUser?.displayName)


            val saveButton=view.findViewById<Button>(R.id.save_name_button)

            saveButton.setOnClickListener {
                updateUser(firebaseAuth.currentUser,nameField.text.toString(),view.context)
                name.text = nameField.text.toString()
                dialog.cancel()
            }
            val deleteUserButton=view.findViewById<Button>(R.id.delete_account)
            deleteUserButton.setOnClickListener {
                val user = Firebase.auth.currentUser!!
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(ContentValues.TAG, "User account deleted.")
                            Toast.makeText(view.context,"Delete Successful", Toast.LENGTH_SHORT).show()
                        }
                    }
                val intent=Intent(view.context,SignIn::class.java)
                startActivity(intent)
            }


            dialog.setCancelable(true)
            dialog.setContentView(view)
            dialog.show()
        }


        logOutButton.setOnClickListener {
            firebaseAuth.signOut();
            Toast.makeText(view.context,"Logout Successful", Toast.LENGTH_SHORT).show()
            val intent= Intent(view.context,SignIn::class.java)
            startActivity(intent)


        }
        return view
    }

    private fun updateUser(user: FirebaseUser?, name: String, context: Context) {
        val profileUpdates = UserProfileChangeRequest.Builder().apply {
            displayName = name
        }.build()
        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context,"Name Changed Successful", Toast.LENGTH_SHORT).show()

            }
        }
    }
}