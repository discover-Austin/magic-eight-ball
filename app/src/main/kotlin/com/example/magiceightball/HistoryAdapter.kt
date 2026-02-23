package com.example.magiceightball

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.magiceightball.databinding.ItemHistoryBinding

/** RecyclerView adapter that displays a list of [HistoryEntry] items (newest first). */
class HistoryAdapter(
    private val onShareClick: (HistoryEntry) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val items = mutableListOf<HistoryEntry>()

    /** Inserts [entry] at the top of the list. */
    fun addEntry(entry: HistoryEntry) {
        items.add(0, entry)
        notifyItemInserted(0)
    }

    /** Removes all entries from the list. */
    fun clear() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }

    val size: Int get() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: HistoryEntry) {
            binding.questionText.text = entry.question.ifBlank { "(shaken)" }
            binding.responseText.text = entry.response.text

            val colorRes = when (entry.response.sentiment) {
                EightBall.Sentiment.POSITIVE -> R.color.response_positive
                EightBall.Sentiment.NEUTRAL -> R.color.response_neutral
                EightBall.Sentiment.NEGATIVE -> R.color.response_negative
            }
            binding.responseText.setTextColor(binding.root.context.getColor(colorRes))

            binding.shareButton.setOnClickListener { onShareClick(entry) }
            binding.shareButton.contentDescription =
                binding.root.context.getString(R.string.share_response)
        }
    }
}
