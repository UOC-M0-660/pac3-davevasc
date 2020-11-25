package edu.uoc.pac3.twitch.streams

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebStorage
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.network.Network
import edu.uoc.pac3.data.streams.Stream
import edu.uoc.pac3.databinding.ActivityStreamsBinding
import kotlinx.coroutines.launch
import edu.uoc.pac3.R
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.oauth.LoginActivity
import edu.uoc.pac3.twitch.profile.ProfileActivity

class StreamsActivity : AppCompatActivity() {

    private val TAG = "StreamsActivity"
    private lateinit var binding: ActivityStreamsBinding
    private lateinit var adapter: StreamsAdapter
    private lateinit var twitchService: TwitchApiService
    private lateinit var layoutManager: LinearLayoutManager
    private val lastVisibleItemPosition: Int
        get() = layoutManager.findLastVisibleItemPosition()
    private var cursor: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set binding variable for current activity
        binding = ActivityStreamsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Create Twitch Service
        twitchService = TwitchApiService(Network.createHttpClient(applicationContext))
        // Init RecyclerView
        initRecyclerView()
        // Set SwipeRefresh Listener
        setSwipeRefreshListener()
        // Set Recyclerview Scroll Listener
        setRecyclerViewScrollListener()
        // Set Show 20 More Streams Listener
        setShowMoreStreamsListener()
        // Get Streams
        loadStreamsFromTwitch()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.tmProfile -> {
                startActivity(Intent(applicationContext, ProfileActivity::class.java))
                return true
            }
            R.id.tmLogout -> {
                binding.pbLoading.visibility = View.VISIBLE
                SessionManager().logoutSession()
                binding.pbLoading.visibility = View.GONE
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initRecyclerView() {
        // Set Layout Manager
        binding.tvShow.visibility = View.GONE
        layoutManager = LinearLayoutManager(applicationContext)
        // Binding recyclerview Layout Manager
        binding.rvStreams.layoutManager = layoutManager
        // Init Adapter
        adapter = StreamsAdapter(mutableListOf())
        binding.rvStreams.adapter = adapter
    }

    private fun setSwipeRefreshListener() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.clearStreams()
            cursor = null
            loadStreamsFromTwitch()
            binding.swipeRefreshLayout.isRefreshing = false
            Toast.makeText(applicationContext, "Streams were reloaded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setRecyclerViewScrollListener() {
        binding.rvStreams.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                recyclerView.layoutManager.let {layoutM ->
                    val totalItemCount = layoutM?.itemCount as Int
                    if (totalItemCount <= lastVisibleItemPosition + 1) {
                        binding.tvShow.visibility = View.VISIBLE
                    } else  {
                        binding.tvShow.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun setShowMoreStreamsListener() {
        binding.tvShow.setOnClickListener {
            loadStreamsFromTwitch()
            binding.tvShow.visibility = View.GONE
            Toast.makeText(applicationContext, "More streams were loaded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadStreamsFromTwitch() {
        // Start Coroutine
        lifecycleScope.launch {
            binding.pbLoading.visibility = View.VISIBLE
            // Get Tokens from Twitch
            val response = twitchService.getStreams(cursor)
            // Show streams in the recyclerview
            response?.pagination?.cursor?.let { cursor = it }
            response?.data?.let { adapter.setStreams(it.toMutableList()) }
            binding.pbLoading.visibility = View.GONE
        }
    }

}