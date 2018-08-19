package aromko.de.wishlist.activity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import aromko.de.wishlist.R;
import aromko.de.wishlist.adapter.ChatMessageViewHolderAdapter;
import aromko.de.wishlist.model.ChatMessage;
import aromko.de.wishlist.viewModel.ChatMessageViewModel;
import aromko.de.wishlist.viewModel.ChatMessageViewModelFactory;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageText;
    private Button mSendButton;

    private String mUsername;
    private ChatMessageViewModel chatMessageViewModel;
    private ArrayList<ChatMessage> listItems = new ArrayList<ChatMessage>();
    private String wishId;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.txtMessages);

        Intent myIntent = getIntent();
        wishId = myIntent.getStringExtra("wishId");

        mUsername = String.valueOf(R.string.txtNameAnonym);

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
        mMessageText = findViewById(R.id.messageEditText);
        mSendButton = findViewById(R.id.sendButton);

        mLinearLayoutManager = new LinearLayoutManager(this);

        chatMessageViewModel = ViewModelProviders.of(this, new ChatMessageViewModelFactory(this.getApplication(), wishId)).get(ChatMessageViewModel.class);

        final LiveData<List<ChatMessage>> listsLiveData = chatMessageViewModel.getListsLiveData();

        listsLiveData.observe(this, new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(@Nullable List<ChatMessage> chatMessages) {
                mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
                listItems.clear();

                for (ChatMessage chatMessage : chatMessages) {
                    listItems.add(chatMessage);
                }
                mMessageRecyclerView.setAdapter(new ChatMessageViewHolderAdapter(listItems, mUsername));
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        mMessageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    mSendButton.setEnabled(false);
                } else {
                    mSendButton.setEnabled(true);
                }
            }
        });
    }


    public void insertChatMessage(View view) {
        chatMessageViewModel.insertMessage(mFirebaseUser.getDisplayName(), mMessageText.getText().toString());
        mMessageText.setText("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
