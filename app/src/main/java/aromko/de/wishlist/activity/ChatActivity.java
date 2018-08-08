package aromko.de.wishlist.activity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import aromko.de.wishlist.R;
import aromko.de.wishlist.model.ChatMessage;
import aromko.de.wishlist.model.Wish;
import aromko.de.wishlist.viewModel.ChatMessageViewModel;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;

    private String mUsername;
    private ChatMessageViewModel chatMessageViewModel;
    private ArrayList<ChatMessage> listItems = new ArrayList<ChatMessage>();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mUsername = "anonym";

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
        }

        mProgressBar = findViewById(R.id.progressBar);
        mMessageRecyclerView = findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);


        chatMessageViewModel = ViewModelProviders.of(this).get(ChatMessageViewModel.class);

        final LiveData<List<ChatMessage>> listsLiveData = chatMessageViewModel.getListsLiveData();

        listsLiveData.observe(this, new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(@Nullable List<ChatMessage> chatMessages) {
                mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
                listItems.clear();

                for (ChatMessage chatMessage : chatMessages) {
                    listItems.add(chatMessage);
                }
                mMessageRecyclerView.setAdapter(new ChatMessageViewHolder(listItems));
            }
        });

    }

    public class ChatMessageViewHolder extends RecyclerView.Adapter<ChatMessageViewHolder.ViewHolder> {

        private final List<ChatMessage> mValues;
        private Context context;

        public ChatMessageViewHolder(List<ChatMessage> mValues) {
            this.mValues = mValues;

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_chat, viewGroup, false);
            context = viewGroup.getContext();
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);

            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            ChatMessage chatMessage = holder.mItem;
            if (chatMessage.getText() != null) {
                holder.messageTextView.setText(chatMessage.getText());
                holder.messageTextView.setVisibility(TextView.VISIBLE);
                holder.messageImageView.setVisibility(ImageView.GONE);
            }
        }
        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            TextView messageTextView;
            ImageView messageImageView;
            TextView messengerTextView;
            CircleImageView messengerImageView;

            public ChatMessage mItem;

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
}
