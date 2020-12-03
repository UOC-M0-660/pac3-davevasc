package edu.uoc.pac3.twitch.streams

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.network.Network
import edu.uoc.pac3.databinding.ActivityStreamsBinding
import kotlinx.coroutines.launch
import edu.uoc.pac3.R
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.twitch.profile.ProfileActivity

/** Done by david on 27/11/2020.
 * Class for define Streams Activity */
class StreamsActivity : AppCompatActivity() {

    /** Object with constants for use in this activity */
    companion object {
        private const val TAG = "PEC3_StreamsActivity"
    }
    // Declare binding, twitch service, and more variables for this activity
    private lateinit var binding: ActivityStreamsBinding
    private lateinit var adapter: StreamsAdapter
    private lateinit var twitchService: TwitchApiService
    private lateinit var layoutManager: LinearLayoutManager
    private val lastVisibleItemPosition: Int
        get() = layoutManager.findLastVisibleItemPosition()
    private var cursor: String? = null

    /** Streams Activity on Create function */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set binding variable for current activity
        binding = ActivityStreamsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Create Twitch Service
        twitchService = TwitchApiService(Network.createHttpClient())
        // Init RecyclerView
        initRecyclerView()
        // Set SwipeRefresh Listener
        setSwipeRefreshListener()
        // Set Recyclerview Scroll Listener
        setRecyclerViewScrollListener()
        // Get Streams from Twitch by API
        loadStreamsFromTwitch()
    }
    /** This Activity has options menu into action bar */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    /** Define options menu different actions */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // When select user profile icon, open Profile Activity
            R.id.tmProfile -> {
                startActivity(Intent(applicationContext, ProfileActivity::class.java))
                return true
            }
            // When select logout icon, logout session
            R.id.tmLogout -> {
                // Show Loading Indicator
                binding.pbLoading.visibility = View.VISIBLE
                // Logout session and return to Login Activity
                SessionManager().logoutSession()
                // Hide Loading Indicator
                binding.pbLoading.visibility = View.GONE
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    /** Initialize Recycler View */
    private fun initRecyclerView() {
        // Set Layout Manager
        layoutManager = LinearLayoutManager(applicationContext)
        // Binding recyclerview Layout Manager
        binding.rvStreams.layoutManager = layoutManager
        // Init Adapter
        adapter = StreamsAdapter(mutableListOf())
        // Add adapter to recycler view streams
        binding.rvStreams.adapter = adapter
    }
    /** Set swipe refresh listener */
    private fun setSwipeRefreshListener() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Clear recycler view streams
            adapter.clearStreams()
            // Restart pagination cursor
            cursor = null
            // Load streams from Twitch again
            loadStreamsFromTwitch()
            // Stop refresh animation
            binding.swipeRefreshLayout.isRefreshing = false
            // Inform to user by Toast
            Toast.makeText(applicationContext, "Streams were reloaded", Toast.LENGTH_SHORT).show()
        }
    }
    /** Set recycler view scroll listener */
    private fun setRecyclerViewScrollListener() {
        binding.rvStreams.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                recyclerView.layoutManager.let { layoutM ->
                    val totalItemCount = layoutM?.itemCount as Int
                    // If we are seeing in the screen the last position of recycler view -> label is showed
                    if (totalItemCount <= lastVisibleItemPosition + 1) {
                        // Load streams from Twitch with current cursor
                        loadStreamsFromTwitch()
                        // Inform to user by Toast
                        Toast.makeText(applicationContext, "More streams were loaded", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
    /** Load streams with pagination from Twitch by API */
    private fun loadStreamsFromTwitch() {
        // Start Coroutine
        lifecycleScope.launch {
            // Show Loading Indicator
            binding.pbLoading.visibility = View.VISIBLE
            // Get Tokens from Twitch with cursor pagination
            val response = twitchService.getStreams(cursor)
            // Shave cursor and show streams in the recyclerview
            response?.pagination?.cursor?.let { cursor = it }
            response?.data?.let { adapter.setStreams(it.toMutableList()) }
            // Hide Loading Indicator
            binding.pbLoading.visibility = View.GONE
            Log.d(TAG, "loadStreamsFromTwitch -> Streams loaded correctly")
        }
    }
}