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
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage


class BuyersInquire : Fragment() {

    private lateinit var circleIndicator: ProgressBar
    private lateinit var title:EditText
    private lateinit var description:EditText
    private lateinit var phoneNumber:EditText
    private  lateinit var imageView: ImageView
    private lateinit var url:String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var recycleView: RecyclerView
    private lateinit var arrayList: ArrayList<Product>
    private lateinit var myAdapter: inqurieAdapter
    private lateinit var recycleViewTwo: RecyclerView
    private lateinit var arrayListTwo: ArrayList<Product>
    private lateinit var myAdapterTwo: inqurieAdapter
    private lateinit var updateInquireTitle:EditText
    private lateinit var updateInquireAmount:EditText
    private lateinit var deleteId:String
    private  lateinit var idGlobal: String

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseAuth=FirebaseAuth.getInstance()
        firebaseFirestore= FirebaseFirestore.getInstance()
        loadAllInquries()
        loadMyInquries()


        val view=inflater.inflate(R.layout.fragment_buyers_inquire, container, false)
        val addInquiresButton=view.findViewById<FloatingActionButton>(R.id.add_inquries_button)


        addInquiresButton.setOnClickListener {
            val productView = layoutInflater.inflate(R.layout.add_inquries_bottom_sheet, null)
            circleIndicator=productView.findViewById(R.id.progressBar)
            circleIndicator.visibility=View.GONE
            val dialog = container?.context?.let { BottomSheetDialog(it) }

            val uploadButton=productView.findViewById<Button>(R.id.upload_picture_button_inquries)
            title=productView.findViewById<EditText>(R.id.item_text_inquries)
            description=productView.findViewById<EditText>(R.id.discription_text_inquries)
            phoneNumber=productView.findViewById<EditText>(R.id.phone_number_text_inquries)
            imageView=productView.findViewById(R.id.upload_image_view_inquries)
            val submitButton=productView.findViewById<Button>(R.id.submit_button_inquires)

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
                        addProduct(title.text.toString(),description.text.toString(),phoneNumber.text.toString(),"0",url,dialog)
                    }
                }
            }
            dialog?.setCancelable(true)
            dialog?.setContentView(productView)
            dialog?.show()

        }
        recycleView=view.findViewById(R.id.my_inquries_view)
        recycleView.layoutManager= LinearLayoutManager(container?.context)
        arrayList= arrayListOf()
        myAdapter= inqurieAdapter(arrayList,view.context)
        recycleView.adapter=myAdapter

        recycleViewTwo=view.findViewById(R.id.all_inquries_view)
        recycleViewTwo.layoutManager= LinearLayoutManager(container?.context)
        arrayListTwo= arrayListOf()
        myAdapterTwo= inqurieAdapter(arrayListTwo,view.context)
        recycleViewTwo.adapter=myAdapterTwo

        myAdapter.setOnClickListener(object :inqurieAdapter.OnClickListener{
            override fun onClick(position: Int, model: Product) {

                val dialog = container?.context?.let { BottomSheetDialog(it) }

                val view = layoutInflater.inflate(R.layout.update_my_inquries_bottom_sheet, null)
                val deleteButton=view.findViewById<Button>(R.id.delete_Inqurie_button)
                val updateButton=view.findViewById<Button>(R.id.update_inquries_button)
                updateInquireTitle=view.findViewById<EditText>(R.id.inquries_title_edit_text)
                updateInquireAmount=view.findViewById<EditText>(R.id.inquries_discription_edit_text)
                updateInquireTitle.setText(model.title)
                updateInquireAmount.setText(model.description)

                deleteButton.setOnClickListener{
                    if (model.id!=null){
                        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myInquires").
                        collection("myInquires").document(model.id!!).
                        addSnapshotListener{ documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                            if (documentSnapshot != null) {

                                deleteId=documentSnapshot.data?.get("idGlobal").toString()
                                firebaseFirestore.collection("allInquires").document(deleteId!!).delete()
                            }

                        }

                        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myInquires").collection("myInquires").document(model.id!!).delete()
                        Toast.makeText(context,"Successful Deleted", Toast.LENGTH_SHORT).show()
                        arrayList.removeAt(position)
                        myAdapter.notifyDataSetChanged()


                    }

                    dialog?.cancel()

                }

                updateButton.setOnClickListener{

                    val updates = hashMapOf<String, Any>(
                        "title" to updateInquireTitle.text.toString(),
                        "description" to updateInquireAmount.text.toString(),


                    )
                    if (model.id!=null){
                        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myInquires").
                        collection("myInquires").document(model.id!!).
                        addSnapshotListener{ documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                            if (documentSnapshot != null) {

                                deleteId=documentSnapshot.data?.get("idGlobal").toString()
                                firebaseFirestore.collection("allInquires").document(deleteId).update(updates).addOnSuccessListener {
                                    //Toast.makeText(context,"Successful Updated",Toast.LENGTH_SHORT).show()
                                    arrayList[position].title=updateInquireTitle.text.toString()
                                    arrayList[position].description=updateInquireAmount.text.toString()

                                }
                            }

                        }
                        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myInquires").collection("myInquires").document(
                            model.id!!).update(updates).addOnSuccessListener{
                            arrayList[position].title=updateInquireTitle.text.toString()
                            arrayList[position].description=updateInquireAmount.text.toString()
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
        return view
    }
    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return phoneNumber.length == 10
    }
    private fun addProduct(title:String,description:String,phoneNumber:String,amount:String,url:String,bottomSheetDialog: BottomSheetDialog){

        if (!isPhoneNumberValid(phoneNumber)) {
            Toast.makeText(context, "Invalid phone number. Please enter a 10-digit number.", Toast.LENGTH_SHORT).show()
            return  // Abort the function execution
        }

        val refMyProduct=
            firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myInquires").collection("myInquires")
        val refAllProduct=
            firebaseFirestore.collection("allInquires")
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
                val storageRef= FirebaseStorage.getInstance().reference

                // Upload Task with upload to directory 'file'
                // and name of the file remains same
                val uploadTask = imageUri?.let { storageRef.child("file/$sd").putFile(it) }

                // On success, download the file URL and display it
                uploadTask?.addOnSuccessListener {
                    // using glide library to display the image
                    storageRef.child("file/$sd").downloadUrl.addOnSuccessListener {
                        url= it.toString()
                        Glide.with(this@BuyersInquire)
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

    private fun isValidate(): Boolean {
        if (title?.text?.length==0) {
            title?.error = "Title is required"
            return false
        }
        if (description?.text?.length == 0) {
            description?.error="Description is required"
            return false
        }

        if (phoneNumber?.text?.length == 0) {
            phoneNumber?.error="Phone number is required"
            return false
        }
        return true
    }

    private fun loadMyInquries(){

        firebaseFirestore.collection(firebaseAuth.currentUser!!.uid).document("myInquires").collection("myInquires").addSnapshotListener(object :
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

    private fun loadAllInquries(){

        firebaseFirestore.collection("allInquires").addSnapshotListener(object :
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
                        arrayListTwo.add(doc.document.toObject(Product::class.java))
                    }
                }
                myAdapterTwo.notifyDataSetChanged()

            }

        })

    }
}