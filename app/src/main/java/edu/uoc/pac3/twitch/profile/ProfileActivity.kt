package edu.uoc.pac3.twitch.profile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import edu.uoc.pac3.PEC3App
import edu.uoc.pac3.R
import edu.uoc.pac3.data.SessionManager
import edu.uoc.pac3.data.TwitchApiService
import edu.uoc.pac3.data.network.Network
import edu.uoc.pac3.databinding.ActivityProfileBinding
import edu.uoc.pac3.oauth.LoginActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class ProfileActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ProfileActivity"
        const val API_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'"
        const val UI_DATE_FORMAT = "MM/dd/yyyy"
        const val ACTIVITY_NAME = "Profile"
    }

    private lateinit var twitchService: TwitchApiService
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set binding variable for current activity
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set actionbar correct title
        this.supportActionBar?.title = ACTIVITY_NAME
        // Create Twitch Service
        twitchService = TwitchApiService(Network.createHttpClient(applicationContext))
        loadUserFromTwitch()
        setUpdateButton()
        setLogoutButton()
    }

    private fun loadUserFromTwitch() {
        // Start Coroutine
        lifecycleScope.launch {
            binding.pbLoading.visibility = View.VISIBLE
            // Get Tokens from Twitch
            val response = twitchService.getUser()
            // Get User from response if exist
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
            binding.pbLoading.visibility = View.GONE
        }
    }

    private fun setImage(url: String) {
        // Get the drawable from name of image
        val dim = (applicationContext.resources.getDimension(R.dimen.stream_item_image_height) / applicationContext.resources.displayMetrics.density).toInt()
        var urlReplaced = url.replace("{width}", dim.toString())
        urlReplaced = urlReplaced.replace("{height}", dim.toString())
        Glide.with(applicationContext)
                .load(urlReplaced)
                .into(binding.ivImage)
        Log.d(TAG, "setImage -> Image loaded correctly")
    }

    private fun setDate(date: String) {
        val dateType = SimpleDateFormat(API_DATE_TIME_FORMAT, Locale.getDefault()).parse(date) ?: Date()
        binding.tvDate.text = SimpleDateFormat(UI_DATE_FORMAT, Locale.getDefault()).format(dateType)
        Log.d(TAG, "setDate -> Date loaded correctly")
    }

    private fun setUpdateButton() {
        binding.mbUpdate.setOnClickListener {
            // Start Coroutine
            lifecycleScope.launch {
                binding.pbLoading.visibility = View.VISIBLE
                twitchService.updateUserDescription(binding.tfDescription.text.toString())
                Log.d(TAG, "setUpdateButton -> Description uploaded correctly")
                Toast.makeText(applicationContext, "Description uploaded correctly", Toast.LENGTH_SHORT).show()
                binding.pbLoading.visibility = View.GONE
            }
        }
    }

    private fun setLogoutButton() {
        binding.mbLogout.setOnClickListener {
            binding.pbLoading.visibility = View.VISIBLE
            SessionManager().logoutSession()
            binding.pbLoading.visibility = View.GONE
        }
    }
}