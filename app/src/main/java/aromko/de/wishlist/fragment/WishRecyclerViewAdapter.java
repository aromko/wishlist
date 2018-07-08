package aromko.de.wishlist.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import aromko.de.wishlist.R;
import aromko.de.wishlist.activity.GlideApp;
import aromko.de.wishlist.fragment.ItemListFragment.OnListFragmentInteractionListener;
import aromko.de.wishlist.model.Wish;

public class WishRecyclerViewAdapter extends RecyclerView.Adapter<WishRecyclerViewAdapter.ViewHolder> {

    public static final String FAVORITE_LIST_ID = "-LFy-qZjZ7hbaJGYB81t";
    private final List<Wish> mValues;
    private final OnListFragmentInteractionListener mListener;
    FirebaseStorage storage = FirebaseStorage.getInstance("gs://wishlist-app-aromko.appspot.com");
    private Context context;

    public WishRecyclerViewAdapter(List<Wish> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        context = parent.getContext();
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

        if (mValues.get(position).getMarkedAsFavorite() != null && mValues.get(position).getMarkedAsFavorite().containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid()) && mValues.get(position).getMarkedAsFavorite().get(FirebaseAuth.getInstance().getCurrentUser().getUid()).equals(true)) {
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

        if (mValues.get(position).isImageSet()) {
            final StorageReference storageRef = storage.getReference(mValues.get(position).getWishId());
            final float[] rotation = new float[1];

            GlideApp.with(holder.productImage)
                    .load(storageRef)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .onlyRetrieveFromCache(true)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull final Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                @Override
                                public void onSuccess(StorageMetadata storageMetadata) {
                                    rotation[0] = Float.valueOf(storageMetadata.getCustomMetadata("rotation"));
                                    holder.productImage.setImageDrawable(resource);
                                    holder.productImage.setRotation(rotation[0]);
                                }
                            });
                        }
                    });
        }

        holder.tvItemOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, holder.tvItemOptions);

                popupMenu.inflate(R.menu.item_options_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.edit:
                                return true;
                            case R.id.partial_payment:
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
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
        public final ImageView productImage;
        public final TextView tvItemOptions;

        public Wish mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            item_name = (TextView) view.findViewById(R.id.item_name);
            item_price = (TextView) view.findViewById(R.id.item_price);
            favorite = (ImageView) view.findViewById(R.id.favorite);
            productImage = (ImageView) view.findViewById(R.id.ivProductImage);
            tvItemOptions = view.findViewById(R.id.tvItemOptions);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + item_name.getText() + "'";
        }
    }
}
