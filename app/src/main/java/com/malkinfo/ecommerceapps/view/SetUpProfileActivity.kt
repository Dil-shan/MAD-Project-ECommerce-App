package com.malkinfo.ecommerceapps.view

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import com.malkinfo.ecommerceapps.MainActivity
import com.malkinfo.ecommerceapps.R
import kotlinx.android.synthetic.main.activity_set_up_profile.*
import java.util.*
import kotlin.collections.HashMap

class SetUpProfileActivity : AppCompatActivity() {

    var auth:FirebaseAuth? = null
    var database :FirebaseDatabase ? = null
    var storage :FirebaseStorage? = null
    var selectedImage: Uri?=null
    var dialog :ProgressDialog? = null
    var firebaseFireStore:FirebaseFirestore? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_up_profile)
        dialog = ProgressDialog(this@SetUpProfileActivity)
        dialog!!.setMessage("updating Profile...")
        dialog!! .setCancelable(false)
            database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firebaseFireStore = FirebaseFirestore.getInstance()
        supportActionBar?.hide()
        imageView.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type="image/*"
            startActivityForResult(intent,45)

        }

        continueBtn2.setOnClickListener(View.OnClickListener {
            val name :String = nameBox.text.toString()
            if(name.isEmpty()){
                nameBox.error = "Please type a Name"
                return@OnClickListener
            }
            dialog!!.show()
            if (selectedImage != null){
                val refrence = storage!!.reference
                    .child("Profiles")
                    .child(auth!!.uid!!)
                refrence.putFile(selectedImage!!)
                    .addOnCompleteListener { task->

                        if(task.isSuccessful) {
                            refrence.downloadUrl
                                .addOnSuccessListener { uri->
                                    val imageUri = uri.toString()
                                    val uid = auth!!.uid
                                    val phone = auth!!.currentUser!!.phoneNumber
                                    val name :String = nameBox.text.toString()
                                    val user = com.malkinfo.ecommerceapps.model.User(
                                    uid,name,phone,imageUri
                                    )
                                    firebaseFireStore!!.collection(
                                        "USERS"
                                    )
                                        .add(user)
                                        .addOnCompleteListener(object :OnCompleteListener<DocumentReference>{
                                            override fun onComplete(task: Task<DocumentReference>) {
                                               dialog!!.dismiss()
                                                val intent = Intent(this@SetUpProfileActivity,
                                                    MainActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }

                                        })

                                        database!!.reference
                                            .child("users")
                                            .child(uid!!)
                                            .setValue(user)
                                            .addOnSuccessListener {
                                                dialog!!.dismiss()
                                                val intent = Intent(this@SetUpProfileActivity,
                                                    MainActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }

                                }
                        }

                    }



                    }
            else{
                val uid = auth!!.uid
                val phone = auth!!.currentUser!!.phoneNumber
                val user = com.malkinfo.ecommerceapps.model.User(
                    uid,name,phone, "No Phone"
                )
                firebaseFireStore!!.collection("")
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null){
            if(data.data !=null){
                val uri = data.data
                val storage = FirebaseStorage.getInstance()
                val time = Date(). time
                val refrence = storage.reference
                    .child("Profiles")
                    .child(time.toString() + "")
                refrence.putFile(uri!!)
                    .addOnCompleteListener { task->
                        if(task.isSuccessful){
                            refrence.downloadUrl.addOnSuccessListener { uri->
                                val filePath = uri.toString()
                                val obj = HashMap<String,Any>()
                                obj["image"] = filePath
                                database!!.reference.child("users")
                                    .child(FirebaseAuth.getInstance().uid!!)
                                    .updateChildren(obj)
                                    .addOnSuccessListener {  }
                            }
                        }

                    }
                imageView. setImageURI(data.data)
                selectedImage = data.data

            }
        }
    }
}