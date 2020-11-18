package edu.uoc.pac3.twitch.streams

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.uoc.pac3.R
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.network.Network
import kotlinx.coroutines.launch

class StreamsActivity : AppCompatActivity() {

    private val TAG = "StreamsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_streams)
        // Init RecyclerView
        initRecyclerView()
        // TODO: Get Streams
        // Create Twitch Service
        val twitchService = TwitchApiService(Network.createHttpClient(applicationContext))

        lifecycleScope.launch {
            // Get Tokens from Twitch
            val response = twitchService.getStreams()
            val i = 0
            val a = 1
        }
    }

    private fun initRecyclerView() {
        // TODO: Implement
    }

}