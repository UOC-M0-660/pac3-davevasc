package edu.uoc.pac3.twitch.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import edu.uoc.pac3.R
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.network.Network
import edu.uoc.pac3.databinding.ActivityProfileBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/** Done by david on 27/11/2020.
* Class for define User Profile Activity */
class ProfileActivity : AppCompatActivity() {

    /** Object with constants for use in this activity */
    companion object {
        private const val TAG = "ProfileActivity"
        private const val API_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'"
        private const val UI_DATE_FORMAT = "MM/dd/yyyy"
        private const val ACTIVITY_NAME = "Profile"
    }
    // Declare binding and twitch service variables for this activity
    private lateinit var twitchService: TwitchApiService
    private lateinit var binding: ActivityProfileBinding

    /** Profile Activity on Create function */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set binding variable for current activity
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set actionbar correct title
        this.supportActionBar?.title = ACTIVITY_NAME
        // Create Twitch Service
        twitchService = TwitchApiService(Network.createHttpClient())
        // Hide ugly milliseconds lorem ipsum text showing when activity starts
        binding.tfDescription.setText("")
        // Call to load user form Twitch by API
        loadUserFromTwitch()
        // Set update button listener
        setUpdateButtonListener()
        // Set logout button listener
        setLogoutButtonListener()
    }
    /** Load user info form twitch by his API */
    private fun loadUserFromTwitch() {
        // Start Coroutine
        lifecycleScope.launch {
            // Show Loading Indicator
            binding.pbLoading.visibility = View.VISIBLE
            // Get Tokens from Twitch
            val response = twitchService.getUser()
            // Get User from response if exist, and load values into view
            response?.data?.get(0)?.let { user ->
                val name = " " + user.userName + " "
                val views = " " + user.viewCount + " "
                user.profileImage?.let { url -> setImage(url)}
                binding.tvName.text = name
                binding.tvCount.text = views
                binding.tvEmail.text = user.userEmail
                user.createdDate?.let { date -> setDate(date) }
                binding.tfDescription.setText(user.description)
            }
            // Hide Loading Indicator
            binding.pbLoading.visibility = View.GONE
        }
    }
    /** Save image from Twitch into image view */
    private fun setImage(url: String) {
        // Get real dimension from resources
        val dim = (applicationContext.resources.getDimension(R.dimen.stream_item_image_height) / applicationContext.resources.displayMetrics.density).toInt()
        // Replace width and height by real dimension
        var urlReplaced = url.replace("{width}", dim.toString())
        urlReplaced = urlReplaced.replace("{height}", dim.toString())
        // Save image into image view using Glide library
        Glide.with(applicationContext)
                .load(urlReplaced)
                .into(binding.ivImage)
        Log.d(TAG, "setImage -> Image loaded correctly")
    }
    /** Save create date from Twitch into date text view */
    private fun setDate(date: String) {
        // Convert API date ugly format to Kotlin Date type
        val dateType = SimpleDateFormat(API_DATE_TIME_FORMAT, Locale.getDefault()).parse(date) ?: Date()
        // Save date into text view with custom format
        binding.tvDate.text = SimpleDateFormat(UI_DATE_FORMAT, Locale.getDefault()).format(dateType)
        Log.d(TAG, "setDate -> Date loaded correctly")
    }
    /** Set update button listener */
    private fun setUpdateButtonListener() {
        binding.mbUpdate.setOnClickListener {
            // Start Coroutine
            lifecycleScope.launch {
                // Show Loading Indicator
                binding.pbLoading.visibility = View.VISIBLE
                // Send view description to Twitch by API
                twitchService.updateUserDescription(binding.tfDescription.text.toString())
                Log.d(TAG, "setUpdateButton -> Description uploaded correctly")
                Toast.makeText(applicationContext, "Description uploaded correctly", Toast.LENGTH_SHORT).show()
                // Hide Loading Indicator
                binding.pbLoading.visibility = View.GONE
            }
        }
    }
    /** Set logout button listener */
    private fun setLogoutButtonListener() {
        binding.mbLogout.setOnClickListener {
            // Show Loading Indicator
            binding.pbLoading.visibility = View.VISIBLE
            // Logout current session
            SessionManager().logoutSession()
            // Hide Loading Indicator
            binding.pbLoading.visibility = View.GONE
        }
    }
}