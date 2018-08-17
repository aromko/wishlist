package aromko.de.wishlist.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import aromko.de.wishlist.R;
import aromko.de.wishlist.model.ChatMessage;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatMessageViewHolderAdapter extends RecyclerView.Adapter<ChatMessageViewHolderAdapter.ViewHolder> {

    public static final String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private final List<ChatMessage> mValues;
    private Context context;
    private String mUsername;


    public ChatMessageViewHolderAdapter(List<ChatMessage> mValues, String mUsername) {
        this.mValues = mValues;
        this.mUsername = mUsername;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_chat, viewGroup, false);
        context = viewGroup.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        final ChatMessage chatMessage = holder.mItem;

        String date = getDate(chatMessage.getTimestamp());
        if (chatMessage.getText() != null) {
            holder.messageTextView.setText(chatMessage.getText());
            holder.messageTextView.setVisibility(TextView.VISIBLE);
            holder.messageImageView.setVisibility(ImageView.GONE);
            holder.messengerTextView.setText(chatMessage.getName() + " " + date);
        }
    }

    @NonNull
    public String getDate(long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp * 1000);
        return DateFormat.format(DATE_TIME_FORMAT, cal).toString();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public ChatMessage mItem;
        TextView messageTextView;
        ImageView messageImageView;
        TextView messengerTextView;
        CircleImageView messengerImageView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            messageTextView = view.findViewById(R.id.messageTextView);
            messageImageView = view.findViewById(R.id.messageImageView);
            messengerTextView = view.findViewById(R.id.messengerTextView);
            messengerImageView = view.findViewById(R.id.messengerImageView);
        }
    }
}