package com.example.catchycraft

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catchycraft.databinding.FragmentBuyersHomeBinding
import com.example.catchycraft.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*


class BuyersHome : Fragment() {
    private var _binding: FragmentBuyersHomeBinding? = null
    private lateinit var recycleView: RecyclerView
    private lateinit var arrayList: ArrayList<Product>
    private lateinit var myAdapter: MyAdapter
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firebaseAuth=FirebaseAuth.getInstance()
        _binding = FragmentBuyersHomeBinding.inflate(layoutInflater)
        firebaseFirestore= FirebaseFirestore.getInstance()
        loadProducts()
        val view= inflater.inflate(R.layout.fragment_buyers_home, container, false)
        recycleView=view.findViewById(R.id.buyer_all_product_view)
        recycleView.layoutManager= LinearLayoutManager(container?.context)
        arrayList= arrayListOf()
        myAdapter= MyAdapter(arrayList,view.context)
        recycleView.adapter=myAdapter
        return view
    }

    private fun loadProducts(){

        firebaseFirestore.collection("allProducts").addSnapshotListener(object :
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