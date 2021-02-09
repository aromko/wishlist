package aromko.de.wishlist.adapter

import android.content.*
import android.net.Uri
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import aromko.de.wishlist.R
import aromko.de.wishlist.activity.EditWishActivity
import aromko.de.wishlist.fragment.ItemListFragment.OnListFragmentInteractionListener
import aromko.de.wishlist.model.Wish
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import java.text.NumberFormat

class WishRecyclerViewAdapter(private val mValues: List<Wish?>, private val mListener: OnListFragmentInteractionListener?, private val mFavoriteListId: String?) : RecyclerView.Adapter<WishRecyclerViewAdapter.ViewHolder>() {
    private var context: Context? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_item, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        if (holder.mItem?.wishlistId == mFavoriteListId) {
            holder.favorite.visibility = View.INVISIBLE
            holder.tvItemOptions.visibility = View.INVISIBLE
        }
        var counter = 0
        if (holder.mItem?.markedAsFavorite != null) {
            for ((_, value) in holder.mItem?.markedAsFavorite!!) {
                if (value == true) {
                    counter += 1
                }
            }
        }
        holder.tvUsers.text = counter.toString()
        holder.item_name.text = holder.mItem?.title
        val format = NumberFormat.getCurrencyInstance()
        if (holder.mItem?.salvagePrice == 0.0) {
            holder.item_price.text = format.format(holder.mItem?.price)
        } else {
            val priceText = format.format(holder.mItem?.salvagePrice) + " / " + format.format(holder.mItem?.price)
            holder.item_price.text = priceText
        }
        if (holder.mItem?.markedAsFavorite != null && holder.mItem?.markedAsFavorite!!.containsKey(FirebaseAuth.getInstance().currentUser!!.uid) && holder.mItem!!.markedAsFavorite?.get(FirebaseAuth.getInstance().currentUser!!.uid) == true) {
            holder.favorite.setImageResource(R.drawable.ic_favorite)
            holder.favorite.tag = context!!.getString(R.string.txtIsFavorite)
        } else {
            holder.favorite.tag = context!!.getString(R.string.txtIsNoFavorite)
        }
        if (holder.mItem?.description?.isEmpty()!!) {
            holder.ivShowInfos.visibility = View.GONE
        } else {
            holder.ivShowInfos.visibility = View.VISIBLE
        }
        when (holder.mItem?.wishstrength?.toInt()) {
            1 -> holder.ivWishstrength.setImageResource(R.drawable.ic_wishstrength_medium)
            2 -> holder.ivWishstrength.setImageResource(R.drawable.ic_wishstrength_high)
            else -> holder.ivWishstrength.setImageResource(R.drawable.ic_wishstrength_low)
        }
        holder.mView.setOnClickListener { view: View? -> mListener?.onListFragmentInteraction(holder.mItem, holder.adapterPosition) }
        holder.favorite.setOnClickListener { view: View? ->
            var isFavorite = true
            if (holder.favorite.tag === context!!.getString(R.string.txtIsNoFavorite)) {
                holder.favorite.setImageResource(R.drawable.ic_favorite)
                holder.favorite.tag = context!!.getString(R.string.txtIsFavorite)
            } else {
                holder.favorite.setImageResource(R.drawable.ic_favorite_border)
                holder.favorite.tag = context!!.getString(R.string.txtIsNoFavorite)
                isFavorite = false
            }
            mListener?.onFavoriteInteraction(holder.mItem, isFavorite)
        }
        holder.ivMap.setOnClickListener { v: View? ->
            if (null != mListener) {
                if (holder.mItem?.longitude != 0.0 && holder.mItem?.latitude != 0.0) {
                    mListener.onMapInteraction(holder.mItem?.longitude!!, holder.mItem?.latitude!!)
                } else {
                    Toast.makeText(context, R.string.txtNoPlaceFound, Toast.LENGTH_LONG).show()
                }
            }
        }
        if (mValues[position]!!.isImageSet) {
            Picasso.get()
                    .load(Uri.parse(mValues[position]?.photoUrl))
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.productImage, object : Callback {
                        override fun onSuccess() {}
                        override fun onError(e: Exception) {
                            Picasso.get()
                                    .load(Uri.parse(mValues[position]?.photoUrl))
                                    .error(R.drawable.no_image_available)
                                    .into(holder.productImage)
                        }
                    })
        }
        holder.tvItemOptions.setOnClickListener { view: View ->
            val popupMenu = PopupMenu(context, holder.tvItemOptions)
            popupMenu.inflate(R.menu.item_options_menu)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                val itemId = item.itemId
                if (itemId == R.id.edit) {
                    val editWishAcitivity = Intent(view.context, EditWishActivity::class.java)
                    editWishAcitivity.putExtra("wishlistId", holder.mItem?.wishlistId)
                    editWishAcitivity.putExtra("wishId", holder.mItem?.wishId)
                    view.context.startActivity(editWishAcitivity)
                    return@setOnMenuItemClickListener true
                } else if (itemId == R.id.payment) {
                    mListener?.onPaymentInteraction(holder.mItem?.wishId, holder.mItem?.price!!, holder.mItem?.price!!, holder.mItem!!.wishlistId)
                    return@setOnMenuItemClickListener true
                } else if (itemId == R.id.partial_payment) {
                    if (null != mListener) {
                        showPaymentAlertDialog(holder.mItem?.wishId, holder.mItem?.price!!, holder.mItem!!.wishlistId)
                    }
                    return@setOnMenuItemClickListener true
                } else if (itemId == R.id.delete_wish) {
                    if (null != mListener) {
                        showDeleteWishAlertDialog(holder.mItem?.wishId, holder.mItem?.wishlistId)
                    }
                    return@setOnMenuItemClickListener true
                }
                false
            }
            popupMenu.show()
        }
        holder.ivUrl.setOnClickListener { v: View? ->
            if (null != mListener) {
                if (holder.mItem?.url != null && !holder.mItem?.url!!.isEmpty()) {
                    mListener.onUrlInteraction(holder.mItem?.url)
                } else {
                    Toast.makeText(context, R.string.txtNoUrlFound, Toast.LENGTH_LONG).show()
                }
            }
        }
        holder.tvDescription.text = holder.mItem?.description
        holder.ivChat.setOnClickListener { v: View? -> mListener?.onChatInteraction(holder.mItem?.wishId) }
        holder.ivShowInfos.setOnClickListener { v: View? ->
            if (null != mListener) {
                if (holder.tvDescription.visibility == View.GONE) {
                    holder.tvDescription.visibility = View.VISIBLE
                } else {
                    holder.tvDescription.visibility = View.GONE
                }
            }
        }
    }

    fun showPaymentAlertDialog(wishId: String?, price: Double, wishlistId: String?) {
        val builder = AlertDialog.Builder(context!!)
        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewPartialPayment = inflater.inflate(R.layout.dialog_payment, null)
        builder.setView(viewPartialPayment)
        val txtPartialPayment = viewPartialPayment.findViewById<EditText>(R.id.txtPartialPayment)
        builder.setPositiveButton(R.string.txtOk) { dialogInterface: DialogInterface, i: Int ->
            if ("" != txtPartialPayment.text.toString()) {
                val partialPrice = txtPartialPayment.text.toString().replace(",", ".").toDouble()
                if (partialPrice != 0.0) {
                    mListener?.onPaymentInteraction(wishId, price, partialPrice, wishlistId)
                }
            }
            dialogInterface.dismiss()
        }.setNegativeButton(R.string.txtCancel) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
        val dialog = builder.create()
        dialog.show()
    }

    fun showDeleteWishAlertDialog(wishId: String?, wishlistId: String?) {
        val builder = AlertDialog.Builder(context!!)
        val inflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewDeleteWish = inflater.inflate(R.layout.dialog_deletewish, null)
        builder.setView(viewDeleteWish)
        builder.setPositiveButton(R.string.txtOk) { dialogInterface: DialogInterface, i: Int ->
            mListener?.onDeleteWishInteraction(wishId, wishlistId)
            dialogInterface.dismiss()
        }.setNegativeButton(R.string.txtCancel) { dialogInterface: DialogInterface, i: Int -> dialogInterface.cancel() }
        val dialog = builder.create()
        dialog.show()
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val item_name: TextView
        val item_price: TextView
        val favorite: ImageView
        val productImage: ImageView
        val tvItemOptions: TextView
        val tvUsers: TextView
        val rlUsers: RelativeLayout
        val ivWishstrength: ImageView
        val ivMap: ImageView
        val ivUrl: ImageView
        val tvDescription: TextView
        val ivChat: ImageView
        val ivShowInfos: ImageView
        var mItem: Wish? = null
        override fun toString(): String {
            return super.toString() + " '" + item_name.text + "'"
        }

        init {
            item_name = mView.findViewById(R.id.item_name)
            item_price = mView.findViewById(R.id.item_price)
            favorite = mView.findViewById(R.id.favorite)
            productImage = mView.findViewById(R.id.ivProductImage)
            tvItemOptions = mView.findViewById(R.id.tvItemOptions)
            tvUsers = mView.findViewById(R.id.tvUsers)
            rlUsers = mView.findViewById(R.id.rlUsers)
            ivWishstrength = mView.findViewById(R.id.ivWishstrength)
            ivMap = mView.findViewById(R.id.ivMap)
            ivUrl = mView.findViewById(R.id.ivUrl)
            tvDescription = mView.findViewById(R.id.tvDescription)
            ivChat = mView.findViewById(R.id.ivChat)
            ivShowInfos = mView.findViewById(R.id.ivShowInfos)
        }
    }
}