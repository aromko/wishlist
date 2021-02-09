package aromko.de.wishlist.fragment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import aromko.de.wishlist.R
import aromko.de.wishlist.activity.ChatActivity
import aromko.de.wishlist.adapter.WishRecyclerViewAdapter
import aromko.de.wishlist.model.Wish
import aromko.de.wishlist.viewModel.PaymentViewModel
import aromko.de.wishlist.viewModel.WishViewModel
import aromko.de.wishlist.viewModel.WishViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class ItemListFragment : Fragment() {
    private var mColumnCount = 1
    private var mListener: OnListFragmentInteractionListener? = null
    private var wishViewModel: WishViewModel? = null
    private val listItems = ArrayList<Wish?>()
    private var favoriteListId: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mColumnCount = arguments!!.getInt(ARG_COLUMN_COUNT)
        }
        val fFirebaseAuth = FirebaseAuth.getInstance()
        val sharedPreferences = activity!!.getSharedPreferences(fFirebaseAuth.currentUser!!.uid, Context.MODE_PRIVATE)
        favoriteListId = sharedPreferences.getString("favoriteListId", "")
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View? {
        var wishlistId: String? = ""
        if (arguments!!.size() > 0) {
            wishlistId = arguments!!.getString("wishlistId")
        }
        val view = inflater.inflate(R.layout.fragment_item_list, container, false)
        if (view is RecyclerView) {
            val context = view.getContext()
            val recyclerView = view
            if (mColumnCount <= 1) {
                recyclerView.layoutManager = LinearLayoutManager(context)
            } else {
                recyclerView.layoutManager = GridLayoutManager(context, mColumnCount)
            }
            wishViewModel = ViewModelProvider(this, WishViewModelFactory(this.activity!!.application, wishlistId)).get(WishViewModel::class.java)
            val listsLiveData = wishViewModel!!.listsLiveData
            listsLiveData.observe(viewLifecycleOwner, { lists: List<Wish?>? ->
                val myLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val scrollPosition = myLayoutManager!!.findFirstVisibleItemPosition()
                listItems.clear()
                listItems.addAll(lists!!)
                recyclerView.adapter = WishRecyclerViewAdapter(listItems, mListener, favoriteListId)
                recyclerView.scrollToPosition(scrollPosition)
            })
            mListener = object : OnListFragmentInteractionListener {
                override fun onListFragmentInteraction(item: Wish?, adapterPosition: Int) {
                    recyclerView.scrollToPosition(adapterPosition)
                }

                override fun onFavoriteInteraction(wish: Wish?, isFavorite: Boolean?) {
                    val markedAsFavorite: MutableMap<String?, Boolean?> = HashMap()
                    if (wish?.markedAsFavorite != null) {
                        markedAsFavorite.putAll(wish.markedAsFavorite!!)
                    }
                    markedAsFavorite[FirebaseAuth.getInstance().currentUser!!.uid] = isFavorite
                    wish?.markedAsFavorite = markedAsFavorite
                    wishViewModel!!.setWishAsFavorite(wish?.wishlistId, wish?.wishId, wish, favoriteListId)
                }

                override fun onMapInteraction(longitude: Double, latitude: Double) {
                    try {
                        val gmmIntentUri = Uri.parse(GOOGLE_NAVIGATION_Q + latitude + "," + longitude)
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        startActivity(mapIntent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "Leider befindet sich keine Navigationsapp auf Ihrem Handy oder ist deaktiviert.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onUrlInteraction(url: String?) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }

                override fun onPaymentInteraction(wishId: String?, price: Double, partialPrice: Double, wishlistId: String?) {
                    val paymentViewModel = PaymentViewModel()
                    paymentViewModel.buyItem(wishId, price, partialPrice, wishlistId)
                }

                override fun onChatInteraction(wishId: String?) {
                    val chatActivity = Intent(getContext(), ChatActivity::class.java)
                    chatActivity.putExtra("wishId", wishId)
                    startActivity(chatActivity)
                }

                override fun onDeleteWishInteraction(wishId: String?, wishlistId: String?) {
                    wishViewModel!!.deleteWish(wishId, wishlistId, favoriteListId)
                }
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnListFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: Wish?, adapterPosition: Int)
        fun onFavoriteInteraction(wish: Wish?, isFavorite: Boolean?)
        fun onMapInteraction(longitude: Double, latitude: Double)
        fun onUrlInteraction(url: String?)
        fun onPaymentInteraction(wishId: String?, price: Double, partialPrice: Double, wishlistId: String?)
        fun onChatInteraction(wishId: String?)
        fun onDeleteWishInteraction(wishId: String?, wishlistId: String?)
    }

    companion object {
        const val GOOGLE_NAVIGATION_Q = "google.navigation:q="
        const val COM_GOOGLE_ANDROID_APPS_MAPS = "com.google.android.apps.maps"
        private const val ARG_COLUMN_COUNT = "column-count"
    }
}