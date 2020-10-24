package aromko.de.wishlist.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import aromko.de.wishlist.R;
import aromko.de.wishlist.activity.EditWishActivity;
import aromko.de.wishlist.fragment.ItemListFragment.OnListFragmentInteractionListener;
import aromko.de.wishlist.model.Wish;

public class WishRecyclerViewAdapter extends RecyclerView.Adapter<WishRecyclerViewAdapter.ViewHolder> {

    private final List<Wish> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Context context;
    private String mFavoriteListId;

    public WishRecyclerViewAdapter(List<Wish> items, OnListFragmentInteractionListener listener, String favoriteListId) {
        mValues = items;
        mListener = listener;
        mFavoriteListId = favoriteListId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        if (holder.mItem.getWishlistId().equals(mFavoriteListId)) {
            holder.favorite.setVisibility(View.INVISIBLE);
            holder.tvItemOptions.setVisibility(View.INVISIBLE);
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
        if (holder.mItem.getPrice() == holder.mItem.getSalvagePrice()) {
            holder.item_price.setText(format.format(holder.mItem.getPrice()));
        } else {
            String priceText = format.format(holder.mItem.getPrice()) + " (" + format.format(holder.mItem.getSalvagePrice()) + ")";
            holder.item_price.setText(priceText);
        }


        if (holder.mItem.getMarkedAsFavorite() != null && holder.mItem.getMarkedAsFavorite().containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid()) && holder.mItem.getMarkedAsFavorite().get(FirebaseAuth.getInstance().getCurrentUser().getUid()).equals(true)) {
            holder.favorite.setImageResource(R.drawable.ic_favorite);
            holder.favorite.setTag(context.getString(R.string.txtIsFavorite));
        } else {
            holder.favorite.setTag(context.getString(R.string.txtIsNoFavorite));
        }

        if(holder.mItem.getDescription().isEmpty()) {
            holder.ivShowInfos.setVisibility(View.GONE);
        } else {
            holder.ivShowInfos.setVisibility(View.VISIBLE);
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
        holder.mView.setOnClickListener(view -> {
            if (null != mListener) {
                mListener.onListFragmentInteraction(holder.mItem, holder.getAdapterPosition());
            }
        });

        holder.favorite.setOnClickListener(view -> {
            boolean isFavorite = true;

            if (holder.favorite.getTag() == context.getString(R.string.txtIsNoFavorite)) {
                holder.favorite.setImageResource(R.drawable.ic_favorite);
                holder.favorite.setTag(context.getString(R.string.txtIsFavorite));
            } else {
                holder.favorite.setImageResource(R.drawable.ic_favorite_border);
                holder.favorite.setTag(context.getString(R.string.txtIsNoFavorite));
                isFavorite = false;
            }
            if (null != mListener) {
                mListener.onFavoriteInteraction(holder.mItem, isFavorite);
            }
        });

        holder.ivMap.setOnClickListener(v -> {
            if (null != mListener) {
                if (holder.mItem.getLongitude() != 0 && holder.mItem.getLatitude() != 0) {
                    mListener.onMapInteraction(holder.mItem.getLongitude(), holder.mItem.getLatitude());
                } else {
                    Toast.makeText(context, R.string.txtNoPlaceFound, Toast.LENGTH_LONG).show();
                }
            }
        });

        if (mValues.get(position).isImageSet()) {
            Picasso.get()
                    .load(Uri.parse(mValues.get(position).getPhotoUrl()))
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.productImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(Uri.parse(mValues.get(position).getPhotoUrl()))
                                    .error(R.drawable.no_image_available)
                                    .into(holder.productImage);
                        }
                    });
        }

        holder.tvItemOptions.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, holder.tvItemOptions);

            popupMenu.inflate(R.menu.item_options_menu);

            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.edit) {
                    Intent editWishAcitivity = new Intent(view.getContext(), EditWishActivity.class);
                    editWishAcitivity.putExtra("wishlistId", holder.mItem.getWishlistId());
                    editWishAcitivity.putExtra("wishId", holder.mItem.getWishId());
                    view.getContext().startActivity(editWishAcitivity);
                    return true;
                } else if (itemId == R.id.payment) {
                    if (null != mListener) {
                        mListener.onPaymentInteraction(holder.mItem.getWishId(), holder.mItem.getPrice(), holder.mItem.getPrice(), holder.mItem.getWishlistId());
                    }
                    return true;
                } else if (itemId == R.id.partial_payment) {
                    if (null != mListener) {
                        showPaymentAlertDialog(holder.mItem.getWishId(), holder.mItem.getPrice(), holder.mItem.getWishlistId());
                    }
                    return true;
                } else if (itemId == R.id.delete_wish) {
                    if (null != mListener) {
                        showDeleteWishAlertDialog(holder.mItem.getWishId(), holder.mItem.getWishlistId());
                    }
                    return true;
                }
                return false;
            });
            popupMenu.show();

        });

        holder.ivUrl.setOnClickListener(v -> {
            if (null != mListener) {
                if (holder.mItem.getUrl() != null && !holder.mItem.getUrl().isEmpty()) {
                    mListener.onUrlInteraction(holder.mItem.getUrl());
                } else {
                    Toast.makeText(context, R.string.txtNoUrlFound, Toast.LENGTH_LONG).show();
                }
            }
        });

        holder.tvDescription.setText(holder.mItem.getDescription());

        holder.ivChat.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onChatInteraction(holder.mItem.getWishId());
            }
        });

        holder.ivShowInfos.setOnClickListener(v -> {
            if (null != mListener) {
                Log.i("XXXX", String.valueOf(holder.tvDescription.getVisibility()));
                if (holder.tvDescription.getVisibility() == View.VISIBLE) {
                    holder.tvDescription.setVisibility(View.VISIBLE);
                } else {
                    holder.tvDescription.setVisibility(View.GONE);
                }
            }
        });
    }

    public void showPaymentAlertDialog(final String wishId, final double price, final String wishlistId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View viewPartialPayment = inflater.inflate(R.layout.dialog_payment, null);

        builder.setView(viewPartialPayment);
        final EditText txtPartialPayment = viewPartialPayment.findViewById(R.id.txtPartialPayment);

        builder.setPositiveButton(R.string.txtOk, (dialogInterface, i) -> {
            double partialPrice = Double.parseDouble(txtPartialPayment.getText().toString().replace(",", "."));
            if (partialPrice != 0) {
                if (null != mListener) {
                    mListener.onPaymentInteraction(wishId, price, partialPrice, wishlistId);
                }
            }
            dialogInterface.dismiss();
        }).setNegativeButton(R.string.txtCancel, (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showDeleteWishAlertDialog(final String wishId, final String wishlistId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View viewDeleteWish = inflater.inflate(R.layout.dialog_deletewish, null);

        builder.setView(viewDeleteWish);

        builder.setPositiveButton(R.string.txtOk, (dialogInterface, i) -> {

            if (null != mListener) {
                mListener.onDeleteWishInteraction(wishId, wishlistId);
            }

            dialogInterface.dismiss();
        }).setNegativeButton(R.string.txtCancel, (dialogInterface, i) -> dialogInterface.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
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
        public final ImageView ivChat;
        public final ImageView ivShowInfos;

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
            ivChat = view.findViewById(R.id.ivChat);
            ivShowInfos = view.findViewById(R.id.ivShowInfos);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + item_name.getText() + "'";
        }
    }
}
