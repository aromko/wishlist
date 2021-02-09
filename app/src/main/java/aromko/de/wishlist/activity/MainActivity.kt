package aromko.de.wishlist.activity

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import aromko.de.wishlist.R
import aromko.de.wishlist.adapter.WishlistAdapter
import aromko.de.wishlist.fragment.ItemListFragment
import aromko.de.wishlist.fragment.ItemListFragment.OnListFragmentInteractionListener
import aromko.de.wishlist.model.UserSetting
import aromko.de.wishlist.model.Wish
import aromko.de.wishlist.model.Wishlist
import aromko.de.wishlist.repositories.UserSettingRepository
import aromko.de.wishlist.services.UploadService
import aromko.de.wishlist.utilities.PhotoHelper
import aromko.de.wishlist.viewModel.WishlistViewModel
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.dynamiclinks.DynamicLink.AndroidParameters
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.dynamiclinks.ShortDynamicLink
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class MainActivity : AppCompatActivity(), OnListFragmentInteractionListener {
    var photoHelper: PhotoHelper? = null
    private var favoriteListId: String? = ""
    private var fFirebaseAuth: FirebaseAuth? = null
    private var fFirebaseUser: FirebaseUser? = null
    private var etUserEmail: TextView? = null
    private var etUserName: TextView? = null
    private var listView: ListView? = null
    private val listItems = ArrayList<Wishlist>()
    private var ibAddWishList: ImageButton? = null
    private var listViewModel: WishlistViewModel? = null
    private var selectedWishlistId: String? = null
    private var fab: FloatingActionButton? = null
    private var tvInfo: TextView? = null
    private var civImage: CircleImageView? = null
    private var ibDeleteWishlist: ImageButton? = null
    private var hideMenuItem: String? = null
    private var sharedText: String? = null
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras
            if (bundle != null) {
                val resultCode = bundle.getInt(UploadService.Companion.RESULT)
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this@MainActivity,
                            getString(R.string.txtPictureSuccesfulUploaded),
                            Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.txtPictureNotSuccesfulUploaded),
                            Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if ((ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(this@MainActivity, R.style.Theme_MaterialComponents_Dialog_Alert)
                        .setMessage(getString(R.string.txtGetPermissions))
                        .setPositiveButton(R.string.txtOk) { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                                    MY_PERMISSIONS_REQUEST)
                        }
                        .setNegativeButton(R.string.txtCancel) { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.cancel()
                            signOut()
                        }
                        .create()
                        .show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        MY_PERMISSIONS_REQUEST)
            }
        }
        ibAddWishList = findViewById(R.id.ibAddWishList)
        ibDeleteWishlist = findViewById(R.id.ibDeleteWishList)
        tvInfo = findViewById(R.id.tvInfo)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        etUserEmail = navigationView.getHeaderView(0).findViewById(R.id.etUserEmail)
        etUserName = navigationView.getHeaderView(0).findViewById(R.id.etUserName)
        civImage = navigationView.getHeaderView(0).findViewById(R.id.civImage)
        photoHelper = PhotoHelper(this)
        checkIfUserLoggedIn()
        fab = findViewById(R.id.fab)
        fab?.setOnClickListener(View.OnClickListener { view: View? ->
            val wishActivity = Intent(this@MainActivity, WishActivity::class.java)
            wishActivity.putExtra("wishlistId", selectedWishlistId)
            wishActivity.putExtra("sharedText", sharedText)
            sharedText = null
            startActivity(wishActivity)
        })
        listView = findViewById(R.id.listView)
        listView?.choiceMode = ListView.CHOICE_MODE_SINGLE
        val drawListAdapter = WishlistAdapter(this, listItems)
        listView?.adapter = drawListAdapter
        drawListAdapter.setNotifyOnChange(true)
        listViewModel = ViewModelProvider(this).get(WishlistViewModel::class.java)
        try {
            checkIfFavoriteListIdExists()
        } catch (e: Exception) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
        val listsLiveData = listViewModel!!.listsLiveData
        listsLiveData.observe(this, { lists: List<Wishlist?>? ->
            drawListAdapter.clear()
            for (list in lists!!) {
                if (list?.name.equals("Favoriten", ignoreCase = true)) {
                    drawListAdapter.insert(list, 0)
                } else {
                    drawListAdapter.add(list)
                }
            }
        })
        addListeners()
        processFirebaseDynamicLink()
        val intent = intent
        val action = intent.action
        val type = intent.type
        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                handleSendText(intent)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, getString(R.string.txtHaveFunWithTheApp), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, getString(R.string.txtPermissionDenied), Toast.LENGTH_SHORT).show()
                signOut()
                finish()
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (sharedText != null) {
            Toast.makeText(applicationContext, R.string.txtTextFromIntent, Toast.LENGTH_LONG).show()
        }
    }

    private fun processFirebaseDynamicLink() {
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
                .addOnSuccessListener(this) { data: PendingDynamicLinkData? ->
                    if (data != null) {
                        val deepLink = data.link
                        listViewModel!!.addUserToWishlist(deepLink?.getQueryParameter("param"))
                    }
                }
                .addOnFailureListener(this) { e: Exception? -> Toast.makeText(applicationContext, R.string.txtNoInvitationFound, Toast.LENGTH_LONG).show() }
    }

    private fun addListeners() {
        listView!!.onItemClickListener = OnItemClickListener { adapterView: AdapterView<*>?, view: View?, position: Int, id: Long ->
            for (i in 0 until listView!!.childCount) {
                listView!!.getChildAt(i).setBackgroundColor(Color.WHITE)
            }
            if (listItems[position].wishCounter > 0) {
                tvInfo!!.visibility = View.INVISIBLE
            } else {
                tvInfo!!.visibility = View.VISIBLE
            }
            openFragment(position)
            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
        }
        listView!!.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(view: View, i: Int, i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int) {
                if (listView!!.adapter.count > 0) {
                    listView!!.removeOnLayoutChangeListener(this)
                    selectFavoritesOnStartup()
                }
            }
        })
        listView!!.onItemLongClickListener = OnItemLongClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            listView!!.setSelection(position)
            selectedWishlistId = listItems[position].key
            showAlertDialog(position, R.layout.dialog_editwishlist)
            true
        }
    }

    fun showAlertDialog(position: Int, layoutId: Int) {
        val builder = AlertDialog.Builder(this@MainActivity)
        val inflater = this@MainActivity.layoutInflater
        val viewAddWishlist = inflater.inflate(layoutId, null)
        builder.setView(viewAddWishlist)
        val txtNewWishlist = viewAddWishlist.findViewById<EditText>(R.id.txtNewWishlist)
        if (position != -1) {
            txtNewWishlist.setText(listItems[position].name)
        }
        builder.setPositiveButton(R.string.txtOk) { dialogInterface: DialogInterface, i: Int ->
            val text = txtNewWishlist.text.toString()
            if (!text.isEmpty() || layoutId == R.layout.dialog_deletewishlist) {
                if (layoutId == R.layout.dialog_editwishlist) {
                    listViewModel!!.updateList(selectedWishlistId, text)
                } else if (layoutId == R.layout.dialog_addwishlist) {
                    listViewModel!!.insertList(text, false)
                } else if (layoutId == R.layout.dialog_deletewishlist) {
                    listViewModel!!.deleteList(selectedWishlistId)
                }
            }
            dialogInterface.dismiss()
        }.setNegativeButton(R.string.txtCancel) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
        val dialog = builder.create()
        dialog.show()
    }

    fun openFragment(position: Int) {
        listView!!.setSelection(position)
        selectedWishlistId = listItems[position].key
        hideMenuItem = ""
        if (selectedWishlistId == favoriteListId) {
            fab!!.hide()
            hideMenuItem = HIDE_INVITE_PEOPLE
        } else {
            fab!!.show()
        }
        invalidateOptionsMenu()
        val bundle = Bundle()
        bundle.putString("wishlistId", selectedWishlistId)
        val fragment = Fragment.instantiate(this, ItemListFragment::class.java.name, bundle) as ItemListFragment
        if (fragment != null) {
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.content_frame, fragment)
            ft.commit()
        }
        listView!!.getChildAt(position).setBackgroundColor(resources.getColor(R.color.colorPrimary))
        val selectedWishlist = listView!!.getItemAtPosition(position) as Wishlist
        this@MainActivity.title = selectedWishlist.name
    }

    fun selectFavoritesOnStartup() {
        for (favoriteWishlist in listItems) {
            if (favoriteWishlist.isFavoriteList) {
                if (favoriteWishlist.wishCounter > 0) {
                    tvInfo!!.visibility = View.INVISIBLE
                }
                openFragment(listItems.indexOf(favoriteWishlist))
                break
            }
        }
    }

    fun checkIfUserLoggedIn() {
        fFirebaseAuth = FirebaseAuth.getInstance()
        fFirebaseUser = fFirebaseAuth!!.currentUser
        if (fFirebaseUser != null) {
            etUserEmail!!.text = fFirebaseUser!!.email
            etUserName!!.text = fFirebaseUser!!.displayName
            loadProfilePicture(fFirebaseAuth!!)
        } else {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }

    fun loadProfilePicture(mAuth: FirebaseAuth) {
        val uid = Objects.requireNonNull(mAuth.currentUser)?.uid
        if ("" != uid) {
            photoHelper!!.requestProfilePicture(uid)
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        if (HIDE_INVITE_PEOPLE == hideMenuItem) {
            menu.findItem(R.id.action_invitePeople).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            return true
        } else if (id == R.id.action_logout) {
            signOut()
            finish()
            return true
        } else if (id == R.id.action_invitePeople) {
            onInviteClicked()
        } else if (id == R.id.action_profile) {
            startActivity(Intent(this@MainActivity, ProfilActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    fun signOut() {
        fFirebaseAuth!!.signOut()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
    }

    override fun onListFragmentInteraction(item: Wish?, adapterPosition: Int) {}
    override fun onFavoriteInteraction(wish: Wish?, isFavorite: Boolean?) {}
    override fun onMapInteraction(longitude: Double, latitude: Double) {}
    override fun onUrlInteraction(url: String?) {}
    override fun onPaymentInteraction(wishId: String?, price: Double, partialPrice: Double, wishlistId: String?) {}
    override fun onChatInteraction(wishId: String?) {}
    override fun onDeleteWishInteraction(wishId: String?, wishlistId: String?) {}
    fun addWishList(view: View?) {
        showAlertDialog(-1, R.layout.dialog_addwishlist)
    }

    fun onInviteClicked() {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(EXAMPLE_LINK + selectedWishlistId))
                .setDomainUriPrefix(AROMKO_PAGE_LINK)
                .setAndroidParameters(AndroidParameters.Builder().build())
                .buildShortDynamicLink()
                .addOnCompleteListener(this) { task: Task<ShortDynamicLink> ->
                    if (task.isSuccessful) {
                        val shortLink = task.result.shortLink
                        val sendIntent = Intent()
                        if (shortLink != null) {
                            val msg = getString(R.string.txtInvitationMessage) + " " + shortLink.toString()
                            sendIntent.action = Intent.ACTION_SEND
                            sendIntent.putExtra(Intent.EXTRA_TEXT, msg)
                            sendIntent.type = TEXT_PLAIN
                            startActivityForResult(sendIntent, 0)
                        }
                    } else {
                        Toast.makeText(applicationContext, R.string.txtInvitationLinkCouldNotBeCreated, Toast.LENGTH_LONG).show()
                    }
                }
    }

    fun checkIfFavoriteListIdExists() {
        val userSettingRepository = UserSettingRepository()
        val sharedPreferences = applicationContext.getSharedPreferences(fFirebaseAuth!!.currentUser!!.uid, MODE_PRIVATE)
        if (sharedPreferences.getString("favoriteListId", "")!!.isEmpty()) {
            userSettingRepository[fFirebaseAuth!!.currentUser!!.uid, { userSetting: UserSetting ->
                val favoriteListId: String?
                val sharedPreferences1 = applicationContext.getSharedPreferences(fFirebaseAuth!!.currentUser!!.uid, MODE_PRIVATE)
                if (userSetting.favoriteListId!!.isEmpty()) {
                    favoriteListId = listViewModel!!.insertList("Favoriten", true)
                    userSettingRepository.insert(fFirebaseAuth!!.currentUser!!.uid, favoriteListId)
                } else {
                    favoriteListId = userSetting.favoriteListId
                }
                sharedPreferences1.edit().putString("favoriteListId", favoriteListId).commit()
                val refresh = Intent(this, MainActivity::class.java)
                startActivity(refresh)
                finish()
            }]
        } else {
            favoriteListId = sharedPreferences.getString("favoriteListId", "")
        }
    }

    fun deleteWishlist(view: View?) {
        if (selectedWishlistId == favoriteListId) {
            Toast.makeText(this, R.string.txtFavoritelistCantBeDeleted, Toast.LENGTH_LONG).show()
        } else {
            showAlertDialog(-1, R.layout.dialog_deletewishlist)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter(
                UploadService.Companion.NOTIFICATION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    companion object {
        const val AROMKO_PAGE_LINK = "https://aromko.page.link"
        const val EXAMPLE_LINK = "https://www.example.com/page?param="
        const val TEXT_PLAIN = "text/plain"
        private const val MY_PERMISSIONS_REQUEST = 1
        private const val HIDE_INVITE_PEOPLE = "hide_invite_people"
    }
}