package com.example.snaplog

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.snaplog.databinding.ActivityViewEntryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewEntryActivity : AppCompatActivity() {

    private lateinit var b: ActivityViewEntryBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        b = ActivityViewEntryBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnBack.setOnClickListener { finish() }

        val entryId = intent.getStringExtra("ENTRY_ID") ?: return
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .collection("entries").document(entryId)
            .get()
            .addOnSuccessListener { d ->
                b.tvCaption.text = d.getString("caption") ?: ""
                b.tvQuote.text = d.getString("quote") ?: ""
                val url = d.getString("imageUrl") ?: ""

                b.ivPhoto.setImageURI(android.net.Uri.parse(url))

            }
    }
}
