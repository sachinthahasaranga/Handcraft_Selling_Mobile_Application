package com.example.catchycraft

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*


class Notification : Fragment() {
    private lateinit var spinner:Spinner
    private lateinit var languages : ArrayList<Product>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private  lateinit var amount: String
    private  lateinit var title: String
    private  lateinit var url: String
    private  lateinit var description: String
    private  lateinit var phoneNumber: String
     private var initialValue: Int = 0
    private lateinit var promotionAmount:EditText
    private lateinit var promotionTitle:EditText
    private lateinit var recycleView: RecyclerView
    private lateinit var arrayList: ArrayList<Product>
    private lateinit var myAdapter: promotionAdapter
    private  lateinit var idGlobal: String
    private  lateinit var deleteId: String
    private lateinit var updatePromotionTitle:EditText
    private lateinit var updatePromotionAmount:EditText



    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseFirestore= FirebaseFirestore.getInstance()
        firebaseAuth=FirebaseAuth.getInstance()
        languages= arrayListOf()
        languages.add(Product("amount","dis","0763350366","0","Select product","url",))
        loadProducts()
        loadPromotion()
        val view=inflater.inflate(R.layout.fragment_notification, container, false)

        recycleView=view.findViewById(R.id.list_promotions)
        recycleView.layoutManager= LinearLayoutManager(container?.context)
        arrayList= arrayListOf()
        myAdapter= promotionAdapter(arrayList,view.context)
        recycleView.adapter=myAdapter
        val addPromotionButton=view.findViewById<FloatingActionButton>(R.id.add_notification_button)

        myAdapter.setOnClickListener(object :promotionAdapter.OnClickListener{
            override fun onClick(position: Int, model: Product) {

                val dialog = container?.context?.let { BottomSheetDialog(it) }

                val view = layoutInflater.inflate(R.layout.update_promotion_bottom_sheet, null)
                val deleteButton=view.findViewById<Button>(R.id.delete_promotion_button)
                val updateButton=view.findViewById<Button>(R.id.update_promotion_button)
                updatePromotionTitle=view.findViewById<EditText>(R.id.promotion_title_edit_text)
                updatePromotionAmount=view.findViewById<EditText>(R.id.promotion_amount_edit_text)
                updatePromotionTitle.setText(model.description)
                updatePromotionAmount.setText(model.phoneNumber)

                deleteButton.setOnClickListener{
                    if (model.id!=null){
                        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myPromotions").
                        collection("myPromotions").document(model.id!!).
                        addSnapshotListener{ documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                            if (documentSnapshot != null) {

                                deleteId=documentSnapshot.data?.get("idGlobal").toString()
                                firebaseFirestore.collection("allPromotions").document(deleteId).delete()
                            }

                        }

                        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myPromotions").collection("myPromotions").document(model.id!!).delete()
                        Toast.makeText(context,"Successful Deleted", Toast.LENGTH_SHORT).show()
                        arrayList.removeAt(position)
                        myAdapter.notifyDataSetChanged()


                    }

                    dialog?.cancel()

                }

                updateButton.setOnClickListener{


                    val amnt=(model.amount.toString().toInt()+model.phoneNumber.toString().toInt())-updatePromotionAmount.text.toString().toInt()
                    if((model.amount.toString().toInt()+model.phoneNumber.toString().toInt())>updatePromotionAmount.text.toString().toInt()){
                        val updates = hashMapOf<String, Any>(
                            "description" to updatePromotionTitle.text.toString(),
                            "phoneNumber" to updatePromotionAmount.text.toString(),
                            "amount" to amnt.toString()

                        )
                        if (model.id!=null){
                            firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myPromotions").
                            collection("myPromotions").document(model.id!!).
                            addSnapshotListener{ documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                                if (documentSnapshot != null) {

                                    deleteId=documentSnapshot.data?.get("idGlobal").toString()
                                    firebaseFirestore.collection("allPromotions").document(deleteId).update(updates).addOnSuccessListener {
                                        Toast.makeText(context,"Successful Updated",Toast.LENGTH_SHORT).show()
                                        arrayList[position].description=updatePromotionTitle.text.toString()
                                        arrayList[position].phoneNumber=updatePromotionAmount.text.toString()
                                        arrayList[position].amount=amnt.toString()
                                    }
                                }

                            }
                            firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myPromotions").collection("myPromotions").document(
                                model.id!!).update(updates).addOnSuccessListener{
                                Toast.makeText(context,"Successful Updated",Toast.LENGTH_SHORT).show()
                                arrayList[position].description=updatePromotionTitle.text.toString()
                                arrayList[position].phoneNumber=updatePromotionAmount.text.toString()
                                arrayList[position].amount=amnt.toString()
                                myAdapter.notifyDataSetChanged()
                                dialog?.cancel()
                            }



                        }

                    }else{
                        Toast.makeText(it.context,"Promotion amount should be lower than net amount", Toast.LENGTH_SHORT).show()
                    }



                }


                dialog?.setCancelable(true)
                dialog?.setContentView(view)
                dialog?.show()

            }})

        addPromotionButton.setOnClickListener {
            val dialog = container?.context?.let { BottomSheetDialog(it) }
            val notificationView = layoutInflater.inflate(R.layout.add_notification_bottom_sheet, null)
            spinner = notificationView.findViewById<Spinner>(R.id.product_spinner)
           promotionTitle = notificationView.findViewById<EditText>(R.id.promotion_title_field)
           promotionAmount = notificationView.findViewById<EditText>(R.id.promotion_amount_field)
            val  submitButton = notificationView.findViewById<Button>(R.id.promotion_text_button)
            if (spinner != null) {
                val adapter = ArrayAdapter(view.context,
                    android.R.layout.simple_spinner_item, languages)
                spinner.adapter = adapter
                spinner.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>,
                                                view: View, position: Int, id: Long) {
                        initialValue=position
                        if(position!=0){
                            amount= languages[position].amount.toString()
                            title=languages[position].title
                            description=promotionTitle.text.toString()
                            url=languages[position].url.toString()
                            phoneNumber=languages[position].phoneNumber.toString()

                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // write code to perform some action
                    }
                }
            }
            submitButton.setOnClickListener {
                if(initialValue!=0){
                    if (isValidate()){
                            if (amount.toInt()>promotionAmount.text.toString().toInt()){
                                val promotionprice=(amount.toInt()-promotionAmount.text.toString().toInt())

                                addPromotion(title,promotionTitle.text.toString(),promotionAmount.text.toString(),promotionprice.toString(),url,dialog)

                            } else{
                                Toast.makeText(it.context,"Promotion amount should be lower than net amount", Toast.LENGTH_SHORT).show()
                            }

                        }
                }else{
                    Toast.makeText(it.context,"Please select product", Toast.LENGTH_SHORT).show()
                }



            }


            dialog?.setCancelable(true)
            dialog?.setContentView(notificationView)
            dialog?.show()
        }


        return view
    }

    private fun loadProducts(){
        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myProducts").collection("myProducts").addSnapshotListener(object :
            EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error!=null){
                    Log.e("err",error.message.toString())
                    Toast.makeText(context,"No Data Found",Toast.LENGTH_SHORT).show()
                    return
                }
                for (doc : DocumentChange in value?.documentChanges!! ){

                    languages.add(Product(doc.document.get("amount").toString(),doc.document.get("description").toString(),doc.document.get("phoneNumber").toString(),doc.document.get("id").toString(),doc.document.get("title").toString(),doc.document.get("url").toString()))
                }
            }

        })
    }



    private fun loadPromotion(){

        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myPromotions").collection("myPromotions").addSnapshotListener(object :
            EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error!=null){
                    Log.e("err",error.message.toString())
                    Toast.makeText(context,"No Data Found",Toast.LENGTH_SHORT).show()
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

    private fun addPromotion(title:String,description:String,phoneNumber:String,amount:String,url:String,bottomSheet:BottomSheetDialog?){
        val refMyProduct=
            firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myPromotions").collection("myPromotions")
        val refAllProduct=
            firebaseFirestore.collection("allPromotions")
        val user = hashMapOf(
            "title" to title,
            "description" to description,
            "phoneNumber" to phoneNumber,
            "url" to url,
            "amount" to amount

        )
        refMyProduct.add(user).addOnSuccessListener { documentReference ->
            refMyProduct.document(documentReference.id).update("id",documentReference.id)
            idGlobal=documentReference.id
            Toast.makeText(context,"Promotion Successful Added", Toast.LENGTH_SHORT).show()

            Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
        }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
                Toast.makeText(context,e.message, Toast.LENGTH_SHORT).show()
            }

        refAllProduct.add(user).addOnSuccessListener { documentReference ->
            refAllProduct.document(documentReference.id).update("id",documentReference.id)
            refMyProduct.document(idGlobal).update("idGlobal",documentReference.id)
            Toast.makeText(context,"Promotion Successful Added", Toast.LENGTH_SHORT).show()
            bottomSheet?.cancel()

            Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
        }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
                Toast.makeText(context,e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun isValidate(): Boolean {
        if (promotionTitle?.text?.length==0) {
            promotionTitle?.error = "Promotion title is required"
            return false
        }
        if (promotionAmount?.text?.length == 0) {
            promotionAmount?.error="Promotion title is required"
            return false
        }

        return true
    }




}