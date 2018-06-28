package aromko.de.wishlist.fragment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import aromko.de.wishlist.R;
import aromko.de.wishlist.fragment.ItemListFragment.OnListFragmentInteractionListener;
import aromko.de.wishlist.model.Wish;

public class WishRecyclerViewAdapter extends RecyclerView.Adapter<WishRecyclerViewAdapter.ViewHolder> {

    public static final String FAVORITE_LIST_ID = "-LFy-qZjZ7hbaJGYB81t";
    private final List<Wish> mValues;
    private final OnListFragmentInteractionListener mListener;

    public WishRecyclerViewAdapter(List<Wish> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        if (holder.mItem.getWishlistId().equals(FAVORITE_LIST_ID)) {
            holder.favorite.setVisibility(View.INVISIBLE);
        }
        holder.item_name.setText(mValues.get(position).getTitle());
        holder.item_price.setText(String.valueOf(mValues.get(position).getPrice() + " €"));
        if (mValues.get(position).isFavorite()) {
            holder.favorite.setImageResource(R.drawable.ic_favorite);
            holder.favorite.setTag("isFavorite");
        } else {
            holder.favorite.setTag("isNoFavorite");
        }


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem, holder.getAdapterPosition());
                }
            }
        });

        holder.favorite.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isFavorite = true;

                if (holder.favorite.getTag() == "isNoFavorite") {
                    holder.favorite.setImageResource(R.drawable.ic_favorite);
                    holder.favorite.setTag("isFavorite");
                } else {
                    holder.favorite.setImageResource(R.drawable.ic_favorite_border);
                    holder.favorite.setTag("isNoFavorite");
                    isFavorite = false;
                }
                if (null != mListener) {
                    mListener.onFavoriteInteraction(holder.mItem, isFavorite);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView item_name;
        public final TextView item_price;
        public final ImageView favorite;

        public Wish mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            item_name = (TextView) view.findViewById(R.id.item_name);
            item_price = (TextView) view.findViewById(R.id.item_price);
            favorite = (ImageView) view.findViewById(R.id.favorite);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + item_name.getText() + "'";
        }
    }
}
