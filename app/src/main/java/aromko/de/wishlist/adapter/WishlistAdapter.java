package aromko.de.wishlist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import aromko.de.wishlist.R;
import aromko.de.wishlist.model.WishList;

public class WishlistAdapter extends ArrayAdapter<WishList> {
    private final Context context;
    private final ArrayList<WishList> wishlistArrayList;

    public WishlistAdapter(Context context, ArrayList<WishList> wishlistArrayList) {

        super(context, R.layout.wishlist_item, wishlistArrayList);

        this.context = context;
        this.wishlistArrayList = wishlistArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = null;
        rowView = inflater.inflate(R.layout.wishlist_item, parent, false);

        TextView nameView = rowView.findViewById(R.id.item_name);
        TextView counterView = rowView.findViewById(R.id.item_wishcounter);

        nameView.setText(wishlistArrayList.get(position).getName());
        counterView.setText(String.valueOf(wishlistArrayList.get(position).getWishCounter()));

        return rowView;
    }
}
