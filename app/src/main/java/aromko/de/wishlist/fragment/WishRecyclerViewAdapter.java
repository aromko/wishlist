package aromko.de.wishlist.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import aromko.de.wishlist.R;
import aromko.de.wishlist.fragment.ItemListFragment.OnListFragmentInteractionListener;
import aromko.de.wishlist.model.Wish;

public class WishRecyclerViewAdapter extends RecyclerView.Adapter<WishRecyclerViewAdapter.ViewHolder> {

    private final List<Wish> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final static FirebaseStorage STORAGE = FirebaseStorage.getInstance("gs://wishlist-app-aromko.appspot.com");
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
        }

        if (holder.mItem.getLatitude() == 0 && holder.mItem.getLongitude() == 0) {
            holder.ivMap.setVisibility(View.INVISIBLE);
        }

        if (holder.mItem.getUrl().isEmpty()) {
            holder.ivUrl.setVisibility(View.INVISIBLE);
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
        if(holder.mItem.getPrice() == holder.mItem.getSalvagePrice()){
            holder.item_price.setText(format.format(holder.mItem.getPrice()));
        } else {
            holder.item_price.setText(format.format(holder.mItem.getPrice()) + " (" + format.format(holder.mItem.getSalvagePrice()) + ")");
        }


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
            STORAGE.getReference(mValues.get(position).getWishId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(final Uri uri) {
                    Picasso.get()
                            .load(String.valueOf(uri))
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(holder.productImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get()
                                            .load(String.valueOf(uri))
                                            .error(R.drawable.no_image_available)
                                            .into(holder.productImage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Log.v("Picasso","Could not fetch image" + String.valueOf(uri));
                                                }
                                            });
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
                                if(null != mListener){
                                    showPaymentAlertDialog(holder.mItem.getWishId(), holder.mItem.getPrice(), holder.mItem.getWishlistId());
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        holder.ivUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onUrlInteraction(holder.mItem.getUrl());
                }
            }
        });

        holder.tvDescription.setText(holder.mItem.getDescription());

        holder.ivPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mListener)
                    mListener.onPaymentInteraction(holder.mItem.getWishId(), holder.mItem.getPrice(), holder.mItem.getPrice(), holder.mItem.getWishlistId());

            }
        });
    }

    public void showPaymentAlertDialog(final String wishId, final double price, final String wishlistId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View viewPartialPayment = inflater.inflate(R.layout.dialog_payment, null);

        builder.setView(viewPartialPayment);
        final EditText txtPartialPayment = viewPartialPayment.findViewById(R.id.txtPartialPayment);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                double partialPrice = Double.parseDouble(txtPartialPayment.getText().toString().replace(",", "."));
                if (partialPrice != 0) {
                    if (null != mListener) {
                        mListener.onPaymentInteraction(wishId, price, partialPrice, wishlistId);
                    }
                }
                dialogInterface.dismiss();
            }
        }).setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
        public final ImageView ivUrl;
        public final TextView tvDescription;
        public final ImageView ivPayment;

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
            ivUrl = view.findViewById(R.id.ivUrl);
            tvDescription = view.findViewById(R.id.tvDescription);
            ivPayment = view.findViewById(R.id.ivPayment);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + item_name.getText() + "'";
        }
    }
}
