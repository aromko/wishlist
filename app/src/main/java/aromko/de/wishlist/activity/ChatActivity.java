package aromko.de.wishlist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private RecyclerView rvMessageRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ProgressBar pbProgressBar;
    private EditText etMessageText;
    private Button btnSendButton;

    private String username;
    private ChatMessageViewModel chatMessageViewModel;
    private ArrayList<ChatMessage> listItems = new ArrayList<ChatMessage>();
    private String wishId;

    private FirebaseAuth fFirebaseAuth;
    private FirebaseUser fFirebaseUser;

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

        username = String.valueOf(R.string.txtNameAnonym);

        fFirebaseAuth = FirebaseAuth.getInstance();
        fFirebaseUser = fFirebaseAuth.getCurrentUser();

        if (fFirebaseUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        } else {
            username = fFirebaseUser.getDisplayName();
        }

        pbProgressBar = findViewById(R.id.pbProgressBar);
        rvMessageRecyclerView = findViewById(R.id.rvMessageRecyclerView);
        etMessageText = findViewById(R.id.etMessageText);
        btnSendButton = findViewById(R.id.btnSendButton);

        linearLayoutManager = new LinearLayoutManager(this);

        ViewModelProvider viewModelProvider = new ViewModelProvider(this, new ChatMessageViewModelFactory(this.getApplication(), wishId));
        chatMessageViewModel = viewModelProvider.get(ChatMessageViewModel.class);

        final LiveData<List<ChatMessage>> listsLiveData = chatMessageViewModel.getListsLiveData();

        listsLiveData.observe(this, new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(@Nullable List<ChatMessage> chatMessages) {
                rvMessageRecyclerView.setLayoutManager(linearLayoutManager);
                listItems.clear();

                for (ChatMessage chatMessage : chatMessages) {
                    listItems.add(chatMessage);
                }
                rvMessageRecyclerView.setAdapter(new ChatMessageViewHolderAdapter(listItems, username));
                pbProgressBar.setVisibility(View.INVISIBLE);
            }
        });

        etMessageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    btnSendButton.setEnabled(false);
                } else {
                    btnSendButton.setEnabled(true);
                }
            }
        });
    }


    public void insertChatMessage(View view) {
        chatMessageViewModel.insertMessage(fFirebaseUser.getDisplayName(), etMessageText.getText().toString());
        etMessageText.setText("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
