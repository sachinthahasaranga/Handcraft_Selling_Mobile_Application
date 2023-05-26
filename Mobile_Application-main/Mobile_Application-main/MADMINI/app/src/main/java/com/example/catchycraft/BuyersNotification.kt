package com.example.catchycraft

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catchycraft.databinding.FragmentBuyersHomeBinding
import com.example.catchycraft.databinding.FragmentBuyersNotificationBinding
import com.example.catchycraft.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*


class BuyersNotification : Fragment() {
    private var _binding: FragmentBuyersNotificationBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private  lateinit var imageView: ImageView
    private lateinit var circleIndicator: ProgressBar
    private lateinit var url:String
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var title: EditText
    private lateinit var description: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var amount: EditText
    private lateinit var recycleView: RecyclerView
    private lateinit var arrayList: ArrayList<Product>
    private lateinit var myAdapter: promotionAdapter



    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? { firebaseAuth=FirebaseAuth.getInstance()
        _binding = FragmentBuyersNotificationBinding.inflate(layoutInflater)
        firebaseFirestore= FirebaseFirestore.getInstance()
        loadProducts()
        val view=inflater.inflate(R.layout.fragment_buyers_notification, container, false)
        recycleView=view.findViewById(R.id.buyer_app_promotion_view)
        recycleView.layoutManager= LinearLayoutManager(container?.context)
        arrayList= arrayListOf()
        myAdapter= promotionAdapter(arrayList,view.context)
        recycleView.adapter=myAdapter
        return view
    }
    private fun loadProducts(){

        firebaseFirestore.collection("allPromotions").addSnapshotListener(object :
            EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error!=null){
                    Log.e("err",error.message.toString())
                    Toast.makeText(context,"No Data Found", Toast.LENGTH_SHORT).show()
                    return
                }
                for (doc : DocumentChange in value?.documentChanges!! ){
                    println(  doc.document.id)
                    if (doc.type== DocumentChange.Type.ADDED){
                        arrayList.add(doc.document.toObject(Product::class.java))
                    }
                }
                myAdapter.notifyDataSetChanged()

            }

        })

    }

}