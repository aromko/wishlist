package aromko.de.wishlist.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import aromko.de.wishlist.R
import aromko.de.wishlist.activity.LoginActivity
import aromko.de.wishlist.adapter.ChatMessageViewHolderAdapter
import aromko.de.wishlist.model.ChatMessage
import aromko.de.wishlist.viewModel.ChatMessageViewModel
import aromko.de.wishlist.viewModel.ChatMessageViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.*

class ChatActivity : AppCompatActivity() {
    private var rvMessageRecyclerView: RecyclerView? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var pbProgressBar: ProgressBar? = null
    private var etMessageText: EditText? = null
    private var btnSendButton: Button? = null
    private var username: String? = null
    private var chatMessageViewModel: ChatMessageViewModel? = null
    private val listItems = ArrayList<ChatMessage?>()
    private var wishId: String? = null
    private var fFirebaseAuth: FirebaseAuth? = null
    private var fFirebaseUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setTitle(R.string.txtMessages)
        val myIntent = intent
        wishId = myIntent.getStringExtra("wishId")
        username = R.string.txtNameAnonym.toString()
        fFirebaseAuth = FirebaseAuth.getInstance()
        fFirebaseUser = fFirebaseAuth!!.currentUser
        username = if (fFirebaseUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        } else {
            fFirebaseUser!!.displayName
        }
        pbProgressBar = findViewById(R.id.pbProgressBar)
        rvMessageRecyclerView = findViewById(R.id.rvMessageRecyclerView)
        etMessageText = findViewById(R.id.etMessageText)
        btnSendButton = findViewById(R.id.btnSendButton)
        linearLayoutManager = LinearLayoutManager(this)
        val viewModelProvider = ViewModelProvider(this, ChatMessageViewModelFactory(this.application, wishId))
        chatMessageViewModel = viewModelProvider.get(ChatMessageViewModel::class.java)
        val listsLiveData = chatMessageViewModel!!.listsLiveData
        listsLiveData.observe(this, { chatMessages: List<ChatMessage?>? ->
            rvMessageRecyclerView?.layoutManager = linearLayoutManager
            listItems.clear()
            listItems.addAll(chatMessages!!)
            rvMessageRecyclerView?.adapter = ChatMessageViewHolderAdapter(listItems, username)
            pbProgressBar?.visibility = View.INVISIBLE
        })
        etMessageText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                btnSendButton?.isEnabled = !s.toString().isEmpty()
            }
        })
    }

    fun insertChatMessage(view: View?) {
        chatMessageViewModel!!.insertMessage(fFirebaseUser!!.displayName, etMessageText!!.text.toString())
        etMessageText!!.setText("")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}