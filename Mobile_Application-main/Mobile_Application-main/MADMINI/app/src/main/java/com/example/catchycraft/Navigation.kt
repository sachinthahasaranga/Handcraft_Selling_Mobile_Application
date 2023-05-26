package com.example.catchycraft

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*

class Navigation : AppCompatActivity() {
    private var firebaseAuth: FirebaseAuth
    private var firebaseFirestore: FirebaseFirestore
    init {
        firebaseAuth=FirebaseAuth.getInstance()
        firebaseFirestore=FirebaseFirestore.getInstance()
    }
    private  var role:String?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        isHasUser(firebaseAuth.currentUser)



    }


    private fun isHasUser(user: FirebaseUser?){
        if (user!=null){

            firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("Profile").collection("Profile").addSnapshotListener(object :
                EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error!=null){
                        Log.e("err",error.message.toString())
                        return
                    }
                    for (doc : DocumentChange in value?.documentChanges!! ){
                        role=  doc.document["role"].toString()
                        if (role=="Seller"){

                            println("_________________________> seller")
                            val indent= Intent(this@Navigation,MainActivity::class.java)
                            startActivity(indent)

                        }else if (role=="Buyer"){

                            println("_________________________> Buyer")

                            val indent= Intent(this@Navigation,BuyerBottomNavigation::class.java)
                            startActivity(indent)

                        }else{
                            val indent= Intent(this@Navigation,SignIn::class.java)
                            startActivity(indent)

                        }
                    }
                }

            })

        }else{

            val indent= Intent(this@Navigation,SignIn::class.java)
            startActivity(indent)
        }

    }


}