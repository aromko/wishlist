package aromko.de.wishlist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import aromko.de.wishlist.R
import aromko.de.wishlist.model.Wishlist
import java.util.*

class WishlistAdapter(private val contextr: Context, private val wishlistArrayList: ArrayList<Wishlist>) : ArrayAdapter<Wishlist?>(contextr, R.layout.wishlist_item, wishlistArrayList as List<Wishlist?>) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = contextr
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var rowView: View? = null
        rowView = inflater.inflate(R.layout.wishlist_item, parent, false)
        val nameView = rowView.findViewById<TextView>(R.id.item_name)
        val counterView = rowView.findViewById<TextView>(R.id.item_wishcounter)
        nameView.text = wishlistArrayList[position].name
        counterView.text = wishlistArrayList[position].wishCounter.toString()
        return rowView
    }
}