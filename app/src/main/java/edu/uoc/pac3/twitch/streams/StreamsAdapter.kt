package edu.uoc.pac3.twitch.streams

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.uoc.pac3.data.streams.Stream
import edu.uoc.pac3.databinding.StreamItemBinding
import edu.uoc.pac3.R

/** Done by david on 27/11/2020.
 * Adapter of Streams recycler view */
class StreamsAdapter (private var streams: MutableList<Stream>) :
        RecyclerView.Adapter<StreamsAdapter.BaseViewHolder<*>>() {

    /** Object with constants for use in this adapter */
    companion object {
        private const val TAG = "StreamsAdapter"
    }
    // Declare binding variable for Streams Activity
    private lateinit var binding: StreamItemBinding

    /** Declare View Holder Base of this adapter */
    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T, position: Int)
    }
    /** Holds an instance to the view for re-use */
    inner class StreamViewHolder(binding: StreamItemBinding) : BaseViewHolder<Stream>(binding.root) {
        // For each cell of recycler view, load item values into cell view
        override fun bind(item: Stream, position: Int) {
            val trend = " " + (position + 1).toString()
            val count = " " + item.viewerCount.toString()
            item.thumbnailUrl?.let { url -> setImage(url, itemView) }
            binding.tvTrend.text = trend
            binding.tvTitle.text = item.title
            binding.tvUser.text = item.userName
            binding.tvCount.text = count
        }
    }
    /** Creates View Holder for re-use */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        binding = StreamItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StreamViewHolder(binding)
    }
    /** Binds re-usable View for a given position */
    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            // If current holder is StreamViewHolder Type, then binds current item of list
            is StreamViewHolder -> {
                holder.bind(streams[position], position)
            }
            else -> throw IllegalArgumentException()
        }
    }
    /** Returns total items in Adapter */
    override fun getItemCount(): Int {
        return streams.size
    }
    /** Returns current position in Adapter */
    override fun getItemViewType(position: Int): Int {
        return position
    }
    /** Load more streams into recycler view and notify for reload data */
    fun setStreams(streams: MutableList<Stream>) {
        // Add more streams to recycler view list
        this.streams.addAll(streams)
        // Reloads the RecyclerView with new adapter data
        notifyDataSetChanged()
    }
    /** Clear or delete recycler view elements */
    fun clearStreams() {
        // Clear recycler view all current items
        this.streams.clear()
        // Reloads the RecyclerView with new adapter data
        notifyDataSetChanged()
    }
    /** Save given url image into image view */
    fun setImage(url: String, itemView: View) {
        // Get width and height in 16:9 scale
        val width = (itemView.context.resources.getDimension(R.dimen.stream_item_image_height) / itemView.context.resources.displayMetrics.density).toInt()
        val height = width / 16 * 9
        // Replace url width and height with custom obtained values
        var urlReplaced = url.replace("{width}", width.toString())
        urlReplaced = urlReplaced.replace("{height}", height.toString())
        // Use Glide library for set url image into image view
        Glide.with(itemView.context)
                .load(urlReplaced)
                .into(binding.ivImage)
        Log.d(TAG, "setImage -> Image loaded correctly")
    }
}
