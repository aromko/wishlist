package aromko.de.wishlist.adapter

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import aromko.de.wishlist.R
import aromko.de.wishlist.model.ChatMessage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ChatMessageViewHolderAdapter(private val mValues: List<ChatMessage?>, private val mUsername: String?) : RecyclerView.Adapter<ChatMessageViewHolderAdapter.ViewHolder>() {
    private var context: Context? = null
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_chat, viewGroup, false)
        context = viewGroup.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        val chatMessage = holder.mItem
        val date = getDate(chatMessage?.timestamp!!)
        if (chatMessage.text != null) {
            holder.messageTextView.text = chatMessage.text.toString()
            holder.messageTextView.visibility = TextView.VISIBLE
            holder.messageImageView.visibility = ImageView.GONE
            holder.messengerTextView.text = chatMessage.name + " " + date
        }
    }

    fun getDate(timestamp: Long): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = timestamp * 1000
        return DateFormat.format(DATE_TIME_FORMAT, cal).toString()
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        var mItem: ChatMessage? = null
        var messageTextView: TextView
        var messageImageView: ImageView
        var messengerTextView: TextView
        var messengerImageView: CircleImageView

        init {
            messageTextView = mView.findViewById(R.id.messageTextView)
            messageImageView = mView.findViewById(R.id.messageImageView)
            messengerTextView = mView.findViewById(R.id.messengerTextView)
            messengerImageView = mView.findViewById(R.id.messengerImageView)
        }
    }

    companion object {
        const val DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss"
    }
}
