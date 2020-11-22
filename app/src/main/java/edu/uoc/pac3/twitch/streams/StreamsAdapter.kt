package edu.uoc.pac3.twitch.streams

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.uoc.pac3.data.streams.Stream
import edu.uoc.pac3.databinding.StreamItemBinding
import edu.uoc.pac3.R

class StreamsAdapter (private var streams: MutableList<Stream>) :
        RecyclerView.Adapter<StreamsAdapter.BaseViewHolder<*>>() {

    private lateinit var binding: StreamItemBinding

    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T, position: Int)
    }

    // Holds an instance to the view for re-use
    inner class StreamViewHolder(binding: StreamItemBinding) : BaseViewHolder<Stream>(binding.root) {

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

    // Creates View Holder for re-use
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        binding = StreamItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StreamViewHolder(binding)
    }

    // Binds re-usable View for a given position
    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when (holder) {
            is StreamViewHolder -> {
                //binding.tvCount.text = position.toString()
                holder.bind(streams[position], position)
            }
            else -> throw IllegalArgumentException()
        }
    }

    // Returns total items in Adapter
    override fun getItemCount(): Int {
        return streams.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun setStreams(streams: MutableList<Stream>) {
        this.streams.addAll(streams)
        // Reloads the RecyclerView with new adapter data
        notifyDataSetChanged()
    }
    fun clearStreams() {
        this.streams.clear()
        // Reloads the RecyclerView with new adapter data
        notifyDataSetChanged()
    }

    fun setImage(url: String, itemView: View) {
        // Get the drawable from name of image
        val width = (itemView.context.resources.getDimension(R.dimen.stream_item_image_height) / itemView.context.resources.displayMetrics.density).toInt()
        val height = width / 16 * 9
        var urlReplaced = url.replace("{width}", width.toString())
        urlReplaced = urlReplaced.replace("{height}", height.toString())
        Glide.with(itemView.context)
                .load(urlReplaced)
                .into(binding.ivImage)
    }

}
