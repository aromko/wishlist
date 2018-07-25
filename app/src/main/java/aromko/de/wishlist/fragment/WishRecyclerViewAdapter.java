package aromko.de.wishlist.fragment;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import aromko.de.wishlist.R;
import aromko.de.wishlist.activity.GlideApp;
import aromko.de.wishlist.fragment.ItemListFragment.OnListFragmentInteractionListener;
import aromko.de.wishlist.model.Wish;

public class WishRecyclerViewAdapter extends RecyclerView.Adapter<WishRecyclerViewAdapter.ViewHolder> {

    private final List<Wish> mValues;
    private final OnListFragmentInteractionListener mListener;
    FirebaseStorage storage = FirebaseStorage.getInstance("gs://wishlist-app-aromko.appspot.com");
    private Context context;
    private String mFavoriteListId = "";

    public WishRecyclerViewAdapter(List<Wish> items, OnListFragmentInteractionListener listener, String favoriteListId) {
        mValues = items;
        mListener = listener;
        mFavoriteListId = favoriteListId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        context = parent.getContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("", Context.MODE_PRIVATE);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        if (holder.mItem.getWishlistId().equals(mFavoriteListId)) {
            holder.favorite.setVisibility(View.INVISIBLE);
            holder.rlUsers.setVisibility(View.INVISIBLE);
        }

        int counter = 0;
        if (holder.mItem.getMarkedAsFavorite() != null) {
            for (Map.Entry<String, Boolean> entry : holder.mItem.getMarkedAsFavorite().entrySet()) {
                if (entry.getValue().equals(true)) {
                    counter += 1;
                }
            }
        }

        holder.tvUsers.setText(String.valueOf(counter));

        holder.item_name.setText(holder.mItem.getTitle());
        NumberFormat format = NumberFormat.getCurrencyInstance();
        holder.item_price.setText(format.format(holder.mItem.getPrice()));

        if (holder.mItem.getMarkedAsFavorite() != null && holder.mItem.getMarkedAsFavorite().containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid()) && holder.mItem.getMarkedAsFavorite().get(FirebaseAuth.getInstance().getCurrentUser().getUid()).equals(true)) {
            holder.favorite.setImageResource(R.drawable.ic_favorite);
            holder.favorite.setTag("isFavorite");
        } else {
            holder.favorite.setTag("isNoFavorite");
        }


        switch ((int) holder.mItem.getWishstrength()) {
            case 1:
                holder.ivWishstrength.setImageResource(R.drawable.ic_wishstrength_medium);
                break;
            case 2:
                holder.ivWishstrength.setImageResource(R.drawable.ic_wishstrength_high);
                break;
            default:
                holder.ivWishstrength.setImageResource(R.drawable.ic_wishstrength_low);
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

        holder.ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onMapInteraction(holder.mItem.getLongitude(), holder.mItem.getLatitude());
                }
            }
        });
        if (mValues.get(position).isImageSet()) {
            final StorageReference storageRef = storage.getReference(mValues.get(position).getWishId());
            final float[] rotation = new float[1];

            GlideApp.with(holder.productImage)
                    .load(storageRef)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .onlyRetrieveFromCache(false)
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
        public final TextView tvUsers;
        public final RelativeLayout rlUsers;
        public final ImageView ivWishstrength;
        public final ImageView ivMap;

        public Wish mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            item_name = view.findViewById(R.id.item_name);
            item_price = view.findViewById(R.id.item_price);
            favorite = view.findViewById(R.id.favorite);
            productImage = view.findViewById(R.id.ivProductImage);
            tvItemOptions = view.findViewById(R.id.tvItemOptions);
            tvUsers = view.findViewById(R.id.tvUsers);
            rlUsers = view.findViewById(R.id.rlUsers);
            ivWishstrength = view.findViewById(R.id.ivWishstrength);
            ivMap = view.findViewById(R.id.ivMap);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + item_name.getText() + "'";
        }
    }
}
