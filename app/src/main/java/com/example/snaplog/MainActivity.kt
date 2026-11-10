package com.example.snaplog

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.snaplog.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val adapter = EntryAdapter(
        onClick = { entry ->
            val i = Intent(this, ViewEntryActivity::class.java)
            i.putExtra("ENTRY_ID", entry.id)
            startActivity(i)
        },
        onLongPress = { entry ->
            val uid = auth.currentUser?.uid ?: return@EntryAdapter
            db.collection("users").document(uid)
                .collection("entries").document(entry.id)
                .delete()
        }
    )

    private val connectivityManager by lazy { getSystemService(ConnectivityManager::class.java) }
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.rvEntries.layoutManager = LinearLayoutManager(this)
        b.rvEntries.adapter = adapter

        b.btnNewEntry.setOnClickListener {
            startActivity(Intent(this, NewEntryActivity::class.java))
        }

        b.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        listenForEntries()
        setupInternetBanner()
    }

    private fun listenForEntries() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .collection("entries")
            .orderBy("createdAt")
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                val list = snap.documents.map { d ->
                    Entry(
                        id = d.id,
                        caption = d.getString("caption") ?: "",
                        quote = d.getString("quote") ?: "",
                        imageUrl = d.getString("imagePath") ?: "",
                        createdAt = d.getLong("createdAt") ?: 0L
                    )
                }.reversed()
                adapter.submitList(list)
            }
    }

    private fun setupInternetBanner() {
        fun updateBanner(hasInternet: Boolean) {
            b.tvNoInternet.visibility =
                if (hasInternet) android.view.View.GONE else android.view.View.VISIBLE
        }

        updateBanner(hasInternetNow())

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = updateBanner(true)
            override fun onLost(network: Network) = updateBanner(hasInternetNow())
        }

        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }

    private fun hasInternetNow(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
    }
}
