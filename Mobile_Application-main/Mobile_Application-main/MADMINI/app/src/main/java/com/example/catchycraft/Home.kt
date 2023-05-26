package com.example.catchycraft

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.catchycraft.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage


class Home : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private  lateinit var imageView: ImageView
    private lateinit var circleIndicator:ProgressBar
    private  var url:String?=null
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var title:EditText
    private lateinit var description:EditText
    private lateinit var phoneNumber:EditText
    private lateinit var amount:EditText
    private lateinit var updateProductTitle:EditText
    private lateinit var updateProductDescription:EditText
    private lateinit var updateProductAmount:EditText
    private lateinit var updateProductContact:EditText
    private lateinit var recycleView: RecyclerView
    private lateinit var arrayList: ArrayList<Product>
    private lateinit var myAdapter: MyAdapter
    private  lateinit var idGlobal: String
    private lateinit var documentId:String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firebaseAuth=FirebaseAuth.getInstance()
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        firebaseFirestore= FirebaseFirestore.getInstance()

        loadProducts()
        val view= inflater.inflate(R.layout.fragment_home, container, false)
        recycleView=view.findViewById(R.id.list_view)
        recycleView.layoutManager= LinearLayoutManager(container?.context)
        arrayList= arrayListOf()
        myAdapter= MyAdapter(arrayList,view.context)
        recycleView.adapter=myAdapter
        myAdapter.setOnClickListener(object :MyAdapter.OnClickListener{
            override fun onClick(position: Int, model: Product) {

                val dialog = container?.context?.let { BottomSheetDialog(it) }

                val view = layoutInflater.inflate(R.layout.update_product_bottom_heet, null)

                val deleteButton=view.findViewById<Button>(R.id.product_delete_button)

                deleteButton.setOnClickListener{
                    if (model.id!=null){
                        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myProducts").
                        collection("myProducts").document(model.id!!).
                        addSnapshotListener{ documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                            if (documentSnapshot != null) {

                                documentId=documentSnapshot.data?.get("idGlobal").toString()
                                firebaseFirestore.collection("allProducts").document(documentId!!).delete()
                            }

                        }

                        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myProducts").collection("myProducts").document(model.id!!).delete()
                        Toast.makeText(context,"Successful Deleted", Toast.LENGTH_SHORT).show()
                        arrayList.removeAt(position)
                        myAdapter.notifyDataSetChanged()


                    }

                    dialog?.cancel()

                }



                val updateButton=view.findViewById<Button>(R.id.product_update_button)
                updateProductTitle=view.findViewById<EditText>(R.id.product_title_field)
                updateProductDescription=view.findViewById<EditText>(R.id.product_discription_field)
                 updateProductAmount=view.findViewById<EditText>(R.id.product_amount_field)
                  updateProductContact=view.findViewById<EditText>(R.id.product_contact_field)

                updateProductTitle.setText(model.title)
                updateProductDescription.setText(model.description)
                updateProductAmount.setText(model.amount)
                updateProductContact.setText(model.phoneNumber)



                updateButton.setOnClickListener{
                    val update = hashMapOf(
                        "title" to updateProductTitle.text.toString(),
                        "description" to updateProductDescription.text.toString(),
                        "phoneNumber" to updateProductContact.text.toString(),
                        "amount" to updateProductAmount.text.toString()
                    )
                    if (model.id!=null){
                        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myProducts").
                        collection("myProducts").document(model.id!!).
                        addSnapshotListener{ documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                            if (documentSnapshot != null) {

                                documentId=documentSnapshot.data?.get("idGlobal").toString()
                                firebaseFirestore.collection("allProducts").document(documentId).update(
                                    update as Map<String, Any>
                                ).addOnSuccessListener {

                                    arrayList[position].title=updateProductTitle.text.toString()
                                    arrayList[position].description=updateProductDescription.text.toString()
                                    arrayList[position].amount= updateProductAmount.text.toString()
                                    arrayList[position].phoneNumber=updateProductContact.text.toString()

                                }
                            }

                        }
                        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myProducts").collection("myProducts").document(
                            model.id!!).update(update as Map<String, Any>).addOnSuccessListener{

                            arrayList[position].title=updateProductTitle.text.toString()
                            arrayList[position].description=updateProductDescription.text.toString()
                            arrayList[position].amount= updateProductAmount.text.toString()
                            arrayList[position].phoneNumber=updateProductContact.text.toString()
                            myAdapter.notifyDataSetChanged()
                            Toast.makeText(context,"Successful Updated",Toast.LENGTH_SHORT).show()
                            dialog?.cancel()
                        }



                    }


                }


                dialog?.setCancelable(true)
                dialog?.setContentView(view)
                dialog?.show()

            }})

        val addButton=view.findViewById<FloatingActionButton>(R.id.add_product_button)
        addButton.setOnClickListener {
            val dialog = container?.context?.let { BottomSheetDialog(it) }
            val productView = layoutInflater.inflate(R.layout.add_product_bottom_sheet, null)
            dialog?.setCancelable(true)
            dialog?.setContentView(productView)
            dialog?.show()


            val uploadButton=productView.findViewById<Button>(R.id.upload_picture_button)
             circleIndicator=productView.findViewById(R.id.progressBar)
            circleIndicator.visibility=View.GONE
             imageView=productView.findViewById(R.id.upload_image_view)

             title=productView.findViewById<EditText>(R.id.item_text)
             description=productView.findViewById<EditText>(R.id.discription_text)
             phoneNumber=productView.findViewById<EditText>(R.id.phone_number_text)
            amount=productView.findViewById<EditText>(R.id.amount_text)

            val submitButton=productView.findViewById<Button>(R.id.submit_button)

            uploadButton.setOnClickListener{
                circleIndicator.visibility=View.VISIBLE
                uploadButton.visibility=View.GONE
                val galleryIntent = Intent(Intent.ACTION_PICK)
                // here item is type of image
                galleryIntent.type = "image/*"
                // ActivityResultLauncher callback
                imagePickerActivityResult.launch(galleryIntent)

            }
            submitButton.setOnClickListener{
                if (isValidate()){
                    if (dialog != null) {
                        if (url!=null){
                            addProduct(title.text.toString(),description.text.toString(),phoneNumber.text.toString(),amount.text.toString(),url!!,dialog)
                        }else{
                            Toast.makeText(context,"Please upload photo",Toast.LENGTH_SHORT).show()
                        }

                    }
                }


            }
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
                    println(  doc.document.id)
                    if (doc.type== DocumentChange.Type.ADDED){
                        arrayList.add(doc.document.toObject(Product::class.java))
                    }
                }
                myAdapter.notifyDataSetChanged()

            }

        })

    }

    private var imagePickerActivityResult: ActivityResultLauncher<Intent> =
    // lambda expression to receive a result back, here we
        // receive single item(photo) on selection
        registerForActivityResult( ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null) {
                // getting URI of selected Image
                val imageUri: Uri? = result.data?.data

                // val fileName = imageUri?.pathSegments?.last()

                // extract the file name with extension
                val sd = context?.let { getFileName(it, imageUri!!) }
                val storageRef=FirebaseStorage.getInstance().reference

                // Upload Task with upload to directory 'file'
                // and name of the file remains same
                val uploadTask = imageUri?.let { storageRef.child("file/$sd").putFile(it) }

                // On success, download the file URL and display it
                uploadTask?.addOnSuccessListener {
                    // using glide library to display the image
                    storageRef.child("file/$sd").downloadUrl.addOnSuccessListener {
                       url= it.toString()
                        Glide.with(this@Home)
                            .load(it)
                            .into(imageView)
                        circleIndicator.visibility=View.GONE

                        Log.e("Firebase", "download passed")
                    }.addOnFailureListener {
                        Log.e("Firebase", "Failed in downloading")
                    }
                }?.addOnFailureListener {
                    Log.e("Firebase", "Image Upload fail")
                }
            }
        }

    @SuppressLint("Range")
    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (cursor != null) {
                    if(cursor.moveToFirst()) {
                        return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
            }
        }
        return uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it) }
    }

    private fun addProduct(title:String,description:String,phoneNumber:String,amount:String,url:String,bottomSheetDialog: BottomSheetDialog){
        val refMyProduct=
            firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myProducts").collection("myProducts")
        val refAllProduct=
            firebaseFirestore.collection("allProducts")
        val user = hashMapOf(
            "title" to title,
            "description" to description,
            "phoneNumber" to phoneNumber,
            "url" to url,
            "amount" to amount

        )
        refMyProduct.add(user).addOnSuccessListener { documentReference ->
            idGlobal=documentReference.id
            refMyProduct.document(documentReference.id).update("id",documentReference.id)
            Toast.makeText(context,"Successful Added", Toast.LENGTH_SHORT).show()

            Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
        }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
                Toast.makeText(context,e.message, Toast.LENGTH_SHORT).show()
            }

        refAllProduct.add(user).addOnSuccessListener { documentReference ->
            refAllProduct.document(documentReference.id).update("id",documentReference.id)
            refMyProduct.document(idGlobal).update("idGlobal",documentReference.id)
            Toast.makeText(context,"Successful Added", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.cancel()

            Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
        }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
                Toast.makeText(context,e.message, Toast.LENGTH_SHORT).show()
            }

    }

    private fun isValidate(): Boolean {
        if (title?.text?.length==0) {
            title?.error = "Title is required"
            return false
        }
        if (description?.text?.length == 0) {
            description?.error="Description is required"
            return false
        }
        if (amount?.text?.length == 0) {
            amount?.error="Amount is required"
            return false
        }
        if (phoneNumber?.text?.length!=10) {
            phoneNumber?.error="Enter valid phone number is required"
            return false
        }
        return true
    }



}