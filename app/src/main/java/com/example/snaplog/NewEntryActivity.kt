package com.example.snaplog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.snaplog.databinding.ActivityNewEntryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File

class NewEntryActivity : AppCompatActivity() {

    private lateinit var b: ActivityNewEntryBinding
    private var photoUri: Uri? = null

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val REQ_CAMERA = 200

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) {
            b.ivPhoto.setImageURI(photoUri)
        } else {
            Toast.makeText(this, "Camera canceled.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        b = ActivityNewEntryBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CAMERA)
            } else {
                startCamera()
            }
        }

        b.btnFetchQuote.setOnClickListener { fetchQuote() }
        b.btnSave.setOnClickListener { saveEntry() }
        b.btnCancel.setOnClickListener { finish() }
    }

    private fun startCamera() {
        val dir = File(cacheDir, "images")
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, "snaplog_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        takePicture.launch(photoUri)
    }

    private fun fetchQuote() {
        val queue = Volley.newRequestQueue(this)
        val url = "https://dummyjson.com/quotes/random"

        val req = JsonObjectRequest(
            url,
            { json ->
                val quote = json.optString("quote", "Keep going.")
                val author = json.optString("author", "")
                b.tvQuote.text = if (author.isNotEmpty()) "“$quote” — $author" else "“$quote”"
            },
            { err ->
                // show real reason + fallback so your submission works
                val reason = err.toString()
                b.tvQuote.text = "API failed: $reason\n\n“Small steps every day add up.” — SnapLog"
            }
        )

        queue.add(req)
    }



    private fun saveEntry() {
        val uid = auth.currentUser?.uid ?: return
        val caption = b.etCaption.text.toString().trim()
        val quote = b.tvQuote.text.toString().trim()
        val uri = photoUri

        if (uri == null) {
            Toast.makeText(this, "Please take a photo first.", Toast.LENGTH_SHORT).show()
            return
        }
        if (caption.isEmpty()) {
            Toast.makeText(this, "Please type a caption.", Toast.LENGTH_SHORT).show()
            return
        }

        val data = hashMapOf(
            "caption" to caption,
            "quote" to quote,
            "imagePath" to uri.toString(),   // saved locally
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users").document(uid)
            .collection("entries")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
